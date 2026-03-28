package com.example.demo.security

import com.example.demo.domain.model.User
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date

@Component
class JwtTokenProvider {

    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.expiration}")
    private var jwtExpiration: Long = 86400000

    private fun key(): Key {
        return Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    fun generateToken(user: User): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpiration)

        logger.debug("Generating JWT token for username={}, userId={}", user.username, user.id)

        return Jwts.builder()
            .setSubject(user.username)
            .claim("userId", user.id)
            .claim("role", user.role.name)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun getUsernameFromToken(token: String): String {
        logger.debug("Extracting username from JWT token")
        return Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .body
            .subject
    }

    fun getUserIdFromToken(token: String): Long {
        logger.debug("Extracting userId from JWT token")

        val claims = Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .body

        val userId = claims["userId"] as Number
        return userId.toLong()
    }

    fun getRoleFromToken(token: String): String {
        logger.debug("Extracting role from JWT token")

        val claims = Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .body

        return claims["role"] as String
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)

            logger.debug("JWT token is valid")
            true
        } catch (ex: MalformedJwtException) {
            logger.warn("Invalid JWT token: {}", ex.message)
            false
        } catch (ex: ExpiredJwtException) {
            logger.warn("Expired JWT token: {}", ex.message)
            false
        } catch (ex: UnsupportedJwtException) {
            logger.warn("Unsupported JWT token: {}", ex.message)
            false
        } catch (ex: IllegalArgumentException) {
            logger.warn("JWT claims string is empty: {}", ex.message)
            false
        }
    }
}
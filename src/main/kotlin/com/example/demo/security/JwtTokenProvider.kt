package com.example.demo.security

import com.example.demo.domain.model.User
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date
import java.util.logging.Logger

@Component  // Убедитесь, что эта аннотация есть
class JwtTokenProvider {

    private val logger = Logger.getLogger(JwtTokenProvider::class.java.name)

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
        return Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .body
            .subject
    }

    fun getUserIdFromToken(token: String): Long {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .body

        val userId = claims["userId"] as Number
        return userId.toLong()
    }

    fun getRoleFromToken(token: String): String {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .body

        return claims["role"] as String
    }

    fun validateToken(token: String): Boolean {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(token)
            return true
        } catch (ex: MalformedJwtException) {
            logger.info("Invalid JWT token: ${ex.message}")
        } catch (ex: ExpiredJwtException) {
            logger.info("Expired JWT token: ${ex.message}")
        } catch (ex: UnsupportedJwtException) {
            logger.info("Unsupported JWT token: ${ex.message}")
        } catch (ex: IllegalArgumentException) {
            logger.info("JWT claims string is empty: ${ex.message}")
        }
        return false
    }
}
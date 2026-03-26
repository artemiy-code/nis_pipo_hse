package com.example.demo.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.util.logging.Logger

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val customUserDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {

    private val logger = Logger.getLogger(JwtAuthenticationFilter::class.java.name)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.requestURI

        // Пропускаем запросы к аутентификации и статическим файлам
        if (path.startsWith("/api/auth") ||
            path.startsWith("/css") ||
            path.startsWith("/js") ||
            path == "/" ||
            path == "/index.html") {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val token = getTokenFromRequest(request)

            if (token != null && jwtTokenProvider.validateToken(token)) {
                val username = jwtTokenProvider.getUsernameFromToken(token)
                val role = jwtTokenProvider.getRoleFromToken(token)
                logger.info("Authenticating user: $username with role: $role")

                val userDetails = customUserDetailsService.loadUserByUsername(username)

                val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))

                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    authorities
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

                SecurityContextHolder.getContext().authentication = authentication
                logger.info("Authentication successful for: $username")
            }
        } catch (e: Exception) {
            logger.info("Authentication error: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }

    private fun getTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }
        return null
    }
}
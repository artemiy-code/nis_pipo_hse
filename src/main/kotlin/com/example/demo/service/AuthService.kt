package com.example.demo.service

import com.example.demo.api.dto.AuthResponse
import com.example.demo.api.dto.LoginRequest
import com.example.demo.api.dto.RegisterRequest
import com.example.demo.domain.model.Role
import com.example.demo.domain.model.User
import com.example.demo.domain.repository.UserRepository
import com.example.demo.security.JwtTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        logger.info("Attempting to register user: {}", request.username)

        // Проверка существования user'a
        if (userRepository.existsByUsername(request.username)) {
            logger.warn("Username already exists: {}", request.username)
            throw IllegalArgumentException("Username already exists")
        }

        if (userRepository.existsByEmail(request.email)) {
            logger.warn("Email already exists: {}", request.email)
            throw IllegalArgumentException("Email already exists")
        }

        // Создание user'a
        val user = User(
            username = request.username,
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            role = Role.USER
        )

        val savedUser = userRepository.save(user)
        logger.info("User registered successfully: {}", savedUser.username)

        // генерим JWT токен
        val token = jwtTokenProvider.generateToken(savedUser)

        return AuthResponse(
            token = token,
            username = savedUser.username,
            role = savedUser.role.name
        )
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        logger.info("Login attempt for user: {}", request.username)

        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.username, request.password)
            )

            val user = authentication.principal as User
            val token = jwtTokenProvider.generateToken(user)

            logger.info("User logged in successfully: {}", user.username)

            return AuthResponse(
                token = token,
                username = user.username,
                role = user.role.name
            )
        } catch (e: AuthenticationException) {
            logger.error("Login failed for user: {}", request.username, e)
            throw IllegalArgumentException("Invalid username or password")
        }
    }
}
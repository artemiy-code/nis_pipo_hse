package com.example.demo.service

import com.example.demo.api.dto.AuthResponse
import com.example.demo.api.dto.LoginRequest
import com.example.demo.api.dto.RegisterRequest
import com.example.demo.domain.model.Role
import com.example.demo.domain.model.User
import com.example.demo.domain.repository.UserRepository
import com.example.demo.security.JwtTokenProvider
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
class AuthServiceTest {

    private lateinit var authService: AuthService
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var authenticationManager: AuthenticationManager

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        passwordEncoder = mockk()
        jwtTokenProvider = mockk()
        authenticationManager = mockk()

        authService = AuthService(
            userRepository,
            passwordEncoder,
            jwtTokenProvider,
            authenticationManager
        )
    }

    @Test
    fun `register should create new user and return token`() {
        val request = RegisterRequest("testuser", "test@example.com", "password123")
        val encodedPassword = "encodedPassword"
        val now = LocalDateTime.now()
        val user = User(
            id = 1,
            username = request.username,
            email = request.email,
            passwordHash = encodedPassword,
            role = Role.USER,
            createdAt = now,
            updatedAt = now
        )
        val expectedToken = "jwt.token.here"

        every { userRepository.existsByUsername(request.username) } returns false
        every { userRepository.existsByEmail(request.email) } returns false
        every { passwordEncoder.encode(request.password) } returns encodedPassword
        every { userRepository.save(any<User>()) } returns user
        every { jwtTokenProvider.generateToken(any<User>()) } returns expectedToken

        val result = authService.register(request)

        assertEquals(expectedToken, result.token)
        assertEquals(request.username, result.username)
        assertEquals(Role.USER.name, result.role)
        verify(exactly = 1) { userRepository.save(any<User>()) }
        verify(exactly = 1) { jwtTokenProvider.generateToken(any<User>()) }
    }

    @Test
    fun `register should throw exception when username already exists`() {
        val request = RegisterRequest("existinguser", "test@example.com", "password123")
        every { userRepository.existsByUsername(request.username) } returns true

        val exception = assertThrows(IllegalArgumentException::class.java) {
            authService.register(request)
        }
        assertEquals("Username already exists", exception.message)
        verify(exactly = 0) { userRepository.save(any<User>()) }
    }

    @Test
    fun `register should throw exception when email already exists`() {
        val request = RegisterRequest("newuser", "existing@example.com", "password123")
        every { userRepository.existsByUsername(request.username) } returns false
        every { userRepository.existsByEmail(request.email) } returns true

        val exception = assertThrows(IllegalArgumentException::class.java) {
            authService.register(request)
        }
        assertEquals("Email already exists", exception.message)
        verify(exactly = 0) { userRepository.save(any<User>()) }
    }

    @Test
    fun `login should authenticate user and return token`() {
        val request = LoginRequest("testuser", "password123")
        val now = LocalDateTime.now()
        val user = User(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            passwordHash = "encoded",
            role = Role.USER,
            createdAt = now,
            updatedAt = now
        )
        val authentication = mockk<Authentication>()
        val expectedToken = "jwt.token.here"

        every { authenticationManager.authenticate(any<UsernamePasswordAuthenticationToken>()) } returns authentication
        every { authentication.principal } returns user
        every { jwtTokenProvider.generateToken(user) } returns expectedToken

        val result = authService.login(request)

        assertEquals(expectedToken, result.token)
        assertEquals(user.username, result.username)
        assertEquals(Role.USER.name, result.role)
        verify(exactly = 1) { authenticationManager.authenticate(any()) }
    }

    @Test
    fun `login should throw exception when credentials are invalid`() {
        val request = LoginRequest("wronguser", "wrongpass")
        val authException = BadCredentialsException("Bad credentials")
        every { authenticationManager.authenticate(any<UsernamePasswordAuthenticationToken>()) } throws authException

        val exception = assertThrows(IllegalArgumentException::class.java) {
            authService.login(request)
        }
        assertEquals("Invalid username or password", exception.message)
    }
}
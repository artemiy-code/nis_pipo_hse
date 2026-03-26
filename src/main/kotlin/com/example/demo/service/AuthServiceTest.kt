//package com.example.demo.service
//
//import com.example.demo.api.dto.LoginRequest
//import com.example.demo.api.dto.RegisterRequest
//import com.example.demo.domain.model.Role
//import com.example.demo.domain.model.User
//import com.example.demo.domain.repository.UserRepository
//import com.example.demo.security.JwtTokenProvider
//import io.mockk.*
//import org.junit.jupiter.api.Assertions.*
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.springframework.security.authentication.AuthenticationManager
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
//import org.springframework.security.core.Authentication
//import org.springframework.security.crypto.password.PasswordEncoder
//import java.util.*
//
//class AuthServiceTest {
//
//    private lateinit var authService: AuthService
//    private lateinit var userRepository: UserRepository
//    private lateinit var passwordEncoder: PasswordEncoder
//    private lateinit var jwtTokenProvider: JwtTokenProvider
//    private lateinit var authenticationManager: AuthenticationManager
//
//    @BeforeEach
//    fun setUp() {
//        userRepository = mockk()
//        passwordEncoder = mockk()
//        jwtTokenProvider = mockk()
//        authenticationManager = mockk()
//
//        authService = AuthService(
//            userRepository,
//            passwordEncoder,
//            jwtTokenProvider,
//            authenticationManager
//        )
//    }
//
//    @Test
//    fun `register should create new user and return token`() {
//        // Given
//        val request = RegisterRequest("testuser", "test@example.com", "password123")
//        val encodedPassword = "encodedPassword"
//        val user = User(
//            id = 1,
//            username = request.username,
//            email = request.email,
//            passwordHash = encodedPassword,
//            role = Role.USER
//        )
//        val expectedToken = "jwt.token.here"
//
//        every { userRepository.existsByUsername(request.username) } returns false
//        every { userRepository.existsByEmail(request.email) } returns false
//        every { passwordEncoder.encode(request.password) } returns encodedPassword
//        every { userRepository.save(any<User>()) } returns user
//        every { jwtTokenProvider.generateToken(user) } returns expectedToken
//
//        // When
//        val result = authService.register(request)
//
//        // Then
//        assertEquals(expectedToken, result.token)
//        assertEquals(request.username, result.username)
//        assertEquals(Role.USER.name, result.role)
//
//        verify(exactly = 1) { userRepository.save(any<User>()) }
//        verify(exactly = 1) { jwtTokenProvider.generateToken(user) }
//    }
//
//    @Test
//    fun `register should throw exception when username already exists`() {
//        // Given
//        val request = RegisterRequest("existinguser", "test@example.com", "password123")
//
//        every { userRepository.existsByUsername(request.username) } returns true
//
//        // When & Then
//        assertThrows(IllegalArgumentException::class.java) {
//            authService.register(request)
//        }
//
//        verify(exactly = 0) { userRepository.save(any<User>()) }
//    }
//
//    @Test
//    fun `login should authenticate user and return token`() {
//        // Given
//        val request = LoginRequest("testuser", "password123")
//        val user = User(
//            id = 1,
//            username = "testuser",
//            email = "test@example.com",
//            passwordHash = "encoded",
//            role = Role.USER
//        )
//        val authentication = mockk<Authentication>()
//        val expectedToken = "jwt.token.here"
//
//        every { authenticationManager.authenticate(any<UsernamePasswordAuthenticationToken>()) } returns authentication
//        every { authentication.principal } returns user
//        every { jwtTokenProvider.generateToken(user) } returns expectedToken
//
//        // When
//        val result = authService.login(request)
//
//        // Then
//        assertEquals(expectedToken, result.token)
//        assertEquals(user.username, result.username)
//        assertEquals(Role.USER.name, result.role)
//    }
//}
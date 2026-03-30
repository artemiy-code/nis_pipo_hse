package com.example.demo.integration

import com.example.demo.api.dto.AuthResponse
import com.example.demo.api.dto.LoginRequest
import com.example.demo.api.dto.RegisterRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    private lateinit var headers: HttpHeaders

    @BeforeEach
    fun setUp() {
        headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
    }

    @Test
    fun `should register new user successfully`() {
        val uniqueUsername = "newuser_${System.currentTimeMillis()}"
        val uniqueEmail = "${uniqueUsername}@example.com"

        val request = RegisterRequest(uniqueUsername, uniqueEmail, "password123")
        val httpEntity = HttpEntity(request, headers)

        val response = restTemplate.postForEntity(
            "/api/auth/register",
            httpEntity,
            AuthResponse::class.java
        )

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertEquals(uniqueUsername, response.body?.username)
        assertEquals("USER", response.body?.role)
        assertNotNull(response.body?.token)
    }

    @Test
    fun `should login successfully with valid credentials`() {
        val uniqueUsername = "loginuser_${System.currentTimeMillis()}"
        val uniqueEmail = "${uniqueUsername}@example.com"

        val registerRequest = RegisterRequest(uniqueUsername, uniqueEmail, "password123")
        val registerResponse = restTemplate.postForEntity(
            "/api/auth/register",
            HttpEntity(registerRequest, headers),
            AuthResponse::class.java
        )
        assertNotNull(registerResponse.body, "User should be registered")

        val loginRequest = LoginRequest(uniqueUsername, "password123")
        val response = restTemplate.postForEntity(
            "/api/auth/login",
            HttpEntity(loginRequest, headers),
            AuthResponse::class.java
        )

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertEquals(uniqueUsername, response.body?.username)
        assertNotNull(response.body?.token)
    }

    @Test
    fun `should fail login with invalid credentials`() {
        val loginRequest = LoginRequest("nonexistent_${System.currentTimeMillis()}", "wrongpass")
        val response = restTemplate.postForEntity(
            "/api/auth/login",
            HttpEntity(loginRequest, headers),
            String::class.java
        )

        assertEquals(400, response.statusCode.value())
    }
}
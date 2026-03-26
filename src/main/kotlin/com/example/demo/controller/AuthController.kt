package com.example.demo.controller

import com.example.demo.api.dto.*
import com.example.demo.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management endpoints")
class AuthController(
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User registered successfully"),
            ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
        ]
    )
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        logger.info("Received registration request for username: {}", request.username)
        val response = authService.register(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user and returns JWT token")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Login successful"),
            ApiResponse(responseCode = "401", description = "Invalid credentials")
        ]
    )
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        logger.info("Received login request for username: {}", request.username)
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }
}
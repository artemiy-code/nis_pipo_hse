package com.example.demo.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request for user registration")
data class RegisterRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @field:Schema(description = "Username", example = "john_doe")
    val username: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @field:Schema(description = "Email address", example = "john@example.com")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    @field:Schema(description = "Password", example = "password123")
    val password: String
)

@Schema(description = "Request for user login")
data class LoginRequest(
    @field:NotBlank(message = "Username is required")
    @field:Schema(description = "Username", example = "john_doe")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Schema(description = "Password", example = "password123")
    val password: String
)

@Schema(description = "Authentication response")
data class AuthResponse(
    @field:Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiIs...")
    val token: String,
    @field:Schema(description = "Username", example = "john_doe")
    val username: String,
    @field:Schema(description = "User role", example = "USER")
    val role: String
)


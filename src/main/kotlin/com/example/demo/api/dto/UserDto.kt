package com.example.demo.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request for user registration")
data class RegisterRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Username", example = "john_doe")
    val username: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @Schema(description = "Email address", example = "john@example.com")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "Password", example = "password123")
    val password: String
)

@Schema(description = "Request for user login")
data class LoginRequest(
    @field:NotBlank(message = "Username is required")
    @Schema(description = "Username", example = "john_doe")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @Schema(description = "Password", example = "password123")
    val password: String
)

@Schema(description = "Authentication response")
data class AuthResponse(
    @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiIs...")
    val token: String,
    @Schema(description = "Username", example = "john_doe")
    val username: String,
    @Schema(description = "User role", example = "USER")
    val role: String
)

@Schema(description = "User information response")
data class UserResponse(
    @Schema(description = "User ID", example = "1")
    val id: Long,
    @Schema(description = "Username", example = "john_doe")
    val username: String,
    @Schema(description = "Email address", example = "john@example.com")
    val email: String,
    @Schema(description = "User role", example = "USER")
    val role: String,
    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00")
    val createdAt: String
)
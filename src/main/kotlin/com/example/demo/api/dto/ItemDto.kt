package com.example.demo.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "Request to create a new item")
data class CreateItemRequest(
    @field:NotBlank(message = "Item name is required")
    @Schema(description = "Item name", example = "Laptop")
    val name: String,

    @Schema(description = "Item description", example = "High-performance laptop")
    val description: String? = null,

    @field:NotNull(message = "Price is required")
    @field:Positive(message = "Price must be positive")
    @Schema(description = "Item price", example = "999.99")
    val price: BigDecimal
)

@Schema(description = "Request to update an item")
data class UpdateItemRequest(
    @Schema(description = "Item name", example = "Gaming Laptop")
    val name: String? = null,

    @Schema(description = "Item description", example = "High-performance gaming laptop")
    val description: String? = null,

    @Schema(description = "Item price", example = "1299.99")
    val price: BigDecimal? = null,

    @Schema(description = "Item status", example = "ACTIVE", allowableValues = ["ACTIVE", "INACTIVE", "DELETED"])
    val status: String? = null
)

@Schema(description = "Item information response")
data class ItemResponse(
    @Schema(description = "Item ID", example = "1")
    val id: Long,

    @Schema(description = "Item name", example = "Laptop")
    val name: String,

    @Schema(description = "Item description", example = "High-performance laptop")
    val description: String?,

    @Schema(description = "Item price", example = "999.99")
    val price: BigDecimal,

    @Schema(description = "Owner user ID", example = "1")
    val userId: Long,

    @Schema(description = "Owner username", example = "john_doe")
    val username: String,

    @Schema(description = "Item status", example = "ACTIVE")
    val status: String,

    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00")
    val createdAt: String,

    @Schema(description = "Last update timestamp", example = "2024-01-01T10:00:00")
    val updatedAt: String
)
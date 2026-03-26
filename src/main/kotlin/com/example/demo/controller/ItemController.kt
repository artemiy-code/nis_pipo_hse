package com.example.demo.controller

import com.example.demo.api.dto.CreateItemRequest
import com.example.demo.api.dto.ItemResponse
import com.example.demo.api.dto.UpdateItemRequest
import com.example.demo.service.ItemService
import com.example.demo.security.JwtTokenProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/items")
@Tag(name = "Items", description = "Item management endpoints")
@SecurityRequirement(name = "bearerAuth")
class ItemController(
    private val itemService: ItemService,
    private val jwtTokenProvider: JwtTokenProvider
) {
    private val logger = LoggerFactory.getLogger(ItemController::class.java)

    @PostMapping
    @Operation(summary = "Create a new item", description = "Creates a new item for the authenticated user")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Item created successfully"),
            ApiResponse(responseCode = "400", description = "Invalid input"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun createItem(
        @RequestHeader("Authorization") authHeader: String,
        @Valid @RequestBody request: CreateItemRequest
    ): ResponseEntity<ItemResponse> {
        // Добавьте логирование
        println("=== CREATE ITEM ===")
        println("Auth header: $authHeader")
        println("Request: $request")

        val userId = getUserIdFromToken(authHeader)
        println("User ID from token: $userId")

        val response = itemService.createItem(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID", description = "Retrieves a specific item by its ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Item found"),
            ApiResponse(responseCode = "404", description = "Item not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun getItem(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable id: Long
    ): ResponseEntity<ItemResponse> {
        val userId = getUserIdFromToken(authHeader)
        logger.info("Fetching item ID: {} for user ID: {}", id, userId)

        val response = itemService.getItemById(id, userId)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    @Operation(summary = "Get user items", description = "Retrieves all items for the authenticated user")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Items retrieved successfully"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun getUserItems(
        @RequestHeader("Authorization") authHeader: String,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<ItemResponse>> {
        val userId = getUserIdFromToken(authHeader)
        logger.info("Fetching items for user ID: {} with pageable: {}", userId, pageable)

        val response = itemService.getUserItems(userId, pageable)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update item", description = "Updates an existing item")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Item updated successfully"),
            ApiResponse(responseCode = "404", description = "Item not found"),
            ApiResponse(responseCode = "403", description = "Access denied"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun updateItem(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateItemRequest
    ): ResponseEntity<ItemResponse> {
        val userId = getUserIdFromToken(authHeader)
        logger.info("Updating item ID: {} for user ID: {}", id, userId)

        val response = itemService.updateItem(id, userId, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete item", description = "Deletes (soft delete) an item")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Item deleted successfully"),
            ApiResponse(responseCode = "404", description = "Item not found"),
            ApiResponse(responseCode = "403", description = "Access denied"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun deleteItem(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        val userId = getUserIdFromToken(authHeader)
        logger.info("Deleting item ID: {} for user ID: {}", id, userId)

        itemService.deleteItem(id, userId)
        return ResponseEntity.noContent().build()
    }

    private fun getUserIdFromToken(authHeader: String): Long {
        val token = authHeader.removePrefix("Bearer ").trim()
        return jwtTokenProvider.getUserIdFromToken(token)
    }
}
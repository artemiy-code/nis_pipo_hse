package com.example.demo.api.controller

import com.example.demo.api.dto.CreateItemRequest
import com.example.demo.api.dto.ItemResponse
import com.example.demo.controller.ItemController
import com.example.demo.security.CustomUserDetailsService
import com.example.demo.security.JwtAuthenticationFilter
import com.example.demo.security.JwtTokenProvider
import com.example.demo.service.ItemService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.mockito.Mockito.*
import java.math.BigDecimal
import java.time.LocalDateTime

@WebMvcTest(ItemController::class)
@AutoConfigureMockMvc(addFilters = false)
class ItemControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var itemService: ItemService

    @MockBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @MockBean
    private lateinit var customUserDetailsService: CustomUserDetailsService

    @MockBean
    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    private val testToken = "Bearer test-jwt-token"
    private val testUserId = 1L

    @BeforeEach
    fun setUp() {
        // Мокаем получение userId из токена для всех тестов
        `when`(jwtTokenProvider.getUserIdFromToken(anyString())).thenReturn(testUserId)
    }

    private fun createTestItemResponse(id: Long = 1, name: String = "Test Item"): ItemResponse {
        val now = LocalDateTime.now().toString()
        return ItemResponse(
            id = id,
            name = name,
            description = "Test description",
            price = BigDecimal("99.99"),
            userId = testUserId,
            username = "testuser",
            status = "ACTIVE",
            createdAt = now,
            updatedAt = now
        )
    }

    @Test
    fun `createItem should return 400 when validation fails`() {
        val request = CreateItemRequest("", null, BigDecimal("-1"))

        mockMvc.perform(
            post("/api/items")
                .header("Authorization", testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `getItemById should return 200 when item exists`() {
        val itemId = 1L
        val response = createTestItemResponse(itemId)

        `when`(itemService.getItemById(itemId, testUserId)).thenReturn(response)

        mockMvc.perform(
            get("/api/items/$itemId")
                .header("Authorization", testToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(itemId))
            .andExpect(jsonPath("$.name").value("Test Item"))
    }

    @Test
    fun `getItemById should return 404 when item not found`() {
        val itemId = 999L

        `when`(itemService.getItemById(itemId, testUserId))
            .thenThrow(NoSuchElementException("Item not found"))

        mockMvc.perform(
            get("/api/items/$itemId")
                .header("Authorization", testToken)
        )
            .andExpect(status().isNotFound)
    }


    @Test
    fun `deleteItem should return 204 when successful`() {
        val itemId = 1L

        doNothing().`when`(itemService).deleteItem(itemId, testUserId)

        mockMvc.perform(
            delete("/api/items/$itemId")
                .header("Authorization", testToken)
        )
            .andExpect(status().isNoContent)

        verify(itemService, times(1)).deleteItem(itemId, testUserId)
    }

    @Test
    fun `deleteItem should return 404 when item not found`() {
        val itemId = 999L

        doThrow(NoSuchElementException("Item not found"))
            .`when`(itemService).deleteItem(itemId, testUserId)

        mockMvc.perform(
            delete("/api/items/$itemId")
                .header("Authorization", testToken)
        )
            .andExpect(status().isNotFound)
    }

}
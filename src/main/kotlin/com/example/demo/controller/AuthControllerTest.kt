//package com.example.demo.api.controller
//
//import com.example.demo.api.dto.AuthResponse
//import com.example.demo.api.dto.LoginRequest
//import com.example.demo.api.dto.RegisterRequest
//import com.example.demo.service.AuthService
//import com.fasterxml.jackson.databind.ObjectMapper
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
//import org.springframework.boot.test.mock.mockito.MockBean
//import org.springframework.http.MediaType
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
//import org.mockito.Mockito.*
//
//@WebMvcTest(AuthController::class)
//class AuthControllerTest {
//
//    @Autowired
//    private lateinit var mockMvc: MockMvc
//
//    @Autowired
//    private lateinit var objectMapper: ObjectMapper
//
//    @MockBean
//    private lateinit var authService: AuthService
//
//    @Test
//    fun `register should return 200 when successful`() {
//        // Given
//        val request = RegisterRequest("testuser", "test@example.com", "password123")
//        val response = AuthResponse("token", "testuser", "USER")
//
//        `when`(authService.register(any(RegisterRequest::class.java))).thenReturn(response)
//
//        // When & Then
//        mockMvc.perform(
//            post("/api/auth/register")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request))
//        )
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.token").value("token"))
//            .andExpect(jsonPath("$.username").value("testuser"))
//    }
//
//    @Test
//    fun `register should return 400 when validation fails`() {
//        // Given
//        val request = RegisterRequest("", "invalid-email", "123")
//
//        // When & Then
//        mockMvc.perform(
//            post("/api/auth/register")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request))
//        )
//            .andExpect(status().isBadRequest)
//    }
//}
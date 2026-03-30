package com.example.demo.integration

import com.example.demo.api.dto.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class ItemIntegrationTest {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    private lateinit var authToken: String
    private lateinit var headers: HttpHeaders

    private val testUsername = "itemuser_${System.currentTimeMillis()}"
    private val testEmail = "${testUsername}@example.com"

    @BeforeEach
    fun setUp() {
        val registerRequest = RegisterRequest(testUsername, testEmail, "password123")
        val registerHeaders = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val registerResponse = restTemplate.postForEntity(
            "/api/auth/register",
            HttpEntity(registerRequest, registerHeaders),
            AuthResponse::class.java
        )
        authToken = registerResponse.body?.token ?: ""

        headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set("Authorization", "Bearer $authToken")
        }
    }

    @Test
    fun `should create item successfully`() {
        val request = CreateItemRequest("Laptop", "High-performance laptop", BigDecimal("999.99"))
        val httpEntity = HttpEntity(request, headers)

        val response = restTemplate.postForEntity(
            "/api/items",
            httpEntity,
            ItemResponse::class.java
        )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
        assertEquals("Laptop", response.body?.name)
        assertEquals(BigDecimal("999.99"), response.body?.price)
        assertEquals("ACTIVE", response.body?.status)
    }

    @Test
    fun `should get item by id`() {
        val createRequest = CreateItemRequest("Test Item", "Description", BigDecimal("50.00"))
        val createResponse = restTemplate.postForEntity(
            "/api/items",
            HttpEntity(createRequest, headers),
            ItemResponse::class.java
        )
        val itemId = createResponse.body?.id

        val response = restTemplate.exchange(
            "/api/items/$itemId",
            HttpMethod.GET,
            HttpEntity<Any>(headers),
            ItemResponse::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Test Item", response.body?.name)
        assertEquals(BigDecimal("50.00"), response.body?.price)
    }

    @Test
    fun `should get all user items`() {
        val items = listOf(
            CreateItemRequest("Item1", "Desc1", BigDecimal("100")),
            CreateItemRequest("Item2", "Desc2", BigDecimal("200")),
            CreateItemRequest("Item3", "Desc3", BigDecimal("300"))
        )

        items.forEach { item ->
            restTemplate.postForEntity(
                "/api/items",
                HttpEntity(item, headers),
                ItemResponse::class.java
            )
        }

        val response = restTemplate.exchange(
            "/api/items?page=0&size=10",
            HttpMethod.GET,
            HttpEntity<Any>(headers),
            String::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        val content = response.body
        assertNotNull(content)
        assertTrue(content!!.contains("Item1"))
        assertTrue(content.contains("Item2"))
        assertTrue(content.contains("Item3"))
    }

    @Test
    fun `should update item`() {
        val createRequest = CreateItemRequest("Old Name", "Old Description", BigDecimal("100"))
        val createResponse = restTemplate.postForEntity(
            "/api/items",
            HttpEntity(createRequest, headers),
            ItemResponse::class.java
        )
        val itemId = createResponse.body?.id

        val updateRequest = UpdateItemRequest(
            name = "New Name",
            description = "New Description",
            price = BigDecimal("200")
        )
        val response = restTemplate.exchange(
            "/api/items/$itemId",
            HttpMethod.PUT,
            HttpEntity(updateRequest, headers),
            ItemResponse::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("New Name", response.body?.name)
        assertEquals(BigDecimal("200"), response.body?.price)
    }

    @Test
    fun `should delete item`() {
        val createRequest = CreateItemRequest("To Delete", "Will be deleted", BigDecimal("50"))
        val createResponse = restTemplate.postForEntity(
            "/api/items",
            HttpEntity(createRequest, headers),
            ItemResponse::class.java
        )
        val itemId = createResponse.body?.id
        assertNotNull(itemId, "Item should be created")

        val getBeforeDelete = restTemplate.exchange(
            "/api/items/$itemId",
            HttpMethod.GET,
            HttpEntity<Any>(headers),
            ItemResponse::class.java
        )
        assertEquals(HttpStatus.OK, getBeforeDelete.statusCode)

        val deleteResponse = restTemplate.exchange(
            "/api/items/$itemId",
            HttpMethod.DELETE,
            HttpEntity<Any>(headers),
            String::class.java
        )
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.statusCode)

        val getAfterDelete = restTemplate.exchange(
            "/api/items/$itemId",
            HttpMethod.GET,
            HttpEntity<Any>(headers),
            String::class.java
        )
        assertEquals(HttpStatus.BAD_REQUEST, getAfterDelete.statusCode)
    }

    @Test
    fun `should not allow access without token`() {
        val request = CreateItemRequest("Valid Item", "Valid description", BigDecimal("100.00"))
        val headersWithoutToken = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val response = restTemplate.postForEntity(
            "/api/items",
            HttpEntity(request, headersWithoutToken),
            String::class.java
        )

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }
}
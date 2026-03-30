package com.example.demo.service

import com.example.demo.api.dto.CreateItemRequest
import com.example.demo.api.dto.UpdateItemRequest
import com.example.demo.domain.model.Item
import com.example.demo.domain.model.ItemStatus
import com.example.demo.domain.model.User
import com.example.demo.domain.repository.ItemRepository
import com.example.demo.domain.repository.UserRepository
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
class ItemServiceTest {

    private lateinit var itemService: ItemService
    private lateinit var itemRepository: ItemRepository
    private lateinit var userRepository: UserRepository
    private lateinit var meterRegistry: MeterRegistry

    @BeforeEach
    fun setUp() {
        itemRepository = mockk()
        userRepository = mockk()
        meterRegistry = SimpleMeterRegistry()

        itemService = ItemService(itemRepository, userRepository, meterRegistry)
    }

    private fun createTestUser(id: Long = 1L): User {
        val now = LocalDateTime.now()
        return User(
            id = id,
            username = "testuser",
            email = "test@example.com",
            passwordHash = "encoded",
            role = com.example.demo.domain.model.Role.USER,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun createTestItem(id: Long, name: String, price: BigDecimal, user: User, status: ItemStatus = ItemStatus.ACTIVE): Item {
        val now = LocalDateTime.now()
        return Item(
            id = id,
            name = name,
            description = "Description",
            price = price,
            user = user,
            status = status,
            createdAt = now,
            updatedAt = now
        )
    }

    @Test
    fun `createItem should create new item and return response`() {
        val userId = 1L
        val request = CreateItemRequest("Laptop", "High-performance laptop", BigDecimal("999.99"))
        val user = createTestUser(userId)
        val item = createTestItem(1, request.name, request.price, user)

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { itemRepository.save(any<Item>()) } returns item

        val result = itemService.createItem(userId, request)

        assertEquals(item.id, result.id)
        assertEquals(request.name, result.name)
        assertEquals(request.price, result.price)
        verify(exactly = 1) { itemRepository.save(any<Item>()) }
    }

    @Test
    fun `createItem should throw exception when user not found`() {
        val userId = 999L
        val request = CreateItemRequest("Laptop", "Description", BigDecimal("999.99"))
        every { userRepository.findById(userId) } returns Optional.empty()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            itemService.createItem(userId, request)
        }
        assertEquals("User not found with id: $userId", exception.message)
    }

    @Test
    fun `getItemById should return item when user owns it`() {
        val userId = 1L
        val itemId = 1L
        val user = createTestUser(userId)
        val item = createTestItem(itemId, "Laptop", BigDecimal("999.99"), user)

        every { itemRepository.findById(itemId) } returns Optional.of(item)

        val result = itemService.getItemById(itemId, userId)

        assertEquals(item.id, result.id)
        assertEquals(item.name, result.name)
    }

    @Test
    fun `getItemById should throw exception when user does not own item`() {
        val userId = 1L
        val otherUserId = 2L
        val itemId = 1L
        val user = createTestUser(otherUserId)
        val item = createTestItem(itemId, "Laptop", BigDecimal("999.99"), user)

        every { itemRepository.findById(itemId) } returns Optional.of(item)

        val exception = assertThrows(SecurityException::class.java) {
            itemService.getItemById(itemId, userId)
        }
        assertEquals("You don't have permission to access this item", exception.message)
    }

    @Test
    fun `getItemById should throw exception when item is deleted`() {
        val userId = 1L
        val itemId = 1L
        val user = createTestUser(userId)
        val item = createTestItem(itemId, "Laptop", BigDecimal("999.99"), user, ItemStatus.DELETED)

        every { itemRepository.findById(itemId) } returns Optional.of(item)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            itemService.getItemById(itemId, userId)
        }
        assertEquals("Item has been deleted", exception.message)
    }

    @Test
    fun `getUserItems should return page of items for user`() {
        val userId = 1L
        val pageable = PageRequest.of(0, 10)
        val user = createTestUser(userId)
        val items = listOf(
            createTestItem(1, "Item1", BigDecimal("100"), user),
            createTestItem(2, "Item2", BigDecimal("200"), user)
        )
        val page = PageImpl(items, pageable, items.size.toLong())

        every { itemRepository.findByUserIdAndStatus(userId, ItemStatus.ACTIVE, pageable) } returns page

        val result = itemService.getUserItems(userId, pageable)

        assertEquals(2, result.totalElements)
        assertEquals(items[0].id, result.content[0].id)
        assertEquals(items[1].id, result.content[1].id)
    }

    @Test
    fun `updateItem should update item when user owns it`() {
        val userId = 1L
        val itemId = 1L
        val request = UpdateItemRequest(name = "Updated Laptop", price = BigDecimal("1299.99"))
        val user = createTestUser(userId)
        val item = createTestItem(itemId, "Laptop", BigDecimal("999.99"), user)

        every { itemRepository.findById(itemId) } returns Optional.of(item)
        every { itemRepository.save(any<Item>()) } returns item

        val result = itemService.updateItem(itemId, userId, request)

        assertEquals("Updated Laptop", result.name)
        assertEquals(BigDecimal("1299.99"), result.price)
        verify(exactly = 1) { itemRepository.save(item) }
    }

    @Test
    fun `updateItem should throw exception when updating deleted item`() {
        val userId = 1L
        val itemId = 1L
        val request = UpdateItemRequest(name = "Updated Laptop")
        val user = createTestUser(userId)
        val item = createTestItem(itemId, "Laptop", BigDecimal("999.99"), user, ItemStatus.DELETED)

        every { itemRepository.findById(itemId) } returns Optional.of(item)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            itemService.updateItem(itemId, userId, request)
        }
        assertEquals("Cannot update deleted item", exception.message)
    }

    @Test
    fun `deleteItem should soft delete item`() {
        val userId = 1L
        val itemId = 1L
        val user = createTestUser(userId)
        val item = createTestItem(itemId, "Laptop", BigDecimal("999.99"), user)

        every { itemRepository.findById(itemId) } returns Optional.of(item)
        every { itemRepository.save(any<Item>()) } returns item

        itemService.deleteItem(itemId, userId)

        assertEquals(ItemStatus.DELETED, item.status)
        verify(exactly = 1) { itemRepository.save(item) }
    }

    @Test
    fun `deleteItem should throw exception when item already deleted`() {
        val userId = 1L
        val itemId = 1L
        val user = createTestUser(userId)
        val item = createTestItem(itemId, "Laptop", BigDecimal("999.99"), user, ItemStatus.DELETED)

        every { itemRepository.findById(itemId) } returns Optional.of(item)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            itemService.deleteItem(itemId, userId)
        }
        assertEquals("Item is already deleted", exception.message)
    }
}
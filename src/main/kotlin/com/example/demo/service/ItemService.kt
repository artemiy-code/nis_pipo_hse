package com.example.demo.service

import com.example.demo.api.dto.CreateItemRequest
import com.example.demo.api.dto.ItemResponse
import com.example.demo.api.dto.UpdateItemRequest
import com.example.demo.domain.model.Item
import com.example.demo.domain.model.ItemStatus
import com.example.demo.domain.repository.ItemRepository
import com.example.demo.domain.repository.UserRepository
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ItemService(
    private val itemRepository: ItemRepository,
    private val userRepository: UserRepository,
    private val meterRegistry: MeterRegistry
) {
    private val logger = LoggerFactory.getLogger(ItemService::class.java)

    init {
        meterRegistry.counter("items.created.total")
        meterRegistry.counter("items.updated.total")
        meterRegistry.counter("items.deleted.total")
    }

    @Transactional
    fun createItem(userId: Long, request: CreateItemRequest): ItemResponse {
        logger.info("Creating item for user ID: {}", userId)

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with id: $userId") }

        val item = Item(
            name = request.name,
            description = request.description,
            price = request.price,
            user = user
        )

        val savedItem = itemRepository.save(item)
        meterRegistry.counter("items.created.total").increment()

        logger.info("Item created successfully with ID: {}", savedItem.id)

        return mapToResponse(savedItem)
    }

    @Transactional(readOnly = true)
    fun getItemById(itemId: Long, userId: Long): ItemResponse {
        logger.debug("Fetching item ID: {} for user ID: {}", itemId, userId)

        val item = itemRepository.findById(itemId)
            .orElseThrow { IllegalArgumentException("Item not found") }

        if (item.user.id != userId) {
            throw SecurityException("You don't have permission to access this item")
        }

        if (item.status == ItemStatus.DELETED) {
            throw IllegalArgumentException("Item has been deleted")
        }

        return mapToResponse(item)
    }

    @Transactional(readOnly = true)
    fun getUserItems(userId: Long, pageable: Pageable): Page<ItemResponse> {
        logger.debug("Fetching items for user ID: {}", userId)

        // Используем правильный метод репозитория
        return itemRepository.findByUserIdAndStatus(userId, ItemStatus.ACTIVE, pageable)
            .map { mapToResponse(it) }
    }

    @Transactional
    fun updateItem(itemId: Long, userId: Long, request: UpdateItemRequest): ItemResponse {
        logger.info("Updating item ID: {} for user ID: {}", itemId, userId)

        val item = itemRepository.findById(itemId)
            .orElseThrow { IllegalArgumentException("Item not found") }

        if (item.user.id != userId) {
            throw SecurityException("You don't have permission to update this item")
        }

        if (item.status == ItemStatus.DELETED) {
            throw IllegalArgumentException("Cannot update deleted item")
        }

        request.name?.let { item.name = it }
        request.description?.let { item.description = it }
        request.price?.let { item.price = it }
        request.status?.let { status ->
            item.status = try {
                ItemStatus.valueOf(status.uppercase())
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid status value. Allowed: ACTIVE, INACTIVE, DELETED")
            }
        }

        val updatedItem = itemRepository.save(item)
        meterRegistry.counter("items.updated.total").increment()

        logger.info("Item updated successfully: {}", updatedItem.id)

        return mapToResponse(updatedItem)
    }

    @Transactional
    fun deleteItem(itemId: Long, userId: Long) {
        logger.info("Deleting item ID: {} for user ID: {}", itemId, userId)

        val item = itemRepository.findById(itemId)
            .orElseThrow { IllegalArgumentException("Item not found") }

        if (item.user.id != userId) {
            throw SecurityException("You don't have permission to delete this item")
        }

        if (item.status == ItemStatus.DELETED) {
            throw IllegalArgumentException("Item is already deleted")
        }

        item.status = ItemStatus.DELETED
        itemRepository.save(item)

        meterRegistry.counter("items.deleted.total").increment()

        logger.info("Item marked as deleted: {}", itemId)
    }

    private fun mapToResponse(item: Item): ItemResponse {
        return ItemResponse(
            id = item.id!!,
            name = item.name,
            description = item.description,
            price = item.price,
            userId = item.user.id!!,
            username = item.user.username,
            status = item.status.name,
            createdAt = item.createdAt.toString(),
            updatedAt = item.updatedAt.toString()
        )
    }
}
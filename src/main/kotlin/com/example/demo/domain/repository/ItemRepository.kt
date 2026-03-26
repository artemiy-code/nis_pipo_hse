package com.example.demo.domain.repository

import com.example.demo.domain.model.Item
import com.example.demo.domain.model.ItemStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemRepository : JpaRepository<Item, Long> {

    fun findByUserId(userId: Long, pageable: Pageable): Page<Item>

    fun findByUserIdAndStatus(userId: Long, status: ItemStatus, pageable: Pageable): Page<Item>

    fun findByIdAndUserId(id: Long, userId: Long): Item?
}
package com.beerstoresystem.order.infrastructure.persistence.jpa

import com.beerstoresystem.order.infrastructure.persistence.entity.OrderStatusHistoryEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderStatusHistoryJpaRepository : JpaRepository<OrderStatusHistoryEntity, Long> {
    fun findAllByOrderIdOrderByChangedAtAsc(orderId: Long): List<OrderStatusHistoryEntity>
}

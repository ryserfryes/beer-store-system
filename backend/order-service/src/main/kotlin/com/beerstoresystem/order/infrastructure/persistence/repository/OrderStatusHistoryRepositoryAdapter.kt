package com.beerstoresystem.order.infrastructure.persistence.repository

import com.beerstoresystem.order.domain.model.OrderStatusHistory
import com.beerstoresystem.order.domain.repository.OrderStatusHistoryRepository
import com.beerstoresystem.order.infrastructure.persistence.jpa.OrderStatusHistoryJpaRepository
import com.beerstoresystem.order.infrastructure.persistence.mapper.toDomain
import com.beerstoresystem.order.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Component

@Component
class OrderStatusHistoryRepositoryAdapter(
    private val jpa: OrderStatusHistoryJpaRepository
) : OrderStatusHistoryRepository {

    override fun save(history: OrderStatusHistory): OrderStatusHistory =
        jpa.save(history.toEntity()).toDomain()
}

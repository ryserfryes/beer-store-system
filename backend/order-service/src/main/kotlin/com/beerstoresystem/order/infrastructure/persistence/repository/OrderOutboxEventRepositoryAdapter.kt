package com.beerstoresystem.order.infrastructure.persistence.repository

import com.beerstoresystem.order.domain.model.OrderOutboxEvent
import com.beerstoresystem.order.domain.repository.OrderOutboxEventRepository
import com.beerstoresystem.order.infrastructure.persistence.jpa.OrderOutboxEventJpaRepository
import com.beerstoresystem.order.infrastructure.persistence.mapper.toDomain
import com.beerstoresystem.order.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Component

@Component
class OrderOutboxEventRepositoryAdapter(
    private val jpa: OrderOutboxEventJpaRepository
) : OrderOutboxEventRepository {

    override fun findUnpublished(): List<OrderOutboxEvent> =
        jpa.findUnpublished().map { it.toDomain() }

    override fun save(event: OrderOutboxEvent): OrderOutboxEvent =
        jpa.save(event.toEntity()).toDomain()
}

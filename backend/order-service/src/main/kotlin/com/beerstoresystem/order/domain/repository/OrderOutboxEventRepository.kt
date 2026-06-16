package com.beerstoresystem.order.domain.repository

import com.beerstoresystem.order.domain.model.OrderOutboxEvent

interface OrderOutboxEventRepository {
    fun findUnpublished(): List<OrderOutboxEvent>
    fun save(event: OrderOutboxEvent): OrderOutboxEvent
}

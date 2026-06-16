package com.beerstoresystem.order.domain.model

import java.time.OffsetDateTime

data class OrderStatusHistory(
    val id: Long,
    val orderId: Long,
    val changedByEmployeeId: Long?,
    val fromStatus: OrderStatus?,
    val toStatus: OrderStatus,
    val changedAt: OffsetDateTime
)

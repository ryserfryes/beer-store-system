package com.beerstoresystem.order.domain.model

import java.math.BigDecimal
import java.time.OffsetDateTime

data class CustomerOrder(
    val id: Long,
    val customerId: Long,
    val pickupPointId: Long,
    val status: OrderStatus,
    val subtotalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val pickupCode: String,
    val placedAt: OffsetDateTime,
    val readyForPickupAt: OffsetDateTime?,
    val pickupExpiresAt: OffsetDateTime?,
    val pickedUpAt: OffsetDateTime?,
    val items: List<OrderItem>
)

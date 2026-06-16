package com.beerstoresystem.order.integration.rest.dto

import java.math.BigDecimal
import java.time.OffsetDateTime

data class OrderItemDto(
    val id: Long,
    val variantId: Long,
    val batchId: Long?,
    val beerId: Long?,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val lineDiscount: BigDecimal
)

data class OrderDto(
    val id: Long,
    val customerId: Long,
    val pickupPointId: Long,
    val status: String,
    val subtotalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val pickupCode: String,
    val placedAt: OffsetDateTime,
    val readyForPickupAt: OffsetDateTime?,
    val pickupExpiresAt: OffsetDateTime?,
    val pickedUpAt: OffsetDateTime?,
    val items: List<OrderItemDto>
)

data class PlaceOrderRequest(
    val customerId: Long,
    val pickupPointId: Long
)

data class OrderActionRequest(
    val employeeId: Long? = null
)

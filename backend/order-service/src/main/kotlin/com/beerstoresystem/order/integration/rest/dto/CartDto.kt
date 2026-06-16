package com.beerstoresystem.order.integration.rest.dto

import java.time.OffsetDateTime

data class CartItemDto(
    val id: Long,
    val variantId: Long,
    val quantity: Int,
    val addedAt: OffsetDateTime
)

data class CartDto(
    val id: Long,
    val customerId: Long,
    val updatedAt: OffsetDateTime,
    val items: List<CartItemDto>
)

data class AddCartItemRequest(
    val variantId: Long,
    val quantity: Int
)

data class UpdateCartItemRequest(
    val quantity: Int
)

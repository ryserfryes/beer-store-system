package com.beerstoresystem.order.domain.model

import java.time.OffsetDateTime

data class CartItem(
    val id: Long,
    val cartId: Long,
    val variantId: Long,
    val quantity: Int,
    val addedAt: OffsetDateTime
)

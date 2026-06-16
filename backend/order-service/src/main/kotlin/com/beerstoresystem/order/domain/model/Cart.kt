package com.beerstoresystem.order.domain.model

import java.time.OffsetDateTime

data class Cart(
    val id: Long,
    val customerId: Long,
    val updatedAt: OffsetDateTime,
    val items: List<CartItem>
)

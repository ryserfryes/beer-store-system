package com.beerstoresystem.order.integration.rest.mapper

import com.beerstoresystem.order.domain.model.Cart
import com.beerstoresystem.order.domain.model.CartItem
import com.beerstoresystem.order.integration.rest.dto.CartDto
import com.beerstoresystem.order.integration.rest.dto.CartItemDto

fun CartItem.toDto(): CartItemDto = CartItemDto(
    id = id,
    variantId = variantId,
    quantity = quantity,
    addedAt = addedAt
)

fun Cart.toDto(): CartDto = CartDto(
    id = id,
    customerId = customerId,
    updatedAt = updatedAt,
    items = items.map { it.toDto() }
)

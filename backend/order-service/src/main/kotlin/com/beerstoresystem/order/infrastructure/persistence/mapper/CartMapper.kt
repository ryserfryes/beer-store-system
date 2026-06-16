package com.beerstoresystem.order.infrastructure.persistence.mapper

import com.beerstoresystem.order.domain.model.Cart
import com.beerstoresystem.order.domain.model.CartItem
import com.beerstoresystem.order.infrastructure.persistence.entity.CartEntity
import com.beerstoresystem.order.infrastructure.persistence.entity.CartItemEntity

fun CartItemEntity.toDomain(): CartItem = CartItem(
    id = id,
    cartId = cart?.id ?: 0L,
    variantId = variantId,
    quantity = quantity,
    addedAt = addedAt
)

fun CartItem.toEntity(cartEntity: CartEntity): CartItemEntity = CartItemEntity().also { e ->
    if (id != 0L) e.id = id
    e.cart = cartEntity
    e.variantId = variantId
    e.quantity = quantity
    e.addedAt = addedAt
}

fun CartEntity.toDomain(): Cart = Cart(
    id = id,
    customerId = customerId,
    updatedAt = updatedAt,
    items = items.map { it.toDomain() }
)

fun Cart.toEntity(): CartEntity = CartEntity().also { e ->
    if (id != 0L) e.id = id
    e.customerId = customerId
    e.updatedAt = updatedAt
}

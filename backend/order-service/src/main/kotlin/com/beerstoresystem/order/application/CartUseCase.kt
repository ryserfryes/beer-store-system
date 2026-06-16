package com.beerstoresystem.order.application

import com.beerstoresystem.order.domain.model.Cart

data class AddCartItemCommand(
    val variantId: Long,
    val quantity: Int
)

interface CartUseCase {
    fun getCart(customerId: Long): Cart
    fun addItem(customerId: Long, command: AddCartItemCommand): Cart
    fun updateItem(customerId: Long, variantId: Long, quantity: Int): Cart
    fun removeItem(customerId: Long, variantId: Long)
    fun clearCart(customerId: Long)
}

package com.beerstoresystem.order.domain.repository

import com.beerstoresystem.order.domain.model.Cart

interface CartRepository {
    fun findByCustomerId(customerId: Long): Cart?
    fun findByCustomerIdWithItems(customerId: Long): Cart?
    fun save(cart: Cart): Cart
}

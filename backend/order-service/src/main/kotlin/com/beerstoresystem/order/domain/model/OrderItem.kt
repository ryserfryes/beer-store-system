package com.beerstoresystem.order.domain.model

import java.math.BigDecimal

data class OrderItem(
    val id: Long,
    val orderId: Long,
    val variantId: Long,
    val batchId: Long?,
    val beerId: Long?,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val lineDiscount: BigDecimal
)

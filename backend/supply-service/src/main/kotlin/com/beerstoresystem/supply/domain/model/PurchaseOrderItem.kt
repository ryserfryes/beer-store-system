package com.beerstoresystem.supply.domain.model

import java.math.BigDecimal

data class PurchaseOrderItem(
    val id: Long,
    val purchaseOrderId: Long,
    val variantId: Long,
    val quantity: Int,
    val unitCost: BigDecimal
)

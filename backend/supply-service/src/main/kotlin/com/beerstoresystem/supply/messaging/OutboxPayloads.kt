package com.beerstoresystem.supply.messaging

import java.math.BigDecimal

data class OutboxItemPayload(
    val variantId: Long,
    val purchaseOrderItemId: Long,
    val quantity: Int,
    val lotCode: String,
    val wholesaleCost: BigDecimal
)

data class OutboxPayload(
    val purchaseOrderId: Long,
    val warehouseId: Long,
    val items: List<OutboxItemPayload>
)

package com.beerstoresystem.supply.domain.model

import java.time.OffsetDateTime

data class PurchaseOrder(
    val id: Long,
    val supplierId: Long,
    val warehouseId: Long,
    val status: PurchaseOrderStatus,
    val orderedAt: OffsetDateTime,
    val expectedAt: OffsetDateTime?,
    val receivedAt: OffsetDateTime?
)

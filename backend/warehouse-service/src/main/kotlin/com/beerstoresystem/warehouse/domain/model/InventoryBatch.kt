package com.beerstoresystem.warehouse.domain.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

data class InventoryBatch(
    val id: Long,
    val variantId: Long,
    val warehouseId: Long,
    val purchaseOrderItemId: Long?,
    val lotCode: String?,
    val quantityOnHand: Int,
    val wholesaleCost: BigDecimal?,
    val producedOn: LocalDate?,
    val expiresOn: LocalDate?,
    val receivedAt: OffsetDateTime?
)

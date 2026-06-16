package com.beerstoresystem.supply.integration.rest.dto

import java.math.BigDecimal
import java.time.OffsetDateTime

data class PurchaseOrderSummaryDto(
    val id: Long,
    val supplierName: String,
    val warehouseId: Long,
    val status: String,
    val orderedAt: OffsetDateTime,
    val expectedAt: OffsetDateTime?,
    val receivedAt: OffsetDateTime?
)

data class PurchaseOrderDetailDto(
    val id: Long,
    val supplierId: Long,
    val supplierName: String,
    val warehouseId: Long,
    val status: String,
    val orderedAt: OffsetDateTime,
    val expectedAt: OffsetDateTime?,
    val receivedAt: OffsetDateTime?,
    val items: List<PurchaseOrderItemDto>
)

data class PurchaseOrderItemDto(
    val id: Long,
    val variantId: Long,
    val quantity: Int,
    val unitCost: BigDecimal
)

data class CreatePurchaseOrderRequest(
    val supplierId: Long,
    val warehouseId: Long,
    val expectedAt: OffsetDateTime? = null
)

data class AddItemRequest(
    val variantId: Long,
    val quantity: Int,
    val unitCost: BigDecimal
)

data class UpdateStatusRequest(
    val status: String
)

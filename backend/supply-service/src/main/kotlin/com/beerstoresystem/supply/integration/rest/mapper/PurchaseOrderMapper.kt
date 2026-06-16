package com.beerstoresystem.supply.integration.rest.mapper

import com.beerstoresystem.supply.domain.model.PurchaseOrder
import com.beerstoresystem.supply.domain.model.PurchaseOrderItem
import com.beerstoresystem.supply.integration.rest.dto.PurchaseOrderDetailDto
import com.beerstoresystem.supply.integration.rest.dto.PurchaseOrderItemDto
import com.beerstoresystem.supply.integration.rest.dto.PurchaseOrderSummaryDto

fun PurchaseOrder.toSummaryDto(supplierName: String) = PurchaseOrderSummaryDto(
    id = id,
    supplierName = supplierName,
    warehouseId = warehouseId,
    status = status.name.lowercase(),
    orderedAt = orderedAt,
    expectedAt = expectedAt,
    receivedAt = receivedAt
)

fun PurchaseOrder.toDetailDto(supplierName: String, items: List<PurchaseOrderItem>) =
    PurchaseOrderDetailDto(
        id = id,
        supplierId = supplierId,
        supplierName = supplierName,
        warehouseId = warehouseId,
        status = status.name.lowercase(),
        orderedAt = orderedAt,
        expectedAt = expectedAt,
        receivedAt = receivedAt,
        items = items.map { it.toItemDto() }
    )

fun PurchaseOrderItem.toItemDto() = PurchaseOrderItemDto(
    id = id,
    variantId = variantId,
    quantity = quantity,
    unitCost = unitCost
)

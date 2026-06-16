package com.beerstoresystem.warehouse.infrastructure.persistence.mapper

import com.beerstoresystem.warehouse.domain.model.InventoryBatch
import com.beerstoresystem.warehouse.infrastructure.persistence.entity.InventoryBatchEntity

fun InventoryBatchEntity.toDomain(): InventoryBatch = InventoryBatch(
    id = id,
    variantId = variantId,
    warehouseId = warehouseId,
    purchaseOrderItemId = purchaseOrderItemId,
    lotCode = lotCode,
    quantityOnHand = quantityOnHand,
    wholesaleCost = wholesaleCost,
    producedOn = producedOn,
    expiresOn = expiresOn,
    receivedAt = receivedAt
)

fun InventoryBatch.toEntity(): InventoryBatchEntity = InventoryBatchEntity().also { e ->
    e.id = id
    e.variantId = variantId
    e.warehouseId = warehouseId
    e.purchaseOrderItemId = purchaseOrderItemId
    e.lotCode = lotCode
    e.quantityOnHand = quantityOnHand
    e.wholesaleCost = wholesaleCost
    e.producedOn = producedOn
    e.expiresOn = expiresOn
    e.receivedAt = receivedAt
}

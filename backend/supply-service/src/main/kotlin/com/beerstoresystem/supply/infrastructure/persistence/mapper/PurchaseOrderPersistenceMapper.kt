package com.beerstoresystem.supply.infrastructure.persistence.mapper

import com.beerstoresystem.supply.domain.model.PurchaseOrder
import com.beerstoresystem.supply.domain.model.PurchaseOrderItem
import com.beerstoresystem.supply.infrastructure.persistence.entity.PurchaseOrderEntity
import com.beerstoresystem.supply.infrastructure.persistence.entity.PurchaseOrderItemEntity

fun PurchaseOrderEntity.toDomain() = PurchaseOrder(
    id = id,
    supplierId = supplierId,
    warehouseId = warehouseId,
    status = status,
    orderedAt = orderedAt,
    expectedAt = expectedAt,
    receivedAt = receivedAt
)

fun PurchaseOrder.toEntity(): PurchaseOrderEntity = PurchaseOrderEntity().apply {
    if (this@toEntity.id != 0L) id = this@toEntity.id
    supplierId = this@toEntity.supplierId
    warehouseId = this@toEntity.warehouseId
    status = this@toEntity.status
    orderedAt = this@toEntity.orderedAt
    expectedAt = this@toEntity.expectedAt
    receivedAt = this@toEntity.receivedAt
}

fun PurchaseOrderItemEntity.toDomain() = PurchaseOrderItem(
    id = id,
    purchaseOrderId = purchaseOrderId,
    variantId = variantId,
    quantity = quantity,
    unitCost = unitCost
)

fun PurchaseOrderItem.toEntity(): PurchaseOrderItemEntity = PurchaseOrderItemEntity().apply {
    if (this@toEntity.id != 0L) id = this@toEntity.id
    purchaseOrderId = this@toEntity.purchaseOrderId
    variantId = this@toEntity.variantId
    quantity = this@toEntity.quantity
    unitCost = this@toEntity.unitCost
}

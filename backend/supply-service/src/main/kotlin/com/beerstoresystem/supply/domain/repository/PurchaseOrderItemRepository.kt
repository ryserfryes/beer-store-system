package com.beerstoresystem.supply.domain.repository

import com.beerstoresystem.supply.domain.model.PurchaseOrderItem

interface PurchaseOrderItemRepository {
    fun findAllByPurchaseOrderId(purchaseOrderId: Long): List<PurchaseOrderItem>
    fun save(item: PurchaseOrderItem): PurchaseOrderItem
}

package com.beerstoresystem.warehouse.application

import com.beerstoresystem.warehouse.domain.model.InventoryBatch
import com.beerstoresystem.warehouse.domain.model.PickupPoint
import com.beerstoresystem.warehouse.domain.model.Warehouse

data class StockItem(val variantId: Long, val quantity: Int)

data class StockAvailabilityResult(
    val variantId: Long,
    val quantityOnHand: Int,
    val available: Boolean
)

data class ReservedBatchResult(
    val variantId: Long,
    val batchId: Long,
    val quantity: Int
)

data class SupplyItem(
    val variantId: Long,
    val purchaseOrderItemId: Long?,
    val quantity: Int,
    val lotCode: String?,
    val wholesaleCost: Double?
)

interface WarehouseUseCase {
    fun getAllWarehouses(): List<Warehouse>
    fun getWarehouseById(id: Long): Warehouse?
    fun getPickupPoints(activeOnly: Boolean): List<PickupPoint>
    fun getInventoryForWarehouse(warehouseId: Long): List<InventoryBatch>
    fun checkStock(items: List<StockItem>): List<StockAvailabilityResult>
    fun reserveStock(orderId: Long, items: List<StockItem>): Pair<Boolean, List<ReservedBatchResult>>
    fun deductStockByBatch(batchId: Long, quantity: Int)
    fun deductStockFifo(variantId: Long, quantity: Int)
    fun receiveSupply(purchaseOrderId: Long, warehouseId: Long, items: List<SupplyItem>)
}

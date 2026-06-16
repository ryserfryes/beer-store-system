package com.beerstoresystem.order.domain.port

data class StockCheckRequest(
    val variantId: Long,
    val quantity: Int
)

data class StockAvailabilityResult(
    val variantId: Long,
    val quantityOnHand: Int,
    val available: Boolean
)

data class CheckStockResult(
    val allAvailable: Boolean,
    val availability: List<StockAvailabilityResult>
)

data class ReservedBatchResult(
    val variantId: Long,
    val batchId: Long,
    val quantity: Int
)

data class ReserveStockResult(
    val success: Boolean,
    val reserved: List<ReservedBatchResult>
)

interface WarehousePort {
    fun checkStock(items: List<StockCheckRequest>): CheckStockResult
    fun reserveStock(orderId: Long, items: List<StockCheckRequest>): ReserveStockResult
}

package com.beerstoresystem.warehouse.domain.repository

import com.beerstoresystem.warehouse.domain.model.InventoryBatch
import java.time.LocalDate

interface InventoryBatchDomainRepository {
    fun findByWarehouseId(warehouseId: Long): List<InventoryBatch>
    fun findAvailableByVariantIdFifo(variantId: Long, today: LocalDate): List<InventoryBatch>
    fun sumAvailableQuantityByVariantId(variantId: Long, today: LocalDate): Int
    fun findById(id: Long): InventoryBatch?
    fun save(batch: InventoryBatch): InventoryBatch
}

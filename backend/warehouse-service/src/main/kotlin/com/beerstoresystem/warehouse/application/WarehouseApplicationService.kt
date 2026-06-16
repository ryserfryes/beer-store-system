package com.beerstoresystem.warehouse.application

import com.beerstoresystem.warehouse.domain.model.InventoryBatch
import com.beerstoresystem.warehouse.domain.model.PickupPoint
import com.beerstoresystem.warehouse.domain.model.Warehouse
import com.beerstoresystem.warehouse.domain.repository.InventoryBatchDomainRepository
import com.beerstoresystem.warehouse.domain.repository.PickupPointDomainRepository
import com.beerstoresystem.warehouse.domain.repository.WarehouseDomainRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
@Transactional(readOnly = true)
class WarehouseApplicationService(
    private val warehouseRepo: WarehouseDomainRepository,
    private val pickupPointRepo: PickupPointDomainRepository,
    private val inventoryBatchRepo: InventoryBatchDomainRepository
) : WarehouseUseCase {

    override fun getAllWarehouses(): List<Warehouse> = warehouseRepo.findAll()

    override fun getWarehouseById(id: Long): Warehouse? = warehouseRepo.findById(id)

    override fun getPickupPoints(activeOnly: Boolean): List<PickupPoint> =
        if (activeOnly) pickupPointRepo.findByIsActive(true)
        else pickupPointRepo.findAll()

    override fun getInventoryForWarehouse(warehouseId: Long): List<InventoryBatch> =
        inventoryBatchRepo.findByWarehouseId(warehouseId)

    override fun checkStock(items: List<StockItem>): List<StockAvailabilityResult> {
        val today = LocalDate.now()
        return items.map { item ->
            val onHand = inventoryBatchRepo.sumAvailableQuantityByVariantId(item.variantId, today)
            StockAvailabilityResult(
                variantId = item.variantId,
                quantityOnHand = onHand,
                available = onHand >= item.quantity
            )
        }
    }

    @Transactional
    override fun reserveStock(orderId: Long, items: List<StockItem>): Pair<Boolean, List<ReservedBatchResult>> {
        val today = LocalDate.now()
        val reserved = mutableListOf<ReservedBatchResult>()

        for (item in items) {
            val batches = inventoryBatchRepo.findAvailableByVariantIdFifo(item.variantId, today)
            var remaining = item.quantity

            for (batch in batches) {
                if (remaining <= 0) break
                val deduct = minOf(remaining, batch.quantityOnHand)
                inventoryBatchRepo.save(batch.copy(quantityOnHand = batch.quantityOnHand - deduct))
                reserved.add(ReservedBatchResult(item.variantId, batch.id, deduct))
                remaining -= deduct
            }

            if (remaining > 0) {
                return Pair(false, emptyList())
            }
        }

        return Pair(true, reserved)
    }

    @Transactional
    override fun deductStockByBatch(batchId: Long, quantity: Int) {
        val batch = inventoryBatchRepo.findById(batchId) ?: return
        inventoryBatchRepo.save(batch.copy(quantityOnHand = maxOf(0, batch.quantityOnHand - quantity)))
    }

    @Transactional
    override fun deductStockFifo(variantId: Long, quantity: Int) {
        val today = LocalDate.now()
        val batches = inventoryBatchRepo.findAvailableByVariantIdFifo(variantId, today)
        var remaining = quantity

        for (batch in batches) {
            if (remaining <= 0) break
            val deduct = minOf(remaining, batch.quantityOnHand)
            inventoryBatchRepo.save(batch.copy(quantityOnHand = batch.quantityOnHand - deduct))
            remaining -= deduct
        }
    }

    @Transactional
    override fun receiveSupply(purchaseOrderId: Long, warehouseId: Long, items: List<SupplyItem>) {
        val now = OffsetDateTime.now()
        for (item in items) {
            val batch = InventoryBatch(
                id = 0L,
                variantId = item.variantId,
                warehouseId = warehouseId,
                purchaseOrderItemId = item.purchaseOrderItemId,
                lotCode = item.lotCode,
                quantityOnHand = item.quantity,
                wholesaleCost = item.wholesaleCost?.let { BigDecimal.valueOf(it) },
                producedOn = null,
                expiresOn = null,
                receivedAt = now
            )
            inventoryBatchRepo.save(batch)
        }
    }
}

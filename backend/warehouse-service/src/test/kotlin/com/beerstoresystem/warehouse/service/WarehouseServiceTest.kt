package com.beerstoresystem.warehouse.service

import io.mockk.*
import io.mockk.junit5.MockKExtension
import com.beerstoresystem.warehouse.application.StockItem
import com.beerstoresystem.warehouse.application.SupplyItem
import com.beerstoresystem.warehouse.application.WarehouseApplicationService
import com.beerstoresystem.warehouse.domain.model.InventoryBatch
import com.beerstoresystem.warehouse.domain.model.PickupPoint
import com.beerstoresystem.warehouse.domain.model.Warehouse
import com.beerstoresystem.warehouse.domain.repository.InventoryBatchDomainRepository
import com.beerstoresystem.warehouse.domain.repository.PickupPointDomainRepository
import com.beerstoresystem.warehouse.domain.repository.WarehouseDomainRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.time.OffsetDateTime

@ExtendWith(MockKExtension::class)
class WarehouseServiceTest {

    private val warehouseRepo = mockk<WarehouseDomainRepository>()
    private val pickupPointRepo = mockk<PickupPointDomainRepository>()
    private val inventoryBatchRepo = mockk<InventoryBatchDomainRepository>()

    private val service = WarehouseApplicationService(warehouseRepo, pickupPointRepo, inventoryBatchRepo)

    private fun makeWarehouse(id: Long, name: String): Warehouse =
        Warehouse(id = id, countryId = 1L, name = name, addressLine = "Addr", city = "City", postalCode = "00000", createdAt = null)

    private fun makePickupPoint(id: Long, active: Boolean): PickupPoint =
        PickupPoint(id = id, warehouseId = 1L, name = "PP-$id", city = "City", addressLine = "Addr", postalCode = "00000", workingHours = null, isActive = active)

    private fun makeBatch(id: Long, variantId: Long, qty: Int): InventoryBatch =
        InventoryBatch(
            id = id, variantId = variantId, warehouseId = 1L, purchaseOrderItemId = null,
            lotCode = "LOT-$id", quantityOnHand = qty, wholesaleCost = null,
            producedOn = null, expiresOn = null, receivedAt = OffsetDateTime.now()
        )

    @Test
    fun `getAllWarehouses delegates to repository`() {
        val wh = makeWarehouse(1L, "Main WH")
        every { warehouseRepo.findAll() } returns listOf(wh)

        val result = service.getAllWarehouses()

        assertEquals(1, result.size)
        assertEquals("Main WH", result[0].name)
    }

    @Test
    fun `getPickupPoints with activeOnly=true filters`() {
        val active = makePickupPoint(1L, true)
        every { pickupPointRepo.findByIsActive(true) } returns listOf(active)

        val result = service.getPickupPoints(activeOnly = true)

        assertEquals(1, result.size)
        verify { pickupPointRepo.findByIsActive(true) }
        verify(exactly = 0) { pickupPointRepo.findAll() }
    }

    @Test
    fun `getPickupPoints with activeOnly=false returns all`() {
        every { pickupPointRepo.findAll() } returns listOf(mockk(), mockk())

        val result = service.getPickupPoints(activeOnly = false)

        assertEquals(2, result.size)
    }

    @Test
    fun `checkStock returns available=true when enough stock`() {
        every { inventoryBatchRepo.sumAvailableQuantityByVariantId(1L, any()) } returns 10

        val result = service.checkStock(listOf(StockItem(variantId = 1L, quantity = 5)))

        assertEquals(1, result.size)
        assertTrue(result[0].available)
        assertEquals(10, result[0].quantityOnHand)
    }

    @Test
    fun `checkStock returns available=false when insufficient`() {
        every { inventoryBatchRepo.sumAvailableQuantityByVariantId(2L, any()) } returns 3

        val result = service.checkStock(listOf(StockItem(variantId = 2L, quantity = 10)))

        assertFalse(result[0].available)
    }

    @Test
    fun `reserveStock returns success when enough across batches`() {
        val batch1 = makeBatch(id = 1, variantId = 1L, qty = 3)
        val batch2 = makeBatch(id = 2, variantId = 1L, qty = 5)
        every { inventoryBatchRepo.findAvailableByVariantIdFifo(1L, any()) } returns listOf(batch1, batch2)
        every { inventoryBatchRepo.save(any()) } answers { firstArg() }

        val (success, reserved) = service.reserveStock(orderId = 10L, listOf(StockItem(1L, 7)))

        assertTrue(success)
        assertEquals(2, reserved.size)
    }

    @Test
    fun `reserveStock returns failure when insufficient stock`() {
        val batch = makeBatch(id = 1, variantId = 1L, qty = 2)
        every { inventoryBatchRepo.findAvailableByVariantIdFifo(1L, any()) } returns listOf(batch)
        every { inventoryBatchRepo.save(any()) } answers { firstArg() }

        val (success, _) = service.reserveStock(orderId = 10L, listOf(StockItem(1L, 10)))

        assertFalse(success)
    }

    @Test
    fun `deductStockByBatch does nothing when batch not found`() {
        every { inventoryBatchRepo.findById(99L) } returns null

        service.deductStockByBatch(99L, 5)

        verify(exactly = 0) { inventoryBatchRepo.save(any()) }
    }

    @Test
    fun `deductStockByBatch reduces quantity and clamps to zero`() {
        val batch = makeBatch(id = 1, variantId = 1L, qty = 3)
        every { inventoryBatchRepo.findById(1L) } returns batch
        every { inventoryBatchRepo.save(any()) } answers { firstArg() }

        service.deductStockByBatch(1L, 100)

        verify { inventoryBatchRepo.save(match { it.quantityOnHand == 0 }) }
    }

    @Test
    fun `receiveSupply creates inventory batches`() {
        every { inventoryBatchRepo.save(any()) } answers { firstArg() }

        service.receiveSupply(
            purchaseOrderId = 1L, warehouseId = 2L,
            items = listOf(SupplyItem(variantId = 5L, purchaseOrderItemId = 10L, quantity = 50, lotCode = "LOT-A", wholesaleCost = 4.99))
        )

        verify(exactly = 1) { inventoryBatchRepo.save(any()) }
    }
}

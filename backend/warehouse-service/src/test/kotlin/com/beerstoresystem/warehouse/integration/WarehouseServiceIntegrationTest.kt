package com.beerstoresystem.warehouse.integration

import com.beerstoresystem.warehouse.application.StockItem
import com.beerstoresystem.warehouse.application.SupplyItem
import com.beerstoresystem.warehouse.application.WarehouseUseCase
import com.beerstoresystem.warehouse.infrastructure.persistence.entity.InventoryBatchEntity
import com.beerstoresystem.warehouse.infrastructure.persistence.entity.PickupPointEntity
import com.beerstoresystem.warehouse.infrastructure.persistence.entity.WarehouseEntity
import com.beerstoresystem.warehouse.infrastructure.persistence.jpa.InventoryBatchJpaRepository
import com.beerstoresystem.warehouse.infrastructure.persistence.jpa.PickupPointJpaRepository
import com.beerstoresystem.warehouse.infrastructure.persistence.jpa.WarehouseJpaRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.OffsetDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@Transactional
class WarehouseServiceIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
    }

    @Autowired
    private lateinit var warehouseUseCase: WarehouseUseCase

    @Autowired
    private lateinit var warehouseJpaRepository: WarehouseJpaRepository

    @Autowired
    private lateinit var pickupPointJpaRepository: PickupPointJpaRepository

    @Autowired
    private lateinit var inventoryBatchJpaRepository: InventoryBatchJpaRepository

    private fun saveWarehouse(name: String = "Test WH"): WarehouseEntity =
        warehouseJpaRepository.save(WarehouseEntity().apply {
            this.name = name; addressLine = "Test St 1"; city = "Moscow"; postalCode = "101000"
        })

    private fun savePickupPoint(name: String, active: Boolean, warehouse: WarehouseEntity): PickupPointEntity =
        pickupPointJpaRepository.save(PickupPointEntity().apply {
            this.name = name; isActive = active; warehouseId = warehouse.id
            city = "Moscow"; addressLine = "Lenina 1"; postalCode = "101000"
        })

    private fun saveBatch(variantId: Long, qty: Int, warehouse: WarehouseEntity): InventoryBatchEntity =
        inventoryBatchJpaRepository.save(InventoryBatchEntity().apply {
            this.variantId = variantId; quantityOnHand = qty
            warehouseId = warehouse.id; lotCode = "LOT-$variantId"; receivedAt = OffsetDateTime.now()
        })

    @Test
    fun `getAllWarehouses returns saved warehouses`() {
        saveWarehouse("Main Warehouse")

        val result = warehouseUseCase.getAllWarehouses()

        assertTrue(result.any { it.name == "Main Warehouse" })
    }

    @Test
    fun `getPickupPoints activeOnly=true returns only active`() {
        val wh = saveWarehouse()
        savePickupPoint("Active Point", active = true, warehouse = wh)
        savePickupPoint("Inactive Point", active = false, warehouse = wh)

        val result = warehouseUseCase.getPickupPoints(activeOnly = true)

        assertTrue(result.all { it.isActive })
        assertTrue(result.any { it.name == "Active Point" })
        assertTrue(result.none { it.name == "Inactive Point" })
    }

    @Test
    fun `getPickupPoints activeOnly=false returns all`() {
        val wh = saveWarehouse()
        savePickupPoint("PP1", active = true, warehouse = wh)
        savePickupPoint("PP2", active = false, warehouse = wh)

        val result = warehouseUseCase.getPickupPoints(activeOnly = false)

        assertTrue(result.size >= 2)
    }

    @Test
    fun `checkStock returns available when batch has enough quantity`() {
        val wh = saveWarehouse()
        saveBatch(variantId = 11L, qty = 20, warehouse = wh)

        val result = warehouseUseCase.checkStock(listOf(StockItem(variantId = 11L, quantity = 10)))

        assertEquals(1, result.size)
        assertTrue(result[0].available)
        assertEquals(20, result[0].quantityOnHand)
    }

    @Test
    fun `checkStock returns not available when insufficient`() {
        val wh = saveWarehouse()
        saveBatch(variantId = 12L, qty = 3, warehouse = wh)

        val result = warehouseUseCase.checkStock(listOf(StockItem(variantId = 12L, quantity = 10)))

        assertFalse(result[0].available)
    }

    @Test
    fun `reserveStock deducts from batches FIFO and returns success`() {
        val wh = saveWarehouse()
        saveBatch(variantId = 13L, qty = 5, warehouse = wh)
        saveBatch(variantId = 13L, qty = 10, warehouse = wh)

        val (success, reserved) = warehouseUseCase.reserveStock(orderId = 1L, listOf(StockItem(13L, 7)))

        assertTrue(success)
        assertTrue(reserved.isNotEmpty())
    }

    @Test
    fun `reserveStock returns failure when insufficient`() {
        val wh = saveWarehouse()
        saveBatch(variantId = 14L, qty = 2, warehouse = wh)

        val (success, _) = warehouseUseCase.reserveStock(orderId = 2L, listOf(StockItem(14L, 100)))

        assertFalse(success)
    }

    @Test
    fun `receiveSupply creates inventory batch`() {
        val wh = saveWarehouse()
        val countBefore = inventoryBatchJpaRepository.count()

        warehouseUseCase.receiveSupply(
            purchaseOrderId = 1L,
            warehouseId = wh.id,
            items = listOf(SupplyItem(variantId = 15L, purchaseOrderItemId = 1L, quantity = 50, lotCode = "LOT-NEW", wholesaleCost = 3.50))
        )

        assertEquals(countBefore + 1, inventoryBatchJpaRepository.count())
    }
}

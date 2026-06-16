package com.beerstoresystem.supply.application

import io.mockk.*
import io.mockk.junit5.MockKExtension
import com.beerstoresystem.supply.domain.model.PurchaseOrder
import com.beerstoresystem.supply.domain.model.PurchaseOrderItem
import com.beerstoresystem.supply.domain.model.PurchaseOrderStatus
import com.beerstoresystem.supply.domain.model.Supplier
import com.beerstoresystem.supply.domain.repository.OutboxEventRepository
import com.beerstoresystem.supply.domain.repository.PurchaseOrderItemRepository
import com.beerstoresystem.supply.domain.repository.PurchaseOrderRepository
import com.beerstoresystem.supply.domain.repository.SupplierRepository
import com.beerstoresystem.supply.integration.rest.dto.AddItemRequest
import com.beerstoresystem.supply.integration.rest.dto.CreatePurchaseOrderRequest
import com.beerstoresystem.supply.integration.rest.dto.CreateSupplierRequest
import com.beerstoresystem.supply.integration.rest.dto.UpdateStatusRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import tools.jackson.databind.ObjectMapper
import java.math.BigDecimal
import java.time.OffsetDateTime

@ExtendWith(MockKExtension::class)
class SupplyApplicationServiceTest {

    private val supplierRepository = mockk<SupplierRepository>()
    private val purchaseOrderRepository = mockk<PurchaseOrderRepository>()
    private val purchaseOrderItemRepository = mockk<PurchaseOrderItemRepository>()
    private val outboxEventRepository = mockk<OutboxEventRepository>()
    private val objectMapper = mockk<ObjectMapper>()

    private val service = SupplyApplicationService(
        supplierRepository, purchaseOrderRepository,
        purchaseOrderItemRepository, outboxEventRepository, objectMapper
    )

    private fun makeSupplier(id: Long = 1L, name: String = "Supplier A") = Supplier(
        id = id,
        name = name,
        countryId = null,
        contactEmail = "a@test.com",
        contactPhone = null,
        createdAt = OffsetDateTime.now()
    )

    private fun makeOrder(id: Long = 1L, supplierId: Long = 1L) = PurchaseOrder(
        id = id,
        supplierId = supplierId,
        warehouseId = 10,
        status = PurchaseOrderStatus.DRAFTING,
        orderedAt = OffsetDateTime.now(),
        expectedAt = null,
        receivedAt = null
    )

    @Test
    fun `getAllSuppliers returns mapped list`() {
        every { supplierRepository.findAll() } returns listOf(makeSupplier())

        val result = service.getAllSuppliers()

        assertEquals(1, result.size)
        assertEquals("Supplier A", result[0].name)
    }

    @Test
    fun `createSupplier saves and returns dto`() {
        val saved = makeSupplier(id = 5L, name = "New Supplier")
        every { supplierRepository.save(any()) } returns saved

        val result = service.createSupplier(CreateSupplierRequest("New Supplier"))

        assertEquals(5L, result.id)
        assertEquals("New Supplier", result.name)
        verify { supplierRepository.save(any()) }
    }

    @Test
    fun `createPurchaseOrder throws when supplier not found`() {
        every { supplierRepository.findById(99L) } returns null

        assertThrows<NoSuchElementException> {
            service.createPurchaseOrder(CreatePurchaseOrderRequest(supplierId = 99L, warehouseId = 1L))
        }
    }

    @Test
    fun `createPurchaseOrder saves and returns summary`() {
        val supplier = makeSupplier()
        val order = makeOrder()
        every { supplierRepository.findById(1L) } returns supplier
        every { purchaseOrderRepository.save(any()) } returns order

        val result = service.createPurchaseOrder(CreatePurchaseOrderRequest(supplierId = 1L, warehouseId = 10L))

        assertEquals("drafting", result.status)
        assertEquals(10L, result.warehouseId)
    }

    @Test
    fun `addItem throws when order not found`() {
        every { purchaseOrderRepository.findById(99L) } returns null

        assertThrows<NoSuchElementException> {
            service.addItem(99L, AddItemRequest(variantId = 1L, quantity = 5, unitCost = BigDecimal("10.00")))
        }
    }

    @Test
    fun `addItem saves item and returns dto`() {
        val order = makeOrder()
        val item = PurchaseOrderItem(id = 1, purchaseOrderId = 1, variantId = 2, quantity = 5, unitCost = BigDecimal("10.00"))
        every { purchaseOrderRepository.findById(1L) } returns order
        every { purchaseOrderItemRepository.save(any()) } returns item

        val result = service.addItem(1L, AddItemRequest(variantId = 2L, quantity = 5, unitCost = BigDecimal("10.00")))

        assertEquals(2L, result.variantId)
        assertEquals(5, result.quantity)
    }

    @Test
    fun `updateStatus throws on unknown status`() {
        val order = makeOrder()
        every { purchaseOrderRepository.findById(1L) } returns order

        assertThrows<IllegalArgumentException> {
            service.updateStatus(1L, UpdateStatusRequest("INVALID"))
        }
    }

    @Test
    fun `updateStatus to RECEIVED writes outbox event`() {
        val supplier = makeSupplier()
        val order = makeOrder()
        val item = PurchaseOrderItem(id = 1, purchaseOrderId = 1, variantId = 2, quantity = 3, unitCost = BigDecimal("5.00"))
        every { purchaseOrderRepository.findById(1L) } returns order
        every { purchaseOrderItemRepository.findAllByPurchaseOrderId(1L) } returns listOf(item)
        every { objectMapper.writeValueAsString(any()) } returns """{"test":"payload"}"""
        every { outboxEventRepository.save(any()) } returns mockk()
        every { purchaseOrderRepository.save(any()) } returns order.copy(status = PurchaseOrderStatus.RECEIVED, receivedAt = OffsetDateTime.now())
        every { supplierRepository.findById(1L) } returns supplier

        val result = service.updateStatus(1L, UpdateStatusRequest("RECEIVED"))

        assertEquals("received", result.status)
        assertNotNull(result.receivedAt)
        verify { outboxEventRepository.save(any()) }
    }

    @Test
    fun `updateStatus to non-RECEIVED does not write outbox`() {
        val supplier = makeSupplier()
        val order = makeOrder()
        every { purchaseOrderRepository.findById(1L) } returns order
        every { purchaseOrderRepository.save(any()) } returns order.copy(status = PurchaseOrderStatus.WAITING_APPROVAL)
        every { supplierRepository.findById(1L) } returns supplier

        service.updateStatus(1L, UpdateStatusRequest("WAITING_APPROVAL"))

        verify(exactly = 0) { outboxEventRepository.save(any()) }
    }
}

package com.beerstoresystem.order.application

import io.mockk.*
import io.mockk.junit5.MockKExtension
import com.beerstoresystem.order.domain.command.PlaceOrderCommand
import com.beerstoresystem.order.domain.model.*
import com.beerstoresystem.order.domain.port.*
import com.beerstoresystem.order.domain.repository.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import tools.jackson.databind.ObjectMapper
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockKExtension::class)
class OrderApplicationServiceTest {

    private val customerRepository = mockk<CustomerRepository>()
    private val cartRepository = mockk<CartRepository>()
    private val customerOrderRepository = mockk<CustomerOrderRepository>()
    private val orderStatusHistoryRepository = mockk<OrderStatusHistoryRepository>()
    private val outboxEventRepository = mockk<OrderOutboxEventRepository>()
    private val objectMapper = mockk<ObjectMapper>()
    private val catalogPort = mockk<CatalogPort>()
    private val warehousePort = mockk<WarehousePort>()

    private val service = OrderApplicationService(
        pickupExpiryDays = 3L,
        customerRepository = customerRepository,
        cartRepository = cartRepository,
        customerOrderRepository = customerOrderRepository,
        orderStatusHistoryRepository = orderStatusHistoryRepository,
        outboxEventRepository = outboxEventRepository,
        objectMapper = objectMapper,
        catalogPort = catalogPort,
        warehousePort = warehousePort
    )

    private fun makeCustomer(id: Long = 1L) = Customer(
        id = id, email = "test@example.com", firstName = "Test", lastName = "User",
        phone = null, birthDate = null, registeredAt = OffsetDateTime.now()
    )

    private fun makeCartItem(variantId: Long = 5L, qty: Int = 2, cartId: Long = 1L) = CartItem(
        id = 10L, cartId = cartId, variantId = variantId, quantity = qty,
        addedAt = OffsetDateTime.now()
    )

    private fun makeCart(id: Long = 1L, customerId: Long = 1L, items: List<CartItem> = emptyList()) = Cart(
        id = id, customerId = customerId, updatedAt = OffsetDateTime.now(), items = items
    )

    private fun makeOrder(id: Long = 100L, customerId: Long = 1L, items: List<OrderItem> = emptyList()) = CustomerOrder(
        id = id, customerId = customerId, pickupPointId = 2L,
        status = OrderStatus.PENDING,
        subtotalAmount = BigDecimal("19.98"), discountAmount = BigDecimal.ZERO,
        totalAmount = BigDecimal("19.98"), pickupCode = "ABCDE12345",
        placedAt = OffsetDateTime.now(),
        readyForPickupAt = null, pickupExpiresAt = null, pickedUpAt = null,
        items = items
    )

    private fun makeVariantInfo(id: Long = 5L, price: Double = 9.99) =
        VariantInfo(id = id, sku = "SKU-$id", unitPrice = price, beerId = null)

    private fun makeCheckStockResult(allAvailable: Boolean = true) = CheckStockResult(
        allAvailable = allAvailable,
        availability = listOf(
            StockAvailabilityResult(variantId = 5L, quantityOnHand = 10, available = allAvailable)
        )
    )

    private fun makeReserveStockResult(success: Boolean = true) = ReserveStockResult(
        success = success,
        reserved = listOf(ReservedBatchResult(variantId = 5L, batchId = 7L, quantity = 2))
    )

    @Test
    fun `getOrder throws when not found`() {
        every { customerOrderRepository.findByIdWithItems(99L) } returns null

        assertThrows<NoSuchElementException> { service.getOrder(99L) }
    }

    @Test
    fun `getOrder returns domain model when found`() {
        val order = makeOrder()
        every { customerOrderRepository.findByIdWithItems(100L) } returns order

        val result = service.getOrder(100L)

        assertEquals(100L, result.id)
        assertEquals(OrderStatus.PENDING, result.status)
    }

    @Test
    fun `getCustomerOrders throws when customer not found`() {
        every { customerRepository.findById(99L) } returns null

        assertThrows<NoSuchElementException> { service.getCustomerOrders(99L) }
    }

    @Test
    fun `getCustomerOrders returns list`() {
        every { customerRepository.findById(1L) } returns makeCustomer()
        every { customerOrderRepository.findAllByCustomerId(1L) } returns listOf(makeOrder(), makeOrder(id = 101L))

        val result = service.getCustomerOrders(1L)

        assertEquals(2, result.size)
    }

    @Test
    fun `placeOrder throws when customer not found`() {
        every { customerRepository.findById(99L) } returns null

        assertThrows<NoSuchElementException> {
            service.placeOrder(PlaceOrderCommand(customerId = 99L, pickupPointId = 1L))
        }
    }

    @Test
    fun `placeOrder throws when cart not found`() {
        every { customerRepository.findById(1L) } returns makeCustomer()
        every { cartRepository.findByCustomerIdWithItems(1L) } returns null

        assertThrows<IllegalStateException> {
            service.placeOrder(PlaceOrderCommand(customerId = 1L, pickupPointId = 1L))
        }
    }

    @Test
    fun `placeOrder throws when cart is empty`() {
        every { customerRepository.findById(1L) } returns makeCustomer()
        every { cartRepository.findByCustomerIdWithItems(1L) } returns makeCart()

        assertThrows<IllegalStateException> {
            service.placeOrder(PlaceOrderCommand(customerId = 1L, pickupPointId = 1L))
        }
    }

    @Test
    fun `placeOrder throws when variant not found in catalog`() {
        val cartItem = makeCartItem(variantId = 5L)
        val cart = makeCart(items = listOf(cartItem))
        every { customerRepository.findById(1L) } returns makeCustomer()
        every { cartRepository.findByCustomerIdWithItems(1L) } returns cart
        every { catalogPort.getVariants(listOf(5L)) } returns emptyList()

        assertThrows<IllegalStateException> {
            service.placeOrder(PlaceOrderCommand(customerId = 1L, pickupPointId = 1L))
        }
    }

    @Test
    fun `placeOrder throws when insufficient stock`() {
        val cartItem = makeCartItem(variantId = 5L)
        val cart = makeCart(items = listOf(cartItem))
        every { customerRepository.findById(1L) } returns makeCustomer()
        every { cartRepository.findByCustomerIdWithItems(1L) } returns cart
        every { catalogPort.getVariants(listOf(5L)) } returns listOf(makeVariantInfo())
        every { warehousePort.checkStock(any()) } returns makeCheckStockResult(allAvailable = false)

        assertThrows<IllegalStateException> {
            service.placeOrder(PlaceOrderCommand(customerId = 1L, pickupPointId = 1L))
        }
    }

    @Test
    fun `placeOrder succeeds and writes outbox event`() {
        val cartItem = makeCartItem(variantId = 5L, qty = 2)
        val cart = makeCart(items = listOf(cartItem))
        val savedOrder = makeOrder()
        val finalOrder = makeOrder(items = listOf(
            OrderItem(id = 1L, orderId = 100L, variantId = 5L, batchId = 7L, beerId = null,
                quantity = 2, unitPrice = BigDecimal("9.99"), lineDiscount = BigDecimal.ZERO)
        ))

        every { customerRepository.findById(1L) } returns makeCustomer()
        every { cartRepository.findByCustomerIdWithItems(1L) } returns cart
        every { catalogPort.getVariants(listOf(5L)) } returns listOf(makeVariantInfo())
        every { warehousePort.checkStock(any()) } returns makeCheckStockResult()
        every { customerOrderRepository.save(any()) } returns savedOrder
        every { warehousePort.reserveStock(any(), any()) } returns makeReserveStockResult()
        every { orderStatusHistoryRepository.save(any()) } returns mockk()
        every { objectMapper.writeValueAsString(any()) } returns """{"orderId":100}"""
        every { outboxEventRepository.save(any()) } returns mockk()
        every { cartRepository.save(any()) } returns cart
        every { customerOrderRepository.findByIdWithItems(savedOrder.id) } returns finalOrder

        val result = service.placeOrder(PlaceOrderCommand(customerId = 1L, pickupPointId = 2L))

        assertEquals(100L, result.id)
        verify { outboxEventRepository.save(any()) }
        verify { cartRepository.save(match { it.items.isEmpty() }) }
    }

    @Test
    fun `advanceOrderStatus throws when order not found`() {
        every { customerOrderRepository.findByIdWithItems(99L) } returns null

        assertThrows<NoSuchElementException> {
            service.advanceOrderStatus(99L, null)
        }
    }

    @Test
    fun `advanceOrderStatus throws when order is terminal`() {
        val order = makeOrder().copy(status = OrderStatus.PICKED_UP)
        every { customerOrderRepository.findByIdWithItems(100L) } returns order

        assertThrows<IllegalStateException> {
            service.advanceOrderStatus(100L, null)
        }
    }

    @Test
    fun `advanceOrderStatus ASSEMBLING to READY_FOR_PICKUP sets timestamps`() {
        val order = makeOrder().copy(status = OrderStatus.ASSEMBLING)
        every { customerOrderRepository.findByIdWithItems(100L) } returns order
        every { customerOrderRepository.save(any()) } returns mockk(relaxed = true)
        every { orderStatusHistoryRepository.save(any()) } returns mockk()

        val result = service.advanceOrderStatus(100L, null)

        assertEquals(OrderStatus.READY_FOR_PICKUP, result.status)
        assertNotNull(result.readyForPickupAt)
        assertNotNull(result.pickupExpiresAt)
        verify(exactly = 0) { outboxEventRepository.save(any()) }
    }

    @Test
    fun `advanceOrderStatus READY_FOR_PICKUP to PICKED_UP writes outbox event`() {
        val order = makeOrder().copy(status = OrderStatus.READY_FOR_PICKUP)
        every { customerOrderRepository.findByIdWithItems(100L) } returns order
        every { customerOrderRepository.save(any()) } returns mockk(relaxed = true)
        every { orderStatusHistoryRepository.save(any()) } returns mockk()
        every { objectMapper.writeValueAsString(any()) } returns """{"orderId":100}"""
        every { outboxEventRepository.save(any()) } returns mockk()

        val result = service.advanceOrderStatus(100L, null)

        assertEquals(OrderStatus.PICKED_UP, result.status)
        assertNotNull(result.pickedUpAt)
        verify { outboxEventRepository.save(any()) }
    }

    @Test
    fun `cancelOrder throws when order not found`() {
        every { customerOrderRepository.findByIdWithItems(99L) } returns null

        assertThrows<NoSuchElementException> {
            service.cancelOrder(99L, null)
        }
    }

    @Test
    fun `cancelOrder throws when order is already terminal`() {
        val order = makeOrder().copy(status = OrderStatus.CANCELED)
        every { customerOrderRepository.findByIdWithItems(100L) } returns order

        assertThrows<IllegalStateException> {
            service.cancelOrder(100L, null)
        }
    }

    @Test
    fun `cancelOrder sets CANCELED status`() {
        val order = makeOrder().copy(status = OrderStatus.PAID)
        every { customerOrderRepository.findByIdWithItems(100L) } returns order
        every { customerOrderRepository.save(any()) } returns mockk(relaxed = true)
        every { orderStatusHistoryRepository.save(any()) } returns mockk()

        val result = service.cancelOrder(100L, employeeId = 5L)

        assertEquals(OrderStatus.CANCELED, result.status)
        verify(exactly = 0) { outboxEventRepository.save(any()) }
    }
}

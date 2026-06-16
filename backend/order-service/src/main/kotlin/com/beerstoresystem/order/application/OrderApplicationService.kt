package com.beerstoresystem.order.application

import tools.jackson.databind.ObjectMapper
import com.beerstoresystem.order.domain.command.PlaceOrderCommand
import com.beerstoresystem.order.domain.model.CustomerOrder
import com.beerstoresystem.order.domain.model.OrderOutboxEvent
import com.beerstoresystem.order.domain.model.OrderStatus
import com.beerstoresystem.order.domain.model.OrderStatusHistory
import com.beerstoresystem.order.domain.model.OrderItem
import com.beerstoresystem.order.domain.exception.BusinessRuleException
import com.beerstoresystem.order.domain.exception.EmptyCartException
import com.beerstoresystem.order.domain.exception.NotFoundException
import com.beerstoresystem.order.domain.exception.OrderInTerminalStateException
import com.beerstoresystem.order.domain.exception.OutOfStockException
import com.beerstoresystem.order.domain.port.CatalogPort
import com.beerstoresystem.order.domain.port.StockCheckRequest
import com.beerstoresystem.order.domain.port.WarehousePort
import com.beerstoresystem.order.domain.repository.CartRepository
import com.beerstoresystem.order.domain.repository.CustomerOrderRepository
import com.beerstoresystem.order.domain.repository.CustomerRepository
import com.beerstoresystem.order.domain.repository.OrderOutboxEventRepository
import com.beerstoresystem.order.domain.repository.OrderStatusHistoryRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Service
@Transactional(readOnly = true)
class OrderApplicationService(
    @Value("\${order.pickup.expiry-days:3}") private val pickupExpiryDays: Long,
    private val customerRepository: CustomerRepository,
    private val cartRepository: CartRepository,
    private val customerOrderRepository: CustomerOrderRepository,
    private val orderStatusHistoryRepository: OrderStatusHistoryRepository,
    private val outboxEventRepository: OrderOutboxEventRepository,
    private val objectMapper: ObjectMapper,
    private val catalogPort: CatalogPort,
    private val warehousePort: WarehousePort
) : OrderUseCase {

    private val log = LoggerFactory.getLogger(OrderApplicationService::class.java)

    override fun getOrder(id: Long): CustomerOrder =
        customerOrderRepository.findByIdWithItems(id)
            ?: throw NotFoundException("Order not found: $id")

    override fun getCustomerOrders(customerId: Long): List<CustomerOrder> {
        customerRepository.findById(customerId)
            ?: throw NotFoundException("Customer not found: $customerId")
        return customerOrderRepository.findAllByCustomerId(customerId)
    }

    @Transactional
    override fun placeOrder(command: PlaceOrderCommand): CustomerOrder {
        val customer = customerRepository.findById(command.customerId)
            ?: throw NotFoundException("Customer not found: ${command.customerId}")

        val cart = cartRepository.findByCustomerIdWithItems(command.customerId)
            ?: throw NotFoundException("Cart not found for customer: ${command.customerId}")

        if (cart.items.isEmpty()) {
            throw EmptyCartException("Cart is empty for customer: ${command.customerId}")
        }

        val variantIds = cart.items.map { it.variantId }
        val variants = runCatching {
            catalogPort.getVariants(variantIds)
        }.getOrElse { e ->
            throw IllegalStateException("Failed to fetch variants from catalog: ${e.message}", e)
        }

        val variantMap = variants.associateBy { it.id }
        for (item in cart.items) {
            if (!variantMap.containsKey(item.variantId)) {
                throw NotFoundException("Variant ${item.variantId} not found in catalog")
            }
        }

        val stockItems = cart.items.map { item ->
            StockCheckRequest(variantId = item.variantId, quantity = item.quantity)
        }

        val checkStockResult = runCatching {
            warehousePort.checkStock(stockItems)
        }.getOrElse { e ->
            throw IllegalStateException("Failed to check stock: ${e.message}", e)
        }

        if (!checkStockResult.allAvailable) {
            val unavailable = checkStockResult.availability
                .filter { !it.available }
                .joinToString { "variantId=${it.variantId} (available=${it.quantityOnHand})" }
            throw OutOfStockException("Insufficient stock: $unavailable")
        }

        var subtotal = BigDecimal.ZERO
        for (item in cart.items) {
            val variant = variantMap[item.variantId]!!
            subtotal = subtotal.add(
                BigDecimal.valueOf(variant.unitPrice).multiply(BigDecimal(item.quantity))
            )
        }
        val discountAmount = BigDecimal.ZERO
        val totalAmount = subtotal.subtract(discountAmount)

        val pickupCode = generatePickupCode()
        val now = OffsetDateTime.now()
        val newOrder = CustomerOrder(
            id = 0L,
            customerId = customer.id,
            pickupPointId = command.pickupPointId,
            status = OrderStatus.PENDING,
            subtotalAmount = subtotal,
            discountAmount = discountAmount,
            totalAmount = totalAmount,
            pickupCode = pickupCode,
            placedAt = now,
            readyForPickupAt = null,
            pickupExpiresAt = null,
            pickedUpAt = null,
            items = emptyList()
        )
        val savedOrder = customerOrderRepository.save(newOrder)

        val reserveResult = runCatching {
            warehousePort.reserveStock(savedOrder.id, stockItems)
        }.getOrElse { e ->
            throw IllegalStateException("Failed to reserve stock: ${e.message}", e)
        }

        if (!reserveResult.success) {
            throw OutOfStockException("Stock reservation failed for order ${savedOrder.id}")
        }

        val batchMap = reserveResult.reserved.associateBy { it.variantId }

        val orderItems = cart.items.map { cartItem ->
            val variant = variantMap[cartItem.variantId]!!
            val reservedBatch = batchMap[cartItem.variantId]
            OrderItem(
                id = 0L,
                orderId = savedOrder.id,
                variantId = cartItem.variantId,
                batchId = reservedBatch?.batchId,
                beerId = variantMap[cartItem.variantId]?.beerId,
                quantity = cartItem.quantity,
                unitPrice = BigDecimal.valueOf(variant.unitPrice),
                lineDiscount = BigDecimal.ZERO
            )
        }
        val orderWithItems = savedOrder.copy(items = orderItems)
        customerOrderRepository.save(orderWithItems)

        orderStatusHistoryRepository.save(
            OrderStatusHistory(
                id = 0L,
                orderId = savedOrder.id,
                changedByEmployeeId = null,
                fromStatus = null,
                toStatus = OrderStatus.PENDING,
                changedAt = OffsetDateTime.now()
            )
        )

        val itemsPayload = orderItems.map { item ->
            mapOf(
                "variantId" to item.variantId,
                "quantity" to item.quantity,
                "batchId" to item.batchId
            )
        }
        val placedPayload = objectMapper.writeValueAsString(
            mapOf("orderId" to savedOrder.id, "items" to itemsPayload)
        )
        outboxEventRepository.save(
            OrderOutboxEvent(
                id = UUID.randomUUID(),
                aggregateType = "CustomerOrder",
                aggregateId = savedOrder.id.toString(),
                eventType = "ORDER_PLACED",
                payload = placedPayload,
                createdAt = OffsetDateTime.now(),
                publishedAt = null
            )
        )

        cartRepository.save(cart.copy(items = emptyList(), updatedAt = OffsetDateTime.now()))

        log.info("Order {} placed for customer {}", savedOrder.id, command.customerId)
        return customerOrderRepository.findByIdWithItems(savedOrder.id)!!
    }

    @Transactional
    override fun advanceOrderStatus(orderId: Long, employeeId: Long?): CustomerOrder {
        val order = customerOrderRepository.findByIdWithItems(orderId)
            ?: throw NotFoundException("Order not found: $orderId")
        val nextStatus = NEXT_STATUS[order.status]
            ?: throw OrderInTerminalStateException("Order $orderId in terminal state ${order.status}, cannot advance")
        return applyStatusTransition(order, nextStatus, employeeId)
    }

    @Transactional
    override fun cancelOrder(orderId: Long, employeeId: Long?): CustomerOrder {
        val order = customerOrderRepository.findByIdWithItems(orderId)
            ?: throw NotFoundException("Order not found: $orderId")
        if (order.status in TERMINAL_STATUSES) {
            throw OrderInTerminalStateException("Order $orderId already in terminal state ${order.status}")
        }
        return applyStatusTransition(order, OrderStatus.CANCELED, employeeId)
    }

    private fun applyStatusTransition(order: CustomerOrder, newStatus: OrderStatus, employeeId: Long?): CustomerOrder {
        val now = OffsetDateTime.now()
        val updatedOrder = when (newStatus) {
            OrderStatus.READY_FOR_PICKUP -> order.copy(
                status = newStatus,
                readyForPickupAt = now,
                pickupExpiresAt = now.plusDays(pickupExpiryDays)
            )
            OrderStatus.PICKED_UP -> order.copy(status = newStatus, pickedUpAt = now)
            else -> order.copy(status = newStatus)
        }
        customerOrderRepository.save(updatedOrder)
        orderStatusHistoryRepository.save(
            OrderStatusHistory(
                id = 0L,
                orderId = order.id,
                changedByEmployeeId = employeeId,
                fromStatus = order.status,
                toStatus = newStatus,
                changedAt = now
            )
        )
        if (newStatus == OrderStatus.PICKED_UP) {
            val payload = objectMapper.writeValueAsString(
                mapOf(
                    "orderId" to order.id,
                    "customerId" to updatedOrder.customerId,
                    "pickedUpAt" to updatedOrder.pickedUpAt.toString()
                )
            )
            outboxEventRepository.save(
                OrderOutboxEvent(
                    id = UUID.randomUUID(),
                    aggregateType = "CustomerOrder",
                    aggregateId = order.id.toString(),
                    eventType = "ORDER_PICKED_UP",
                    payload = payload,
                    createdAt = now,
                    publishedAt = null
                )
            )
            log.info("Order {} picked up, outbox event written", order.id)
        }
        return updatedOrder
    }

    private fun generatePickupCode(): String =
        (1..PICKUP_CODE_LENGTH).map { PICKUP_CODE_CHARS.random() }.joinToString("")

    companion object {
        private const val PICKUP_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        private const val PICKUP_CODE_LENGTH = 10
        private val NEXT_STATUS = mapOf(
            OrderStatus.PENDING to OrderStatus.PAID,
            OrderStatus.PAID to OrderStatus.ASSEMBLING,
            OrderStatus.ASSEMBLING to OrderStatus.READY_FOR_PICKUP,
            OrderStatus.READY_FOR_PICKUP to OrderStatus.PICKED_UP
        )
        private val TERMINAL_STATUSES = setOf(OrderStatus.PICKED_UP, OrderStatus.CANCELED, OrderStatus.EXPIRED)
    }
}

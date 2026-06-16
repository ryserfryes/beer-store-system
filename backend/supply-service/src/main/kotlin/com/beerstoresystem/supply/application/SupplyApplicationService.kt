package com.beerstoresystem.supply.application

import tools.jackson.databind.ObjectMapper
import com.beerstoresystem.supply.domain.model.OutboxEvent
import com.beerstoresystem.supply.domain.model.PurchaseOrder
import com.beerstoresystem.supply.domain.model.PurchaseOrderItem
import com.beerstoresystem.supply.domain.model.PurchaseOrderStatus
import com.beerstoresystem.supply.domain.model.Supplier
import com.beerstoresystem.supply.domain.exception.BusinessRuleException
import com.beerstoresystem.supply.domain.exception.NotFoundException
import com.beerstoresystem.supply.domain.repository.OutboxEventRepository
import com.beerstoresystem.supply.domain.repository.PurchaseOrderItemRepository
import com.beerstoresystem.supply.domain.repository.PurchaseOrderRepository
import com.beerstoresystem.supply.domain.repository.SupplierRepository
import com.beerstoresystem.supply.integration.rest.dto.AddItemRequest
import com.beerstoresystem.supply.integration.rest.dto.CreatePurchaseOrderRequest
import com.beerstoresystem.supply.integration.rest.dto.CreateSupplierRequest
import com.beerstoresystem.supply.integration.rest.dto.PurchaseOrderDetailDto
import com.beerstoresystem.supply.integration.rest.dto.PurchaseOrderItemDto
import com.beerstoresystem.supply.integration.rest.dto.PurchaseOrderSummaryDto
import com.beerstoresystem.supply.integration.rest.dto.SupplierDto
import com.beerstoresystem.supply.integration.rest.dto.UpdateStatusRequest
import com.beerstoresystem.supply.integration.rest.mapper.toDetailDto
import com.beerstoresystem.supply.integration.rest.mapper.toDto
import com.beerstoresystem.supply.integration.rest.mapper.toItemDto
import com.beerstoresystem.supply.integration.rest.mapper.toSummaryDto
import com.beerstoresystem.supply.messaging.OutboxItemPayload
import com.beerstoresystem.supply.messaging.OutboxPayload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

private const val LOT_CODE_FORMAT = "PO-%d-%d"

@Service
@Transactional(readOnly = true)
class SupplyApplicationService(
    private val supplierRepository: SupplierRepository,
    private val purchaseOrderRepository: PurchaseOrderRepository,
    private val purchaseOrderItemRepository: PurchaseOrderItemRepository,
    private val outboxEventRepository: OutboxEventRepository,
    private val objectMapper: ObjectMapper
) : SupplyUseCase {

    override fun getAllSuppliers(): List<SupplierDto> =
        supplierRepository.findAll().map { it.toDto() }

    @Transactional
    override fun createSupplier(request: CreateSupplierRequest): SupplierDto {
        val supplier = Supplier(
            id = 0L,
            name = request.name,
            countryId = request.countryId,
            contactEmail = request.contactEmail,
            contactPhone = request.contactPhone,
            createdAt = OffsetDateTime.now()
        )
        return supplierRepository.save(supplier).toDto()
    }

    override fun getAllPurchaseOrders(): List<PurchaseOrderSummaryDto> {
        val orders = purchaseOrderRepository.findAll()
        val supplierIds = orders.map { it.supplierId }.toSet()
        val suppliersById = supplierRepository.findAllByIds(supplierIds).associateBy { it.id }
        return orders.map { order ->
            val supplierName = suppliersById[order.supplierId]?.name ?: ""
            order.toSummaryDto(supplierName)
        }
    }

    @Transactional
    override fun createPurchaseOrder(request: CreatePurchaseOrderRequest): PurchaseOrderSummaryDto {
        val supplier = supplierRepository.findById(request.supplierId)
            ?: throw NotFoundException("Supplier not found: ${request.supplierId}")
        val order = PurchaseOrder(
            id = 0L,
            supplierId = request.supplierId,
            warehouseId = request.warehouseId,
            status = PurchaseOrderStatus.DRAFTING,
            orderedAt = OffsetDateTime.now(),
            expectedAt = request.expectedAt,
            receivedAt = null
        )
        return purchaseOrderRepository.save(order).toSummaryDto(supplier.name)
    }

    override fun getPurchaseOrderById(id: Long): PurchaseOrderDetailDto {
        val order = purchaseOrderRepository.findById(id)
            ?: throw NotFoundException("Purchase order not found: $id")
        val supplier = supplierRepository.findById(order.supplierId)
        val items = purchaseOrderItemRepository.findAllByPurchaseOrderId(id)
        return order.toDetailDto(supplier?.name ?: "", items)
    }

    @Transactional
    override fun addItem(orderId: Long, request: AddItemRequest): PurchaseOrderItemDto {
        purchaseOrderRepository.findById(orderId)
            ?: throw NotFoundException("Purchase order not found: $orderId")
        val item = PurchaseOrderItem(
            id = 0L,
            purchaseOrderId = orderId,
            variantId = request.variantId,
            quantity = request.quantity,
            unitCost = request.unitCost
        )
        return purchaseOrderItemRepository.save(item).toItemDto()
    }

    @Transactional
    override fun updateStatus(orderId: Long, request: UpdateStatusRequest): PurchaseOrderSummaryDto {
        val order = purchaseOrderRepository.findById(orderId)
            ?: throw NotFoundException("Purchase order not found: $orderId")
        val newStatus = try {
            PurchaseOrderStatus.valueOf(request.status.uppercase())
        } catch (e: IllegalArgumentException) {
            throw BusinessRuleException("Unknown status: ${request.status}")
        }

        val updatedOrder = if (newStatus == PurchaseOrderStatus.RECEIVED) {
            val withStatus = order.copy(status = newStatus, receivedAt = OffsetDateTime.now())
            writeOutboxEvent(withStatus)
            withStatus
        } else {
            order.copy(status = newStatus)
        }

        val saved = purchaseOrderRepository.save(updatedOrder)
        val supplier = supplierRepository.findById(order.supplierId)
        return saved.toSummaryDto(supplier?.name ?: "")
    }

    private fun writeOutboxEvent(order: PurchaseOrder) {
        val items = purchaseOrderItemRepository.findAllByPurchaseOrderId(order.id)
        val outboxItems = items.map { item ->
            OutboxItemPayload(
                variantId = item.variantId,
                purchaseOrderItemId = item.id,
                quantity = item.quantity,
                lotCode = LOT_CODE_FORMAT.format(order.id, item.id),
                wholesaleCost = item.unitCost
            )
        }
        val payload = OutboxPayload(
            purchaseOrderId = order.id,
            warehouseId = order.warehouseId,
            items = outboxItems
        )
        val outboxEvent = OutboxEvent(
            id = UUID.randomUUID(),
            aggregateType = "PurchaseOrder",
            aggregateId = order.id.toString(),
            eventType = "supply.received",
            payload = objectMapper.writeValueAsString(payload),
            createdAt = OffsetDateTime.now(),
            publishedAt = null
        )
        outboxEventRepository.save(outboxEvent)
    }
}

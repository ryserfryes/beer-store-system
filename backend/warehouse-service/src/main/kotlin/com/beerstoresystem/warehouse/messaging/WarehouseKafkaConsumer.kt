package com.beerstoresystem.warehouse.messaging

import tools.jackson.databind.ObjectMapper
import com.beerstoresystem.warehouse.application.StockItem
import com.beerstoresystem.warehouse.application.SupplyItem
import com.beerstoresystem.warehouse.application.WarehouseUseCase
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

// ---------- Payload DTOs ----------

data class OrderPlacedItem(val variantId: Long, val quantity: Int, val batchId: Long? = null)
data class OrderPlacedEvent(val orderId: Long, val items: List<OrderPlacedItem>)
data class SupplyReceivedItem(
    val variantId: Long, val purchaseOrderItemId: Long?,
    val quantity: Int, val lotCode: String?, val wholesaleCost: Double?
)
data class SupplyReceivedEvent(val purchaseOrderId: Long, val warehouseId: Long, val items: List<SupplyReceivedItem>)

// ---------- Consumer ----------

@Component
class WarehouseKafkaConsumer(
    private val objectMapper: ObjectMapper,
    private val warehouseUseCase: WarehouseUseCase
) {

    private val log = LoggerFactory.getLogger(WarehouseKafkaConsumer::class.java)

    @KafkaListener(topics = ["orders.placed"], groupId = "warehouse-service")
    fun handleOrderPlaced(payload: String) {
        log.info("Received orders.placed event: {}", payload)
        try {
            val event: OrderPlacedEvent = objectMapper.readValue(payload, OrderPlacedEvent::class.java)
            for (item in event.items) {
                if (item.batchId != null) {
                    warehouseUseCase.deductStockByBatch(item.batchId, item.quantity)
                } else {
                    warehouseUseCase.deductStockFifo(item.variantId, item.quantity)
                }
            }
        } catch (ex: Exception) {
            log.error("Failed to process orders.placed event: {}", payload, ex)
        }
    }

    @KafkaListener(topics = ["supply.received"], groupId = "warehouse-service")
    fun handleSupplyReceived(payload: String) {
        log.info("Received supply.received event: {}", payload)
        try {
            val event: SupplyReceivedEvent = objectMapper.readValue(payload, SupplyReceivedEvent::class.java)
            val supplyItems = event.items.map { item ->
                SupplyItem(
                    variantId = item.variantId,
                    purchaseOrderItemId = item.purchaseOrderItemId,
                    quantity = item.quantity,
                    lotCode = item.lotCode,
                    wholesaleCost = item.wholesaleCost
                )
            }
            warehouseUseCase.receiveSupply(event.purchaseOrderId, event.warehouseId, supplyItems)
        } catch (ex: Exception) {
            log.error("Failed to process supply.received event: {}", payload, ex)
        }
    }
}

package com.beerstoresystem.order.infrastructure.grpc

import net.devh.boot.grpc.client.inject.GrpcClient
import com.beerstoresystem.order.domain.port.CheckStockResult
import com.beerstoresystem.order.domain.port.ReserveStockResult
import com.beerstoresystem.order.domain.port.ReservedBatchResult
import com.beerstoresystem.order.domain.port.StockAvailabilityResult
import com.beerstoresystem.order.domain.port.StockCheckRequest
import com.beerstoresystem.order.domain.port.WarehousePort
import com.beerstoresystem.proto.warehouse.CheckStockRequest
import com.beerstoresystem.proto.warehouse.ReserveStockRequest
import com.beerstoresystem.proto.warehouse.StockCheckItem
import com.beerstoresystem.proto.warehouse.WarehouseGrpcServiceGrpc
import org.springframework.stereotype.Component

@Component
class WarehouseGrpcAdapter : WarehousePort {

    @GrpcClient("warehouse-service")
    private lateinit var stub: WarehouseGrpcServiceGrpc.WarehouseGrpcServiceBlockingStub

    override fun checkStock(items: List<StockCheckRequest>): CheckStockResult {
        val protoItems = items.map { item ->
            StockCheckItem.newBuilder()
                .setVariantId(item.variantId)
                .setQuantity(item.quantity)
                .build()
        }
        val response = stub.checkStock(CheckStockRequest.newBuilder().addAllItems(protoItems).build())
        return CheckStockResult(
            allAvailable = response.allAvailable,
            availability = response.availabilityList.map { a ->
                StockAvailabilityResult(
                    variantId = a.variantId,
                    quantityOnHand = a.quantityOnHand,
                    available = a.available
                )
            }
        )
    }

    override fun reserveStock(orderId: Long, items: List<StockCheckRequest>): ReserveStockResult {
        val protoItems = items.map { item ->
            StockCheckItem.newBuilder()
                .setVariantId(item.variantId)
                .setQuantity(item.quantity)
                .build()
        }
        val response = stub.reserveStock(
            ReserveStockRequest.newBuilder()
                .setOrderId(orderId)
                .addAllItems(protoItems)
                .build()
        )
        return ReserveStockResult(
            success = response.success,
            reserved = response.reservedList.map { r ->
                ReservedBatchResult(
                    variantId = r.variantId,
                    batchId = r.batchId,
                    quantity = r.quantity
                )
            }
        )
    }
}

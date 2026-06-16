package com.beerstoresystem.warehouse.grpc

import net.devh.boot.grpc.server.service.GrpcService
import com.beerstoresystem.proto.warehouse.CheckStockRequest
import com.beerstoresystem.proto.warehouse.CheckStockResponse
import com.beerstoresystem.proto.warehouse.GetPickupPointsRequest
import com.beerstoresystem.proto.warehouse.GetPickupPointsResponse
import com.beerstoresystem.proto.warehouse.PickupPointProto
import com.beerstoresystem.proto.warehouse.ReservedBatch
import com.beerstoresystem.proto.warehouse.ReserveStockRequest
import com.beerstoresystem.proto.warehouse.ReserveStockResponse
import com.beerstoresystem.proto.warehouse.StockAvailability
import com.beerstoresystem.proto.warehouse.WarehouseGrpcServiceGrpcKt
import com.beerstoresystem.warehouse.application.StockItem
import com.beerstoresystem.warehouse.application.WarehouseUseCase

@GrpcService
class WarehouseGrpcServiceImpl(
    private val warehouseUseCase: WarehouseUseCase
) : WarehouseGrpcServiceGrpcKt.WarehouseGrpcServiceCoroutineImplBase() {

    override suspend fun getPickupPoints(request: GetPickupPointsRequest): GetPickupPointsResponse {
        val points = warehouseUseCase.getPickupPoints(request.activeOnly)
        val protoPoints = points.map { point ->
            PickupPointProto.newBuilder()
                .setId(point.id)
                .setName(point.name)
                .setCity(point.city)
                .setAddressLine(point.addressLine)
                .setWorkingHours(point.workingHours ?: "")
                .setIsActive(point.isActive)
                .build()
        }
        return GetPickupPointsResponse.newBuilder()
            .addAllPickupPoints(protoPoints)
            .build()
    }

    override suspend fun checkStock(request: CheckStockRequest): CheckStockResponse {
        val items = request.itemsList.map { StockItem(it.variantId, it.quantity) }
        val results = warehouseUseCase.checkStock(items)

        val availabilities = results.map { result ->
            StockAvailability.newBuilder()
                .setVariantId(result.variantId)
                .setQuantityOnHand(result.quantityOnHand)
                .setAvailable(result.available)
                .build()
        }

        val allAvailable = results.all { it.available }

        return CheckStockResponse.newBuilder()
            .setAllAvailable(allAvailable)
            .addAllAvailability(availabilities)
            .build()
    }

    override suspend fun reserveStock(request: ReserveStockRequest): ReserveStockResponse {
        val items = request.itemsList.map { StockItem(it.variantId, it.quantity) }
        val (success, reservedBatches) = warehouseUseCase.reserveStock(request.orderId, items)

        val protoReserved = reservedBatches.map { r ->
            ReservedBatch.newBuilder()
                .setVariantId(r.variantId)
                .setBatchId(r.batchId)
                .setQuantity(r.quantity)
                .build()
        }

        return ReserveStockResponse.newBuilder()
            .setSuccess(success)
            .addAllReserved(protoReserved)
            .build()
    }
}

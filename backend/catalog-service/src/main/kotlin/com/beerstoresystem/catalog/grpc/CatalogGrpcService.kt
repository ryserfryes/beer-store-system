package com.beerstoresystem.catalog.grpc

import net.devh.boot.grpc.server.service.GrpcService
import com.beerstoresystem.catalog.application.CatalogUseCase
import com.beerstoresystem.proto.catalog.CatalogGrpcServiceGrpcKt
import com.beerstoresystem.proto.catalog.GetVariantsRequest
import com.beerstoresystem.proto.catalog.GetVariantsResponse
import com.beerstoresystem.proto.catalog.VariantProto

@GrpcService
class CatalogGrpcServiceImpl(
    private val catalogUseCase: CatalogUseCase
) : CatalogGrpcServiceGrpcKt.CatalogGrpcServiceCoroutineImplBase() {

    override suspend fun getVariants(request: GetVariantsRequest): GetVariantsResponse {
        val variants = catalogUseCase.getVariantsByIds(request.idsList)

        val protoVariants = variants.map { variant ->
            VariantProto.newBuilder()
                .setId(variant.id)
                .setSku(variant.sku)
                .setBeerName(variant.beer?.name ?: "")
                .setBreweryName(variant.beer?.brewery?.name ?: "")
                .setUnitPrice(variant.unitPrice.toDouble())
                .setVolumeMl(variant.volumeMl ?: 0)
                .setIsActive(variant.isActive)
                .setBeerId(variant.beer?.id ?: 0L)
                .build()
        }

        return GetVariantsResponse.newBuilder()
            .addAllVariants(protoVariants)
            .build()
    }
}

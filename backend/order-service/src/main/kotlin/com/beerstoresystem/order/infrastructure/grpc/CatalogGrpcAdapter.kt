package com.beerstoresystem.order.infrastructure.grpc

import net.devh.boot.grpc.client.inject.GrpcClient
import com.beerstoresystem.order.domain.port.CatalogPort
import com.beerstoresystem.order.domain.port.VariantInfo
import com.beerstoresystem.proto.catalog.CatalogGrpcServiceGrpc
import com.beerstoresystem.proto.catalog.GetVariantsRequest
import org.springframework.stereotype.Component

@Component
class CatalogGrpcAdapter : CatalogPort {

    @GrpcClient("catalog-service")
    private lateinit var stub: CatalogGrpcServiceGrpc.CatalogGrpcServiceBlockingStub

    override fun getVariants(ids: List<Long>): List<VariantInfo> {
        val request = GetVariantsRequest.newBuilder().addAllIds(ids).build()
        return stub.getVariants(request).variantsList.map { proto ->
            VariantInfo(id = proto.id, sku = proto.sku, unitPrice = proto.unitPrice, beerId = proto.beerId.takeIf { it != 0L })
        }
    }
}

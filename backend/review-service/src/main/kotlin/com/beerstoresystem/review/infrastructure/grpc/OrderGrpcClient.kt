package com.beerstoresystem.review.infrastructure.grpc

import net.devh.boot.grpc.client.inject.GrpcClient
import com.beerstoresystem.proto.order.HasCustomerPurchasedRequest
import com.beerstoresystem.proto.order.OrderGrpcServiceGrpc
import org.springframework.stereotype.Component

@Component
class OrderGrpcClient {

    @GrpcClient("order-service")
    private lateinit var stub: OrderGrpcServiceGrpc.OrderGrpcServiceBlockingStub

    fun hasCustomerPurchased(customerId: Long, beerId: Long): Boolean {
        val request = HasCustomerPurchasedRequest.newBuilder()
            .setCustomerId(customerId)
            .setBeerId(beerId)
            .build()
        return stub.hasCustomerPurchased(request).hasPurchased
    }
}

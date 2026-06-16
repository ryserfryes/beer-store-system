package com.beerstoresystem.order.grpc

import net.devh.boot.grpc.server.service.GrpcService
import com.beerstoresystem.order.domain.model.OrderStatus
import com.beerstoresystem.order.domain.repository.CustomerOrderRepository
import com.beerstoresystem.proto.order.HasCustomerPurchasedRequest
import com.beerstoresystem.proto.order.HasCustomerPurchasedResponse
import com.beerstoresystem.proto.order.OrderGrpcServiceGrpcKt
import org.slf4j.LoggerFactory

@GrpcService
class OrderGrpcServiceImpl(
    private val customerOrderRepository: CustomerOrderRepository
) : OrderGrpcServiceGrpcKt.OrderGrpcServiceCoroutineImplBase() {

    private val log = LoggerFactory.getLogger(OrderGrpcServiceImpl::class.java)

    override suspend fun hasCustomerPurchased(
        request: HasCustomerPurchasedRequest
    ): HasCustomerPurchasedResponse {
        val customerId = request.customerId
        val beerId = request.beerId

        log.debug("HasCustomerPurchased: customerId={}, beerId={}", customerId, beerId)

        val hasPurchased = customerOrderRepository.existsByCustomerIdAndStatusAndItemBeerId(
            customerId = customerId,
            status = OrderStatus.PICKED_UP,
            beerId = beerId
        )

        val orderId: Long = if (hasPurchased) {
            customerOrderRepository.findFirstOrderIdByCustomerAndStatusAndBeerId(
                customerId = customerId,
                status = OrderStatus.PICKED_UP,
                beerId = beerId
            ).firstOrNull() ?: 0L
        } else {
            0L
        }

        return HasCustomerPurchasedResponse.newBuilder()
            .setHasPurchased(hasPurchased)
            .setOrderId(orderId)
            .build()
    }
}

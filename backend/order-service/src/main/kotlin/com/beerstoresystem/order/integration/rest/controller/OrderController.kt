package com.beerstoresystem.order.integration.rest.controller

import com.beerstoresystem.order.application.OrderUseCase
import com.beerstoresystem.order.domain.command.PlaceOrderCommand
import com.beerstoresystem.order.integration.rest.dto.OrderActionRequest
import com.beerstoresystem.order.integration.rest.dto.OrderDto
import com.beerstoresystem.order.integration.rest.dto.PlaceOrderRequest
import com.beerstoresystem.order.integration.rest.mapper.toDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderUseCase: OrderUseCase
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun placeOrder(@RequestBody body: PlaceOrderRequest): OrderDto =
        orderUseCase.placeOrder(PlaceOrderCommand(customerId = body.customerId, pickupPointId = body.pickupPointId)).toDto()

    @GetMapping("/{id}")
    fun getOrder(@PathVariable id: Long): OrderDto =
        orderUseCase.getOrder(id).toDto()

    @PostMapping("/{id}/advance")
    fun advanceStatus(
        @PathVariable id: Long,
        @RequestBody(required = false) body: OrderActionRequest?
    ): OrderDto = orderUseCase.advanceOrderStatus(id, body?.employeeId).toDto()

    @PostMapping("/{id}/cancel")
    fun cancelOrder(
        @PathVariable id: Long,
        @RequestBody(required = false) body: OrderActionRequest?
    ): OrderDto = orderUseCase.cancelOrder(id, body?.employeeId).toDto()
}
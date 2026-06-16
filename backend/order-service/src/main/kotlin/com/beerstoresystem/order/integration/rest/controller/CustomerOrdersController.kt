package com.beerstoresystem.order.integration.rest.controller

import com.beerstoresystem.order.application.OrderUseCase
import com.beerstoresystem.order.integration.rest.dto.OrderDto
import com.beerstoresystem.order.integration.rest.mapper.toDto
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/customers/{customerId}/orders")
class CustomerOrdersController(
    private val orderUseCase: OrderUseCase
) {

    @GetMapping
    fun getCustomerOrders(@PathVariable customerId: Long): List<OrderDto> =
        orderUseCase.getCustomerOrders(customerId).map { it.toDto() }
}

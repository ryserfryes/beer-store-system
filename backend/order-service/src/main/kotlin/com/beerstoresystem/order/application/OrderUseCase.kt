package com.beerstoresystem.order.application

import com.beerstoresystem.order.domain.command.PlaceOrderCommand
import com.beerstoresystem.order.domain.model.CustomerOrder

interface OrderUseCase {
    fun getOrder(id: Long): CustomerOrder
    fun getCustomerOrders(customerId: Long): List<CustomerOrder>
    fun placeOrder(command: PlaceOrderCommand): CustomerOrder
    fun advanceOrderStatus(orderId: Long, employeeId: Long?): CustomerOrder
    fun cancelOrder(orderId: Long, employeeId: Long?): CustomerOrder
}

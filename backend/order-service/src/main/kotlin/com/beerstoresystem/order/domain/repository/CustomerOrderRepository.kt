package com.beerstoresystem.order.domain.repository

import com.beerstoresystem.order.domain.model.CustomerOrder
import com.beerstoresystem.order.domain.model.OrderStatus

interface CustomerOrderRepository {
    fun findByIdWithItems(id: Long): CustomerOrder?
    fun findAllByCustomerId(customerId: Long): List<CustomerOrder>
    fun save(order: CustomerOrder): CustomerOrder
    fun existsByCustomerIdAndStatusAndItemBeerId(customerId: Long, status: OrderStatus, beerId: Long): Boolean
    fun findFirstOrderIdByCustomerAndStatusAndBeerId(customerId: Long, status: OrderStatus, beerId: Long): List<Long>
}

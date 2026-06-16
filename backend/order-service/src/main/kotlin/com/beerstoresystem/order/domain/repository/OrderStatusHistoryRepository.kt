package com.beerstoresystem.order.domain.repository

import com.beerstoresystem.order.domain.model.OrderStatusHistory

interface OrderStatusHistoryRepository {
    fun save(history: OrderStatusHistory): OrderStatusHistory
}

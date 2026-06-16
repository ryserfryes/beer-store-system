package com.beerstoresystem.order.domain.model

enum class OrderStatus {
    PENDING,
    PAID,
    ASSEMBLING,
    READY_FOR_PICKUP,
    PICKED_UP,
    CANCELED,
    EXPIRED
}

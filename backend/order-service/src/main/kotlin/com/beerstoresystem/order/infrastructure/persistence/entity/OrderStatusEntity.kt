package com.beerstoresystem.order.infrastructure.persistence.entity

enum class OrderStatusEntity {
    PENDING,
    PAID,
    ASSEMBLING,
    READY_FOR_PICKUP,
    PICKED_UP,
    CANCELED,
    EXPIRED
}

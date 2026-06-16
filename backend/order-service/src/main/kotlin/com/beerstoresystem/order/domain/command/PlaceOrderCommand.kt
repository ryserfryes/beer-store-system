package com.beerstoresystem.order.domain.command

data class PlaceOrderCommand(
    val customerId: Long,
    val pickupPointId: Long
)

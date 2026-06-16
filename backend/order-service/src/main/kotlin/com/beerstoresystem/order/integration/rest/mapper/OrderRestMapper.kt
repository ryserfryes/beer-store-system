package com.beerstoresystem.order.integration.rest.mapper

import com.beerstoresystem.order.domain.model.CustomerOrder
import com.beerstoresystem.order.domain.model.OrderItem
import com.beerstoresystem.order.integration.rest.dto.OrderDto
import com.beerstoresystem.order.integration.rest.dto.OrderItemDto

fun OrderItem.toDto(): OrderItemDto = OrderItemDto(
    id = id,
    variantId = variantId,
    batchId = batchId,
    beerId = beerId,
    quantity = quantity,
    unitPrice = unitPrice,
    lineDiscount = lineDiscount
)

fun CustomerOrder.toDto(): OrderDto = OrderDto(
    id = id,
    customerId = customerId,
    pickupPointId = pickupPointId,
    status = status.name.lowercase(),
    subtotalAmount = subtotalAmount,
    discountAmount = discountAmount,
    totalAmount = totalAmount,
    pickupCode = pickupCode,
    placedAt = placedAt,
    readyForPickupAt = readyForPickupAt,
    pickupExpiresAt = pickupExpiresAt,
    pickedUpAt = pickedUpAt,
    items = items.map { it.toDto() }
)

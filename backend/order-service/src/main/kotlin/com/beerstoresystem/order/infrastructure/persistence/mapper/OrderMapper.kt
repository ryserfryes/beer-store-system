package com.beerstoresystem.order.infrastructure.persistence.mapper

import com.beerstoresystem.order.domain.model.CustomerOrder
import com.beerstoresystem.order.domain.model.OrderItem
import com.beerstoresystem.order.domain.model.OrderStatus
import com.beerstoresystem.order.domain.model.OrderStatusHistory
import com.beerstoresystem.order.domain.model.OrderOutboxEvent
import com.beerstoresystem.order.infrastructure.persistence.entity.CustomerOrderEntity
import com.beerstoresystem.order.infrastructure.persistence.entity.OrderItemEntity
import com.beerstoresystem.order.infrastructure.persistence.entity.OrderStatusEntity
import com.beerstoresystem.order.infrastructure.persistence.entity.OrderStatusHistoryEntity
import com.beerstoresystem.order.infrastructure.persistence.entity.OrderOutboxEventEntity

fun OrderStatusEntity.toDomain(): OrderStatus = OrderStatus.valueOf(name)
fun OrderStatus.toEntity(): OrderStatusEntity = OrderStatusEntity.valueOf(name)

fun OrderItemEntity.toDomain(): OrderItem = OrderItem(
    id = id,
    orderId = order?.id ?: 0L,
    variantId = variantId,
    batchId = batchId,
    beerId = beerId,
    quantity = quantity,
    unitPrice = unitPrice,
    lineDiscount = lineDiscount
)

fun CustomerOrderEntity.toDomain(): CustomerOrder = CustomerOrder(
    id = id,
    customerId = customerId,
    pickupPointId = pickupPointId,
    status = status.toDomain(),
    subtotalAmount = subtotalAmount,
    discountAmount = discountAmount,
    totalAmount = totalAmount,
    pickupCode = pickupCode,
    placedAt = placedAt,
    readyForPickupAt = readyForPickupAt,
    pickupExpiresAt = pickupExpiresAt,
    pickedUpAt = pickedUpAt,
    items = items.map { it.toDomain() }
)

fun CustomerOrder.toEntity(): CustomerOrderEntity = CustomerOrderEntity().also { e ->
    if (id != 0L) e.id = id
    e.customerId = customerId
    e.pickupPointId = pickupPointId
    e.status = status.toEntity()
    e.subtotalAmount = subtotalAmount
    e.discountAmount = discountAmount
    e.totalAmount = totalAmount
    e.pickupCode = pickupCode
    e.placedAt = placedAt
    e.readyForPickupAt = readyForPickupAt
    e.pickupExpiresAt = pickupExpiresAt
    e.pickedUpAt = pickedUpAt
}

fun OrderItem.toEntity(orderEntity: CustomerOrderEntity): OrderItemEntity = OrderItemEntity().also { e ->
    if (id != 0L) e.id = id
    e.order = orderEntity
    e.variantId = variantId
    e.batchId = batchId
    e.beerId = beerId
    e.quantity = quantity
    e.unitPrice = unitPrice
    e.lineDiscount = lineDiscount
}

fun OrderStatusHistoryEntity.toDomain(): OrderStatusHistory = OrderStatusHistory(
    id = id,
    orderId = orderId,
    changedByEmployeeId = changedByEmployeeId,
    fromStatus = fromStatus?.toDomain(),
    toStatus = toStatus.toDomain(),
    changedAt = changedAt
)

fun OrderStatusHistory.toEntity(): OrderStatusHistoryEntity = OrderStatusHistoryEntity().also { e ->
    if (id != 0L) e.id = id
    e.orderId = orderId
    e.changedByEmployeeId = changedByEmployeeId
    e.fromStatus = fromStatus?.toEntity()
    e.toStatus = toStatus.toEntity()
    e.changedAt = changedAt
}

fun OrderOutboxEventEntity.toDomain(): OrderOutboxEvent = OrderOutboxEvent(
    id = id,
    aggregateType = aggregateType,
    aggregateId = aggregateId,
    eventType = eventType,
    payload = payload,
    createdAt = createdAt,
    publishedAt = publishedAt
)

fun OrderOutboxEvent.toEntity(): OrderOutboxEventEntity = OrderOutboxEventEntity().also { e ->
    e.id = id
    e.aggregateType = aggregateType
    e.aggregateId = aggregateId
    e.eventType = eventType
    e.payload = payload
    e.createdAt = createdAt
    e.publishedAt = publishedAt
}

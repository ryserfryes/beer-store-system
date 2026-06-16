package com.beerstoresystem.supply.infrastructure.persistence.mapper

import com.beerstoresystem.supply.domain.model.OutboxEvent
import com.beerstoresystem.supply.infrastructure.persistence.entity.OutboxEventEntity

fun OutboxEventEntity.toDomain() = OutboxEvent(
    id = id,
    aggregateType = aggregateType,
    aggregateId = aggregateId,
    eventType = eventType,
    payload = payload,
    createdAt = createdAt,
    publishedAt = publishedAt
)

fun OutboxEvent.toEntity(): OutboxEventEntity = OutboxEventEntity().apply {
    id = this@toEntity.id
    aggregateType = this@toEntity.aggregateType
    aggregateId = this@toEntity.aggregateId
    eventType = this@toEntity.eventType
    payload = this@toEntity.payload
    createdAt = this@toEntity.createdAt
    publishedAt = this@toEntity.publishedAt
}

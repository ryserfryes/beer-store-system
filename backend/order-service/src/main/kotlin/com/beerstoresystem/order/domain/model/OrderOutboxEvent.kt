package com.beerstoresystem.order.domain.model

import java.time.OffsetDateTime
import java.util.UUID

data class OrderOutboxEvent(
    val id: UUID,
    val aggregateType: String,
    val aggregateId: String,
    val eventType: String,
    val payload: String,
    val createdAt: OffsetDateTime,
    val publishedAt: OffsetDateTime?
)

package com.beerstoresystem.order.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "order_outbox_events")
class OrderOutboxEventEntity {
    @Id
    var id: UUID = UUID.randomUUID()

    @Column(name = "aggregate_type", nullable = false, length = 100)
    var aggregateType: String = ""

    @Column(name = "aggregate_id", nullable = false, length = 100)
    var aggregateId: String = ""

    @Column(name = "event_type", nullable = false, length = 100)
    var eventType: String = ""

    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    var payload: String = ""

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "published_at")
    var publishedAt: OffsetDateTime? = null
}

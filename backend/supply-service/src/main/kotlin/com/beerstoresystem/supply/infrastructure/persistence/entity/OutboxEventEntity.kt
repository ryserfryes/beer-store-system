package com.beerstoresystem.supply.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "supply_outbox_events")
class OutboxEventEntity {
    @Id
    var id: UUID = UUID.randomUUID()

    @Column(name = "aggregate_type", nullable = false)
    var aggregateType: String = ""

    @Column(name = "aggregate_id", nullable = false)
    var aggregateId: String = ""

    @Column(name = "event_type", nullable = false)
    var eventType: String = ""

    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    var payload: String = ""

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "published_at")
    var publishedAt: OffsetDateTime? = null
}

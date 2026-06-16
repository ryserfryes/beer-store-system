package com.beerstoresystem.supply.domain.repository

import com.beerstoresystem.supply.domain.model.OutboxEvent

interface OutboxEventRepository {
    fun findTop50PendingOrderedByCreatedAt(): List<OutboxEvent>
    fun save(event: OutboxEvent): OutboxEvent
}

package com.beerstoresystem.supply.infrastructure.persistence.repository

import com.beerstoresystem.supply.domain.model.OutboxEvent
import com.beerstoresystem.supply.domain.repository.OutboxEventRepository
import com.beerstoresystem.supply.infrastructure.persistence.jpa.OutboxEventJpaRepository
import com.beerstoresystem.supply.infrastructure.persistence.mapper.toDomain
import com.beerstoresystem.supply.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Component

@Component
class OutboxEventRepositoryAdapter(
    private val jpaRepository: OutboxEventJpaRepository
) : OutboxEventRepository {

    override fun findTop50PendingOrderedByCreatedAt(): List<OutboxEvent> =
        jpaRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc().map { it.toDomain() }

    override fun save(event: OutboxEvent): OutboxEvent =
        jpaRepository.save(event.toEntity()).toDomain()
}

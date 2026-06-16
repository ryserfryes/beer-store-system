package com.beerstoresystem.supply.infrastructure.persistence.jpa

import com.beerstoresystem.supply.infrastructure.persistence.entity.OutboxEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OutboxEventJpaRepository : JpaRepository<OutboxEventEntity, UUID> {
    fun findTop50ByPublishedAtIsNullOrderByCreatedAtAsc(): List<OutboxEventEntity>
}

package com.beerstoresystem.order.infrastructure.persistence.jpa

import com.beerstoresystem.order.infrastructure.persistence.entity.OrderOutboxEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface OrderOutboxEventJpaRepository : JpaRepository<OrderOutboxEventEntity, UUID> {

    @Query("SELECT e FROM OrderOutboxEventEntity e WHERE e.publishedAt IS NULL ORDER BY e.createdAt ASC")
    fun findUnpublished(): List<OrderOutboxEventEntity>
}

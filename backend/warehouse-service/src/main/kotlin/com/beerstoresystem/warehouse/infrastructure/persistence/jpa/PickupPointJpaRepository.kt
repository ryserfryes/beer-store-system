package com.beerstoresystem.warehouse.infrastructure.persistence.jpa

import com.beerstoresystem.warehouse.infrastructure.persistence.entity.PickupPointEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PickupPointJpaRepository : JpaRepository<PickupPointEntity, Long> {
    fun findByIsActive(isActive: Boolean): List<PickupPointEntity>
}

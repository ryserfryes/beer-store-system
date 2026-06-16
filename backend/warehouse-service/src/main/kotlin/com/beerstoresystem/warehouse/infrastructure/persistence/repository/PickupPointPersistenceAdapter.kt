package com.beerstoresystem.warehouse.infrastructure.persistence.repository

import com.beerstoresystem.warehouse.domain.model.PickupPoint
import com.beerstoresystem.warehouse.domain.repository.PickupPointDomainRepository
import com.beerstoresystem.warehouse.infrastructure.persistence.jpa.PickupPointJpaRepository
import com.beerstoresystem.warehouse.infrastructure.persistence.mapper.toDomain
import org.springframework.stereotype.Component

@Component
class PickupPointPersistenceAdapter(
    private val jpaRepository: PickupPointJpaRepository
) : PickupPointDomainRepository {

    override fun findAll(): List<PickupPoint> =
        jpaRepository.findAll().map { it.toDomain() }

    override fun findByIsActive(isActive: Boolean): List<PickupPoint> =
        jpaRepository.findByIsActive(isActive).map { it.toDomain() }
}

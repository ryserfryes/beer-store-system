package com.beerstoresystem.warehouse.infrastructure.persistence.repository

import com.beerstoresystem.warehouse.domain.model.Warehouse
import com.beerstoresystem.warehouse.domain.repository.WarehouseDomainRepository
import com.beerstoresystem.warehouse.infrastructure.persistence.jpa.WarehouseJpaRepository
import com.beerstoresystem.warehouse.infrastructure.persistence.mapper.toDomain
import org.springframework.stereotype.Component

@Component
class WarehousePersistenceAdapter(
    private val jpaRepository: WarehouseJpaRepository
) : WarehouseDomainRepository {

    override fun findAll(): List<Warehouse> =
        jpaRepository.findAll().map { it.toDomain() }

    override fun findById(id: Long): Warehouse? =
        jpaRepository.findById(id).map { it.toDomain() }.orElse(null)
}

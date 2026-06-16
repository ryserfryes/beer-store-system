package com.beerstoresystem.supply.infrastructure.persistence.repository

import com.beerstoresystem.supply.domain.model.Supplier
import com.beerstoresystem.supply.domain.repository.SupplierRepository
import com.beerstoresystem.supply.infrastructure.persistence.jpa.SupplierJpaRepository
import com.beerstoresystem.supply.infrastructure.persistence.mapper.toDomain
import com.beerstoresystem.supply.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Component

@Component
class SupplierRepositoryAdapter(
    private val jpaRepository: SupplierJpaRepository
) : SupplierRepository {

    override fun findAll(): List<Supplier> =
        jpaRepository.findAll().map { it.toDomain() }

    override fun findById(id: Long): Supplier? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findAllByIds(ids: Set<Long>): List<Supplier> =
        jpaRepository.findAllById(ids).map { it.toDomain() }

    override fun save(supplier: Supplier): Supplier =
        jpaRepository.save(supplier.toEntity()).toDomain()
}

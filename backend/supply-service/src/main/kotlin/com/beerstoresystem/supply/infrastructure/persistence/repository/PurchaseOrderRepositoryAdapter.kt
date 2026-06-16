package com.beerstoresystem.supply.infrastructure.persistence.repository

import com.beerstoresystem.supply.domain.model.PurchaseOrder
import com.beerstoresystem.supply.domain.repository.PurchaseOrderRepository
import com.beerstoresystem.supply.infrastructure.persistence.jpa.PurchaseOrderJpaRepository
import com.beerstoresystem.supply.infrastructure.persistence.mapper.toDomain
import com.beerstoresystem.supply.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Component

@Component
class PurchaseOrderRepositoryAdapter(
    private val jpaRepository: PurchaseOrderJpaRepository
) : PurchaseOrderRepository {

    override fun findAll(): List<PurchaseOrder> =
        jpaRepository.findAll().map { it.toDomain() }

    override fun findById(id: Long): PurchaseOrder? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun save(order: PurchaseOrder): PurchaseOrder =
        jpaRepository.save(order.toEntity()).toDomain()
}

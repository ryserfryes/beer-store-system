package com.beerstoresystem.warehouse.infrastructure.persistence.repository

import com.beerstoresystem.warehouse.domain.model.InventoryBatch
import com.beerstoresystem.warehouse.domain.repository.InventoryBatchDomainRepository
import com.beerstoresystem.warehouse.infrastructure.persistence.jpa.InventoryBatchJpaRepository
import com.beerstoresystem.warehouse.infrastructure.persistence.mapper.toDomain
import com.beerstoresystem.warehouse.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class InventoryBatchPersistenceAdapter(
    private val jpaRepository: InventoryBatchJpaRepository
) : InventoryBatchDomainRepository {

    override fun findByWarehouseId(warehouseId: Long): List<InventoryBatch> =
        jpaRepository.findByWarehouseId(warehouseId).map { it.toDomain() }

    override fun findAvailableByVariantIdFifo(variantId: Long, today: LocalDate): List<InventoryBatch> =
        jpaRepository.findAvailableByVariantIdFifo(variantId, today).map { it.toDomain() }

    override fun sumAvailableQuantityByVariantId(variantId: Long, today: LocalDate): Int =
        jpaRepository.sumAvailableQuantityByVariantId(variantId, today)

    override fun findById(id: Long): InventoryBatch? =
        jpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    override fun save(batch: InventoryBatch): InventoryBatch =
        jpaRepository.save(batch.toEntity()).toDomain()
}

package com.beerstoresystem.supply.infrastructure.persistence.repository

import com.beerstoresystem.supply.domain.model.PurchaseOrderItem
import com.beerstoresystem.supply.domain.repository.PurchaseOrderItemRepository
import com.beerstoresystem.supply.infrastructure.persistence.jpa.PurchaseOrderItemJpaRepository
import com.beerstoresystem.supply.infrastructure.persistence.mapper.toDomain
import com.beerstoresystem.supply.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Component

@Component
class PurchaseOrderItemRepositoryAdapter(
    private val jpaRepository: PurchaseOrderItemJpaRepository
) : PurchaseOrderItemRepository {

    override fun findAllByPurchaseOrderId(purchaseOrderId: Long): List<PurchaseOrderItem> =
        jpaRepository.findAllByPurchaseOrderId(purchaseOrderId).map { it.toDomain() }

    override fun save(item: PurchaseOrderItem): PurchaseOrderItem =
        jpaRepository.save(item.toEntity()).toDomain()
}

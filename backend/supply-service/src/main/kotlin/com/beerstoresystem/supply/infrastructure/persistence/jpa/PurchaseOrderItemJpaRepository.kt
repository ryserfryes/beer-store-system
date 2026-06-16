package com.beerstoresystem.supply.infrastructure.persistence.jpa

import com.beerstoresystem.supply.infrastructure.persistence.entity.PurchaseOrderItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PurchaseOrderItemJpaRepository : JpaRepository<PurchaseOrderItemEntity, Long> {
    fun findAllByPurchaseOrderId(purchaseOrderId: Long): List<PurchaseOrderItemEntity>
}

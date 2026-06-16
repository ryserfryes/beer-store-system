package com.beerstoresystem.supply.infrastructure.persistence.jpa

import com.beerstoresystem.supply.infrastructure.persistence.entity.PurchaseOrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PurchaseOrderJpaRepository : JpaRepository<PurchaseOrderEntity, Long>

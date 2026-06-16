package com.beerstoresystem.supply.infrastructure.persistence.jpa

import com.beerstoresystem.supply.infrastructure.persistence.entity.SupplierEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SupplierJpaRepository : JpaRepository<SupplierEntity, Long>

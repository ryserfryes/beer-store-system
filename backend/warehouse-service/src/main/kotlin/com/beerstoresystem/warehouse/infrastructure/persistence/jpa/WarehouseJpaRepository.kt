package com.beerstoresystem.warehouse.infrastructure.persistence.jpa

import com.beerstoresystem.warehouse.infrastructure.persistence.entity.WarehouseEntity
import org.springframework.data.jpa.repository.JpaRepository

interface WarehouseJpaRepository : JpaRepository<WarehouseEntity, Long>

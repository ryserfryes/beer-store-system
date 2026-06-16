package com.beerstoresystem.warehouse.domain.repository

import com.beerstoresystem.warehouse.domain.model.Warehouse

interface WarehouseDomainRepository {
    fun findAll(): List<Warehouse>
    fun findById(id: Long): Warehouse?
}

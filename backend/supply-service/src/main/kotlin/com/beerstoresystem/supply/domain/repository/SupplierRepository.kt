package com.beerstoresystem.supply.domain.repository

import com.beerstoresystem.supply.domain.model.Supplier

interface SupplierRepository {
    fun findAll(): List<Supplier>
    fun findById(id: Long): Supplier?
    fun findAllByIds(ids: Set<Long>): List<Supplier>
    fun save(supplier: Supplier): Supplier
}

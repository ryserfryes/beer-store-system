package com.beerstoresystem.supply.domain.repository

import com.beerstoresystem.supply.domain.model.PurchaseOrder

interface PurchaseOrderRepository {
    fun findAll(): List<PurchaseOrder>
    fun findById(id: Long): PurchaseOrder?
    fun save(order: PurchaseOrder): PurchaseOrder
}

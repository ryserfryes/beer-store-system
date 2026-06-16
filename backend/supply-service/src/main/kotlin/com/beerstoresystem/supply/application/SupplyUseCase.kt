package com.beerstoresystem.supply.application

import com.beerstoresystem.supply.integration.rest.dto.AddItemRequest
import com.beerstoresystem.supply.integration.rest.dto.CreatePurchaseOrderRequest
import com.beerstoresystem.supply.integration.rest.dto.CreateSupplierRequest
import com.beerstoresystem.supply.integration.rest.dto.PurchaseOrderDetailDto
import com.beerstoresystem.supply.integration.rest.dto.PurchaseOrderItemDto
import com.beerstoresystem.supply.integration.rest.dto.PurchaseOrderSummaryDto
import com.beerstoresystem.supply.integration.rest.dto.SupplierDto
import com.beerstoresystem.supply.integration.rest.dto.UpdateStatusRequest

interface SupplyUseCase {
    fun getAllSuppliers(): List<SupplierDto>
    fun createSupplier(request: CreateSupplierRequest): SupplierDto
    fun getAllPurchaseOrders(): List<PurchaseOrderSummaryDto>
    fun createPurchaseOrder(request: CreatePurchaseOrderRequest): PurchaseOrderSummaryDto
    fun getPurchaseOrderById(id: Long): PurchaseOrderDetailDto
    fun addItem(orderId: Long, request: AddItemRequest): PurchaseOrderItemDto
    fun updateStatus(orderId: Long, request: UpdateStatusRequest): PurchaseOrderSummaryDto
}

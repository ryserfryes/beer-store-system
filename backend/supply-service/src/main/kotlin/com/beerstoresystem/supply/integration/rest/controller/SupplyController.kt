package com.beerstoresystem.supply.integration.rest.controller

import com.beerstoresystem.supply.application.SupplyUseCase
import com.beerstoresystem.supply.integration.rest.dto.AddItemRequest
import com.beerstoresystem.supply.integration.rest.dto.CreatePurchaseOrderRequest
import com.beerstoresystem.supply.integration.rest.dto.CreateSupplierRequest
import com.beerstoresystem.supply.integration.rest.dto.PurchaseOrderDetailDto
import com.beerstoresystem.supply.integration.rest.dto.PurchaseOrderItemDto
import com.beerstoresystem.supply.integration.rest.dto.PurchaseOrderSummaryDto
import com.beerstoresystem.supply.integration.rest.dto.SupplierDto
import com.beerstoresystem.supply.integration.rest.dto.UpdateStatusRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class SupplyController(private val supplyUseCase: SupplyUseCase) {

    @GetMapping("/suppliers")
    fun getAllSuppliers(): List<SupplierDto> =
        supplyUseCase.getAllSuppliers()

    @PostMapping("/suppliers")
    @ResponseStatus(HttpStatus.CREATED)
    fun createSupplier(@RequestBody request: CreateSupplierRequest): SupplierDto =
        supplyUseCase.createSupplier(request)

    @GetMapping("/purchase-orders")
    fun getAllPurchaseOrders(): List<PurchaseOrderSummaryDto> =
        supplyUseCase.getAllPurchaseOrders()

    @PostMapping("/purchase-orders")
    @ResponseStatus(HttpStatus.CREATED)
    fun createPurchaseOrder(@RequestBody request: CreatePurchaseOrderRequest): PurchaseOrderSummaryDto =
        supplyUseCase.createPurchaseOrder(request)

    @GetMapping("/purchase-orders/{id}")
    fun getPurchaseOrderById(@PathVariable id: Long): PurchaseOrderDetailDto =
        supplyUseCase.getPurchaseOrderById(id)

    @PostMapping("/purchase-orders/{id}/items")
    fun addItem(
        @PathVariable id: Long,
        @RequestBody request: AddItemRequest
    ): PurchaseOrderItemDto = supplyUseCase.addItem(id, request)

    @PutMapping("/purchase-orders/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody request: UpdateStatusRequest
    ): Any = supplyUseCase.updateStatus(id, request)
}

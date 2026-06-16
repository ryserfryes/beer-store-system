package com.beerstoresystem.warehouse.integration.rest.controller

import com.beerstoresystem.warehouse.application.WarehouseUseCase
import com.beerstoresystem.warehouse.integration.rest.dto.InventoryBatchResponse
import com.beerstoresystem.warehouse.integration.rest.dto.PickupPointResponse
import com.beerstoresystem.warehouse.integration.rest.dto.WarehouseResponse
import com.beerstoresystem.warehouse.integration.rest.mapper.toResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class WarehouseController(
    private val warehouseUseCase: WarehouseUseCase
) {

    @GetMapping("/warehouses")
    fun getAllWarehouses(): List<WarehouseResponse> =
        warehouseUseCase.getAllWarehouses().map { it.toResponse() }

    @GetMapping("/warehouses/{id}")
    fun getWarehouseById(@PathVariable id: Long): ResponseEntity<WarehouseResponse> {
        val warehouse = warehouseUseCase.getWarehouseById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(warehouse.toResponse())
    }

    @GetMapping("/warehouses/{id}/inventory")
    fun getWarehouseInventory(@PathVariable id: Long): ResponseEntity<List<InventoryBatchResponse>> {
        warehouseUseCase.getWarehouseById(id)
            ?: return ResponseEntity.notFound().build()
        val inventory = warehouseUseCase.getInventoryForWarehouse(id).map { it.toResponse() }
        return ResponseEntity.ok(inventory)
    }

    @GetMapping("/pickup-points")
    fun getPickupPoints(
        @RequestParam(name = "active", required = false) active: Boolean?
    ): List<PickupPointResponse> =
        warehouseUseCase.getPickupPoints(active ?: false).map { it.toResponse() }
}

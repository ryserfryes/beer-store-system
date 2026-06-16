package com.beerstoresystem.warehouse.integration.rest.mapper

import com.beerstoresystem.warehouse.domain.model.InventoryBatch
import com.beerstoresystem.warehouse.domain.model.PickupPoint
import com.beerstoresystem.warehouse.domain.model.Warehouse
import com.beerstoresystem.warehouse.integration.rest.dto.InventoryBatchResponse
import com.beerstoresystem.warehouse.integration.rest.dto.PickupPointResponse
import com.beerstoresystem.warehouse.integration.rest.dto.WarehouseResponse

fun Warehouse.toResponse(): WarehouseResponse = WarehouseResponse(
    id = id,
    countryId = countryId,
    name = name,
    addressLine = addressLine,
    city = city,
    postalCode = postalCode,
    createdAt = createdAt
)

fun PickupPoint.toResponse(): PickupPointResponse = PickupPointResponse(
    id = id,
    warehouseId = warehouseId,
    name = name,
    city = city,
    addressLine = addressLine,
    postalCode = postalCode,
    workingHours = workingHours,
    isActive = isActive
)

fun InventoryBatch.toResponse(): InventoryBatchResponse = InventoryBatchResponse(
    id = id,
    variantId = variantId,
    lotCode = lotCode,
    quantityOnHand = quantityOnHand,
    expiresOn = expiresOn
)

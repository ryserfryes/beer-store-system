package com.beerstoresystem.warehouse.infrastructure.persistence.mapper

import com.beerstoresystem.warehouse.domain.model.Warehouse
import com.beerstoresystem.warehouse.infrastructure.persistence.entity.WarehouseEntity

fun WarehouseEntity.toDomain(): Warehouse = Warehouse(
    id = id,
    countryId = countryId,
    name = name,
    addressLine = addressLine,
    city = city,
    postalCode = postalCode,
    createdAt = createdAt
)

fun Warehouse.toEntity(): WarehouseEntity = WarehouseEntity().also { e ->
    e.id = id
    e.countryId = countryId
    e.name = name
    e.addressLine = addressLine
    e.city = city
    e.postalCode = postalCode
    e.createdAt = createdAt
}

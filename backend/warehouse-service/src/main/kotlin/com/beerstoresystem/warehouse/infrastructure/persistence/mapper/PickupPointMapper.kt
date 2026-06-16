package com.beerstoresystem.warehouse.infrastructure.persistence.mapper

import com.beerstoresystem.warehouse.domain.model.PickupPoint
import com.beerstoresystem.warehouse.infrastructure.persistence.entity.PickupPointEntity

fun PickupPointEntity.toDomain(): PickupPoint = PickupPoint(
    id = id,
    warehouseId = warehouseId,
    name = name,
    city = city,
    addressLine = addressLine,
    postalCode = postalCode,
    workingHours = workingHours,
    isActive = isActive
)

fun PickupPoint.toEntity(): PickupPointEntity = PickupPointEntity().also { e ->
    e.id = id
    e.warehouseId = warehouseId
    e.name = name
    e.city = city
    e.addressLine = addressLine
    e.postalCode = postalCode
    e.workingHours = workingHours
    e.isActive = isActive
}

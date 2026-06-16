package com.beerstoresystem.warehouse.domain.model

data class PickupPoint(
    val id: Long,
    val warehouseId: Long,
    val name: String,
    val city: String,
    val addressLine: String,
    val postalCode: String,
    val workingHours: String?,
    val isActive: Boolean
)

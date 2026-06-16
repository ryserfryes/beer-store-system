package com.beerstoresystem.warehouse.integration.rest.dto

data class PickupPointResponse(
    val id: Long,
    val warehouseId: Long,
    val name: String,
    val city: String,
    val addressLine: String,
    val postalCode: String,
    val workingHours: String?,
    val isActive: Boolean
)

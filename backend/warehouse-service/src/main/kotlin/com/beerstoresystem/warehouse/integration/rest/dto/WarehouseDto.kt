package com.beerstoresystem.warehouse.integration.rest.dto

import java.time.OffsetDateTime

data class WarehouseResponse(
    val id: Long,
    val countryId: Long,
    val name: String,
    val addressLine: String,
    val city: String,
    val postalCode: String,
    val createdAt: OffsetDateTime?
)

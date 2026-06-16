package com.beerstoresystem.warehouse.domain.model

import java.time.OffsetDateTime

data class Warehouse(
    val id: Long,
    val countryId: Long,
    val name: String,
    val addressLine: String,
    val city: String,
    val postalCode: String,
    val createdAt: OffsetDateTime?
)

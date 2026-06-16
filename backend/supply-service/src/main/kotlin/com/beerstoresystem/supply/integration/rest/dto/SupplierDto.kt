package com.beerstoresystem.supply.integration.rest.dto

import java.time.OffsetDateTime

data class SupplierDto(
    val id: Long,
    val name: String,
    val countryId: Long?,
    val contactEmail: String?,
    val contactPhone: String?,
    val createdAt: OffsetDateTime?
)

data class CreateSupplierRequest(
    val name: String,
    val countryId: Long? = null,
    val contactEmail: String? = null,
    val contactPhone: String? = null
)

package com.beerstoresystem.supply.domain.model

import java.time.OffsetDateTime

data class Supplier(
    val id: Long,
    val name: String,
    val countryId: Long?,
    val contactEmail: String?,
    val contactPhone: String?,
    val createdAt: OffsetDateTime?
)

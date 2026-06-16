package com.beerstoresystem.catalog.domain.model

import java.math.BigDecimal
import java.time.OffsetDateTime

data class Beer(
    val id: Long,
    val name: String,
    val description: String?,
    val abv: BigDecimal?,
    val isActive: Boolean,
    val brewery: Brewery?,
    val style: BeerStyle?,
    val createdAt: OffsetDateTime?
)

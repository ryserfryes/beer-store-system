package com.beerstoresystem.catalog.integration.rest.dto

import java.math.BigDecimal

data class BeerSummaryDto(
    val id: Long,
    val name: String,
    val brewery: String,
    val style: String,
    val abv: BigDecimal?,
    val variants: List<VariantDto>
)

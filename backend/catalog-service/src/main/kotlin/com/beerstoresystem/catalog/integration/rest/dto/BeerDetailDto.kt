package com.beerstoresystem.catalog.integration.rest.dto

import java.math.BigDecimal

data class BeerDetailDto(
    val id: Long,
    val name: String,
    val description: String?,
    val abv: BigDecimal?,
    val isActive: Boolean,
    val brewery: BreweryDto,
    val style: BeerStyleDto,
    val variants: List<VariantDto>
)

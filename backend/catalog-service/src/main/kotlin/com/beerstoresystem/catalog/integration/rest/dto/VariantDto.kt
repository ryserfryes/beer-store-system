package com.beerstoresystem.catalog.integration.rest.dto

import java.math.BigDecimal

data class VariantDto(
    val id: Long,
    val sku: String,
    val volumeMl: Int?,
    val unitPrice: BigDecimal,
    val isActive: Boolean,
    val packageType: String?
)

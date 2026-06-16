package com.beerstoresystem.catalog.domain.model

import java.math.BigDecimal
import java.time.OffsetDateTime

data class ProductVariant(
    val id: Long,
    val sku: String,
    val volumeMl: Int?,
    val unitPrice: BigDecimal,
    val isActive: Boolean,
    val beer: Beer?,
    val packageType: PackageType?,
    val createdAt: OffsetDateTime?
)

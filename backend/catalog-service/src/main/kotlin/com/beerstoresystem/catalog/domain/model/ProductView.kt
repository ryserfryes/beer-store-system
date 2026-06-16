package com.beerstoresystem.catalog.domain.model

import java.time.OffsetDateTime

data class ProductView(
    val id: Long,
    val variantId: Long,
    val customerId: Long?,
    val viewedAt: OffsetDateTime?
)

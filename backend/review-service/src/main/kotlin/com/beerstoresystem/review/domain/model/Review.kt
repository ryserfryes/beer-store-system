package com.beerstoresystem.review.domain.model

import java.time.OffsetDateTime

data class Review(
    val id: Long,
    val customerId: Long,
    val beerId: Long,
    val rating: Short,
    val comment: String?,
    val createdAt: OffsetDateTime
)

package com.beerstoresystem.review.integration.rest.dto

import java.time.OffsetDateTime

data class ReviewResponse(
    val id: Long,
    val customerId: Long,
    val beerId: Long,
    val rating: Short,
    val comment: String?,
    val createdAt: OffsetDateTime
)

data class ReviewStatsResponse(
    val beerId: Long,
    val averageRating: Double,
    val count: Long
)

data class CreateReviewRequest(
    val customerId: Long,
    val beerId: Long,
    val rating: Short,
    val comment: String? = null
)

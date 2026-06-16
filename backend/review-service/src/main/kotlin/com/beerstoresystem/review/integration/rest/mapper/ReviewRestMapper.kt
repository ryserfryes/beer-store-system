package com.beerstoresystem.review.integration.rest.mapper

import com.beerstoresystem.review.application.CreateReviewCommand
import com.beerstoresystem.review.application.ReviewStats
import com.beerstoresystem.review.domain.model.Review
import com.beerstoresystem.review.integration.rest.dto.CreateReviewRequest
import com.beerstoresystem.review.integration.rest.dto.ReviewResponse
import com.beerstoresystem.review.integration.rest.dto.ReviewStatsResponse

fun Review.toResponse(): ReviewResponse = ReviewResponse(
    id = id,
    customerId = customerId,
    beerId = beerId,
    rating = rating,
    comment = comment,
    createdAt = createdAt
)

fun ReviewStats.toResponse(): ReviewStatsResponse = ReviewStatsResponse(
    beerId = beerId,
    averageRating = averageRating,
    count = count
)

fun CreateReviewRequest.toCommand(): CreateReviewCommand = CreateReviewCommand(
    customerId = customerId,
    beerId = beerId,
    rating = rating,
    comment = comment
)

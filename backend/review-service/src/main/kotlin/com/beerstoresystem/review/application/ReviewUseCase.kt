package com.beerstoresystem.review.application

import com.beerstoresystem.review.domain.model.Review

interface ReviewUseCase {
    fun getReviewsByBeerId(beerId: Long): List<Review>
    fun getReviewStatsByBeerId(beerId: Long): ReviewStats
    fun getReviewsByCustomerId(customerId: Long): List<Review>
    fun createReview(command: CreateReviewCommand): Review
}

data class ReviewStats(
    val beerId: Long,
    val averageRating: Double,
    val count: Long
)

data class CreateReviewCommand(
    val customerId: Long,
    val beerId: Long,
    val rating: Short,
    val comment: String? = null
)

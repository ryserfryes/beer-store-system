package com.beerstoresystem.review.domain.model

import java.time.OffsetDateTime

data class ReviewEligibleCustomer(
    val customerId: Long,
    val orderId: Long,
    val createdAt: OffsetDateTime
)

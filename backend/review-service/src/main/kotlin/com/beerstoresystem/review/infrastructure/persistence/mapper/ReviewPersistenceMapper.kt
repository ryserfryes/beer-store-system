package com.beerstoresystem.review.infrastructure.persistence.mapper

import com.beerstoresystem.review.domain.model.Review
import com.beerstoresystem.review.domain.model.ReviewEligibleCustomer
import com.beerstoresystem.review.infrastructure.persistence.entity.ReviewEligibleCustomerEntity
import com.beerstoresystem.review.infrastructure.persistence.entity.ReviewEligibleCustomerEntityId
import com.beerstoresystem.review.infrastructure.persistence.entity.ReviewEntity

fun ReviewEntity.toDomain(): Review = Review(
    id = id,
    customerId = customerId,
    beerId = beerId,
    rating = rating,
    comment = comment,
    createdAt = createdAt
)

fun Review.toEntity(): ReviewEntity = ReviewEntity().apply {
    id = this@toEntity.id
    customerId = this@toEntity.customerId
    beerId = this@toEntity.beerId
    rating = this@toEntity.rating
    comment = this@toEntity.comment
    createdAt = this@toEntity.createdAt
}

fun ReviewEligibleCustomerEntity.toDomain(): ReviewEligibleCustomer = ReviewEligibleCustomer(
    customerId = id.customerId,
    orderId = id.orderId,
    createdAt = createdAt
)

fun ReviewEligibleCustomer.toEntity(): ReviewEligibleCustomerEntity = ReviewEligibleCustomerEntity().apply {
    id = ReviewEligibleCustomerEntityId().apply {
        customerId = this@toEntity.customerId
        orderId = this@toEntity.orderId
    }
    createdAt = this@toEntity.createdAt
}

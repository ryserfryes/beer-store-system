package com.beerstoresystem.review.domain.repository

import com.beerstoresystem.review.domain.model.Review

interface ReviewDomainRepository {
    fun findByBeerId(beerId: Long): List<Review>
    fun findByCustomerId(customerId: Long): List<Review>
    fun existsByCustomerIdAndBeerId(customerId: Long, beerId: Long): Boolean
    fun save(review: Review): Review
}

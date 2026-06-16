package com.beerstoresystem.review.domain.repository

import com.beerstoresystem.review.domain.model.ReviewEligibleCustomer

interface ReviewEligibleCustomerDomainRepository {
    fun existsByCustomerId(customerId: Long): Boolean
    fun existsByCustomerIdAndOrderId(customerId: Long, orderId: Long): Boolean
    fun save(reviewEligibleCustomer: ReviewEligibleCustomer): ReviewEligibleCustomer
}

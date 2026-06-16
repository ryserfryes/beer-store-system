package com.beerstoresystem.review.infrastructure.persistence.repository

import com.beerstoresystem.review.domain.model.ReviewEligibleCustomer
import com.beerstoresystem.review.domain.repository.ReviewEligibleCustomerDomainRepository
import com.beerstoresystem.review.infrastructure.persistence.entity.ReviewEligibleCustomerEntityId
import com.beerstoresystem.review.infrastructure.persistence.jpa.ReviewEligibleCustomerJpaRepository
import com.beerstoresystem.review.infrastructure.persistence.mapper.toDomain
import com.beerstoresystem.review.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Component

@Component
class ReviewEligibleCustomerPersistenceAdapter(
    private val reviewEligibleCustomerJpaRepository: ReviewEligibleCustomerJpaRepository
) : ReviewEligibleCustomerDomainRepository {

    override fun existsByCustomerId(customerId: Long): Boolean =
        reviewEligibleCustomerJpaRepository.existsByIdCustomerId(customerId)

    override fun existsByCustomerIdAndOrderId(customerId: Long, orderId: Long): Boolean =
        reviewEligibleCustomerJpaRepository.existsById(
            ReviewEligibleCustomerEntityId().apply {
                this.customerId = customerId
                this.orderId = orderId
            }
        )

    override fun save(reviewEligibleCustomer: ReviewEligibleCustomer): ReviewEligibleCustomer =
        reviewEligibleCustomerJpaRepository.save(reviewEligibleCustomer.toEntity()).toDomain()
}

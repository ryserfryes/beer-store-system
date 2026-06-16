package com.beerstoresystem.review.infrastructure.persistence.jpa

import com.beerstoresystem.review.infrastructure.persistence.entity.ReviewEligibleCustomerEntity
import com.beerstoresystem.review.infrastructure.persistence.entity.ReviewEligibleCustomerEntityId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReviewEligibleCustomerJpaRepository : JpaRepository<ReviewEligibleCustomerEntity, ReviewEligibleCustomerEntityId> {
    fun existsByIdCustomerId(customerId: Long): Boolean
}

package com.beerstoresystem.review.infrastructure.persistence.jpa

import com.beerstoresystem.review.infrastructure.persistence.entity.ReviewEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReviewJpaRepository : JpaRepository<ReviewEntity, Long> {
    fun findByBeerId(beerId: Long): List<ReviewEntity>
    fun findByCustomerId(customerId: Long): List<ReviewEntity>
    fun existsByCustomerIdAndBeerId(customerId: Long, beerId: Long): Boolean
}

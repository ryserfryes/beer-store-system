package com.beerstoresystem.review.infrastructure.persistence.repository

import com.beerstoresystem.review.domain.model.Review
import com.beerstoresystem.review.domain.repository.ReviewDomainRepository
import com.beerstoresystem.review.infrastructure.persistence.jpa.ReviewJpaRepository
import com.beerstoresystem.review.infrastructure.persistence.mapper.toDomain
import com.beerstoresystem.review.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Component

@Component
class ReviewPersistenceAdapter(
    private val reviewJpaRepository: ReviewJpaRepository
) : ReviewDomainRepository {

    override fun findByBeerId(beerId: Long): List<Review> =
        reviewJpaRepository.findByBeerId(beerId).map { it.toDomain() }

    override fun findByCustomerId(customerId: Long): List<Review> =
        reviewJpaRepository.findByCustomerId(customerId).map { it.toDomain() }

    override fun existsByCustomerIdAndBeerId(customerId: Long, beerId: Long): Boolean =
        reviewJpaRepository.existsByCustomerIdAndBeerId(customerId, beerId)

    override fun save(review: Review): Review =
        reviewJpaRepository.save(review.toEntity()).toDomain()
}

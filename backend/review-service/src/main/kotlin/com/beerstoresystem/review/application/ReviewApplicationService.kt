package com.beerstoresystem.review.application

import com.beerstoresystem.review.domain.model.Review
import com.beerstoresystem.review.domain.repository.ReviewDomainRepository
import com.beerstoresystem.review.domain.repository.ReviewEligibleCustomerDomainRepository
import com.beerstoresystem.review.infrastructure.grpc.OrderGrpcClient
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

@Service
@Transactional(readOnly = true)
class ReviewApplicationService(
    private val reviewDomainRepository: ReviewDomainRepository,
    private val reviewEligibleCustomerDomainRepository: ReviewEligibleCustomerDomainRepository,
    private val orderGrpcClient: OrderGrpcClient
) : ReviewUseCase {

    override fun getReviewsByBeerId(beerId: Long): List<Review> =
        reviewDomainRepository.findByBeerId(beerId)

    override fun getReviewStatsByBeerId(beerId: Long): ReviewStats {
        val reviews = reviewDomainRepository.findByBeerId(beerId)
        val avg = if (reviews.isEmpty()) 0.0 else reviews.map { it.rating.toDouble() }.average()
        return ReviewStats(
            beerId = beerId,
            averageRating = avg,
            count = reviews.size.toLong()
        )
    }

    override fun getReviewsByCustomerId(customerId: Long): List<Review> =
        reviewDomainRepository.findByCustomerId(customerId)

    @Transactional
    override fun createReview(command: CreateReviewCommand): Review {
        if (command.rating < 1 || command.rating > 5) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5")
        }

        val isEligible = reviewEligibleCustomerDomainRepository.existsByCustomerId(command.customerId)
        if (!isEligible) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Customer ${command.customerId} has no completed orders and cannot leave a review"
            )
        }

        val hasPurchased = orderGrpcClient.hasCustomerPurchased(command.customerId, command.beerId)
        if (!hasPurchased) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Customer ${command.customerId} has not purchased beer ${command.beerId}"
            )
        }

        if (reviewDomainRepository.existsByCustomerIdAndBeerId(command.customerId, command.beerId)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Customer ${command.customerId} has already reviewed beer ${command.beerId}"
            )
        }

        val review = Review(
            id = 0L,
            customerId = command.customerId,
            beerId = command.beerId,
            rating = command.rating,
            comment = command.comment,
            createdAt = OffsetDateTime.now()
        )

        return reviewDomainRepository.save(review)
    }
}

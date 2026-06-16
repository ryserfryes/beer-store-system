package com.beerstoresystem.review.service

import io.mockk.*
import io.mockk.junit5.MockKExtension
import com.beerstoresystem.review.application.CreateReviewCommand
import com.beerstoresystem.review.application.ReviewApplicationService
import com.beerstoresystem.review.domain.model.Review
import com.beerstoresystem.review.domain.repository.ReviewDomainRepository
import com.beerstoresystem.review.domain.repository.ReviewEligibleCustomerDomainRepository
import com.beerstoresystem.review.infrastructure.grpc.OrderGrpcClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

@ExtendWith(MockKExtension::class)
class ReviewServiceTest {

    private val reviewDomainRepository = mockk<ReviewDomainRepository>()
    private val reviewEligibleCustomerDomainRepository = mockk<ReviewEligibleCustomerDomainRepository>()
    private val orderGrpcClient = mockk<OrderGrpcClient>()

    private val service = ReviewApplicationService(
        reviewDomainRepository,
        reviewEligibleCustomerDomainRepository,
        orderGrpcClient
    )

    private fun makeReview(id: Long = 1L, customerId: Long = 10L, beerId: Long = 5L, rating: Short = 4): Review =
        Review(
            id = id,
            customerId = customerId,
            beerId = beerId,
            rating = rating,
            comment = null,
            createdAt = OffsetDateTime.now()
        )

    @Test
    fun `getReviewsByBeerId returns mapped dtos`() {
        every { reviewDomainRepository.findByBeerId(5L) } returns listOf(makeReview())

        val result = service.getReviewsByBeerId(5L)

        assertEquals(1, result.size)
        assertEquals(5L, result[0].beerId)
        assertEquals(4, result[0].rating)
    }

    @Test
    fun `getReviewStatsByBeerId returns average and count`() {
        every { reviewDomainRepository.findByBeerId(5L) } returns listOf(
            makeReview(rating = 4), makeReview(id = 2, rating = 2)
        )

        val result = service.getReviewStatsByBeerId(5L)

        assertEquals(3.0, result.averageRating)
        assertEquals(2L, result.count)
    }

    @Test
    fun `getReviewStatsByBeerId with no reviews returns zero`() {
        every { reviewDomainRepository.findByBeerId(99L) } returns emptyList()

        val result = service.getReviewStatsByBeerId(99L)

        assertEquals(0.0, result.averageRating)
        assertEquals(0L, result.count)
    }

    @Test
    fun `createReview throws BAD_REQUEST when rating out of range`() {
        val ex = assertThrows<ResponseStatusException> {
            service.createReview(CreateReviewCommand(customerId = 1L, beerId = 1L, rating = 6))
        }
        assertEquals(400, ex.statusCode.value())
    }

    @Test
    fun `createReview throws FORBIDDEN when customer not eligible`() {
        every { reviewEligibleCustomerDomainRepository.existsByCustomerId(1L) } returns false

        val ex = assertThrows<ResponseStatusException> {
            service.createReview(CreateReviewCommand(customerId = 1L, beerId = 1L, rating = 4))
        }
        assertEquals(403, ex.statusCode.value())
    }

    @Test
    fun `createReview throws FORBIDDEN when customer has not purchased beer`() {
        every { reviewEligibleCustomerDomainRepository.existsByCustomerId(1L) } returns true
        every { orderGrpcClient.hasCustomerPurchased(1L, 5L) } returns false

        val ex = assertThrows<ResponseStatusException> {
            service.createReview(CreateReviewCommand(customerId = 1L, beerId = 5L, rating = 4))
        }
        assertEquals(403, ex.statusCode.value())
    }

    @Test
    fun `createReview throws CONFLICT when review already exists`() {
        every { reviewEligibleCustomerDomainRepository.existsByCustomerId(1L) } returns true
        every { orderGrpcClient.hasCustomerPurchased(1L, 5L) } returns true
        every { reviewDomainRepository.existsByCustomerIdAndBeerId(1L, 5L) } returns true

        val ex = assertThrows<ResponseStatusException> {
            service.createReview(CreateReviewCommand(customerId = 1L, beerId = 5L, rating = 4))
        }
        assertEquals(409, ex.statusCode.value())
    }

    @Test
    fun `createReview saves and returns domain model on success`() {
        val saved = makeReview(id = 10L, customerId = 1L, beerId = 5L, rating = 5)
        every { reviewEligibleCustomerDomainRepository.existsByCustomerId(1L) } returns true
        every { orderGrpcClient.hasCustomerPurchased(1L, 5L) } returns true
        every { reviewDomainRepository.existsByCustomerIdAndBeerId(1L, 5L) } returns false
        every { reviewDomainRepository.save(any()) } returns saved

        val result = service.createReview(CreateReviewCommand(customerId = 1L, beerId = 5L, rating = 5))

        assertEquals(10L, result.id)
        assertEquals(5, result.rating)
        verify { reviewDomainRepository.save(any()) }
    }

    @Test
    fun `getReviewsByCustomerId returns customer reviews`() {
        every { reviewDomainRepository.findByCustomerId(10L) } returns listOf(
            makeReview(), makeReview(id = 2, beerId = 6)
        )

        val result = service.getReviewsByCustomerId(10L)

        assertEquals(2, result.size)
    }
}

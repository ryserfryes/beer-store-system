package com.beerstoresystem.review.integration

import com.beerstoresystem.review.application.CreateReviewCommand
import com.beerstoresystem.review.application.ReviewUseCase
import com.beerstoresystem.review.domain.model.Review
import com.beerstoresystem.review.domain.model.ReviewEligibleCustomer
import com.beerstoresystem.review.domain.repository.ReviewDomainRepository
import com.beerstoresystem.review.domain.repository.ReviewEligibleCustomerDomainRepository
import com.beerstoresystem.review.infrastructure.grpc.OrderGrpcClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.OffsetDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@Transactional
class ReviewServiceIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
    }

    @MockitoBean
    private lateinit var orderGrpcClient: OrderGrpcClient

    @Autowired
    private lateinit var reviewUseCase: ReviewUseCase

    @Autowired
    private lateinit var reviewDomainRepository: ReviewDomainRepository

    @Autowired
    private lateinit var reviewEligibleCustomerDomainRepository: ReviewEligibleCustomerDomainRepository

    private fun saveReview(customerId: Long = 1L, beerId: Long = 10L, rating: Short = 4): Review =
        reviewDomainRepository.save(
            Review(
                id = 0L,
                customerId = customerId,
                beerId = beerId,
                rating = rating,
                comment = null,
                createdAt = OffsetDateTime.now()
            )
        )

    private fun makeEligible(customerId: Long, orderId: Long = 1L) {
        reviewEligibleCustomerDomainRepository.save(
            ReviewEligibleCustomer(
                customerId = customerId,
                orderId = orderId,
                createdAt = OffsetDateTime.now()
            )
        )
    }

    @Test
    fun `getReviewsByBeerId returns empty list for unknown beer`() {
        val result = reviewUseCase.getReviewsByBeerId(999999L)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getReviewsByBeerId returns saved reviews`() {
        saveReview(beerId = 20L, rating = 5)
        saveReview(customerId = 2L, beerId = 20L, rating = 3)

        val result = reviewUseCase.getReviewsByBeerId(20L)

        assertEquals(2, result.size)
        assertTrue(result.all { it.beerId == 20L })
    }

    @Test
    fun `getReviewStatsByBeerId returns correct average`() {
        saveReview(beerId = 30L, rating = 4)
        saveReview(customerId = 2L, beerId = 30L, rating = 2)

        val stats = reviewUseCase.getReviewStatsByBeerId(30L)

        assertEquals(3.0, stats.averageRating)
        assertEquals(2L, stats.count)
    }

    @Test
    fun `getReviewStatsByBeerId returns zero for no reviews`() {
        val stats = reviewUseCase.getReviewStatsByBeerId(999999L)

        assertEquals(0.0, stats.averageRating)
        assertEquals(0L, stats.count)
    }

    @Test
    fun `createReview throws 400 when rating out of range`() {
        val ex = assertThrows<ResponseStatusException> {
            reviewUseCase.createReview(CreateReviewCommand(customerId = 1L, beerId = 10L, rating = 6))
        }
        assertEquals(400, ex.statusCode.value())
    }

    @Test
    fun `createReview throws 403 when customer not eligible`() {
        val ex = assertThrows<ResponseStatusException> {
            reviewUseCase.createReview(CreateReviewCommand(customerId = 99L, beerId = 10L, rating = 4))
        }
        assertEquals(403, ex.statusCode.value())
    }

    @Test
    fun `createReview saves review when eligible and purchased`() {
        makeEligible(customerId = 5L)
        given(orderGrpcClient.hasCustomerPurchased(5L, 40L)).willReturn(true)

        val result = reviewUseCase.createReview(CreateReviewCommand(customerId = 5L, beerId = 40L, rating = 5))

        assertTrue(result.id > 0)
        assertEquals(5L, result.customerId)
        assertEquals(40L, result.beerId)
        assertEquals(5, result.rating)
        assertTrue(reviewDomainRepository.existsByCustomerIdAndBeerId(5L, 40L))
    }

    @Test
    fun `createReview throws 409 when review already exists`() {
        makeEligible(customerId = 6L)
        given(orderGrpcClient.hasCustomerPurchased(6L, 50L)).willReturn(true)
        saveReview(customerId = 6L, beerId = 50L)

        val ex = assertThrows<ResponseStatusException> {
            reviewUseCase.createReview(CreateReviewCommand(customerId = 6L, beerId = 50L, rating = 3))
        }
        assertEquals(409, ex.statusCode.value())
    }

    @Test
    fun `getReviewsByCustomerId returns customer reviews`() {
        saveReview(customerId = 7L, beerId = 1L)
        saveReview(customerId = 7L, beerId = 2L)
        saveReview(customerId = 8L, beerId = 1L)

        val result = reviewUseCase.getReviewsByCustomerId(7L)

        assertEquals(2, result.size)
        assertTrue(result.all { it.customerId == 7L })
    }
}

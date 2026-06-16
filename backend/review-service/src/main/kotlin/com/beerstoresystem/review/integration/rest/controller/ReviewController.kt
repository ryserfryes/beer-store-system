package com.beerstoresystem.review.integration.rest.controller

import com.beerstoresystem.review.application.ReviewUseCase
import com.beerstoresystem.review.integration.rest.dto.CreateReviewRequest
import com.beerstoresystem.review.integration.rest.dto.ReviewResponse
import com.beerstoresystem.review.integration.rest.dto.ReviewStatsResponse
import com.beerstoresystem.review.integration.rest.mapper.toCommand
import com.beerstoresystem.review.integration.rest.mapper.toResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reviews")
class ReviewController(private val reviewUseCase: ReviewUseCase) {

    @GetMapping("/beer/{beerId}")
    fun getReviewsByBeer(@PathVariable beerId: Long): ResponseEntity<List<ReviewResponse>> =
        ResponseEntity.ok(reviewUseCase.getReviewsByBeerId(beerId).map { it.toResponse() })

    @GetMapping("/beer/{beerId}/stats")
    fun getReviewStatsByBeer(@PathVariable beerId: Long): ResponseEntity<ReviewStatsResponse> =
        ResponseEntity.ok(reviewUseCase.getReviewStatsByBeerId(beerId).toResponse())

    @GetMapping("/customer/{customerId}")
    fun getReviewsByCustomer(@PathVariable customerId: Long): ResponseEntity<List<ReviewResponse>> =
        ResponseEntity.ok(reviewUseCase.getReviewsByCustomerId(customerId).map { it.toResponse() })

    @PostMapping
    fun createReview(@RequestBody request: CreateReviewRequest): ResponseEntity<ReviewResponse> {
        val review = reviewUseCase.createReview(request.toCommand())
        return ResponseEntity.status(HttpStatus.CREATED).body(review.toResponse())
    }
}

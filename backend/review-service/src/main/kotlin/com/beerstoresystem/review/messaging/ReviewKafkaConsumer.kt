package com.beerstoresystem.review.messaging

import tools.jackson.databind.ObjectMapper
import com.beerstoresystem.review.domain.model.ReviewEligibleCustomer
import com.beerstoresystem.review.domain.repository.ReviewEligibleCustomerDomainRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

data class OrderPickedUpEvent(val orderId: Long, val customerId: Long, val pickedUpAt: String)

@Component
class ReviewKafkaConsumer(
    private val reviewEligibleCustomerDomainRepository: ReviewEligibleCustomerDomainRepository,
    private val objectMapper: ObjectMapper
) {

    private val log = LoggerFactory.getLogger(ReviewKafkaConsumer::class.java)

    @KafkaListener(topics = ["orders.picked-up"])
    @Transactional
    fun onOrderPickedUp(message: String) {
        try {
            val event = objectMapper.readValue(message, OrderPickedUpEvent::class.java)
            log.info("Received orders.picked-up event: orderId=${event.orderId}, customerId=${event.customerId}")

            if (!reviewEligibleCustomerDomainRepository.existsByCustomerIdAndOrderId(event.customerId, event.orderId)) {
                val eligible = ReviewEligibleCustomer(
                    customerId = event.customerId,
                    orderId = event.orderId,
                    createdAt = OffsetDateTime.now()
                )
                reviewEligibleCustomerDomainRepository.save(eligible)
                log.info("Saved review eligibility for customerId=${event.customerId}, orderId=${event.orderId}")
            } else {
                log.debug("Review eligibility already exists for customerId=${event.customerId}, orderId=${event.orderId}")
            }
        } catch (e: Exception) {
            log.error("Failed to process orders.picked-up message: $message", e)
        }
    }
}

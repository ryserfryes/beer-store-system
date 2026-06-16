package com.beerstoresystem.review.infrastructure.persistence.entity

import jakarta.persistence.*
import java.io.Serializable
import java.time.OffsetDateTime

@Embeddable
class ReviewEligibleCustomerEntityId : Serializable {
    @Column(name = "customer_id", nullable = false)
    var customerId: Long = 0

    @Column(name = "order_id", nullable = false)
    var orderId: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReviewEligibleCustomerEntityId) return false
        return customerId == other.customerId && orderId == other.orderId
    }

    override fun hashCode(): Int = 31 * customerId.hashCode() + orderId.hashCode()
}

@Entity
@Table(name = "review_eligible_customers")
class ReviewEligibleCustomerEntity {
    @EmbeddedId
    var id: ReviewEligibleCustomerEntityId = ReviewEligibleCustomerEntityId()

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
}

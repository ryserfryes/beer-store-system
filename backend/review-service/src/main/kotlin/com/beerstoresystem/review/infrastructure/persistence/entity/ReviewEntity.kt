package com.beerstoresystem.review.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    name = "reviews",
    uniqueConstraints = [UniqueConstraint(columnNames = ["customer_id", "beer_id"])]
)
class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "customer_id", nullable = false)
    var customerId: Long = 0

    @Column(name = "beer_id", nullable = false)
    var beerId: Long = 0

    @Column(name = "rating", nullable = false)
    var rating: Short = 0

    @Column(name = "comment", columnDefinition = "TEXT")
    var comment: String? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
}

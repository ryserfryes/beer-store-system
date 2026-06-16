package com.beerstoresystem.catalog.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "product_views")
class ProductViewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    var variant: ProductVariantEntity? = null

    @Column(name = "customer_id")
    var customerId: Long? = null

    @Column(name = "viewed_at")
    var viewedAt: OffsetDateTime? = null
}

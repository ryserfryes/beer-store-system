package com.beerstoresystem.order.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "cart_items")
class CartItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    var cart: CartEntity? = null

    @Column(name = "variant_id", nullable = false)
    var variantId: Long = 0

    @Column(name = "quantity", nullable = false)
    var quantity: Int = 0

    @Column(name = "added_at", nullable = false)
    var addedAt: OffsetDateTime = OffsetDateTime.now()
}

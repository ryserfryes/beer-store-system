package com.beerstoresystem.order.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "carts")
class CartEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "customer_id", nullable = false, unique = true)
    var customerId: Long = 0

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var items: MutableList<CartItemEntity> = mutableListOf()
}

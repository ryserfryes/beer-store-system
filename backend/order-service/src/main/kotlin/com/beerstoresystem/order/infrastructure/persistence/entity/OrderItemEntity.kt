package com.beerstoresystem.order.infrastructure.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "order_items")
class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: CustomerOrderEntity? = null

    @Column(name = "variant_id", nullable = false)
    var variantId: Long = 0

    @Column(name = "batch_id")
    var batchId: Long? = null

    @Column(name = "quantity", nullable = false)
    var quantity: Int = 0

    @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
    var unitPrice: BigDecimal = BigDecimal.ZERO

    @Column(name = "line_discount", precision = 12, scale = 2, nullable = false)
    var lineDiscount: BigDecimal = BigDecimal.ZERO

    @Column(name = "beer_id")
    var beerId: Long? = null
}

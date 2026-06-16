package com.beerstoresystem.supply.infrastructure.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "purchase_order_items")
class PurchaseOrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "purchase_order_id", nullable = false)
    var purchaseOrderId: Long = 0

    @Column(name = "variant_id", nullable = false)
    var variantId: Long = 0

    @Column(name = "quantity", nullable = false)
    var quantity: Int = 0

    @Column(name = "unit_cost", precision = 12, scale = 2, nullable = false)
    var unitCost: BigDecimal = BigDecimal.ZERO
}

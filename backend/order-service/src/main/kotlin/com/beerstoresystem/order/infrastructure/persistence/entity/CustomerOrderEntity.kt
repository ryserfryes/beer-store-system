package com.beerstoresystem.order.infrastructure.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "customer_orders")
class CustomerOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "customer_id", nullable = false)
    var customerId: Long = 0

    @Column(name = "pickup_point_id", nullable = false)
    var pickupPointId: Long = 0

    @Column(name = "status", nullable = false)
    var status: OrderStatusEntity = OrderStatusEntity.PENDING

    @Column(name = "subtotal_amount", precision = 12, scale = 2, nullable = false)
    var subtotalAmount: BigDecimal = BigDecimal.ZERO

    @Column(name = "discount_amount", precision = 12, scale = 2, nullable = false)
    var discountAmount: BigDecimal = BigDecimal.ZERO

    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    var totalAmount: BigDecimal = BigDecimal.ZERO

    @Column(name = "pickup_code", nullable = false, length = 20)
    var pickupCode: String = ""

    @Column(name = "placed_at", nullable = false)
    var placedAt: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "ready_for_pickup_at")
    var readyForPickupAt: OffsetDateTime? = null

    @Column(name = "pickup_expires_at")
    var pickupExpiresAt: OffsetDateTime? = null

    @Column(name = "picked_up_at")
    var pickedUpAt: OffsetDateTime? = null

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var items: MutableList<OrderItemEntity> = mutableListOf()
}

package com.beerstoresystem.order.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "order_status_history")
class OrderStatusHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "order_id", nullable = false)
    var orderId: Long = 0

    @Column(name = "changed_by_employee_id")
    var changedByEmployeeId: Long? = null

    @Column(name = "from_status")
    var fromStatus: OrderStatusEntity? = null

    @Column(name = "to_status", nullable = false)
    var toStatus: OrderStatusEntity = OrderStatusEntity.PENDING

    @Column(name = "changed_at", nullable = false)
    var changedAt: OffsetDateTime = OffsetDateTime.now()
}

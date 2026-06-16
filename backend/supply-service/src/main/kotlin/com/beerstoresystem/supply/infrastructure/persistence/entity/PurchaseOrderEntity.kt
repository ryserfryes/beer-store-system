package com.beerstoresystem.supply.infrastructure.persistence.entity

import jakarta.persistence.*
import com.beerstoresystem.supply.domain.model.PurchaseOrderStatus
import java.time.OffsetDateTime

@Entity
@Table(name = "purchase_orders")
class PurchaseOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "supplier_id", nullable = false)
    var supplierId: Long = 0

    @Column(name = "warehouse_id", nullable = false)
    var warehouseId: Long = 0

    @Column(name = "status", columnDefinition = "purchase_order_status")
    @Convert(converter = PurchaseOrderStatusConverter::class)
    var status: PurchaseOrderStatus = PurchaseOrderStatus.DRAFTING

    @Column(name = "ordered_at", nullable = false)
    var orderedAt: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "expected_at")
    var expectedAt: OffsetDateTime? = null

    @Column(name = "received_at")
    var receivedAt: OffsetDateTime? = null
}

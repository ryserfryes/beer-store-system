package com.beerstoresystem.warehouse.infrastructure.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(name = "inventory_batches")
class InventoryBatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "variant_id", nullable = false)
    var variantId: Long = 0

    @Column(name = "warehouse_id", nullable = false)
    var warehouseId: Long = 0

    @Column(name = "purchase_order_item_id")
    var purchaseOrderItemId: Long? = null

    @Column(name = "lot_code", length = 64)
    var lotCode: String? = null

    @Column(name = "quantity_on_hand", nullable = false)
    var quantityOnHand: Int = 0

    @Column(name = "wholesale_cost", precision = 12, scale = 2)
    var wholesaleCost: BigDecimal? = null

    @Column(name = "produced_on")
    var producedOn: LocalDate? = null

    @Column(name = "expires_on")
    var expiresOn: LocalDate? = null

    @Column(name = "received_at")
    var receivedAt: OffsetDateTime? = null
}

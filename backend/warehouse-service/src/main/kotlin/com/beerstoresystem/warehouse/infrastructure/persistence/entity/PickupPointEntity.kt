package com.beerstoresystem.warehouse.infrastructure.persistence.entity

import jakarta.persistence.*

@Entity
@Table(name = "pickup_points")
class PickupPointEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "warehouse_id", nullable = false)
    var warehouseId: Long = 0

    @Column(name = "name", length = 150, nullable = false)
    var name: String = ""

    @Column(name = "city", length = 100, nullable = false)
    var city: String = ""

    @Column(name = "address_line", length = 255, nullable = false)
    var addressLine: String = ""

    @Column(name = "postal_code", length = 20, nullable = false)
    var postalCode: String = ""

    @Column(name = "working_hours", length = 100)
    var workingHours: String? = null

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true
}

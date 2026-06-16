package com.beerstoresystem.warehouse.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "warehouses")
class WarehouseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "country_id", nullable = false)
    var countryId: Long = 0

    @Column(name = "name", length = 150, nullable = false)
    var name: String = ""

    @Column(name = "address_line", length = 255, nullable = false)
    var addressLine: String = ""

    @Column(name = "city", length = 100, nullable = false)
    var city: String = ""

    @Column(name = "postal_code", length = 20, nullable = false)
    var postalCode: String = ""

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null
}

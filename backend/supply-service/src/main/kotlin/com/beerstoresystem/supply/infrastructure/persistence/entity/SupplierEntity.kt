package com.beerstoresystem.supply.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "suppliers")
class SupplierEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "country_id")
    var countryId: Long? = null

    @Column(name = "name", length = 150, nullable = false)
    var name: String = ""

    @Column(name = "contact_email")
    var contactEmail: String? = null

    @Column(name = "contact_phone")
    var contactPhone: String? = null

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null
}

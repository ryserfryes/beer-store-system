package com.beerstoresystem.catalog.infrastructure.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "product_variants")
class ProductVariantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beer_id")
    var beer: BeerEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_type_id")
    var packageType: PackageTypeEntity? = null

    @Column(name = "sku", length = 64, nullable = false)
    var sku: String = ""

    @Column(name = "volume_ml")
    var volumeMl: Int? = null

    @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
    var unitPrice: BigDecimal = BigDecimal.ZERO

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null
}

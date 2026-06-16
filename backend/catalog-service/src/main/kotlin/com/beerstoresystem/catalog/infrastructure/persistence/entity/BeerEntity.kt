package com.beerstoresystem.catalog.infrastructure.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "beers")
class BeerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brewery_id")
    var brewery: BreweryEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "style_id")
    var style: BeerStyleEntity? = null

    @Column(name = "name", length = 150, nullable = false)
    var name: String = ""

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null

    @Column(name = "abv", precision = 4, scale = 2)
    var abv: BigDecimal? = null

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null
}

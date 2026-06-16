package com.beerstoresystem.catalog.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "breweries")
class BreweryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    var country: CountryEntity? = null

    @Column(name = "name", length = 150, nullable = false)
    var name: String = ""

    @Column(name = "website_url", length = 255)
    var websiteUrl: String? = null

    @Column(name = "founded_year")
    var foundedYear: Int? = null

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null
}

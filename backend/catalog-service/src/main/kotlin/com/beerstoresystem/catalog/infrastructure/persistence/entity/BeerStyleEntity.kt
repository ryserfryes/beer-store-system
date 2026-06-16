package com.beerstoresystem.catalog.infrastructure.persistence.entity

import jakarta.persistence.*

@Entity
@Table(name = "beer_styles")
class BeerStyleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "name", length = 100, nullable = false)
    var name: String = ""

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null
}

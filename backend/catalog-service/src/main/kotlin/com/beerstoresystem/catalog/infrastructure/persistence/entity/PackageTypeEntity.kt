package com.beerstoresystem.catalog.infrastructure.persistence.entity

import jakarta.persistence.*

@Entity
@Table(name = "package_types")
class PackageTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "code", length = 30, nullable = false)
    var code: String = ""

    @Column(name = "name", length = 100, nullable = false)
    var name: String = ""
}

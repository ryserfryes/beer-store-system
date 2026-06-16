package com.beerstoresystem.catalog.infrastructure.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import java.sql.Types

@Entity
@Table(name = "countries")
class CountryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @JdbcTypeCode(Types.CHAR)
    @Column(name = "iso_code", length = 2, nullable = false)
    var isoCode: String = ""

    @Column(name = "name", length = 100, nullable = false)
    var name: String = ""
}

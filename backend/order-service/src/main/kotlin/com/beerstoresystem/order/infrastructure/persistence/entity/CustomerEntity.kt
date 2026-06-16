package com.beerstoresystem.order.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(name = "customers")
class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "email", nullable = false, unique = true, length = 255)
    var email: String = ""

    @Column(name = "phone", length = 40)
    var phone: String? = null

    @Column(name = "first_name", nullable = false, length = 100)
    var firstName: String = ""

    @Column(name = "last_name", nullable = false, length = 100)
    var lastName: String = ""

    @Column(name = "birth_date")
    var birthDate: LocalDate? = null

    @Column(name = "registered_at", nullable = false)
    var registeredAt: OffsetDateTime = OffsetDateTime.now()
}

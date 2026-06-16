package com.beerstoresystem.order.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "employees")
class EmployeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "email", nullable = false, unique = true, length = 255)
    var email: String = ""

    @Column(name = "first_name", nullable = false, length = 100)
    var firstName: String = ""

    @Column(name = "last_name", nullable = false, length = 100)
    var lastName: String = ""

    @Column(name = "role", nullable = false)
    var role: EmployeeRoleEntity = EmployeeRoleEntity.SUPPORT

    @Column(name = "hired_at", nullable = false)
    var hiredAt: LocalDate = LocalDate.now()

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true
}

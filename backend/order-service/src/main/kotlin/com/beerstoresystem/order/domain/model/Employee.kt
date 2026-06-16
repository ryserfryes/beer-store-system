package com.beerstoresystem.order.domain.model

import java.time.LocalDate

data class Employee(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: EmployeeRole,
    val hiredAt: LocalDate,
    val isActive: Boolean
)

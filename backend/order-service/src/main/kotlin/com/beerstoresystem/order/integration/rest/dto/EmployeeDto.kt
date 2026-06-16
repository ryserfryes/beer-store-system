package com.beerstoresystem.order.integration.rest.dto

import java.time.LocalDate

data class EmployeeDto(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val hiredAt: LocalDate,
    val isActive: Boolean
)

data class CreateEmployeeRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val hiredAt: LocalDate
)

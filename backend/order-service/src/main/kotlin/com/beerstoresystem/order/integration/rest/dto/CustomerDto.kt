package com.beerstoresystem.order.integration.rest.dto

import java.time.LocalDate
import java.time.OffsetDateTime

data class CustomerDto(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val birthDate: LocalDate?,
    val registeredAt: OffsetDateTime
)

data class RegisterCustomerRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val birthDate: LocalDate? = null
)

package com.beerstoresystem.order.domain.model

import java.time.LocalDate
import java.time.OffsetDateTime

data class Customer(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val birthDate: LocalDate?,
    val registeredAt: OffsetDateTime
)

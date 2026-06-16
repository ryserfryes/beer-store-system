package com.beerstoresystem.order.integration.rest.mapper

import com.beerstoresystem.order.domain.model.Customer
import com.beerstoresystem.order.integration.rest.dto.CustomerDto

fun Customer.toDto(): CustomerDto = CustomerDto(
    id = id,
    email = email,
    firstName = firstName,
    lastName = lastName,
    phone = phone,
    birthDate = birthDate,
    registeredAt = registeredAt
)

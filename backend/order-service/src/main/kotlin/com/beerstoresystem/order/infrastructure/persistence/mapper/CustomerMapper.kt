package com.beerstoresystem.order.infrastructure.persistence.mapper

import com.beerstoresystem.order.domain.model.Customer
import com.beerstoresystem.order.infrastructure.persistence.entity.CustomerEntity

fun CustomerEntity.toDomain(): Customer = Customer(
    id = id,
    email = email,
    firstName = firstName,
    lastName = lastName,
    phone = phone,
    birthDate = birthDate,
    registeredAt = registeredAt
)

fun Customer.toEntity(): CustomerEntity = CustomerEntity().also { e ->
    if (id != 0L) e.id = id
    e.email = email
    e.firstName = firstName
    e.lastName = lastName
    e.phone = phone
    e.birthDate = birthDate
    e.registeredAt = registeredAt
}

package com.beerstoresystem.order.integration.rest.mapper

import com.beerstoresystem.order.domain.model.Employee
import com.beerstoresystem.order.integration.rest.dto.EmployeeDto

fun Employee.toDto(): EmployeeDto = EmployeeDto(
    id = id,
    email = email,
    firstName = firstName,
    lastName = lastName,
    role = role.name.lowercase(),
    hiredAt = hiredAt,
    isActive = isActive
)

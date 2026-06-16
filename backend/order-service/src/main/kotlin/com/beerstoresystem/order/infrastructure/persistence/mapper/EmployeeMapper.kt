package com.beerstoresystem.order.infrastructure.persistence.mapper

import com.beerstoresystem.order.domain.model.Employee
import com.beerstoresystem.order.domain.model.EmployeeRole
import com.beerstoresystem.order.infrastructure.persistence.entity.EmployeeEntity
import com.beerstoresystem.order.infrastructure.persistence.entity.EmployeeRoleEntity

fun EmployeeRoleEntity.toDomain(): EmployeeRole = EmployeeRole.valueOf(name)
fun EmployeeRole.toEntity(): EmployeeRoleEntity = EmployeeRoleEntity.valueOf(name)

fun EmployeeEntity.toDomain(): Employee = Employee(
    id = id,
    email = email,
    firstName = firstName,
    lastName = lastName,
    role = role.toDomain(),
    hiredAt = hiredAt,
    isActive = isActive
)

fun Employee.toEntity(): EmployeeEntity = EmployeeEntity().also { e ->
    if (id != 0L) e.id = id
    e.email = email
    e.firstName = firstName
    e.lastName = lastName
    e.role = role.toEntity()
    e.hiredAt = hiredAt
    e.isActive = isActive
}

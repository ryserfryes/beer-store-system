package com.beerstoresystem.order.infrastructure.persistence.repository

import com.beerstoresystem.order.domain.model.Employee
import com.beerstoresystem.order.domain.repository.EmployeeRepository
import com.beerstoresystem.order.infrastructure.persistence.jpa.EmployeeJpaRepository
import com.beerstoresystem.order.infrastructure.persistence.mapper.toDomain
import com.beerstoresystem.order.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Component

@Component
class EmployeeRepositoryAdapter(
    private val jpa: EmployeeJpaRepository
) : EmployeeRepository {

    override fun findAll(): List<Employee> =
        jpa.findAll().map { it.toDomain() }

    override fun existsByEmail(email: String): Boolean =
        jpa.existsByEmail(email)

    override fun save(employee: Employee): Employee =
        jpa.save(employee.toEntity()).toDomain()
}

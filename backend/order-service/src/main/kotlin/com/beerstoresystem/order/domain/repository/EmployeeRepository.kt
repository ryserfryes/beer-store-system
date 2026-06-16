package com.beerstoresystem.order.domain.repository

import com.beerstoresystem.order.domain.model.Employee

interface EmployeeRepository {
    fun findAll(): List<Employee>
    fun existsByEmail(email: String): Boolean
    fun save(employee: Employee): Employee
}

package com.beerstoresystem.order.application

import com.beerstoresystem.order.domain.model.Employee
import java.time.LocalDate

data class CreateEmployeeCommand(
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val hiredAt: LocalDate
)

interface EmployeeUseCase {
    fun getAllEmployees(): List<Employee>
    fun createEmployee(command: CreateEmployeeCommand): Employee
}

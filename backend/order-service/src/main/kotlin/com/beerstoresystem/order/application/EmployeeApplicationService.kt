package com.beerstoresystem.order.application

import com.beerstoresystem.order.domain.model.Employee
import com.beerstoresystem.order.domain.model.EmployeeRole
import com.beerstoresystem.order.domain.exception.ConflictException
import com.beerstoresystem.order.domain.repository.EmployeeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class EmployeeApplicationService(
    private val employeeRepository: EmployeeRepository
) : EmployeeUseCase {

    override fun getAllEmployees(): List<Employee> = employeeRepository.findAll()

    @Transactional
    override fun createEmployee(command: CreateEmployeeCommand): Employee {
        if (employeeRepository.existsByEmail(command.email)) {
            throw ConflictException("Employee with email ${command.email} already exists")
        }
        val role = runCatching { EmployeeRole.valueOf(command.role.uppercase()) }.getOrElse {
            throw IllegalArgumentException("Unknown role: ${command.role}")
        }
        return employeeRepository.save(
            Employee(
                id = 0L,
                email = command.email,
                firstName = command.firstName,
                lastName = command.lastName,
                role = role,
                hiredAt = command.hiredAt,
                isActive = true
            )
        )
    }
}

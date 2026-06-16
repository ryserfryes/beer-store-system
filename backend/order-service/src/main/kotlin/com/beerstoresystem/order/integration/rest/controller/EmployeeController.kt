package com.beerstoresystem.order.integration.rest.controller

import com.beerstoresystem.order.application.CreateEmployeeCommand
import com.beerstoresystem.order.application.EmployeeUseCase
import com.beerstoresystem.order.integration.rest.dto.CreateEmployeeRequest
import com.beerstoresystem.order.integration.rest.dto.EmployeeDto
import com.beerstoresystem.order.integration.rest.mapper.toDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/employees")
class EmployeeController(
    private val employeeUseCase: EmployeeUseCase
) {

    @GetMapping
    fun listEmployees(): List<EmployeeDto> =
        employeeUseCase.getAllEmployees().map { it.toDto() }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createEmployee(@RequestBody body: CreateEmployeeRequest): EmployeeDto =
        employeeUseCase.createEmployee(
            CreateEmployeeCommand(
                email = body.email,
                firstName = body.firstName,
                lastName = body.lastName,
                role = body.role,
                hiredAt = body.hiredAt
            )
        ).toDto()
}

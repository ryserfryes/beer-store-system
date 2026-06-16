package com.beerstoresystem.order.integration.rest.controller

import com.beerstoresystem.order.application.CustomerUseCase
import com.beerstoresystem.order.application.RegisterCustomerCommand import com.beerstoresystem.order.integration.rest.dto.CustomerDto
import com.beerstoresystem.order.integration.rest.dto.RegisterCustomerRequest
import com.beerstoresystem.order.integration.rest.mapper.toDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/customers")
class CustomerController(
    private val customerUseCase: CustomerUseCase
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody body: RegisterCustomerRequest): CustomerDto =
        customerUseCase.registerCustomer(
            RegisterCustomerCommand(
                email = body.email,
                firstName = body.firstName,
                lastName = body.lastName,
                phone = body.phone,
                birthDate = body.birthDate
            )
        ).toDto()

    @GetMapping("/{id}")
    fun getCustomer(@PathVariable id: Long): CustomerDto =
        customerUseCase.getCustomer(id).toDto()
}
package com.beerstoresystem.order.application

import com.beerstoresystem.order.domain.model.Customer
import java.time.LocalDate

data class RegisterCustomerCommand(
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val birthDate: LocalDate? = null
)

interface CustomerUseCase {
    fun getCustomer(id: Long): Customer
    fun registerCustomer(command: RegisterCustomerCommand): Customer
}

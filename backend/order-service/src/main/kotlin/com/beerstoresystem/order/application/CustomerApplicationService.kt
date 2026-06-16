package com.beerstoresystem.order.application

import com.beerstoresystem.order.domain.model.Customer
import com.beerstoresystem.order.domain.exception.ConflictException
import com.beerstoresystem.order.domain.exception.NotFoundException
import com.beerstoresystem.order.domain.repository.CustomerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
@Transactional(readOnly = true)
class CustomerApplicationService(
    private val customerRepository: CustomerRepository
) : CustomerUseCase {

    override fun getCustomer(id: Long): Customer =
        customerRepository.findById(id)
            ?: throw NotFoundException("Customer not found: $id")

    @Transactional
    override fun registerCustomer(command: RegisterCustomerCommand): Customer {
        if (customerRepository.existsByEmail(command.email)) {
            throw ConflictException("Customer with email ${command.email} already exists")
        }
        return customerRepository.save(
            Customer(
                id = 0L,
                email = command.email,
                firstName = command.firstName,
                lastName = command.lastName,
                phone = command.phone,
                birthDate = command.birthDate,
                registeredAt = OffsetDateTime.now()
            )
        )
    }
}

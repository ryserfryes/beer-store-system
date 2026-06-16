package com.beerstoresystem.order.infrastructure.persistence.repository

import com.beerstoresystem.order.domain.model.Customer
import com.beerstoresystem.order.domain.repository.CustomerRepository
import com.beerstoresystem.order.infrastructure.persistence.jpa.CustomerJpaRepository
import com.beerstoresystem.order.infrastructure.persistence.mapper.toDomain
import com.beerstoresystem.order.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Component

@Component
class CustomerRepositoryAdapter(
    private val jpa: CustomerJpaRepository
) : CustomerRepository {

    override fun findById(id: Long): Customer? =
        jpa.findById(id).orElse(null)?.toDomain()

    override fun findByEmail(email: String): Customer? =
        jpa.findByEmail(email)?.toDomain()

    override fun existsByEmail(email: String): Boolean =
        jpa.existsByEmail(email)

    override fun save(customer: Customer): Customer =
        jpa.save(customer.toEntity()).toDomain()
}

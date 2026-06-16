package com.beerstoresystem.order.domain.repository

import com.beerstoresystem.order.domain.model.Customer

interface CustomerRepository {
    fun findById(id: Long): Customer?
    fun findByEmail(email: String): Customer?
    fun existsByEmail(email: String): Boolean
    fun save(customer: Customer): Customer
}

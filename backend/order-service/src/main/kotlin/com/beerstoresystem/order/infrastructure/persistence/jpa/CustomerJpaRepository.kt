package com.beerstoresystem.order.infrastructure.persistence.jpa

import com.beerstoresystem.order.infrastructure.persistence.entity.CustomerEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CustomerJpaRepository : JpaRepository<CustomerEntity, Long> {
    fun findByEmail(email: String): CustomerEntity?
    fun existsByEmail(email: String): Boolean
}

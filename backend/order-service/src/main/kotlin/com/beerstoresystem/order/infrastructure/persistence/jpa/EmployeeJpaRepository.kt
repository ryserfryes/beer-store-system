package com.beerstoresystem.order.infrastructure.persistence.jpa

import com.beerstoresystem.order.infrastructure.persistence.entity.EmployeeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface EmployeeJpaRepository : JpaRepository<EmployeeEntity, Long> {
    fun existsByEmail(email: String): Boolean
}

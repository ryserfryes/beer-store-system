package com.beerstoresystem.order.infrastructure.persistence.jpa

import com.beerstoresystem.order.infrastructure.persistence.entity.CartEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CartJpaRepository : JpaRepository<CartEntity, Long> {
    fun findByCustomerId(customerId: Long): CartEntity?

    @Query("SELECT c FROM CartEntity c LEFT JOIN FETCH c.items WHERE c.customerId = :customerId")
    fun findByCustomerIdWithItems(customerId: Long): CartEntity?
}

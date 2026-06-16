package com.beerstoresystem.order.infrastructure.persistence.jpa

import com.beerstoresystem.order.infrastructure.persistence.entity.CustomerOrderEntity
import com.beerstoresystem.order.infrastructure.persistence.entity.OrderStatusEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CustomerOrderJpaRepository : JpaRepository<CustomerOrderEntity, Long> {

    fun findAllByCustomerId(customerId: Long): List<CustomerOrderEntity>

    @Query("SELECT o FROM CustomerOrderEntity o LEFT JOIN FETCH o.items WHERE o.id = :id")
    fun findByIdWithItems(id: Long): CustomerOrderEntity?

    @Query("SELECT o FROM CustomerOrderEntity o LEFT JOIN FETCH o.items WHERE o.customerId = :customerId")
    fun findAllByCustomerIdWithItems(customerId: Long): List<CustomerOrderEntity>

    @Query("""
        SELECT COUNT(o) > 0 FROM CustomerOrderEntity o
        JOIN o.items i
        WHERE o.customerId = :customerId
          AND o.status = :status
          AND i.beerId = :beerId
    """)
    fun existsByCustomerIdAndStatusAndItemBeerId(
        customerId: Long,
        status: OrderStatusEntity,
        beerId: Long
    ): Boolean

    @Query("""
        SELECT o.id FROM CustomerOrderEntity o
        JOIN o.items i
        WHERE o.customerId = :customerId
          AND o.status = :status
          AND i.beerId = :beerId
        ORDER BY o.placedAt DESC
    """)
    fun findFirstOrderIdByCustomerAndStatusAndBeerId(
        customerId: Long,
        status: OrderStatusEntity,
        beerId: Long
    ): List<Long>
}

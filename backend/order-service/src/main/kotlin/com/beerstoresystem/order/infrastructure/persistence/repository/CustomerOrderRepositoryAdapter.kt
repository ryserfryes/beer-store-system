package com.beerstoresystem.order.infrastructure.persistence.repository

import com.beerstoresystem.order.domain.model.CustomerOrder
import com.beerstoresystem.order.domain.model.OrderStatus
import com.beerstoresystem.order.domain.repository.CustomerOrderRepository
import com.beerstoresystem.order.infrastructure.persistence.entity.OrderStatusEntity
import com.beerstoresystem.order.infrastructure.persistence.jpa.CustomerOrderJpaRepository
import com.beerstoresystem.order.infrastructure.persistence.mapper.toDomain
import com.beerstoresystem.order.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Component

@Component
class CustomerOrderRepositoryAdapter(
    private val jpa: CustomerOrderJpaRepository
) : CustomerOrderRepository {

    override fun findByIdWithItems(id: Long): CustomerOrder? =
        jpa.findByIdWithItems(id)?.toDomain()

    override fun findAllByCustomerId(customerId: Long): List<CustomerOrder> =
        jpa.findAllByCustomerId(customerId).map { it.toDomain() }

    override fun save(order: CustomerOrder): CustomerOrder {
        // For existing orders (update path), load the managed entity and update its fields
        if (order.id != 0L) {
            val managed = jpa.findById(order.id).orElseThrow {
                NoSuchElementException("Order not found: ${order.id}")
            }
            managed.customerId = order.customerId
            managed.pickupPointId = order.pickupPointId
            managed.status = OrderStatusEntity.valueOf(order.status.name)
            managed.subtotalAmount = order.subtotalAmount
            managed.discountAmount = order.discountAmount
            managed.totalAmount = order.totalAmount
            managed.pickupCode = order.pickupCode
            managed.placedAt = order.placedAt
            managed.readyForPickupAt = order.readyForPickupAt
            managed.pickupExpiresAt = order.pickupExpiresAt
            managed.pickedUpAt = order.pickedUpAt
            // Sync items if provided (new inserts only; existing items are already tracked)
            if (order.items.isNotEmpty() && managed.items.isEmpty()) {
                order.items.forEach { item ->
                    managed.items.add(item.toEntity(managed))
                }
            }
            return jpa.save(managed).toDomain()
        }
        // New order path
        val entity = order.toEntity()
        val savedEntity = jpa.save(entity)
        if (order.items.isNotEmpty()) {
            order.items.forEach { item ->
                savedEntity.items.add(item.toEntity(savedEntity))
            }
            return jpa.save(savedEntity).toDomain()
        }
        return savedEntity.toDomain()
    }

    override fun existsByCustomerIdAndStatusAndItemBeerId(
        customerId: Long,
        status: OrderStatus,
        beerId: Long
    ): Boolean = jpa.existsByCustomerIdAndStatusAndItemBeerId(
        customerId,
        OrderStatusEntity.valueOf(status.name),
        beerId
    )

    override fun findFirstOrderIdByCustomerAndStatusAndBeerId(
        customerId: Long,
        status: OrderStatus,
        beerId: Long
    ): List<Long> = jpa.findFirstOrderIdByCustomerAndStatusAndBeerId(
        customerId,
        OrderStatusEntity.valueOf(status.name),
        beerId
    )
}

package com.beerstoresystem.order.infrastructure.persistence.repository

import com.beerstoresystem.order.domain.model.Cart
import com.beerstoresystem.order.domain.repository.CartRepository
import com.beerstoresystem.order.infrastructure.persistence.entity.CartItemEntity
import com.beerstoresystem.order.infrastructure.persistence.jpa.CartJpaRepository
import com.beerstoresystem.order.infrastructure.persistence.mapper.toDomain
import com.beerstoresystem.order.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Component

@Component
class CartRepositoryAdapter(
    private val jpa: CartJpaRepository
) : CartRepository {

    override fun findByCustomerId(customerId: Long): Cart? =
        jpa.findByCustomerId(customerId)?.toDomain()

    override fun findByCustomerIdWithItems(customerId: Long): Cart? =
        jpa.findByCustomerIdWithItems(customerId)?.toDomain()

    override fun save(cart: Cart): Cart {
        val entity = if (cart.id != 0L) {
            jpa.getReferenceById(cart.id).also { e ->
                e.updatedAt = cart.updatedAt
                val existingById = e.items.associateBy { it.id }
                e.items.clear()
                cart.items.forEach { item ->
                    e.items.add(
                        if (item.id != 0L) {
                            existingById[item.id]!!.also { it.quantity = item.quantity }
                        } else {
                            CartItemEntity().also { ie ->
                                ie.cart = e
                                ie.variantId = item.variantId
                                ie.quantity = item.quantity
                                ie.addedAt = item.addedAt
                            }
                        }
                    )
                }
            }
        } else {
            cart.toEntity().also { e ->
                cart.items.forEach { item ->
                    e.items.add(CartItemEntity().also { ie ->
                        ie.cart = e
                        ie.variantId = item.variantId
                        ie.quantity = item.quantity
                        ie.addedAt = item.addedAt
                    })
                }
            }
        }
        return jpa.save(entity).toDomain()
    }
}

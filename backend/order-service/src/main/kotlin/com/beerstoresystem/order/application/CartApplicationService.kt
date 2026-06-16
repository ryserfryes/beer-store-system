package com.beerstoresystem.order.application

import com.beerstoresystem.order.domain.model.Cart
import com.beerstoresystem.order.domain.model.CartItem
import com.beerstoresystem.order.domain.exception.NotFoundException
import com.beerstoresystem.order.domain.repository.CartRepository
import com.beerstoresystem.order.domain.repository.CustomerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
@Transactional(readOnly = true)
class CartApplicationService(
    private val cartRepository: CartRepository,
    private val customerRepository: CustomerRepository
) : CartUseCase {

    override fun getCart(customerId: Long): Cart {
        customerRepository.findById(customerId)
            ?: throw NotFoundException("Customer not found: $customerId")
        return cartRepository.findByCustomerIdWithItems(customerId)
            ?: Cart(id = 0L, customerId = customerId, updatedAt = OffsetDateTime.now(), items = emptyList())
    }

    @Transactional
    override fun addItem(customerId: Long, command: AddCartItemCommand): Cart {
        require(command.quantity > 0) { "Quantity must be positive" }
        customerRepository.findById(customerId)
            ?: throw NotFoundException("Customer not found: $customerId")

        val cart = cartRepository.findByCustomerIdWithItems(customerId)
            ?: Cart(id = 0L, customerId = customerId, updatedAt = OffsetDateTime.now(), items = emptyList())

        val existing = cart.items.find { it.variantId == command.variantId }
        val newItems = if (existing != null) {
            cart.items.map {
                if (it.variantId == command.variantId) it.copy(quantity = it.quantity + command.quantity) else it
            }
        } else {
            cart.items + CartItem(
                id = 0L,
                cartId = cart.id,
                variantId = command.variantId,
                quantity = command.quantity,
                addedAt = OffsetDateTime.now()
            )
        }

        return cartRepository.save(cart.copy(items = newItems, updatedAt = OffsetDateTime.now()))
    }

    @Transactional
    override fun updateItem(customerId: Long, variantId: Long, quantity: Int): Cart {
        require(quantity > 0) { "Quantity must be positive" }
        val cart = cartRepository.findByCustomerIdWithItems(customerId)
            ?: throw NotFoundException("Cart not found for customer: $customerId")
        cart.items.find { it.variantId == variantId }
            ?: throw NotFoundException("Item with variantId $variantId not found in cart")
        val newItems = cart.items.map {
            if (it.variantId == variantId) it.copy(quantity = quantity) else it
        }
        return cartRepository.save(cart.copy(items = newItems, updatedAt = OffsetDateTime.now()))
    }

    @Transactional
    override fun removeItem(customerId: Long, variantId: Long) {
        val cart = cartRepository.findByCustomerIdWithItems(customerId)
            ?: throw NotFoundException("Cart not found for customer: $customerId")
        val newItems = cart.items.filter { it.variantId != variantId }
        cartRepository.save(cart.copy(items = newItems, updatedAt = OffsetDateTime.now()))
    }

    @Transactional
    override fun clearCart(customerId: Long) {
        val cart = cartRepository.findByCustomerIdWithItems(customerId)
            ?: throw NotFoundException("Cart not found for customer: $customerId")
        cartRepository.save(cart.copy(items = emptyList(), updatedAt = OffsetDateTime.now()))
    }
}

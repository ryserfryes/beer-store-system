package com.beerstoresystem.order.application

import io.mockk.*
import io.mockk.junit5.MockKExtension
import com.beerstoresystem.order.domain.model.Cart
import com.beerstoresystem.order.domain.model.CartItem
import com.beerstoresystem.order.domain.model.Customer
import com.beerstoresystem.order.domain.repository.CartRepository
import com.beerstoresystem.order.domain.repository.CustomerRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime

@ExtendWith(MockKExtension::class)
class CartApplicationServiceTest {

    private val cartRepository = mockk<CartRepository>()
    private val customerRepository = mockk<CustomerRepository>()

    private val service = CartApplicationService(cartRepository, customerRepository)

    private fun makeCustomer(id: Long = 1L) = Customer(
        id = id, email = "test@example.com", firstName = "Test", lastName = "User",
        phone = null, birthDate = null, registeredAt = OffsetDateTime.now()
    )

    private fun makeCartItem(id: Long = 10L, variantId: Long = 5L, qty: Int = 2, cartId: Long = 1L) = CartItem(
        id = id, cartId = cartId, variantId = variantId, quantity = qty,
        addedAt = OffsetDateTime.now()
    )

    private fun makeCart(id: Long = 1L, customerId: Long = 1L, items: List<CartItem> = emptyList()) = Cart(
        id = id, customerId = customerId, updatedAt = OffsetDateTime.now(), items = items
    )

    @Test
    fun `getCart throws when customer not found`() {
        every { customerRepository.findById(99L) } returns null

        assertThrows<NoSuchElementException> { service.getCart(99L) }
    }

    @Test
    fun `getCart returns empty cart when no cart exists`() {
        every { customerRepository.findById(1L) } returns makeCustomer()
        every { cartRepository.findByCustomerIdWithItems(1L) } returns null

        val result = service.getCart(1L)

        assertEquals(0L, result.id)
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun `getCart returns cart with items`() {
        val item = makeCartItem()
        val cart = makeCart(items = listOf(item))
        every { customerRepository.findById(1L) } returns makeCustomer()
        every { cartRepository.findByCustomerIdWithItems(1L) } returns cart

        val result = service.getCart(1L)

        assertEquals(1L, result.id)
        assertEquals(1, result.items.size)
        assertEquals(5L, result.items[0].variantId)
    }

    @Test
    fun `addItem throws when quantity is zero`() {
        assertThrows<IllegalArgumentException> {
            service.addItem(1L, AddCartItemCommand(variantId = 5L, quantity = 0))
        }
    }

    @Test
    fun `addItem throws when customer not found`() {
        every { customerRepository.findById(99L) } returns null

        assertThrows<NoSuchElementException> {
            service.addItem(99L, AddCartItemCommand(variantId = 5L, quantity = 1))
        }
    }

    @Test
    fun `addItem creates new cart and item when none exist`() {
        val savedCart = makeCart(items = listOf(makeCartItem()))
        every { customerRepository.findById(1L) } returns makeCustomer()
        every { cartRepository.findByCustomerIdWithItems(1L) } returns null
        every { cartRepository.save(any()) } returns savedCart

        val result = service.addItem(1L, AddCartItemCommand(variantId = 5L, quantity = 3))

        assertEquals(1, result.items.size)
        verify { cartRepository.save(match { it.id == 0L && it.items.size == 1 && it.items[0].variantId == 5L }) }
    }

    @Test
    fun `addItem increases quantity when item already in cart`() {
        val existing = makeCartItem(variantId = 5L, qty = 2)
        val cart = makeCart(items = listOf(existing))
        val updatedCart = makeCart(items = listOf(existing.copy(quantity = 5)))
        every { customerRepository.findById(1L) } returns makeCustomer()
        every { cartRepository.findByCustomerIdWithItems(1L) } returns cart
        every { cartRepository.save(any()) } returns updatedCart

        service.addItem(1L, AddCartItemCommand(variantId = 5L, quantity = 3))

        verify { cartRepository.save(match { it.items.any { i -> i.variantId == 5L && i.quantity == 5 } }) }
    }

    @Test
    fun `addItem adds new item to existing cart`() {
        val existingItem = makeCartItem(id = 10L, variantId = 5L, qty = 2)
        val cart = makeCart(items = listOf(existingItem))
        val updatedCart = makeCart(items = listOf(existingItem, makeCartItem(id = 11L, variantId = 7L, qty = 1)))
        every { customerRepository.findById(1L) } returns makeCustomer()
        every { cartRepository.findByCustomerIdWithItems(1L) } returns cart
        every { cartRepository.save(any()) } returns updatedCart

        service.addItem(1L, AddCartItemCommand(variantId = 7L, quantity = 1))

        verify { cartRepository.save(match { it.items.size == 2 }) }
    }

    @Test
    fun `updateItem throws when cart not found`() {
        every { cartRepository.findByCustomerIdWithItems(1L) } returns null

        assertThrows<NoSuchElementException> { service.updateItem(1L, 5L, 3) }
    }

    @Test
    fun `updateItem throws when item not found in cart`() {
        val cart = makeCart(items = listOf(makeCartItem(variantId = 99L)))
        every { cartRepository.findByCustomerIdWithItems(1L) } returns cart

        assertThrows<NoSuchElementException> { service.updateItem(1L, 5L, 3) }
    }

    @Test
    fun `updateItem updates quantity`() {
        val item = makeCartItem(variantId = 5L, qty = 2)
        val cart = makeCart(items = listOf(item))
        val updatedCart = makeCart(items = listOf(item.copy(quantity = 7)))
        every { cartRepository.findByCustomerIdWithItems(1L) } returns cart
        every { cartRepository.save(any()) } returns updatedCart

        service.updateItem(1L, 5L, 7)

        verify { cartRepository.save(match { it.items.any { i -> i.variantId == 5L && i.quantity == 7 } }) }
    }

    @Test
    fun `removeItem throws when cart not found`() {
        every { cartRepository.findByCustomerIdWithItems(1L) } returns null

        assertThrows<NoSuchElementException> { service.removeItem(1L, 5L) }
    }

    @Test
    fun `removeItem removes item and saves cart`() {
        val item = makeCartItem(variantId = 5L)
        val cart = makeCart(items = listOf(item))
        every { cartRepository.findByCustomerIdWithItems(1L) } returns cart
        every { cartRepository.save(any()) } returns makeCart()

        service.removeItem(1L, 5L)

        verify { cartRepository.save(match { it.items.none { i -> i.variantId == 5L } }) }
    }

    @Test
    fun `clearCart throws when cart not found`() {
        every { cartRepository.findByCustomerIdWithItems(1L) } returns null

        assertThrows<NoSuchElementException> { service.clearCart(1L) }
    }

    @Test
    fun `clearCart saves cart with empty items`() {
        val cart = makeCart(items = listOf(makeCartItem()))
        every { cartRepository.findByCustomerIdWithItems(1L) } returns cart
        every { cartRepository.save(any()) } returns makeCart()

        service.clearCart(1L)

        verify { cartRepository.save(match { it.items.isEmpty() }) }
    }
}

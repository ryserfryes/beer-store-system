package com.beerstoresystem.order.integration

import com.beerstoresystem.order.application.AddCartItemCommand
import com.beerstoresystem.order.application.CartApplicationService
import com.beerstoresystem.order.application.OrderApplicationService
import com.beerstoresystem.order.domain.model.Customer
import com.beerstoresystem.order.domain.port.CatalogPort
import com.beerstoresystem.order.domain.port.WarehousePort
import com.beerstoresystem.order.domain.repository.CustomerRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.OffsetDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@Transactional
class OrderServiceIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
    }

    @MockitoBean
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @MockitoBean
    private lateinit var catalogPort: CatalogPort

    @MockitoBean
    private lateinit var warehousePort: WarehousePort

    @Autowired
    private lateinit var cartService: CartApplicationService

    @Autowired
    private lateinit var orderService: OrderApplicationService

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    private fun saveCustomer(email: String = "test@example.com"): Customer =
        customerRepository.save(
            Customer(
                id = 0L,
                email = email,
                firstName = "Test",
                lastName = "User",
                phone = null,
                birthDate = null,
                registeredAt = OffsetDateTime.now()
            )
        )

    @Test
    fun `getOrder throws when not found`() {
        assertThrows<com.beerstoresystem.order.domain.exception.NotFoundException> { orderService.getOrder(999999L) }
    }

    @Test
    fun `getCustomerOrders throws when customer not found`() {
        assertThrows<com.beerstoresystem.order.domain.exception.NotFoundException> { orderService.getCustomerOrders(999999L) }
    }

    @Test
    fun `getCustomerOrders returns empty list for customer with no orders`() {
        val customer = saveCustomer()

        val result = orderService.getCustomerOrders(customer.id)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getCart returns empty cart for customer with no cart`() {
        val customer = saveCustomer("cart@example.com")

        val result = cartService.getCart(customer.id)

        assertEquals(0L, result.id)
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun `getCart throws when customer not found`() {
        assertThrows<com.beerstoresystem.order.domain.exception.NotFoundException> { cartService.getCart(999999L) }
    }

    @Test
    fun `addItem creates cart and adds item`() {
        val customer = saveCustomer("additem@example.com")

        val result = cartService.addItem(customer.id, AddCartItemCommand(variantId = 5L, quantity = 3))

        assertEquals(customer.id, result.customerId)
        assertEquals(1, result.items.size)
        assertEquals(5L, result.items[0].variantId)
        assertEquals(3, result.items[0].quantity)
    }

    @Test
    fun `addItem merges quantity for existing item`() {
        val customer = saveCustomer("merge@example.com")
        cartService.addItem(customer.id, AddCartItemCommand(variantId = 5L, quantity = 2))

        val result = cartService.addItem(customer.id, AddCartItemCommand(variantId = 5L, quantity = 3))

        assertEquals(1, result.items.size)
        assertEquals(5, result.items[0].quantity)
    }

    @Test
    fun `clearCart removes all items`() {
        val customer = saveCustomer("clear@example.com")
        cartService.addItem(customer.id, AddCartItemCommand(variantId = 1L, quantity = 1))
        cartService.addItem(customer.id, AddCartItemCommand(variantId = 2L, quantity = 2))

        cartService.clearCart(customer.id)

        val cart = cartService.getCart(customer.id)
        assertTrue(cart.items.isEmpty())
    }

    @Test
    fun `updateItem throws when item not in cart`() {
        val customer = saveCustomer("upd@example.com")
        cartService.addItem(customer.id, AddCartItemCommand(variantId = 1L, quantity = 1))

        assertThrows<com.beerstoresystem.order.domain.exception.NotFoundException> {
            cartService.updateItem(customer.id, variantId = 99L, quantity = 5)
        }
    }

    @Test
    fun `removeItem removes specific item from cart`() {
        val customer = saveCustomer("rm@example.com")
        cartService.addItem(customer.id, AddCartItemCommand(variantId = 1L, quantity = 2))
        cartService.addItem(customer.id, AddCartItemCommand(variantId = 2L, quantity = 3))

        cartService.removeItem(customer.id, variantId = 1L)

        val cart = cartService.getCart(customer.id)
        assertEquals(1, cart.items.size)
        assertEquals(2L, cart.items[0].variantId)
    }
}

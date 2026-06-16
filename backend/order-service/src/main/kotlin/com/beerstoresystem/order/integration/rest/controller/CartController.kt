package com.beerstoresystem.order.integration.rest.controller

import com.beerstoresystem.order.application.AddCartItemCommand
import com.beerstoresystem.order.application.CartUseCase
import com.beerstoresystem.order.integration.rest.dto.AddCartItemRequest
import com.beerstoresystem.order.integration.rest.dto.CartDto
import com.beerstoresystem.order.integration.rest.dto.UpdateCartItemRequest
import com.beerstoresystem.order.integration.rest.mapper.toDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/customers/{customerId}/cart")
class CartController(
    private val cartUseCase: CartUseCase
) {

    @GetMapping
    fun getCart(@PathVariable customerId: Long): CartDto =
        cartUseCase.getCart(customerId).toDto()

    @PostMapping("/items")
    fun addItem(
        @PathVariable customerId: Long,
        @RequestBody body: AddCartItemRequest
    ): CartDto = cartUseCase.addItem(customerId, AddCartItemCommand(variantId = body.variantId, quantity = body.quantity)).toDto()

    @PutMapping("/items/{variantId}")
    fun updateItem(
        @PathVariable customerId: Long,
        @PathVariable variantId: Long,
        @RequestBody body: UpdateCartItemRequest
    ): CartDto = cartUseCase.updateItem(customerId, variantId, body.quantity).toDto()

    @DeleteMapping("/items/{variantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeItem(
        @PathVariable customerId: Long,
        @PathVariable variantId: Long
    ) { cartUseCase.removeItem(customerId, variantId) }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun clearCart(@PathVariable customerId: Long) {
        cartUseCase.clearCart(customerId)
    }
}

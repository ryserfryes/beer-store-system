package com.beerstoresystem.supply.integration.rest.advice

import com.beerstoresystem.supply.domain.exception.BusinessRuleException
import com.beerstoresystem.supply.domain.exception.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(): ResponseEntity<Void> =
        ResponseEntity.notFound().build()

    @ExceptionHandler(BusinessRuleException::class)
    fun handleBusinessRuleException(e: BusinessRuleException): ResponseEntity<Map<String, String?>> =
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(mapOf("error" to e.message))

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(): ResponseEntity<Void> =
        ResponseEntity.notFound().build()

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException): ResponseEntity<Map<String, String?>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))

    @ExceptionHandler(IllegalStateException::class)
    fun handleConflict(e: IllegalStateException): ResponseEntity<Map<String, String?>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
}

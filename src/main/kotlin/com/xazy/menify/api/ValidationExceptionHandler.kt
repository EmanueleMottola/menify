package com.xazy.menify.api

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ValidationExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, List<String>>> {
        val errors = ex.bindingResult.fieldErrors
            .groupBy({ it.field }, { it.defaultMessage ?: "Invalid value" })
        return ResponseEntity.badRequest().body(errors)
    }
}
package com.newy.task.spring.error

open class ErrorResponse(
    val message: String,
)

class InvalidErrorResponse(
    message: String,
    val errors: List<FieldError>
) : ErrorResponse(message)

class FieldError(
    val field: String,
    val value: String,
    val reason: String
)
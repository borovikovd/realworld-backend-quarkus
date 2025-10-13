package com.example.shared.exceptions

import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class ValidationExceptionMapper : ExceptionMapper<ValidationException> {
    override fun toResponse(exception: ValidationException): Response =
        Response
            .status(422)
            .entity(mapOf("errors" to exception.errors))
            .build()
}

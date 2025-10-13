package com.example.shared.exceptions

class ValidationException(
    val errors: Map<String, List<String>>,
) : RuntimeException("Validation failed")

package com.example.shared.exceptions

class ForbiddenException(
    message: String = "Forbidden",
) : RuntimeException(message)

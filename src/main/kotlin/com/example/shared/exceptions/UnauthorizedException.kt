package com.example.shared.exceptions

class UnauthorizedException(
    message: String = "Unauthorized",
) : RuntimeException(message)

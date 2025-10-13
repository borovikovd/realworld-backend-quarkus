package com.example.shared.security

import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.jwt.JsonWebToken

@RequestScoped
class SecurityContext {
    @Inject
    lateinit var jwt: JsonWebToken

    val currentUserId: Long?
        get() = if (jwt.subject != null) jwt.subject.toLongOrNull() else null

    val isAuthenticated: Boolean
        get() = currentUserId != null
}

package com.example.shared.security

import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class PasswordHasher {
    private val argon2: Argon2 =
        Argon2Factory.create(
            Argon2Factory.Argon2Types.ARGON2id,
            32,
            64,
        )

    fun hash(password: String): String =
        argon2.hash(
            10,
            65536,
            1,
            password.toCharArray(),
        )

    fun verify(
        hash: String,
        password: String,
    ): Boolean =
        try {
            argon2.verify(hash, password.toCharArray())
        } finally {
            argon2.wipeArray(password.toCharArray())
        }
}

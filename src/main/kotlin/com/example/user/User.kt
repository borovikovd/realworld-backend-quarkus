package com.example.user

import com.example.shared.exceptions.ValidationException
import java.time.OffsetDateTime

class User private constructor(
    var id: Long?,
    var email: String,
    var username: String,
    var passwordHash: String,
    var bio: String?,
    var image: String?,
    val createdAt: OffsetDateTime,
    var updatedAt: OffsetDateTime,
) {
    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

        fun create(
            email: String,
            username: String,
            passwordHash: String,
        ): User {
            validate(email, username)
            val now = OffsetDateTime.now()
            return User(
                id = null,
                email = email,
                username = username,
                passwordHash = passwordHash,
                bio = null,
                image = null,
                createdAt = now,
                updatedAt = now,
            )
        }

        fun reconstitute(
            id: Long,
            email: String,
            username: String,
            passwordHash: String,
            bio: String?,
            image: String?,
            createdAt: OffsetDateTime,
            updatedAt: OffsetDateTime,
        ): User = User(id, email, username, passwordHash, bio, image, createdAt, updatedAt)

        private fun validate(
            email: String,
            username: String,
        ) {
            val errors = mutableMapOf<String, List<String>>()

            if (email.isBlank()) {
                errors["email"] = listOf("must not be blank")
            } else if (!EMAIL_REGEX.matches(email)) {
                errors["email"] = listOf("must be a valid email address")
            }

            if (username.isBlank()) {
                errors["username"] = listOf("must not be blank")
            } else if (username.length < 3 || username.length > 50) {
                errors["username"] = listOf("must be between 3 and 50 characters")
            }

            if (errors.isNotEmpty()) {
                throw ValidationException(errors)
            }
        }
    }

    fun updateProfile(
        email: String?,
        username: String?,
        bio: String?,
        image: String?,
    ) {
        email?.let {
            if (it.isNotBlank()) {
                this.email = it
            }
        }
        username?.let {
            if (it.isNotBlank()) {
                this.username = it
            }
        }
        this.bio = bio
        this.image = image
        this.updatedAt = OffsetDateTime.now()
    }

    fun updatePassword(newPasswordHash: String) {
        this.passwordHash = newPasswordHash
        this.updatedAt = OffsetDateTime.now()
    }
}

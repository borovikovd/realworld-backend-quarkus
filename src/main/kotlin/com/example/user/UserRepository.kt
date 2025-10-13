package com.example.user

interface UserRepository {
    fun save(user: User): User

    fun findById(id: Long): User?

    fun findByEmail(email: String): User?

    fun findByUsername(username: String): User?

    fun existsByEmail(email: String): Boolean

    fun existsByUsername(username: String): Boolean
}

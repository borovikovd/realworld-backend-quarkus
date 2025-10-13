package com.example.user

import com.example.shared.exceptions.UnauthorizedException
import com.example.shared.exceptions.ValidationException
import com.example.shared.security.JwtService
import com.example.shared.security.PasswordHasher
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class UserService {
    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var passwordHasher: PasswordHasher

    @Inject
    lateinit var jwtService: JwtService

    @Transactional
    fun register(
        email: String,
        username: String,
        password: String,
    ): Pair<User, String> {
        val errors = mutableMapOf<String, List<String>>()

        if (userRepository.existsByEmail(email)) {
            errors["email"] = listOf("is already taken")
        }

        if (userRepository.existsByUsername(username)) {
            errors["username"] = listOf("is already taken")
        }

        if (password.length < 8) {
            errors["password"] = listOf("must be at least 8 characters")
        }

        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }

        val passwordHash = passwordHasher.hash(password)
        val user = User.create(email, username, passwordHash)
        val savedUser = userRepository.save(user)
        val token = jwtService.generateToken(savedUser.id!!, savedUser.email, savedUser.username)

        return Pair(savedUser, token)
    }

    @Transactional
    fun login(
        email: String,
        password: String,
    ): Pair<User, String> {
        val user =
            userRepository.findByEmail(email)
                ?: throw UnauthorizedException("Invalid email or password")

        if (!passwordHasher.verify(user.passwordHash, password)) {
            throw UnauthorizedException("Invalid email or password")
        }

        val token = jwtService.generateToken(user.id!!, user.email, user.username)
        return Pair(user, token)
    }

    @Transactional
    fun updateUser(
        userId: Long,
        email: String?,
        username: String?,
        password: String?,
        bio: String?,
        image: String?,
    ): Pair<User, String> {
        val user =
            userRepository.findById(userId)
                ?: throw UnauthorizedException("User not found")

        val errors = mutableMapOf<String, List<String>>()

        email?.let {
            if (it != user.email && userRepository.existsByEmail(it)) {
                errors["email"] = listOf("is already taken")
            }
        }

        username?.let {
            if (it != user.username && userRepository.existsByUsername(it)) {
                errors["username"] = listOf("is already taken")
            }
        }

        password?.let {
            if (it.length < 8) {
                errors["password"] = listOf("must be at least 8 characters")
            }
        }

        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }

        user.updateProfile(email, username, bio, image)

        password?.let {
            val newPasswordHash = passwordHasher.hash(it)
            user.updatePassword(newPasswordHash)
        }

        val updatedUser = userRepository.save(user)
        val token = jwtService.generateToken(updatedUser.id!!, updatedUser.email, updatedUser.username)

        return Pair(updatedUser, token)
    }

    fun getCurrentUser(userId: Long): User =
        userRepository.findById(userId)
            ?: throw UnauthorizedException("User not found")
}

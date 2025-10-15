package com.example.user

import com.example.shared.exceptions.NotFoundException
import com.example.shared.exceptions.UnauthorizedException
import com.example.shared.exceptions.ValidationException
import com.example.shared.security.PasswordHasher
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class UserService {
    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var followRepository: FollowRepository

    @Inject
    lateinit var passwordHasher: PasswordHasher

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
    }

    @Transactional
    fun register(
        email: String,
        username: String,
        password: String,
    ): User {
        val errors = mutableMapOf<String, List<String>>()

        if (userRepository.existsByEmail(email)) {
            errors["email"] = listOf("is already taken")
        }

        if (userRepository.existsByUsername(username)) {
            errors["username"] = listOf("is already taken")
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            errors["password"] = listOf("must be at least $MIN_PASSWORD_LENGTH characters")
        }

        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }

        val passwordHash = passwordHasher.hash(password)
        val user = User(email = email, username = username, passwordHash = passwordHash)
        return userRepository.create(user)
    }

    fun login(
        email: String,
        password: String,
    ): User {
        val user =
            userRepository.findByEmail(email)
                ?: throw UnauthorizedException("Invalid email or password")

        if (!passwordHasher.verify(user.passwordHash, password)) {
            throw UnauthorizedException("Invalid email or password")
        }

        return user
    }

    @Transactional
    fun updateUser(
        userId: Long,
        email: String?,
        username: String?,
        password: String?,
        bio: String?,
        image: String?,
    ): User {
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
            if (it.length < MIN_PASSWORD_LENGTH) {
                errors["password"] = listOf("must be at least $MIN_PASSWORD_LENGTH characters")
            }
        }

        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }

        var updatedUser = user.updateProfile(email, username, bio, image)

        password?.let {
            val newPasswordHash = passwordHasher.hash(it)
            updatedUser = updatedUser.updatePassword(newPasswordHash)
        }

        return userRepository.update(updatedUser)
    }

    fun getCurrentUser(userId: Long): User =
        userRepository.findById(userId)
            ?: throw UnauthorizedException("User not found")

    @Transactional
    fun followUser(
        followerId: Long,
        username: String,
    ) {
        val followee =
            userRepository.findByUsername(username)
                ?: throw NotFoundException("User not found")

        require(followee.id != followerId) { "Cannot follow yourself" }

        followRepository.follow(followerId, followee.id!!)
    }

    @Transactional
    fun unfollowUser(
        followerId: Long,
        username: String,
    ) {
        val followee =
            userRepository.findByUsername(username)
                ?: throw NotFoundException("User not found")

        followRepository.unfollow(followerId, followee.id!!)
    }
}

package com.example.profile

import com.example.shared.exceptions.NotFoundException
import com.example.user.UserRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class ProfileService {
    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var followRepository: FollowRepository

    @Transactional
    fun followUser(
        followerId: Long,
        username: String,
    ) {
        val followee =
            userRepository.findByUsername(username)
                ?: throw NotFoundException("User not found")

        if (followee.id == followerId) {
            throw IllegalArgumentException("Cannot follow yourself")
        }

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

    fun getProfile(
        username: String,
        currentUserId: Long?,
    ): ProfileData {
        val user =
            userRepository.findByUsername(username)
                ?: throw NotFoundException("User not found")

        val following =
            currentUserId?.let {
                followRepository.isFollowing(it, user.id!!)
            } ?: false

        return ProfileData(
            username = user.username,
            bio = user.bio,
            image = user.image,
            following = following,
        )
    }
}

data class ProfileData(
    val username: String,
    val bio: String?,
    val image: String?,
    val following: Boolean,
)

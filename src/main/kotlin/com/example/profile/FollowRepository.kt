package com.example.profile

interface FollowRepository {
    fun follow(
        followerId: Long,
        followeeId: Long,
    )

    fun unfollow(
        followerId: Long,
        followeeId: Long,
    )

    fun isFollowing(
        followerId: Long,
        followeeId: Long,
    ): Boolean
}

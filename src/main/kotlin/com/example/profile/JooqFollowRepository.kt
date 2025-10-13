package com.example.profile

import com.example.jooq.tables.references.FOLLOWERS
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext
import java.time.LocalDateTime

@ApplicationScoped
class JooqFollowRepository : FollowRepository {
    @Inject
    lateinit var dsl: DSLContext

    override fun follow(
        followerId: Long,
        followeeId: Long,
    ) {
        dsl
            .insertInto(FOLLOWERS)
            .set(FOLLOWERS.FOLLOWER_ID, followerId)
            .set(FOLLOWERS.FOLLOWEE_ID, followeeId)
            .set(FOLLOWERS.CREATED_AT, LocalDateTime.now())
            .onDuplicateKeyIgnore()
            .execute()
    }

    override fun unfollow(
        followerId: Long,
        followeeId: Long,
    ) {
        dsl
            .deleteFrom(FOLLOWERS)
            .where(FOLLOWERS.FOLLOWER_ID.eq(followerId))
            .and(FOLLOWERS.FOLLOWEE_ID.eq(followeeId))
            .execute()
    }

    override fun isFollowing(
        followerId: Long,
        followeeId: Long,
    ): Boolean =
        dsl.fetchExists(
            dsl
                .selectFrom(FOLLOWERS)
                .where(FOLLOWERS.FOLLOWER_ID.eq(followerId))
                .and(FOLLOWERS.FOLLOWEE_ID.eq(followeeId)),
        )
}

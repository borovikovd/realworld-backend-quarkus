package com.example.article

import com.example.api.model.Profile
import com.example.jooq.public.tables.references.COMMENTS
import com.example.jooq.public.tables.references.FOLLOWERS
import com.example.jooq.public.tables.references.USERS
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext
import com.example.api.model.Comment as ApiComment

@ApplicationScoped
class CommentQueryService {
    @Inject
    lateinit var dsl: DSLContext

    fun getCommentsBySlug(
        slug: String,
        viewerId: Long? = null,
    ): List<ApiComment> {
        val commentRecords =
            dsl
                .select(
                    COMMENTS.ID,
                    COMMENTS.BODY,
                    COMMENTS.CREATED_AT,
                    COMMENTS.UPDATED_AT,
                    COMMENTS.AUTHOR_ID,
                    USERS.USERNAME,
                    USERS.BIO,
                    USERS.IMAGE,
                ).from(COMMENTS)
                .join(USERS)
                .on(USERS.ID.eq(COMMENTS.AUTHOR_ID))
                .join(com.example.jooq.public.tables.references.ARTICLES)
                .on(
                    com.example.jooq.public.tables.references.ARTICLES.ID
                        .eq(COMMENTS.ARTICLE_ID),
                ).where(
                    com.example.jooq.public.tables.references.ARTICLES.SLUG
                        .eq(slug),
                ).orderBy(COMMENTS.CREATED_AT.desc())
                .fetch()

        return commentRecords.map { record ->
            val authorId = record.get(COMMENTS.AUTHOR_ID)!!
            val following = viewerId?.let { isFollowing(authorId, it) } ?: false

            ApiComment()
                .id(record.get(COMMENTS.ID)?.toInt())
                .body(record.get(COMMENTS.BODY))
                .createdAt(record.get(COMMENTS.CREATED_AT))
                .updatedAt(record.get(COMMENTS.UPDATED_AT))
                .author(
                    Profile()
                        .username(record.get(USERS.USERNAME))
                        .bio(record.get(USERS.BIO))
                        .image(record.get(USERS.IMAGE))
                        .following(following),
                )
        }
    }

    fun getCommentById(
        commentId: Long,
        viewerId: Long? = null,
    ): ApiComment {
        val record =
            dsl
                .select(
                    COMMENTS.ID,
                    COMMENTS.BODY,
                    COMMENTS.CREATED_AT,
                    COMMENTS.UPDATED_AT,
                    COMMENTS.AUTHOR_ID,
                    USERS.USERNAME,
                    USERS.BIO,
                    USERS.IMAGE,
                ).from(COMMENTS)
                .join(USERS)
                .on(USERS.ID.eq(COMMENTS.AUTHOR_ID))
                .where(COMMENTS.ID.eq(commentId))
                .fetchOne() ?: throw com.example.shared.exceptions
                .NotFoundException("Comment not found")

        val authorId = record.get(COMMENTS.AUTHOR_ID)!!
        val following = viewerId?.let { isFollowing(authorId, it) } ?: false

        return ApiComment()
            .id(record.get(COMMENTS.ID)?.toInt())
            .body(record.get(COMMENTS.BODY))
            .createdAt(record.get(COMMENTS.CREATED_AT))
            .updatedAt(record.get(COMMENTS.UPDATED_AT))
            .author(
                Profile()
                    .username(record.get(USERS.USERNAME))
                    .bio(record.get(USERS.BIO))
                    .image(record.get(USERS.IMAGE))
                    .following(following),
            )
    }

    private fun isFollowing(
        followeeId: Long,
        followerId: Long,
    ): Boolean =
        dsl.fetchExists(
            dsl
                .selectFrom(FOLLOWERS)
                .where(FOLLOWERS.FOLLOWEE_ID.eq(followeeId))
                .and(FOLLOWERS.FOLLOWER_ID.eq(followerId)),
        )
}

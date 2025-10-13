package com.example.comment

import com.example.jooq.public.tables.references.COMMENTS
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext

@ApplicationScoped
class JooqCommentRepository : CommentRepository {
    @Inject
    lateinit var dsl: DSLContext

    override fun save(comment: Comment): Comment =
        if (comment.id == null) {
            insert(comment)
        } else {
            update(comment)
        }

    private fun insert(comment: Comment): Comment {
        val record =
            dsl
                .insertInto(COMMENTS)
                .set(COMMENTS.ARTICLE_ID, comment.articleId)
                .set(COMMENTS.AUTHOR_ID, comment.authorId)
                .set(COMMENTS.BODY, comment.body)
                .set(COMMENTS.CREATED_AT, comment.createdAt)
                .set(COMMENTS.UPDATED_AT, comment.updatedAt)
                .returning()
                .fetchOne() ?: throw IllegalStateException("Failed to insert comment")

        return Comment.reconstitute(
            id = record.id!!,
            articleId = record.articleId!!,
            authorId = record.authorId!!,
            body = record.body!!,
            createdAt = record.createdAt!!,
            updatedAt = record.updatedAt!!,
        )
    }

    private fun update(comment: Comment): Comment {
        dsl
            .update(COMMENTS)
            .set(COMMENTS.BODY, comment.body)
            .set(COMMENTS.UPDATED_AT, comment.updatedAt)
            .where(COMMENTS.ID.eq(comment.id))
            .execute()
        return comment
    }

    override fun findById(id: Long): Comment? {
        val record =
            dsl
                .selectFrom(COMMENTS)
                .where(COMMENTS.ID.eq(id))
                .fetchOne() ?: return null

        return Comment.reconstitute(
            id = record.id!!,
            articleId = record.articleId!!,
            authorId = record.authorId!!,
            body = record.body!!,
            createdAt = record.createdAt!!,
            updatedAt = record.updatedAt!!,
        )
    }

    override fun findByArticleId(articleId: Long): List<Comment> =
        dsl
            .selectFrom(COMMENTS)
            .where(COMMENTS.ARTICLE_ID.eq(articleId))
            .orderBy(COMMENTS.CREATED_AT.desc())
            .fetch()
            .map {
                Comment.reconstitute(
                    id = it.id!!,
                    articleId = it.articleId!!,
                    authorId = it.authorId!!,
                    body = it.body!!,
                    createdAt = it.createdAt!!,
                    updatedAt = it.updatedAt!!,
                )
            }

    override fun deleteById(id: Long) {
        dsl
            .deleteFrom(COMMENTS)
            .where(COMMENTS.ID.eq(id))
            .execute()
    }
}

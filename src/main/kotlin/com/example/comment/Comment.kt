package com.example.comment

import com.example.shared.exceptions.ForbiddenException
import com.example.shared.exceptions.ValidationException
import java.time.LocalDateTime

class Comment private constructor(
    var id: Long?,
    val articleId: Long,
    val authorId: Long,
    var body: String,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
) {
    companion object {
        fun create(
            articleId: Long,
            authorId: Long,
            body: String,
        ): Comment {
            validate(body)

            val now = LocalDateTime.now()
            return Comment(
                id = null,
                articleId = articleId,
                authorId = authorId,
                body = body,
                createdAt = now,
                updatedAt = now,
            )
        }

        fun reconstitute(
            id: Long,
            articleId: Long,
            authorId: Long,
            body: String,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime,
        ): Comment = Comment(id, articleId, authorId, body, createdAt, updatedAt)

        private fun validate(body: String) {
            val errors = mutableMapOf<String, List<String>>()

            if (body.isBlank()) {
                errors["body"] = listOf("must not be blank")
            }

            if (errors.isNotEmpty()) {
                throw ValidationException(errors)
            }
        }
    }

    fun canBeDeletedBy(userId: Long): Boolean = userId == authorId

    fun ensureCanBeDeletedBy(userId: Long) {
        if (!canBeDeletedBy(userId)) {
            throw ForbiddenException("You can only delete your own comments")
        }
    }
}

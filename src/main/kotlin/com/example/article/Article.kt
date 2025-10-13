package com.example.article

import com.example.shared.exceptions.ForbiddenException
import com.example.shared.exceptions.ValidationException
import com.example.shared.utils.SlugUtils
import java.time.LocalDateTime

class Article private constructor(
    var id: Long?,
    val slug: String,
    var title: String,
    var description: String,
    var body: String,
    val authorId: Long,
    val tags: MutableSet<String>,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
) {
    companion object {
        fun create(
            title: String,
            description: String,
            body: String,
            authorId: Long,
            tags: List<String>,
        ): Article {
            validate(title, description, body)

            val now = LocalDateTime.now()
            val slug = SlugUtils.toSlug(title)

            return Article(
                id = null,
                slug = slug,
                title = title,
                description = description,
                body = body,
                authorId = authorId,
                tags = tags.toMutableSet(),
                createdAt = now,
                updatedAt = now,
            )
        }

        fun reconstitute(
            id: Long,
            slug: String,
            title: String,
            description: String,
            body: String,
            authorId: Long,
            tags: Set<String>,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime,
        ): Article =
            Article(
                id,
                slug,
                title,
                description,
                body,
                authorId,
                tags.toMutableSet(),
                createdAt,
                updatedAt,
            )

        private fun validate(
            title: String,
            description: String,
            body: String,
        ) {
            val errors = mutableMapOf<String, List<String>>()

            if (title.isBlank()) {
                errors["title"] = listOf("must not be blank")
            }

            if (description.isBlank()) {
                errors["description"] = listOf("must not be blank")
            }

            if (body.isBlank()) {
                errors["body"] = listOf("must not be blank")
            }

            if (errors.isNotEmpty()) {
                throw ValidationException(errors)
            }
        }
    }

    fun update(
        userId: Long,
        title: String?,
        description: String?,
        body: String?,
    ) {
        if (userId != authorId) {
            throw ForbiddenException("You can only update your own articles")
        }

        title?.let {
            if (it.isNotBlank()) {
                this.title = it
            }
        }

        description?.let {
            if (it.isNotBlank()) {
                this.description = it
            }
        }

        body?.let {
            if (it.isNotBlank()) {
                this.body = it
            }
        }

        this.updatedAt = LocalDateTime.now()
    }

    fun canBeDeletedBy(userId: Long): Boolean = userId == authorId
}

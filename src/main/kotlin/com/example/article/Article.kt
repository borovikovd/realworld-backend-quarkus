package com.example.article

import com.example.shared.domain.Entity
import com.example.shared.exceptions.ForbiddenException
import com.example.shared.utils.SlugUtils
import java.time.OffsetDateTime

data class Article(
    override val id: Long? = null,
    val slug: String,
    val title: String,
    val description: String,
    val body: String,
    val authorId: Long,
    val tags: Set<String> = emptySet(),
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
) : Entity<Long> {
    init {
        require(title.isNotBlank()) { "Title must not be blank" }
        require(description.isNotBlank()) { "Description must not be blank" }
        require(body.isNotBlank()) { "Body must not be blank" }
    }

    companion object {
        fun create(
            title: String,
            description: String,
            body: String,
            authorId: Long,
            tags: List<String>,
        ): Article {
            val slug = SlugUtils.toSlug(title)
            return Article(
                slug = slug,
                title = title,
                description = description,
                body = body,
                authorId = authorId,
                tags = tags.toSet(),
            )
        }
    }

    override fun withId(newId: Long): Article = copy(id = newId)

    fun update(
        userId: Long,
        title: String?,
        description: String?,
        body: String?,
    ): Article {
        if (userId != authorId) {
            throw ForbiddenException("You can only update your own articles")
        }

        val updatedTitle = if (title != null && title.isNotBlank()) title else this.title
        val updatedDescription = if (description != null && description.isNotBlank()) description else this.description
        val updatedBody = if (body != null && body.isNotBlank()) body else this.body

        return copy(
            title = updatedTitle,
            description = updatedDescription,
            body = updatedBody,
            updatedAt = OffsetDateTime.now(),
        )
    }

    fun canBeDeletedBy(userId: Long): Boolean = userId == authorId
}

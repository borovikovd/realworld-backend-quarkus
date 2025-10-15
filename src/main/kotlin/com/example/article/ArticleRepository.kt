package com.example.article

import com.example.shared.domain.Repository

interface ArticleRepository : Repository<Article, Long> {
    fun findBySlug(slug: String): Article?

    fun deleteById(id: Long)

    fun favorite(
        articleId: Long,
        userId: Long,
    )

    fun unfavorite(
        articleId: Long,
        userId: Long,
    )

    fun isFavorited(
        articleId: Long,
        userId: Long,
    ): Boolean

    fun getAllTags(): List<String>
}

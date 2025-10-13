package com.example.article

interface ArticleRepository {
    fun save(article: Article): Article

    fun findById(id: Long): Article?

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

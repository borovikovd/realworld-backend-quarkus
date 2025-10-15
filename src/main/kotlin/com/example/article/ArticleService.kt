package com.example.article

import com.example.shared.exceptions.ForbiddenException
import com.example.shared.exceptions.NotFoundException
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class ArticleService {
    @Inject
    lateinit var articleRepository: ArticleRepository

    @Transactional
    fun createArticle(
        userId: Long,
        title: String,
        description: String,
        body: String,
        tags: List<String>,
    ): Article {
        val slug = generateUniqueSlug(title)
        val article =
            Article(
                slug = slug,
                title = title,
                description = description,
                body = body,
                authorId = userId,
                tags = tags.toSet(),
            )
        return articleRepository.create(article)
    }

    @Transactional
    fun updateArticle(
        userId: Long,
        slug: String,
        title: String?,
        description: String?,
        body: String?,
    ): Article {
        val article =
            articleRepository.findBySlug(slug)
                ?: throw NotFoundException("Article not found")

        if (userId != article.authorId) {
            throw ForbiddenException("You can only update your own articles")
        }

        val updatedTitle = if (title != null && title.isNotBlank()) title else article.title
        val updatedDescription =
            if (description != null && description.isNotBlank()) description else article.description
        val updatedBody = if (body != null && body.isNotBlank()) body else article.body

        val updatedSlug =
            if (title != null && title.isNotBlank() && title != article.title) {
                generateUniqueSlug(title, excludeId = article.id)
            } else {
                article.slug
            }

        val updatedArticle = article.update(updatedSlug, updatedTitle, updatedDescription, updatedBody)
        return articleRepository.update(updatedArticle)
    }

    @Transactional
    fun deleteArticle(
        userId: Long,
        slug: String,
    ) {
        val article =
            articleRepository.findBySlug(slug)
                ?: throw NotFoundException("Article not found")

        if (!article.canBeDeletedBy(userId)) {
            throw ForbiddenException("You can only delete your own articles")
        }

        articleRepository.deleteById(article.id!!)
    }

    @Transactional
    fun favoriteArticle(
        userId: Long,
        slug: String,
    ) {
        val article =
            articleRepository.findBySlug(slug)
                ?: throw NotFoundException("Article not found")

        articleRepository.favorite(article.id!!, userId)
    }

    @Transactional
    fun unfavoriteArticle(
        userId: Long,
        slug: String,
    ) {
        val article =
            articleRepository.findBySlug(slug)
                ?: throw NotFoundException("Article not found")

        articleRepository.unfavorite(article.id!!, userId)
    }

    fun getArticle(slug: String): Article =
        articleRepository.findBySlug(slug)
            ?: throw NotFoundException("Article not found")

    fun getAllTags(): List<String> = articleRepository.getAllTags()

    private fun generateUniqueSlug(
        title: String,
        excludeId: Long? = null,
    ): String {
        val baseSlug = generateBaseSlug(title)
        var candidateSlug = baseSlug
        var counter = 2

        while (true) {
            val existing = articleRepository.findBySlug(candidateSlug)
            if (existing == null || existing.id == excludeId) {
                return candidateSlug
            }
            candidateSlug = "$baseSlug-$counter"
            counter++
        }
    }

    private fun generateBaseSlug(title: String): String {
        val normalized = java.text.Normalizer.normalize(title, java.text.Normalizer.Form.NFD)
        return normalized
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
            .replace("[^a-z0-9\\s-]".toRegex(), "")
            .trim()
            .replace("\\s+".toRegex(), "-")
            .replace("-+".toRegex(), "-")
    }
}

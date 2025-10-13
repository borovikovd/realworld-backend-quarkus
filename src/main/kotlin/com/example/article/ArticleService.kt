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
        val article =
            Article.create(
                title = title,
                description = description,
                body = body,
                authorId = userId,
                tags = tags,
            )
        return articleRepository.save(article)
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

        article.update(userId, title, description, body)
        return articleRepository.save(article)
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
}

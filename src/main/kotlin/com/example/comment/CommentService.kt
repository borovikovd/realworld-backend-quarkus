package com.example.comment

import com.example.article.ArticleRepository
import com.example.shared.exceptions.NotFoundException
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class CommentService {
    @Inject
    lateinit var commentRepository: CommentRepository

    @Inject
    lateinit var articleRepository: ArticleRepository

    @Transactional
    fun addComment(
        userId: Long,
        articleSlug: String,
        body: String,
    ): Comment {
        val article =
            articleRepository.findBySlug(articleSlug)
                ?: throw NotFoundException("Article not found")

        val comment =
            Comment(
                articleId = article.id!!,
                authorId = userId,
                body = body,
            )
        return commentRepository.create(comment)
    }

    fun getComments(articleSlug: String): List<Comment> {
        val article =
            articleRepository.findBySlug(articleSlug)
                ?: throw NotFoundException("Article not found")

        return commentRepository.findByArticleId(article.id!!)
    }

    @Transactional
    fun deleteComment(
        userId: Long,
        articleSlug: String,
        commentId: Long,
    ) {
        val article =
            articleRepository.findBySlug(articleSlug)
                ?: throw NotFoundException("Article not found")

        val comment =
            commentRepository.findById(commentId)
                ?: throw NotFoundException("Comment not found")

        if (comment.articleId != article.id) {
            throw NotFoundException("Comment not found for this article")
        }

        comment.ensureCanBeDeletedBy(userId)
        commentRepository.deleteById(commentId)
    }
}

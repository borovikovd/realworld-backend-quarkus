package com.example.comment

interface CommentRepository {
    fun save(comment: Comment): Comment

    fun findById(id: Long): Comment?

    fun findByArticleId(articleId: Long): List<Comment>

    fun deleteById(id: Long)
}

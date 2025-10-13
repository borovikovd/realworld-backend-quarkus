package com.example.comment

import com.example.api.CommentsApi
import com.example.api.model.CreateArticleComment200Response
import com.example.api.model.CreateArticleCommentRequest
import com.example.api.model.GetArticleComments200Response
import com.example.api.model.Profile
import com.example.shared.security.SecurityContext
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import com.example.api.model.Comment as CommentDto

@ApplicationScoped
class CommentResource : CommentsApi {
    @Inject
    lateinit var commentService: CommentService

    @Inject
    lateinit var securityContext: SecurityContext

    @RolesAllowed("**")
    override fun createArticleComment(
        slug: String,
        comment: CreateArticleCommentRequest,
    ): Response {
        val userId = securityContext.currentUserId!!
        val newComment = comment.comment
        val createdComment = commentService.addComment(userId, slug, newComment.body)

        return Response
            .ok(
                CreateArticleComment200Response().comment(
                    CommentDto()
                        .id(createdComment.id?.toInt())
                        .createdAt(createdComment.createdAt)
                        .updatedAt(createdComment.updatedAt)
                        .body(createdComment.body)
                        .author(
                            Profile()
                                .username("")
                                .bio(null)
                                .image(null)
                                .following(false),
                        ),
                ),
            ).build()
    }

    @RolesAllowed("**")
    override fun deleteArticleComment(
        slug: String,
        id: Int,
    ): Response {
        val userId = securityContext.currentUserId!!
        commentService.deleteComment(userId, slug, id.toLong())

        return Response.ok().build()
    }

    override fun getArticleComments(slug: String): Response {
        val comments = commentService.getComments(slug)

        val commentDtos =
            comments.map { comment ->
                CommentDto()
                    .id(comment.id?.toInt())
                    .createdAt(comment.createdAt)
                    .updatedAt(comment.updatedAt)
                    .body(comment.body)
                    .author(
                        Profile()
                            .username("")
                            .bio(null)
                            .image(null)
                            .following(false),
                    )
            }

        return Response
            .ok(
                GetArticleComments200Response().comments(commentDtos),
            ).build()
    }
}

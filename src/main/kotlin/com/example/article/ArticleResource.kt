package com.example.article

import com.example.api.ArticlesApi
import com.example.api.model.CreateArticle201Response
import com.example.api.model.CreateArticleRequest
import com.example.api.model.GetArticlesFeed200Response
import com.example.api.model.Profile
import com.example.api.model.UpdateArticleRequest
import com.example.shared.security.SecurityContext
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import java.time.ZoneOffset
import com.example.api.model.Article as ArticleDto

@ApplicationScoped
class ArticleResource : ArticlesApi {
    @Inject
    lateinit var articleService: ArticleService

    @Inject
    lateinit var securityContext: SecurityContext

    @RolesAllowed("**")
    override fun createArticle(article: CreateArticleRequest): Response {
        val userId = securityContext.currentUserId!!
        val newArticle = article.article

        val created =
            articleService.createArticle(
                userId = userId,
                title = newArticle.title,
                description = newArticle.description,
                body = newArticle.body,
                tags = newArticle.tagList ?: emptyList(),
            )

        return Response
            .status(Response.Status.CREATED)
            .entity(
                CreateArticle201Response().article(
                    ArticleDto()
                        .slug(created.slug)
                        .title(created.title)
                        .description(created.description)
                        .body(created.body)
                        .tagList(created.tags.toList())
                        .createdAt(created.createdAt.atOffset(ZoneOffset.UTC))
                        .updatedAt(created.updatedAt.atOffset(ZoneOffset.UTC))
                        .favorited(false)
                        .favoritesCount(0)
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
    override fun deleteArticle(slug: String): Response {
        val userId = securityContext.currentUserId!!
        articleService.deleteArticle(userId, slug)

        return Response.ok().build()
    }

    override fun getArticle(slug: String): Response {
        val article = articleService.getArticle(slug)

        return Response
            .ok(
                CreateArticle201Response().article(
                    ArticleDto()
                        .slug(article.slug)
                        .title(article.title)
                        .description(article.description)
                        .body(article.body)
                        .tagList(article.tags.toList())
                        .createdAt(article.createdAt.atOffset(ZoneOffset.UTC))
                        .updatedAt(article.updatedAt.atOffset(ZoneOffset.UTC))
                        .favorited(false)
                        .favoritesCount(0)
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

    override fun getArticles(
        tag: String?,
        author: String?,
        favorited: String?,
        offset: Int?,
        limit: Int?,
    ): Response =
        Response
            .ok(
                GetArticlesFeed200Response()
                    .articles(emptyList())
                    .articlesCount(0),
            ).build()

    @RolesAllowed("**")
    override fun getArticlesFeed(
        offset: Int?,
        limit: Int?,
    ): Response =
        Response
            .ok(
                GetArticlesFeed200Response()
                    .articles(emptyList())
                    .articlesCount(0),
            ).build()

    @RolesAllowed("**")
    override fun updateArticle(
        slug: String,
        article: UpdateArticleRequest,
    ): Response {
        val userId = securityContext.currentUserId!!
        val updateData = article.article

        val updated =
            articleService.updateArticle(
                userId = userId,
                slug = slug,
                title = updateData.title,
                description = updateData.description,
                body = updateData.body,
            )

        return Response
            .ok(
                CreateArticle201Response().article(
                    ArticleDto()
                        .slug(updated.slug)
                        .title(updated.title)
                        .description(updated.description)
                        .body(updated.body)
                        .tagList(updated.tags.toList())
                        .createdAt(updated.createdAt.atOffset(ZoneOffset.UTC))
                        .updatedAt(updated.updatedAt.atOffset(ZoneOffset.UTC))
                        .favorited(false)
                        .favoritesCount(0)
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
}

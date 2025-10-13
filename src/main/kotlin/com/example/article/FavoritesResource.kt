package com.example.article

import com.example.api.FavoritesApi
import com.example.api.model.CreateArticle201Response
import com.example.api.model.Profile
import com.example.shared.security.SecurityContext
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import com.example.api.model.Article as ArticleDto

@ApplicationScoped
class FavoritesResource : FavoritesApi {
    @Inject
    lateinit var articleService: ArticleService

    @Inject
    lateinit var securityContext: SecurityContext

    @RolesAllowed("**")
    override fun createArticleFavorite(slug: String): Response {
        val userId = securityContext.currentUserId!!
        articleService.favoriteArticle(userId, slug)

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
                        .createdAt(article.createdAt)
                        .updatedAt(article.updatedAt)
                        .favorited(true)
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
    override fun deleteArticleFavorite(slug: String): Response {
        val userId = securityContext.currentUserId!!
        articleService.unfavoriteArticle(userId, slug)

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
                        .createdAt(article.createdAt)
                        .updatedAt(article.updatedAt)
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

package com.example.article

import com.example.api.model.Profile
import com.example.jooq.public.tables.references.ARTICLES
import com.example.jooq.public.tables.references.ARTICLE_TAGS
import com.example.jooq.public.tables.references.FAVORITES
import com.example.jooq.public.tables.references.FOLLOWERS
import com.example.jooq.public.tables.references.TAGS
import com.example.jooq.public.tables.references.USERS
import com.example.shared.exceptions.NotFoundException
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.Condition
import org.jooq.DSLContext
import com.example.api.model.Article as ApiArticle

@ApplicationScoped
class ArticleQueryService {
    @Inject
    lateinit var dsl: DSLContext

    fun getArticleBySlug(
        slug: String,
        viewerId: Long? = null,
    ): ApiArticle {
        val articleRecord =
            dsl
                .select(
                    ARTICLES.ID,
                    ARTICLES.SLUG,
                    ARTICLES.TITLE,
                    ARTICLES.DESCRIPTION,
                    ARTICLES.BODY,
                    ARTICLES.AUTHOR_ID,
                    ARTICLES.CREATED_AT,
                    ARTICLES.UPDATED_AT,
                    USERS.USERNAME,
                    USERS.BIO,
                    USERS.IMAGE,
                ).from(ARTICLES)
                .join(USERS)
                .on(USERS.ID.eq(ARTICLES.AUTHOR_ID))
                .where(ARTICLES.SLUG.eq(slug))
                .fetchOne() ?: throw NotFoundException("Article not found")

        val articleId = articleRecord.get(ARTICLES.ID)!!
        val authorId = articleRecord.get(ARTICLES.AUTHOR_ID)!!

        val tags = loadTags(articleId)
        val favoritesCount = countFavorites(articleId)
        val favorited = viewerId?.let { isFavorited(articleId, it) } ?: false
        val following = viewerId?.let { isFollowing(authorId, it) } ?: false

        return ApiArticle()
            .slug(articleRecord.get(ARTICLES.SLUG))
            .title(articleRecord.get(ARTICLES.TITLE))
            .description(articleRecord.get(ARTICLES.DESCRIPTION))
            .body(articleRecord.get(ARTICLES.BODY))
            .tagList(tags)
            .createdAt(articleRecord.get(ARTICLES.CREATED_AT))
            .updatedAt(articleRecord.get(ARTICLES.UPDATED_AT))
            .favorited(favorited)
            .favoritesCount(favoritesCount)
            .author(
                Profile()
                    .username(articleRecord.get(USERS.USERNAME))
                    .bio(articleRecord.get(USERS.BIO))
                    .image(articleRecord.get(USERS.IMAGE))
                    .following(following),
            )
    }

    private fun loadTags(articleId: Long): List<String> =
        dsl
            .select(TAGS.NAME)
            .from(TAGS)
            .join(ARTICLE_TAGS)
            .on(ARTICLE_TAGS.TAG_ID.eq(TAGS.ID))
            .where(ARTICLE_TAGS.ARTICLE_ID.eq(articleId))
            .fetch()
            .mapNotNull { it.get(TAGS.NAME) }

    private fun countFavorites(articleId: Long): Int =
        dsl
            .selectCount()
            .from(FAVORITES)
            .where(FAVORITES.ARTICLE_ID.eq(articleId))
            .fetchOne(0, Int::class.java) ?: 0

    private fun isFavorited(
        articleId: Long,
        userId: Long,
    ): Boolean =
        dsl.fetchExists(
            dsl
                .selectFrom(FAVORITES)
                .where(FAVORITES.ARTICLE_ID.eq(articleId))
                .and(FAVORITES.USER_ID.eq(userId)),
        )

    private fun isFollowing(
        followeeId: Long,
        followerId: Long,
    ): Boolean =
        dsl.fetchExists(
            dsl
                .selectFrom(FOLLOWERS)
                .where(FOLLOWERS.FOLLOWEE_ID.eq(followeeId))
                .and(FOLLOWERS.FOLLOWER_ID.eq(followerId)),
        )

    fun getArticles(
        tag: String? = null,
        author: String? = null,
        favorited: String? = null,
        limit: Int = 20,
        offset: Int = 0,
        viewerId: Long? = null,
    ): List<ApiArticle> {
        val conditions = buildConditions(tag, author, favorited)

        val articleRecords =
            dsl
                .select(
                    ARTICLES.ID,
                    ARTICLES.SLUG,
                    ARTICLES.TITLE,
                    ARTICLES.DESCRIPTION,
                    ARTICLES.BODY,
                    ARTICLES.AUTHOR_ID,
                    ARTICLES.CREATED_AT,
                    ARTICLES.UPDATED_AT,
                    USERS.USERNAME,
                    USERS.BIO,
                    USERS.IMAGE,
                ).from(ARTICLES)
                .join(USERS)
                .on(USERS.ID.eq(ARTICLES.AUTHOR_ID))
                .where(conditions)
                .orderBy(ARTICLES.CREATED_AT.desc())
                .limit(limit)
                .offset(offset)
                .fetch()

        return articleRecords.map { record -> mapToApiArticle(record, viewerId) }
    }

    private fun buildConditions(
        tag: String?,
        author: String?,
        favorited: String?,
    ): List<Condition> {
        val conditions = mutableListOf<Condition>()

        tag?.let {
            conditions.add(
                ARTICLES.ID.`in`(
                    dsl
                        .select(ARTICLE_TAGS.ARTICLE_ID)
                        .from(ARTICLE_TAGS)
                        .join(TAGS)
                        .on(TAGS.ID.eq(ARTICLE_TAGS.TAG_ID))
                        .where(TAGS.NAME.eq(it)),
                ),
            )
        }

        author?.let {
            conditions.add(
                ARTICLES.AUTHOR_ID.`in`(
                    dsl
                        .select(USERS.ID)
                        .from(USERS)
                        .where(USERS.USERNAME.eq(it)),
                ),
            )
        }

        favorited?.let {
            conditions.add(
                ARTICLES.ID.`in`(
                    dsl
                        .select(FAVORITES.ARTICLE_ID)
                        .from(FAVORITES)
                        .join(USERS)
                        .on(USERS.ID.eq(FAVORITES.USER_ID))
                        .where(USERS.USERNAME.eq(it)),
                ),
            )
        }

        return conditions
    }

    private fun mapToApiArticle(
        record: org.jooq.Record,
        viewerId: Long?,
    ): ApiArticle {
        val articleId = record.get(ARTICLES.ID)!!
        val authorId = record.get(ARTICLES.AUTHOR_ID)!!

        val tags = loadTags(articleId)
        val favoritesCount = countFavorites(articleId)
        val favorited = viewerId?.let { isFavorited(articleId, it) } ?: false
        val following = viewerId?.let { isFollowing(authorId, it) } ?: false

        return ApiArticle()
            .slug(record.get(ARTICLES.SLUG))
            .title(record.get(ARTICLES.TITLE))
            .description(record.get(ARTICLES.DESCRIPTION))
            .body(record.get(ARTICLES.BODY))
            .tagList(tags)
            .createdAt(record.get(ARTICLES.CREATED_AT))
            .updatedAt(record.get(ARTICLES.UPDATED_AT))
            .favorited(favorited)
            .favoritesCount(favoritesCount)
            .author(
                Profile()
                    .username(record.get(USERS.USERNAME))
                    .bio(record.get(USERS.BIO))
                    .image(record.get(USERS.IMAGE))
                    .following(following),
            )
    }

    fun getArticlesFeed(
        limit: Int = 20,
        offset: Int = 0,
        viewerId: Long,
    ): List<ApiArticle> {
        val articleRecords =
            dsl
                .select(
                    ARTICLES.ID,
                    ARTICLES.SLUG,
                    ARTICLES.TITLE,
                    ARTICLES.DESCRIPTION,
                    ARTICLES.BODY,
                    ARTICLES.AUTHOR_ID,
                    ARTICLES.CREATED_AT,
                    ARTICLES.UPDATED_AT,
                    USERS.USERNAME,
                    USERS.BIO,
                    USERS.IMAGE,
                ).from(ARTICLES)
                .join(USERS)
                .on(USERS.ID.eq(ARTICLES.AUTHOR_ID))
                .where(
                    ARTICLES.AUTHOR_ID.`in`(
                        dsl
                            .select(FOLLOWERS.FOLLOWEE_ID)
                            .from(FOLLOWERS)
                            .where(FOLLOWERS.FOLLOWER_ID.eq(viewerId)),
                    ),
                ).orderBy(ARTICLES.CREATED_AT.desc())
                .limit(limit)
                .offset(offset)
                .fetch()

        return articleRecords.map { record -> mapToApiArticle(record, viewerId) }
    }
}

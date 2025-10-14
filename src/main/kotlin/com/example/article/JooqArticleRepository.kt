package com.example.article

import com.example.jooq.public.tables.references.ARTICLES
import com.example.jooq.public.tables.references.ARTICLE_TAGS
import com.example.jooq.public.tables.references.FAVORITES
import com.example.jooq.public.tables.references.TAGS
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext

@ApplicationScoped
class JooqArticleRepository : ArticleRepository {
    @Inject
    lateinit var dsl: DSLContext

    override fun save(article: Article): Article =
        if (article.id == null) {
            insert(article)
        } else {
            update(article)
        }

    private fun insert(article: Article): Article {
        val record =
            dsl
                .insertInto(ARTICLES)
                .set(ARTICLES.SLUG, article.slug)
                .set(ARTICLES.TITLE, article.title)
                .set(ARTICLES.DESCRIPTION, article.description)
                .set(ARTICLES.BODY, article.body)
                .set(ARTICLES.AUTHOR_ID, article.authorId)
                .set(ARTICLES.CREATED_AT, article.createdAt)
                .set(ARTICLES.UPDATED_AT, article.updatedAt)
                .returning()
                .fetchOne() ?: error("Failed to insert article")

        val articleId = record.id!!

        saveTags(articleId, article.tags)

        return Article.reconstitute(
            id = articleId,
            slug = record.slug!!,
            title = record.title!!,
            description = record.description!!,
            body = record.body!!,
            authorId = record.authorId!!,
            tags = article.tags,
            createdAt = record.createdAt!!,
            updatedAt = record.updatedAt!!,
        )
    }

    private fun update(article: Article): Article {
        dsl
            .update(ARTICLES)
            .set(ARTICLES.TITLE, article.title)
            .set(ARTICLES.DESCRIPTION, article.description)
            .set(ARTICLES.BODY, article.body)
            .set(ARTICLES.UPDATED_AT, article.updatedAt)
            .where(ARTICLES.ID.eq(article.id))
            .execute()

        dsl
            .deleteFrom(ARTICLE_TAGS)
            .where(ARTICLE_TAGS.ARTICLE_ID.eq(article.id))
            .execute()

        saveTags(article.id!!, article.tags)

        return article
    }

    private fun saveTags(
        articleId: Long,
        tags: Set<String>,
    ) {
        for (tagName in tags) {
            val tagId =
                dsl
                    .insertInto(TAGS)
                    .set(TAGS.NAME, tagName)
                    .onDuplicateKeyUpdate()
                    .set(TAGS.NAME, tagName)
                    .returning(TAGS.ID)
                    .fetchOne()!!
                    .id!!

            dsl
                .insertInto(ARTICLE_TAGS)
                .set(ARTICLE_TAGS.ARTICLE_ID, articleId)
                .set(ARTICLE_TAGS.TAG_ID, tagId)
                .onDuplicateKeyIgnore()
                .execute()
        }
    }

    override fun findById(id: Long): Article? {
        val record =
            dsl
                .selectFrom(ARTICLES)
                .where(ARTICLES.ID.eq(id))
                .fetchOne() ?: return null

        val tags = loadTags(id)

        return Article.reconstitute(
            id = record.id!!,
            slug = record.slug!!,
            title = record.title!!,
            description = record.description!!,
            body = record.body!!,
            authorId = record.authorId!!,
            tags = tags,
            createdAt = record.createdAt!!,
            updatedAt = record.updatedAt!!,
        )
    }

    override fun findBySlug(slug: String): Article? {
        val record =
            dsl
                .selectFrom(ARTICLES)
                .where(ARTICLES.SLUG.eq(slug))
                .fetchOne() ?: return null

        val tags = loadTags(record.id!!)

        return Article.reconstitute(
            id = record.id!!,
            slug = record.slug!!,
            title = record.title!!,
            description = record.description!!,
            body = record.body!!,
            authorId = record.authorId!!,
            tags = tags,
            createdAt = record.createdAt!!,
            updatedAt = record.updatedAt!!,
        )
    }

    private fun loadTags(articleId: Long): Set<String> =
        dsl
            .select(TAGS.NAME)
            .from(TAGS)
            .join(ARTICLE_TAGS)
            .on(ARTICLE_TAGS.TAG_ID.eq(TAGS.ID))
            .where(ARTICLE_TAGS.ARTICLE_ID.eq(articleId))
            .fetch()
            .mapNotNull { it.value1() }
            .toSet()

    override fun deleteById(id: Long) {
        dsl
            .deleteFrom(ARTICLE_TAGS)
            .where(ARTICLE_TAGS.ARTICLE_ID.eq(id))
            .execute()

        dsl
            .deleteFrom(FAVORITES)
            .where(FAVORITES.ARTICLE_ID.eq(id))
            .execute()

        dsl
            .deleteFrom(ARTICLES)
            .where(ARTICLES.ID.eq(id))
            .execute()
    }

    override fun favorite(
        articleId: Long,
        userId: Long,
    ) {
        dsl
            .insertInto(FAVORITES)
            .set(FAVORITES.ARTICLE_ID, articleId)
            .set(FAVORITES.USER_ID, userId)
            .onDuplicateKeyIgnore()
            .execute()
    }

    override fun unfavorite(
        articleId: Long,
        userId: Long,
    ) {
        dsl
            .deleteFrom(FAVORITES)
            .where(FAVORITES.ARTICLE_ID.eq(articleId))
            .and(FAVORITES.USER_ID.eq(userId))
            .execute()
    }

    override fun isFavorited(
        articleId: Long,
        userId: Long,
    ): Boolean =
        dsl.fetchExists(
            dsl
                .selectFrom(FAVORITES)
                .where(FAVORITES.ARTICLE_ID.eq(articleId))
                .and(FAVORITES.USER_ID.eq(userId)),
        )

    override fun getAllTags(): List<String> =
        dsl
            .select(TAGS.NAME)
            .from(TAGS)
            .orderBy(TAGS.NAME)
            .fetch()
            .mapNotNull { it.value1() }
}

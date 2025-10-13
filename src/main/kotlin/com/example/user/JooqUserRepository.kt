package com.example.user

import com.example.jooq.tables.references.USERS
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext

@ApplicationScoped
class JooqUserRepository : UserRepository {
    @Inject
    lateinit var dsl: DSLContext

    override fun save(user: User): User =
        if (user.id == null) {
            insert(user)
        } else {
            update(user)
        }

    private fun insert(user: User): User {
        val record =
            dsl
                .insertInto(USERS)
                .set(USERS.EMAIL, user.email)
                .set(USERS.USERNAME, user.username)
                .set(USERS.PASSWORD_HASH, user.passwordHash)
                .set(USERS.BIO, user.bio)
                .set(USERS.IMAGE, user.image)
                .set(USERS.CREATED_AT, user.createdAt)
                .set(USERS.UPDATED_AT, user.updatedAt)
                .returning()
                .fetchOne() ?: throw IllegalStateException("Failed to insert user")

        return User.reconstitute(
            id = record.id!!,
            email = record.email!!,
            username = record.username!!,
            passwordHash = record.passwordHash!!,
            bio = record.bio,
            image = record.image,
            createdAt = record.createdAt!!,
            updatedAt = record.updatedAt!!,
        )
    }

    private fun update(user: User): User {
        dsl
            .update(USERS)
            .set(USERS.EMAIL, user.email)
            .set(USERS.USERNAME, user.username)
            .set(USERS.PASSWORD_HASH, user.passwordHash)
            .set(USERS.BIO, user.bio)
            .set(USERS.IMAGE, user.image)
            .set(USERS.UPDATED_AT, user.updatedAt)
            .where(USERS.ID.eq(user.id))
            .execute()
        return user
    }

    override fun findById(id: Long): User? {
        val record =
            dsl
                .selectFrom(USERS)
                .where(USERS.ID.eq(id))
                .fetchOne() ?: return null

        return User.reconstitute(
            id = record.id!!,
            email = record.email!!,
            username = record.username!!,
            passwordHash = record.passwordHash!!,
            bio = record.bio,
            image = record.image,
            createdAt = record.createdAt!!,
            updatedAt = record.updatedAt!!,
        )
    }

    override fun findByEmail(email: String): User? {
        val record =
            dsl
                .selectFrom(USERS)
                .where(USERS.EMAIL.eq(email))
                .fetchOne() ?: return null

        return User.reconstitute(
            id = record.id!!,
            email = record.email!!,
            username = record.username!!,
            passwordHash = record.passwordHash!!,
            bio = record.bio,
            image = record.image,
            createdAt = record.createdAt!!,
            updatedAt = record.updatedAt!!,
        )
    }

    override fun findByUsername(username: String): User? {
        val record =
            dsl
                .selectFrom(USERS)
                .where(USERS.USERNAME.eq(username))
                .fetchOne() ?: return null

        return User.reconstitute(
            id = record.id!!,
            email = record.email!!,
            username = record.username!!,
            passwordHash = record.passwordHash!!,
            bio = record.bio,
            image = record.image,
            createdAt = record.createdAt!!,
            updatedAt = record.updatedAt!!,
        )
    }

    override fun existsByEmail(email: String): Boolean =
        dsl.fetchExists(
            dsl
                .selectFrom(USERS)
                .where(USERS.EMAIL.eq(email)),
        )

    override fun existsByUsername(username: String): Boolean =
        dsl.fetchExists(
            dsl
                .selectFrom(USERS)
                .where(USERS.USERNAME.eq(username)),
        )
}

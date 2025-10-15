package com.example.shared.domain

/**
 * Base repository interface for entities with generic ID type.
 *
 * @param T The entity type
 * @param ID The identifier type
 */
interface Repository<T : Entity<ID>, ID> {
    /**
     * Creates a new entity in the repository.
     * The entity's ID must be null.
     *
     * @param entity The entity to create
     * @return The created entity with its assigned ID
     * @throws IllegalArgumentException if the entity already has an ID
     */
    fun create(entity: T): T

    /**
     * Updates an existing entity in the repository.
     * The entity's ID must not be null.
     *
     * @param entity The entity to update
     * @return The updated entity
     * @throws IllegalArgumentException if the entity doesn't have an ID
     */
    fun update(entity: T): T

    /**
     * Finds an entity by its ID.
     *
     * @param id The entity's ID
     * @return The entity if found, null otherwise
     */
    fun findById(id: ID): T?
}

package com.example.shared.domain

/**
 * Base interface for all domain entities with a generic ID type.
 *
 * @param ID The type of the entity's identifier (e.g., Long, UUID)
 */
interface Entity<ID> {
    /**
     * The entity's unique identifier.
     * Null for new entities that haven't been persisted yet.
     */
    val id: ID?

    /**
     * Returns a copy of this entity with the given ID.
     * Typically used by repositories after inserting a new entity.
     *
     * @param newId The ID to assign to the entity
     * @return A new instance of this entity with the ID set
     */
    fun withId(newId: ID): Entity<ID>
}

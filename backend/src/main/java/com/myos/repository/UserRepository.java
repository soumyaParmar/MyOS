package com.myos.repository;

import com.myos.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User database operations.
 *
 * WHAT IS A REPOSITORY?
 * In the controller → service → repository architecture:
 * - Controllers handle HTTP requests/responses
 * - Services contain business logic
 * - Repositories handle database queries (persistence layer)
 *
 * WHAT IS JpaRepository?
 * JpaRepository is a Spring Data interface that gives you CRUD operations for free:
 *   - save(entity)       → INSERT or UPDATE
 *   - findById(id)       → SELECT by primary key
 *   - findAll()          → SELECT all rows
 *   - deleteById(id)     → DELETE
 *   - count()            → COUNT rows
 *   ... and many more.
 *
 * You just declare the interface — Spring generates the implementation at runtime!
 * The type parameters are: JpaRepository<EntityType, PrimaryKeyType>
 *   → JpaRepository<User, UUID> means "this repo manages User entities with UUID keys"
 *
 * WHAT IS @Repository?
 * Marks this interface as a Spring-managed bean in the persistence layer.
 * It also enables automatic exception translation — database-specific exceptions
 * (like PostgreSQL errors) are converted to Spring's DataAccessException hierarchy.
 *
 * WHAT IS Optional?
 * Optional<User> means "this might return a User or might be empty."
 * It forces the caller to handle the "not found" case explicitly instead of
 * risking a NullPointerException. Example:
 *   userRepository.findByEmailHash(hash).orElseThrow(() -> new NotFoundException("..."));
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their email address.
     *
     * HOW DOES THIS WORK WITHOUT WRITING SQL?
     * Spring Data JPA uses "query derivation" — it parses the method name and
     * generates the SQL automatically:
     *   findByEmail(String email)
     *   → SELECT * FROM users WHERE email = ?
     *
     * The naming convention is: findBy + FieldName (in camelCase).
     * Spring matches "Email" to the "email" field in the User entity.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email.
     *
     * existsBy + FieldName → SELECT COUNT(*) > 0 FROM users WHERE email = ?
     * Returns true/false instead of loading the full entity (more efficient for checks).
     */
    boolean existsByEmail(String email);
}

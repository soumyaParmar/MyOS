package com.myos.repository;

import com.myos.entity.AiModelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * @Repository registers this interface as a data repository bean in the Spring Container.
 * It provides database interaction methods and automatically translates database exceptions
 * into Spring's portable DataAccessException hierarchy.
 *
 * We extend JpaRepository<AiModelConfig, UUID> which automatically generates CRUD methods
 * (findAll, findById, save, deleteById, count) without writing a single line of implementation!
 */
@Repository
public interface AiModelConfigRepository extends JpaRepository<AiModelConfig, UUID> {

    /**
     * Spring Data Query Method: Automatically derived from the method signature.
     * Searches for a configuration record where is_active is true.
     *
     * @return Optional containing the active configuration, or empty if none.
     */
    Optional<AiModelConfig> findByIsActiveTrue();

    /**
     * Custom JPQL Query: Used to count configurations safely.
     * JPQL (Java Persistence Query Language) queries against entities (AiModelConfig)
     * rather than raw sql tables, making it database-agnostic.
     */
    @Query("SELECT COUNT(a) FROM AiModelConfig a")
    long countAll();
}

package com.myos.repository;

import com.myos.entity.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserPreferences.
 * 
 * LEARNING NOTE:
 * By extending JpaRepository, Spring Data JPA automatically provides 
 * standard CRUD methods (save, findById, delete, etc.) without us writing any SQL.
 */
@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, UUID> {

    /**
     * Finds preferences by the associated user's ID.
     * @param userId The ID of the user.
     * @return An Optional containing the preferences if found.
     */
    Optional<UserPreferences> findByUserId(UUID userId);
}

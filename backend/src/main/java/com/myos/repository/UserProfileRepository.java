package com.myos.repository;

import com.myos.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserProfile entity.
 * 
 * WHAT IS @Repository?
 * Marks this interface as a Spring-managed bean that provides data access logic.
 * It's a "Marker Annotation" that also enables automatic exception translation 
 * (converting DB errors into Spring's DataAccessException).
 * 
 * WHAT IS JpaRepository?
 * By extending JpaRepository, we get all basic CRUD operations (save, findById, 
 * delete, etc.) without writing a single line of implementation code.
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    /**
     * Finds a profile by the associated User's ID.
     * 
     * WHY Optional?
     * It's possible a user doesn't have a profile yet (though we might create one
     * automatically later). Optional prevents NullPointerException and forces the
     * caller to handle the "not found" case.
     */
    Optional<UserProfile> findByUserId(UUID userId);
}

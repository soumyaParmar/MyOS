package com.myos.repository;

import com.myos.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Token database operations.
 *
 * NOTE: No @Repository annotation here — it still works! @Repository is optional
 * for interfaces extending JpaRepository because Spring Data auto-detects them
 * during component scanning. We include it on UserRepository for clarity, but
 * omitting it is perfectly valid.
 */
public interface TokenRepository extends JpaRepository<Token, Integer> {

    /**
     * Finds all valid (non-expired and non-revoked) tokens for a given user.
     *
     * WHAT IS @Query?
     * When the method name gets too complex for query derivation, you can write
     * custom JPQL (Java Persistence Query Language) using @Query.
     *
     * JPQL vs SQL:
     * - SQL operates on tables and columns: SELECT * FROM token WHERE ...
     * - JPQL operates on entities and fields: SELECT t FROM Token t WHERE ...
     *
     * This query joins Token with User and finds tokens that are either
     * not expired OR not revoked (i.e., still potentially valid).
     *
     * The ":id" is a named parameter that maps to the method parameter "id".
     *
     * TEXT BLOCKS (triple quotes):
     * Java 15+ feature — lets you write multi-line strings without concatenation.
     * The \s at the end of lines is an escape that preserves a trailing space.
     */
    @Query(value = """
      select t from Token t inner join User u\s
      on t.user.id = u.id\s
      where u.id = :id and (t.expired = false or t.revoked = false)\s
      """)
    List<Token> findAllValidTokenByUser(UUID id);

    /**
     * Finds a token by its JWT string value.
     * Spring Data derives: SELECT * FROM token WHERE token = ?
     */
    Optional<Token> findByToken(String token);
}

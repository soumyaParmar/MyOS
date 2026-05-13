package com.myos.service;

import com.myos.entity.Token;
import com.myos.entity.TokenType;
import com.myos.entity.User;
import com.myos.repository.TokenRepository;
import org.springframework.stereotype.Service;

/**
 * Service for managing JWT tokens in the database.
 *
 * WHY STORE TOKENS IN THE DATABASE?
 * JWTs are normally stateless — the server doesn't track them. But we need to:
 *   1. Revoke tokens on logout (mark them as revoked)
 *   2. Revoke old tokens when issuing new ones (token rotation)
 *   3. Detect reuse of revoked tokens (security breach indicator)
 *
 * @Service — Registers this class as a Spring-managed service bean.
 */
@Service
public class TokenService {

    private final TokenRepository tokenRepository;

    /**
     * Constructor injection — Spring provides the TokenRepository bean.
     * Since there's only one constructor, @Autowired is implicit (Spring infers it).
     */
    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Saves a new JWT token to the database, linked to a user.
     *
     * Uses the Builder pattern for readable object construction:
     *   Token.builder().user(user).token(jwtToken)... .build()
     *
     * The "var" keyword (Java 10+) lets the compiler infer the type automatically.
     * var token = ... is equivalent to Token token = ...
     */
    public void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)                     // Which user owns this token
                .token(jwtToken)                // The actual JWT string
                .tokenType(TokenType.BEARER)    // Token type (Bearer)
                .expired(false)                 // Not expired yet
                .revoked(false)                 // Not revoked yet
                .build();
        tokenRepository.save(token); // Persists to database (INSERT)
    }

    /**
     * Revokes all existing valid tokens for a user.
     *
     * Called during:
     *   - Login (invalidate old tokens)
     *   - Token refresh (invalidate the old access token)
     *   - Logout (invalidate everything)
     *
     * forEach() — Iterates over each token and sets expired + revoked = true.
     * saveAll() — Batch updates all modified tokens in one database call (efficient).
     */
    public void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return; // Nothing to revoke
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens); // Batch UPDATE
    }
}

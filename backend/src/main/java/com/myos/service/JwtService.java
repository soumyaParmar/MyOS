package com.myos.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for creating and validating JWT (JSON Web Token) tokens.
 *
 * WHAT IS @Service?
 * A specialization of @Component that indicates this class contains business logic.
 * Spring registers it as a bean so it can be injected into other classes.
 * Functionally identical to @Component, but communicates intent:
 *   - @Component → generic bean
 *   - @Service → business logic
 *   - @Repository → data access
 *   - @Controller → HTTP handling
 *
 * WHAT IS JWT?
 * A JWT is a compact, URL-safe token format used for authentication.
 * It has three parts separated by dots: HEADER.PAYLOAD.SIGNATURE
 *   - Header: Algorithm used (HS256) and token type (JWT)
 *   - Payload: "Claims" — data like username, expiration time, roles
 *   - Signature: HMAC of header+payload using our secret key (proves the token wasn't tampered)
 *
 * Example: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0.abc123signature
 */
@Service
public class JwtService {

    /** Secret key for signing JWTs, loaded from application.yml → .env */
    @Value("${app.jwt.secret}")
    private String secretKey;

    /** Access token expiration time in milliseconds (e.g., 3600000 = 1 hour). */
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    /** Refresh token expiration time in milliseconds (e.g., 604800000 = 7 days). */
    @Value("${app.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    /**
     * Extracts the username (email) from a JWT token.
     *
     * In JWT, the "subject" claim (sub) stores the user's identity.
     * We store the email as the subject when creating the token.
     *
     * Claims::getSubject — A method reference. Equivalent to: claims -> claims.getSubject()
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generic method to extract any claim from a JWT.
     *
     * WHAT ARE GENERICS (<T>)?
     * The <T> means this method can return ANY type. The caller specifies what
     * type they want by passing a Function that extracts it from Claims.
     *
     * Function<Claims, T> — A functional interface representing a function that
     * takes Claims as input and returns T as output.
     *
     * Examples:
     *   extractClaim(token, Claims::getSubject)     → returns String (the subject)
     *   extractClaim(token, Claims::getExpiration)   → returns Date (the expiration)
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims); // Apply the function to get the specific claim
    }

    /**
     * Generates an access token for the user (no extra claims).
     *
     * METHOD OVERLOADING: Two methods with the same name but different parameters.
     * Java decides which one to call based on the arguments you pass.
     * This version is a convenience wrapper that calls the other with empty claims.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates an access token with custom claims (e.g., roles, permissions).
     *
     * @param extraClaims additional data to include in the token payload
     * @param userDetails the authenticated user
     * @return the signed JWT string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Generates a refresh token — a long-lived token used to get new access tokens.
     * Refresh tokens have a longer expiration (7 days) compared to access tokens (1 hour).
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    /** Returns the configured access token expiration time. */
    public long getExpirationTime() {
        return jwtExpiration;
    }

    /**
     * Builds and signs a JWT token.
     *
     * This is the core token creation method. It:
     * 1. Adds a random JTI (JWT ID) for uniqueness — prevents token replay attacks
     * 2. Sets the subject (user's email)
     * 3. Sets issued-at and expiration timestamps
     * 4. Signs the token with our HMAC-SHA256 secret key
     * 5. Compacts everything into a dot-separated string
     *
     * private — Only accessible within this class. External callers use
     * generateToken() or generateRefreshToken() instead.
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        // JTI (JWT ID) — a unique identifier for each token, prevents replay attacks
        extraClaims.put("jti", java.util.UUID.randomUUID().toString());
        return Jwts
                .builder()
                .claims(extraClaims)                                         // Custom claims (roles, etc.)
                .subject(userDetails.getUsername())                           // Who this token belongs to
                .issuedAt(new Date(System.currentTimeMillis()))               // When the token was created
                .expiration(new Date(System.currentTimeMillis() + expiration)) // When it expires
                .signWith(getSignInKey(), Jwts.SIG.HS256)                     // Sign with HMAC-SHA256
                .compact();                                                   // Build the final JWT string
    }

    /**
     * Validates a token against a user.
     *
     * Two checks:
     * 1. Does the username in the token match the user's email?
     * 2. Has the token expired?
     *
     * Note: The signature is verified implicitly in extractAllClaims() — if the
     * signature is invalid, Jwts.parser() throws a SignatureException.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /** Checks if a token's expiration date is in the past. */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /** Extracts the expiration date from the token's claims. */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parses and validates a JWT, returning all its claims.
     *
     * This method:
     * 1. Verifies the signature using our secret key (rejects tampered tokens)
     * 2. Checks expiration (rejects expired tokens)
     * 3. Returns the payload (claims) if everything is valid
     *
     * If the token is invalid, this throws a JwtException (caught by the filter).
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()                     // Create a JWT parser
                .verifyWith(getSignInKey())    // Set the key to verify the signature
                .build()                      // Build the parser
                .parseSignedClaims(token)     // Parse and validate the token
                .getPayload();                // Extract the claims (payload)
    }

    /**
     * Converts the Base64-encoded secret key string into a SecretKey object.
     *
     * The secretKey is stored as a Base64 string in application.yml.
     * We decode it to raw bytes and create an HMAC key for JWT signing.
     *
     * HMAC (Hash-based Message Authentication Code) — Uses a secret key + hash function
     * to create a signature that proves the data wasn't modified.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

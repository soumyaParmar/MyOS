package com.myos.service;

import com.myos.security.EncryptionUtil;

import com.myos.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation of Spring Security's UserDetailsService interface.
 *
 * WHAT IS UserDetailsService?
 * It's a core Spring Security interface with one method: loadUserByUsername(String).
 * Spring Security calls this whenever it needs to look up a user during authentication.
 *
 * THE AUTHENTICATION FLOW:
 * 1. User sends email + password to /api/auth/login
 * 2. Spring Security's AuthenticationManager calls our loadUserByUsername(email)
 * 3. This method looks up the user in the DB and returns the UserDetails object
 * 4. Spring Security compares the submitted password with the stored hash
 * 5. If they match → authentication succeeds
 *
 * Since our User entity implements UserDetails, we return the User directly.
 *
 * @Service — Makes this a Spring bean. Spring Security auto-detects it because
 * it implements the UserDetailsService interface.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /** Constructor injection — Spring injects the UserRepository bean. */
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by their email address (used as the "username" in our system).
     *
     * @Override — This method implements the UserDetailsService interface method.
     * The compiler verifies the method signature matches the interface.
     *
     * WHY DO WE HASH THE EMAIL?
     * The email column is encrypted with AES-256-GCM (non-deterministic encryption).
     * We can't search by encrypted email directly because the same email encrypts
     * differently each time. Instead, we compute a SHA-256 hash of the email
     * and search by the deterministic hash column (email_hash).
     *
     * @param email the user's email address (plaintext)
     * @return UserDetails — our User entity (which implements UserDetails)
     * @throws UsernameNotFoundException if no user is found with this email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Step 1: Hash the email for database lookup
        String emailHash = EncryptionUtil.hashForLookup(email);
        // Step 2: Search by the deterministic hash (not the encrypted email)
        return userRepository.findByEmailHash(emailHash)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}

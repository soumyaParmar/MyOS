package com.myos.security;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.myos.service.RateLimitingService;

import java.io.IOException;

/**
 * Filter that enforces rate limiting on all incoming HTTP requests.
 *
 * WHAT IS A FILTER?
 * A component that intercepts every HTTP request before it reaches the controller.
 * It's like a checkpoint. We use OncePerRequestFilter to ensure it only runs once per request.
 *
 * HOW THIS WORKS:
 * 1. Identifies the client (by IP address).
 * 2. Determines if the target is an "Auth" endpoint or a "General" endpoint.
 * 3. Asks RateLimitingService for the corresponding bucket.
 * 4. Tries to "consume" 1 token.
 * 5. If successful → continues to the next filter in the chain.
 * 6. If failed (empty bucket) → returns 429 Too Many Requests immediately.
 *
 * @Component — Registers this filter as a Spring bean.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;

    public RateLimitFilter(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    /**
     * The core logic of the filter.
     *
     * @param request the incoming HTTP request
     * @param response the HTTP response we are building
     * @param filterChain the chain of other filters to call if rate limit isn't hit
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Step 1: Identify the client IP address.
        // We check "X-Forwarded-For" first in case the app is behind a proxy/load balancer.
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }

        // Step 2: Determine if this is an Auth request.
        boolean isAuthRequest = request.getRequestURI().startsWith("/api/auth");

        // Step 3: Resolve the appropriate bucket.
        Bucket bucket = isAuthRequest
                ? rateLimitingService.resolveAuthBucket(clientIp)
                : rateLimitingService.resolveGeneralBucket(clientIp);

        // Step 4: Try to consume a token.
        // tryConsume(1) attempts to take 1 token. Returns true if successful, false otherwise.
        if (bucket.tryConsume(1)) {
            // Success! We could add "X-Rate-Limit-Remaining" here if we used ConsumptionProbe,
            // but for this learning phase, we'll keep it simple with just the boolean check.
            
            // Continue to the next filter in the chain (e.g., JwtAuthenticationFilter).
            filterChain.doFilter(request, response);
        } else {
            // Failure: Rate limit exceeded!
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429
            response.setContentType("application/json");
            
            // Return a JSON error message to the client.
            response.getWriter().write(
                    "{\"error\": \"Too Many Requests\", \"message\": \"You have exceeded the rate limit. Please slow down.\"}"
            );
        }
    }
}

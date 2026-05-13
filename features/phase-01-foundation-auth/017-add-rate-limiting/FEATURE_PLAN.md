# Feature Plan: Add Rate Limiting on API Endpoints

## Source Checkbox
- [ ] Add rate limiting on API endpoints (Phase 1 — Foundation & Auth)

## Goal and User Value
**Goal:** Protect the MyOS API from abuse, brute-force attacks, and accidental denial-of-service by limiting the number of requests a user or IP address can make within a specific timeframe.
**User Value:** Ensures high availability and security for the user's personal data. It prevents malicious actors from overwhelming the system and provides a layer of defense against password-spraying attacks on auth endpoints.

## Scope
- Implementation of a rate-limiting mechanism using **Bucket4j**.
- Configuration of different rate limits for different types of endpoints:
    - **Auth Endpoints** (Login, Register, Refresh): Strict limits to prevent brute-force (e.g., 5 requests per minute per IP).
    - **General API Endpoints**: More relaxed limits (e.g., 100 requests per minute per user).
- Return a standard `429 Too Many Requests` HTTP status code when limits are exceeded.
- Include rate-limit information in response headers (Remaining, Limit, Reset Time).

## Out of Scope
- Distributed rate limiting using Redis (will implement in a future phase when Redis is introduced). This implementation will be in-memory for now.
- IP-based blacklisting/whitelisting UI.
- Sophisticated Web Application Firewall (WAF) features.

## Backend Changes

### Dependencies
- Add `bucket4j-core` to `pom.xml`.

### New Components
- `RateLimitingService`: A service to manage buckets for different keys (IPs or User IDs).
- `RateLimitingFilter` or `Interceptor`: To intercept incoming requests and check against the bucket.
- `RateLimitConfig`: Configuration class to define limits (could be loaded from `application.yml`).

### Modifications
- `SecurityConfig`: Register the rate-limiting filter in the security filter chain.

## Frontend Changes
- No functional changes required, but the API interceptor should ideally handle `429` errors by showing a user-friendly "Too many requests, please slow down" toast message.

## Data Model or Migration Changes
- None (In-memory storage for now).

## API Contracts
- All endpoints will now potentially return:
    - **Status:** `429 Too Many Requests`
    - **Body:** `{"error": "Too Many Requests", "message": "You have exceeded the rate limit. Please try again in X seconds."}`
    - **Headers:**
        - `X-Rate-Limit-Limit`: Maximum requests allowed.
        - `X-Rate-Limit-Remaining`: Requests remaining in the current window.
        - `X-Rate-Limit-Reset`: Time (in seconds) until the limit resets.

## Security and Privacy Considerations
- Rate limiting is a core security feature.
- Using IP addresses for unauthenticated requests is necessary but can be tricky with proxies (need to check `X-Forwarded-For`).
- For authenticated requests, rate limiting should be per-user to prevent one user from affecting others (though this is currently a personal OS, multi-user support is a bonus feature).

## Testing and Verification Plan
- **Unit Tests:** Test `RateLimitingService` logic for bucket consumption and reset.
- **Integration Tests:** Use `MockMvc` to send rapid requests to endpoints and verify that the 11th (or N+1th) request returns 429.
- **Manual Verification:** Use a tool like Postman or a simple loop script to hit the login endpoint repeatedly.

## Acceptance Criteria
- [ ] Auth endpoints (login/register) are limited to 5 requests per minute per IP.
- [ ] General API endpoints are limited to 100 requests per minute per authenticated user.
- [ ] Requests exceeding the limit receive a 429 status code.
- [ ] Response headers include rate limit metadata.
- [ ] Educational comments are added to all new Java classes.

## Open Questions
- Should we use a filter or an interceptor? A filter is better as it sits earlier in the chain, but an interceptor has easier access to Spring beans. (Recommendation: Filter, as we want to block requests as early as possible).
- Do we want to persist rate limit state? (Not for now, in-memory is fine for Phase 1).

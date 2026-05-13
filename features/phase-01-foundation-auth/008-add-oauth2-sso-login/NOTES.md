# Implementation Notes - OAuth2 SSO Login

## Required Environment Variables
The following must be added to `.env`:
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GITHUB_CLIENT_ID`
- `GITHUB_CLIENT_SECRET`

## Spring Security OAuth2 Flow
1. User hits `/oauth2/authorization/google`.
2. Spring redirects to Google.
3. User authenticates.
4. Google redirects back to `/login/oauth2/code/google`.
5. `CustomOAuth2UserService` is called to extract user info.
6. `OAuth2AuthenticationSuccessHandler` is called to generate JWT and redirect to frontend.

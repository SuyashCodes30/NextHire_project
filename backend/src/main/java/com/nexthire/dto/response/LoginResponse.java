package com.nexthire.dto.response;

/**
 * LoginResponse — Returned when a user successfully logs in.
 *
 * Currently returns a mock token (placeholder).
 * When JWT authentication is implemented, this will contain a real signed JWT.
 *
 * Returned by: POST /api/auth/login
 */
public class LoginResponse {

    private String token;           // JWT token (placeholder for now — "MOCK_TOKEN")
    private String tokenType;       // Always "Bearer" for JWT
    private UserResponse user;      // Full user profile (without password)
    private long expiresIn;         // Token expiry in milliseconds (e.g. 86400000 = 24 hours)

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    public LoginResponse() {}

    public LoginResponse(String token, String tokenType, UserResponse user, long expiresIn) {
        this.token = token;
        this.tokenType = tokenType;
        this.user = user;
        this.expiresIn = expiresIn;
    }

    // ---------------------------------------------------------------
    // Getters and Setters
    // ---------------------------------------------------------------

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
}

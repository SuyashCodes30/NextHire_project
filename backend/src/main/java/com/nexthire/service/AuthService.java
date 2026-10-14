package com.nexthire.service;

import com.nexthire.dto.request.LoginRequest;
import com.nexthire.dto.request.RegisterRequest;
import com.nexthire.dto.response.LoginResponse;
import com.nexthire.dto.response.UserResponse;

/**
 * AuthService — Interface defining the authentication contract.
 *
 * TODO (Phase 2 - Authentication):
 *   - Implement BCrypt password hashing in register()
 *   - Implement actual password verification in login()
 *   - Generate and return a real JWT token in login()
 *   - Add refresh token support
 *   - Add logout (token invalidation) support
 */
public interface AuthService {

    /**
     * Register a new user account.
     *
     * @param request Registration data (name, email, password)
     * @return The created user profile (without password)
     *
     * TODO: Hash the password with BCrypt before saving to MySQL.
     * TODO: Check for duplicate email before creating.
     */
    UserResponse register(RegisterRequest request);

    /**
     * Authenticate a user with email and password.
     *
     * @param request Login credentials (email, password)
     * @return A LoginResponse containing the auth token and user profile
     *
     * TODO: Verify password against BCrypt hash stored in MySQL.
     * TODO: Generate a real signed JWT token.
     * TODO: Handle "Remember me" functionality (extended token expiry).
     */
    LoginResponse login(LoginRequest request);
}

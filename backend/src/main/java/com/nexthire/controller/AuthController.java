package com.nexthire.controller;

import com.nexthire.dto.request.LoginRequest;
import com.nexthire.dto.request.RegisterRequest;
import com.nexthire.dto.response.ApiResponse;
import com.nexthire.dto.response.LoginResponse;
import com.nexthire.dto.response.UserResponse;
import com.nexthire.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — Handles user registration and login.
 *
 * Base URL: /api/auth
 *
 * Endpoints:
 *   POST /api/auth/register  — Register a new account
 *   POST /api/auth/login     — Login with email and password
 *
 * Frontend connection:
 *   login.html form → POST /api/auth/login
 *   Sign-up form    → POST /api/auth/register
 *
 * NOTE: Authentication not implemented yet. These endpoints accept
 * requests and return mock responses. JWT and BCrypt to be added in Phase 2.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/register
     *
     * Register a new user account.
     *
     * Request body:
     * {
     *   "firstName": "Aditya",
     *   "lastName":  "Kumar",
     *   "email":     "aditya@example.com",
     *   "password":  "securepassword"
     * }
     *
     * Response 201:
     * {
     *   "success": true,
     *   "message": "Account created successfully. Welcome to NextHire!",
     *   "data": { user profile },
     *   "status": 201
     * }
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new NextHire account")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse newUser = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Account created successfully. Welcome to NextHire!",
                        newUser,
                        201
                ));
    }

    /**
     * POST /api/auth/login
     *
     * Login with email and password.
     *
     * Request body (matches login.html form fields):
     * {
     *   "email":    "demo@nexthire.ai",
     *   "password": "anypassword"
     * }
     *
     * Response 200:
     * {
     *   "success": true,
     *   "message": "Login successful.",
     *   "data": {
     *     "token": "MOCK_JWT_TOKEN_...",
     *     "tokenType": "Bearer",
     *     "user": { user profile },
     *     "expiresIn": 86400000
     *   },
     *   "status": 200
     * }
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate with email and password")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.ok("Login successful.", loginResponse)
        );
    }
}

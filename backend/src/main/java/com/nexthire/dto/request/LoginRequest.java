package com.nexthire.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * LoginRequest — DTO for user login.
 *
 * Sent from login.html form to: POST /api/auth/login
 *
 * Fields:
 *   email    — the candidate's registered email
 *   password — their plain-text password (will be BCrypt-verified on backend later)
 *
 * Future: Add device info, IP address for security logging.
 */
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    public LoginRequest() {}

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // ---------------------------------------------------------------
    // Getters and Setters
    // ---------------------------------------------------------------

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

package com.nexthire.dto.response;

import java.time.LocalDateTime;

/**
 * UserResponse — Safe user data returned to the frontend.
 *
 * NOTE: Password is deliberately excluded from this DTO.
 * Only non-sensitive fields are exposed to the API consumer.
 *
 * Returned by:
 *   GET /api/users/{id}
 *   POST /api/auth/register
 *   GET /api/users/profile
 */
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private int streakDays;      // Gamification: 🔥 shown in navbar
    private int xpPoints;        // Gamification: ⚡ shown in navbar
    private LocalDateTime createdAt;

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    public UserResponse() {}

    public UserResponse(Long id, String firstName, String lastName, String email,
                        String role, int streakDays, int xpPoints, LocalDateTime createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.streakDays = streakDays;
        this.xpPoints = xpPoints;
        this.createdAt = createdAt;
    }

    // ---------------------------------------------------------------
    // Getters and Setters
    // ---------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getStreakDays() { return streakDays; }
    public void setStreakDays(int streakDays) { this.streakDays = streakDays; }

    public int getXpPoints() { return xpPoints; }
    public void setXpPoints(int xpPoints) { this.xpPoints = xpPoints; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** Convenience method */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}

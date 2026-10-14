package com.nexthire.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * User — Represents a registered user/candidate of the NextHire platform.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "email", unique = true, nullable = false, length = 150)
    private String email;

    /**
     * Password field — will be BCrypt-hashed before storage.
     * NOT included in response DTOs for security.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * User role — determines permissions.
     * CANDIDATE = regular user practicing interviews
     * ADMIN     = platform administrator (future use)
     */
    @Column(name = "role", length = 50)
    private String role; // "CANDIDATE" or "ADMIN"

    @Column(name = "streak_days")
    private int streakDays;     // Gamification: current daily streak

    @Column(name = "xp_points")
    private int xpPoints;       // Gamification: total XP earned

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    public User() {}

    public User(Long id, String firstName, String lastName, String email,
                String password, String role, int streakDays, int xpPoints,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.streakDays = streakDays;
        this.xpPoints = xpPoints;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getStreakDays() { return streakDays; }
    public void setStreakDays(int streakDays) { this.streakDays = streakDays; }

    public int getXpPoints() { return xpPoints; }
    public void setXpPoints(int xpPoints) { this.xpPoints = xpPoints; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /** Convenience method to get full name */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}

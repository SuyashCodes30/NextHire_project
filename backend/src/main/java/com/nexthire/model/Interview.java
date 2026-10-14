package com.nexthire.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Interview — Represents a single interview session taken by a candidate.
 */
@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "category", length = 50)
    private String category;      // TECHNICAL, HR, BEHAVIORAL, CODING, MANAGERIAL

    @Column(name = "role", length = 100)
    private String role;          // e.g. "Software Engineer", "Product Manager"

    @Column(name = "difficulty", length = 20)
    private String difficulty;    // EASY, MEDIUM, HARD

    @Column(name = "status", length = 30)
    private String status;        // CREATED, IN_PROGRESS, COMPLETED, ABANDONED

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "duration_secs")
    private Integer durationSecs;  // Duration in seconds

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Comma-separated question IDs assigned to this interview (e.g., "1,4,12,23,55")
     * Persisted so questions can be restored across restarts.
     */
    @Column(name = "question_ids", length = 500)
    private String questionIds;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    public Interview() {}

    public Interview(Long id, Long userId, String category, String role,
                     String difficulty, String status, LocalDateTime startedAt,
                     LocalDateTime completedAt, Integer durationSecs, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.role = role;
        this.difficulty = difficulty;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.durationSecs = durationSecs;
        this.createdAt = createdAt;
    }

    // ---------------------------------------------------------------
    // Getters and Setters
    // ---------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getDurationSecs() { return durationSecs; }
    public void setDurationSecs(Integer durationSecs) { this.durationSecs = durationSecs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getQuestionIds() { return questionIds; }
    public void setQuestionIds(String questionIds) { this.questionIds = questionIds; }
}

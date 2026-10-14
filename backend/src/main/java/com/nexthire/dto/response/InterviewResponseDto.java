package com.nexthire.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nexthire.model.Question;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * InterviewResponseDto — Returns interview session details and questions to the frontend.
 *
 * Returned by:
 *   POST /api/interviews
 *   GET  /api/interviews/{id}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterviewResponseDto {

    private Long id;
    private Long interviewId;         // Alias for id
    private Long userId;
    private String category;          // TECHNICAL, HR, etc.
    private String role;              // Job role
    private String difficulty;        // EASY, MEDIUM, HARD
    private String status;            // CREATED, IN_PROGRESS, COMPLETED, ABANDONED
    private int totalQuestions;       // Total questions in this interview
    private Integer numberOfQuestions;// Alias for totalQuestions
    private List<Question> questions = new ArrayList<>();
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer durationSecs;
    private LocalDateTime createdAt;

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    public InterviewResponseDto() {}

    // ---------------------------------------------------------------
    // Getters and Setters
    // ---------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) {
        this.id = id;
        this.interviewId = id;
    }

    public Long getInterviewId() { return interviewId != null ? interviewId : id; }
    public void setInterviewId(Long interviewId) {
        this.interviewId = interviewId;
        this.id = interviewId;
    }

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

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
        this.numberOfQuestions = totalQuestions;
    }

    public Integer getNumberOfQuestions() { return numberOfQuestions != null ? numberOfQuestions : totalQuestions; }
    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
        if (numberOfQuestions != null) this.totalQuestions = numberOfQuestions;
    }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
        if (questions != null) {
            this.totalQuestions = questions.size();
            this.numberOfQuestions = questions.size();
        }
    }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getDurationSecs() { return durationSecs; }
    public void setDurationSecs(Integer durationSecs) { this.durationSecs = durationSecs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

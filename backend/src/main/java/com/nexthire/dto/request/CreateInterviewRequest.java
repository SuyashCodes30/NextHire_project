package com.nexthire.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * CreateInterviewRequest — DTO for starting a new interview session.
 *
 * Supports category, role, difficulty (Easy, Medium, Hard), and numberOfQuestions (5, 10, 15, 20).
 *
 * POST /api/interviews
 */
public class CreateInterviewRequest {

    @NotBlank(message = "Interview category is required")
    private String category;

    private String role;

    @NotBlank(message = "Difficulty level is required")
    private String difficulty = "MEDIUM";

    private Integer numberOfQuestions = 5;

    private Long userId = 1L;

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    public CreateInterviewRequest() {}

    public CreateInterviewRequest(String category, String role, String difficulty, Long userId) {
        this.category = category;
        this.role = role;
        this.difficulty = difficulty;
        this.userId = userId != null ? userId : 1L;
        this.numberOfQuestions = 5;
    }

    public CreateInterviewRequest(String category, String role, String difficulty, Integer numberOfQuestions, Long userId) {
        this.category = category;
        this.role = role;
        this.difficulty = difficulty;
        this.numberOfQuestions = (numberOfQuestions != null && numberOfQuestions > 0) ? numberOfQuestions : 5;
        this.userId = userId != null ? userId : 1L;
    }

    // ---------------------------------------------------------------
    // Getters and Setters
    // ---------------------------------------------------------------

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRole() {
        if (role == null || role.isBlank()) {
            if (category != null) {
                switch (category.toLowerCase()) {
                    case "data-analyst": return "Data Analyst";
                    case "data-scientist": return "Data Scientist";
                    case "software-developer": return "Software Developer";
                    case "english-communication": return "Communications Specialist";
                    default: return "Candidate";
                }
            }
            return "Candidate";
        }
        return role;
    }
    public void setRole(String role) { this.role = role; }

    public String getDifficulty() { return difficulty != null ? difficulty : "MEDIUM"; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Integer getNumberOfQuestions() {
        return (numberOfQuestions != null && numberOfQuestions > 0) ? numberOfQuestions : 5;
    }
    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = (numberOfQuestions != null && numberOfQuestions > 0) ? numberOfQuestions : 5;
    }

    public Long getUserId() { return userId != null ? userId : 1L; }
    public void setUserId(Long userId) { this.userId = userId; }
}

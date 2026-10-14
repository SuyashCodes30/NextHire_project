package com.nexthire.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * InterviewResponse — Stores a candidate's answer to a specific question along with its evaluation.
 */
@Entity
@Table(name = "interview_responses")
public class InterviewResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "interview_id", nullable = false)
    private Long interviewId;

    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "response_text", columnDefinition = "TEXT")
    private String responseText;    // The candidate's actual answer

    @Column(name = "duration_secs")
    private Integer durationSecs;   // How long the candidate took (in seconds)

    @Column(name = "word_count")
    private Integer wordCount;      // Word count of the response

    @Column(name = "answer_metadata", columnDefinition = "TEXT")
    private String answerMetadata;  // JSON string: e.g. speaking pace, filler words

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    // Evaluation results for this specific answer
    @Column(name = "score")
    private Integer score;          // 0-100

    @Column(name = "is_correct")
    private Boolean isCorrect;      // true if evaluated as Right / Correct

    @Column(name = "status", length = 50)
    private String status;          // "CORRECT", "PARTIALLY_CORRECT", "INCORRECT"

    @Column(name = "status_label", length = 100)
    private String statusLabel;     // "Right / Correct ✓", "Partially Correct ~", "Incorrect / Not Right ✗"

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;        // Assessment of what was right and what was missing

    @Column(name = "expected_answer", columnDefinition = "TEXT")
    private String expectedAnswer;  // Reference model answer

    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    public InterviewResponse() {}

    public InterviewResponse(Long id, Long interviewId, Long questionId,
                             String responseText, Integer durationSecs, Integer wordCount,
                             String answerMetadata, LocalDateTime submittedAt) {
        this.id = id;
        this.interviewId = interviewId;
        this.questionId = questionId;
        this.responseText = responseText;
        this.durationSecs = durationSecs;
        this.wordCount = wordCount;
        this.answerMetadata = answerMetadata;
        this.submittedAt = submittedAt;
    }

    // ---------------------------------------------------------------
    // Getters and Setters
    // ---------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInterviewId() { return interviewId; }
    public void setInterviewId(Long interviewId) { this.interviewId = interviewId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getResponseText() { return responseText; }
    public void setResponseText(String responseText) { this.responseText = responseText; }

    public Integer getDurationSecs() { return durationSecs; }
    public void setDurationSecs(Integer durationSecs) { this.durationSecs = durationSecs; }

    public Integer getWordCount() { return wordCount; }
    public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }

    public String getAnswerMetadata() { return answerMetadata; }
    public void setAnswerMetadata(String answerMetadata) { this.answerMetadata = answerMetadata; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getExpectedAnswer() { return expectedAnswer; }
    public void setExpectedAnswer(String expectedAnswer) { this.expectedAnswer = expectedAnswer; }
}

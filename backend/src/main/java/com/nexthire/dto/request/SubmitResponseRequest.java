package com.nexthire.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * SubmitResponseRequest — DTO for submitting a candidate's answer to a question.
 *
 * POST /api/interviews/{interviewId}/responses
 *
 * Sent from the frontend when the candidate finishes answering each question.
 */
public class SubmitResponseRequest {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotBlank(message = "Response text cannot be empty")
    private String responseText;    // The candidate's answer (transcribed from voice or typed)

    private Integer durationSecs;   // How long they took to answer (optional)
    private Integer wordCount;      // Word count of the answer (optional)

    /**
     * Optional metadata as JSON string.
     * Example: {"speakingPace": "normal", "fillerWords": ["um", "uh"], "pauses": 3}
     * This will be used by the AI evaluator when it's integrated.
     */
    private String answerMetadata;

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    public SubmitResponseRequest() {}

    public SubmitResponseRequest(Long questionId, String responseText,
                                 Integer durationSecs, Integer wordCount, String answerMetadata) {
        this.questionId = questionId;
        this.responseText = responseText;
        this.durationSecs = durationSecs;
        this.wordCount = wordCount;
        this.answerMetadata = answerMetadata;
    }

    // ---------------------------------------------------------------
    // Getters and Setters
    // ---------------------------------------------------------------

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getResponseText() { return responseText; }
    public void setResponseText(String responseText) { this.responseText = responseText; }

    public String getUserAnswer() { return responseText; }
    public void setUserAnswer(String userAnswer) {
        if (this.responseText == null || this.responseText.isBlank()) {
            this.responseText = userAnswer;
        }
    }

    public Integer getDurationSecs() { return durationSecs; }
    public void setDurationSecs(Integer durationSecs) { this.durationSecs = durationSecs; }

    public Integer getWordCount() { return wordCount; }
    public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }

    public String getAnswerMetadata() { return answerMetadata; }
    public void setAnswerMetadata(String answerMetadata) { this.answerMetadata = answerMetadata; }
}

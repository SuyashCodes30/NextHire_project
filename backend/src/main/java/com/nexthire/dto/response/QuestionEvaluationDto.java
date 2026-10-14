package com.nexthire.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nexthire.model.LearningResource;

import java.util.ArrayList;
import java.util.List;

/**
 * QuestionEvaluationDto — Detailed evaluation of a candidate's answer to an interview question.
 * Indicates whether the answer is right or not, score, expected answer, explanation, key points, and deep-dive learning resources.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionEvaluationDto {

    private Long questionId;
    private int questionNumber;
    private String questionText;
    private String category;
    private String topic;
    private String subtopic;
    private String difficulty;
    private String userAnswer;
    private String expectedAnswer;
    private String correctAnswer;       // Alias for expectedAnswer
    private String explanation;         // Conceptual explanation
    private List<String> keyPoints = new ArrayList<>();
    private List<LearningResource> learningResources = new ArrayList<>();
    private int score;                  // 0-100
    private boolean isCorrect;          // true if answer is considered Right/Correct
    private String status;              // "CORRECT", "PARTIALLY_CORRECT", "INCORRECT"
    private String statusLabel;         // "Right / Correct ✓", "Partially Correct ~", "Incorrect / Not Right ✗"
    private String feedback;            // Detailed explanation of what was right and what was missing
    private List<String> coveredConcepts = new ArrayList<>();
    private List<String> missingConcepts = new ArrayList<>();

    public QuestionEvaluationDto() {}

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public int getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(int questionNumber) { this.questionNumber = questionNumber; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getSubtopic() { return subtopic; }
    public void setSubtopic(String subtopic) { this.subtopic = subtopic; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }

    public String getExpectedAnswer() { return expectedAnswer; }
    public void setExpectedAnswer(String expectedAnswer) {
        this.expectedAnswer = expectedAnswer;
        this.correctAnswer = expectedAnswer;
    }

    public String getCorrectAnswer() { return correctAnswer != null ? correctAnswer : expectedAnswer; }
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
        this.expectedAnswer = correctAnswer;
    }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<String> getKeyPoints() { return keyPoints; }
    public void setKeyPoints(List<String> keyPoints) { this.keyPoints = keyPoints; }

    public List<LearningResource> getLearningResources() { return learningResources; }
    public void setLearningResources(List<LearningResource> learningResources) { this.learningResources = learningResources; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public List<String> getCoveredConcepts() { return coveredConcepts; }
    public void setCoveredConcepts(List<String> coveredConcepts) { this.coveredConcepts = coveredConcepts; }

    public List<String> getMissingConcepts() { return missingConcepts; }
    public void setMissingConcepts(List<String> missingConcepts) { this.missingConcepts = missingConcepts; }
}

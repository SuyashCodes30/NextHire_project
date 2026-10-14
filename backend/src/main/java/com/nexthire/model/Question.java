package com.nexthire.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Question — Domain model representing an interview question with deep-dive learning resources.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Question {

    private Long id;
    private String categoryId;
    private String category;
    private String text;                // Question text
    private String difficulty;          // Easy, Medium, Hard
    private String questionType;        // Conceptual, Technical, Practical, Scenario-based, etc.
    private String type;                // Alias for questionType
    private String expectedAnswer;      // Core correct answer
    private String topic;               // e.g. SQL, Excel, Statistics
    private String subtopic;            // e.g. Joins, Window Functions, Regularization
    private String explanation;         // Detailed conceptual explanation
    private List<String> keyPoints = new ArrayList<>();
    private List<LearningResource> learningResources = new ArrayList<>();
    private String tags;
    private List<String> options;
    private boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Question() {}

    public Question(Long id, String categoryId, String text, String difficulty,
                    String questionType, String topic, String expectedAnswer, String tags) {
        this.id = id;
        this.categoryId = categoryId;
        this.category = mapToLegacyCategory(categoryId, questionType);
        this.text = text;
        this.difficulty = difficulty;
        this.questionType = questionType;
        this.type = questionType;
        this.topic = topic;
        this.expectedAnswer = expectedAnswer;
        this.tags = tags;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public Question(Long id, String categoryId, String topic, String subtopic,
                    String difficulty, String questionType, String text,
                    String expectedAnswer, String explanation,
                    List<String> keyPoints, List<LearningResource> learningResources) {
        this.id = id;
        this.categoryId = categoryId;
        this.category = mapToLegacyCategory(categoryId, questionType);
        this.topic = topic;
        this.subtopic = subtopic;
        this.difficulty = difficulty;
        this.questionType = questionType;
        this.type = questionType;
        this.text = text;
        this.expectedAnswer = expectedAnswer;
        this.explanation = explanation;
        if (keyPoints != null) this.keyPoints = keyPoints;
        if (learningResources != null) this.learningResources = learningResources;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    private String mapToLegacyCategory(String catId, String qType) {
        if (catId == null) return "TECHNICAL";
        switch (catId.toLowerCase()) {
            case "software-developer":
            case "data-analyst":
            case "data-scientist":
                return "TECHNICAL";
            case "english-communication":
                return "COMMUNICATION";
            case "general-interview":
                return "BEHAVIORAL";
            default:
                return catId.toUpperCase();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuestionId() { return id; }
    public void setQuestionId(Long questionId) { this.id = questionId; }

    public String getCategoryId() { return categoryId != null ? categoryId : category; }
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
        if (this.category == null) this.category = mapToLegacyCategory(categoryId, this.questionType);
    }

    public String getCategory() { return category != null ? category : categoryId; }
    public void setCategory(String category) {
        this.category = category;
        if (this.categoryId == null) this.categoryId = category;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getQuestion() { return text; }
    public void setQuestion(String question) { this.text = question; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getQuestionType() { return questionType != null ? questionType : type; }
    public void setQuestionType(String questionType) {
        this.questionType = questionType;
        this.type = questionType;
    }

    public String getType() { return type != null ? type : questionType; }
    public void setType(String type) {
        this.type = type;
        if (this.questionType == null) this.questionType = type;
    }

    public String getExpectedAnswer() { return expectedAnswer; }
    public void setExpectedAnswer(String expectedAnswer) { this.expectedAnswer = expectedAnswer; }

    public String getCorrectAnswer() { return expectedAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.expectedAnswer = correctAnswer; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getSubtopic() { return subtopic; }
    public void setSubtopic(String subtopic) { this.subtopic = subtopic; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<String> getKeyPoints() { return keyPoints; }
    public void setKeyPoints(List<String> keyPoints) { this.keyPoints = keyPoints; }

    public List<LearningResource> getLearningResources() { return learningResources; }
    public void setLearningResources(List<LearningResource> learningResources) { this.learningResources = learningResources; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

package com.nexthire.dto.response;

import java.util.ArrayList;
import java.util.List;

/**
 * InterviewResultResponse — The AI-generated result of a completed interview.
 *
 * This matches exactly what the analytics section of the NextHire frontend displays:
 * - Overall score (shown in the ring widget)
 * - Communication score
 * - Confidence score
 * - Technical Knowledge score
 * - Clarity score
 * - Answer Structure score
 * - Readiness label (e.g., "Interview Ready")
 * - Feedback, strengths, areas to improve
 * - Per-question evaluations (right vs not right, expected answer, score, feedback)
 *
 * Returned by: GET /api/interviews/{id}/result
 */
public class InterviewResultResponse {

    private Long interviewId;
    private int overallScore;           // 0-100, shown in ring widget
    private String readinessLabel;      // "Interview Ready", "Needs Practice", etc.

    // ---------------------------------------------------------------
    // Accuracy & Question Statistics (derived from answer correctness)
    // ---------------------------------------------------------------
    private int totalQuestions;
    private int correctCount;           // Questions answered correctly (Right)
    private int partiallyCorrectCount;  // Questions answered partially correctly
    private int incorrectCount;         // Questions answered incorrectly (Not Right)
    private int accuracyPercentage;     // 0-100% accuracy

    // ---------------------------------------------------------------
    // Skill Score Breakdown (matches frontend analytics bars)
    // ---------------------------------------------------------------
    private int communicationScore;
    private int confidenceScore;
    private int technicalScore;
    private int clarityScore;
    private int answerStructureScore;

    // ---------------------------------------------------------------
    // Qualitative AI Feedback
    // ---------------------------------------------------------------
    private String overallFeedback;
    private List<String> strengths = new ArrayList<>();
    private List<String> improvements = new ArrayList<>();
    private String starEvaluation;

    // ---------------------------------------------------------------
    // Detailed Question-by-Question Evaluation List
    // ---------------------------------------------------------------
    private List<QuestionEvaluationDto> questionEvaluations = new ArrayList<>();

    // ---------------------------------------------------------------
    // Topic Mastery & Learning Recommendations
    // ---------------------------------------------------------------
    private List<String> topicsCovered = new ArrayList<>();
    private List<String> strongTopics = new ArrayList<>();
    private List<String> weakTopics = new ArrayList<>();
    private List<com.nexthire.model.LearningResource> recommendedLearning = new ArrayList<>();

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    public InterviewResultResponse() {}

    public List<String> getTopicsCovered() { return topicsCovered; }
    public void setTopicsCovered(List<String> topicsCovered) { this.topicsCovered = topicsCovered; }

    public List<String> getStrongTopics() { return strongTopics; }
    public void setStrongTopics(List<String> strongTopics) { this.strongTopics = strongTopics; }

    public List<String> getWeakTopics() { return weakTopics; }
    public void setWeakTopics(List<String> weakTopics) { this.weakTopics = weakTopics; }

    public List<com.nexthire.model.LearningResource> getRecommendedLearning() { return recommendedLearning; }
    public void setRecommendedLearning(List<com.nexthire.model.LearningResource> recommendedLearning) { this.recommendedLearning = recommendedLearning; }

    // ---------------------------------------------------------------
    // Getters and Setters
    // ---------------------------------------------------------------

    public Long getInterviewId() { return interviewId; }
    public void setInterviewId(Long interviewId) { this.interviewId = interviewId; }

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }

    public String getReadinessLabel() { return readinessLabel; }
    public void setReadinessLabel(String readinessLabel) { this.readinessLabel = readinessLabel; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }

    public int getPartiallyCorrectCount() { return partiallyCorrectCount; }
    public void setPartiallyCorrectCount(int partiallyCorrectCount) { this.partiallyCorrectCount = partiallyCorrectCount; }

    public int getIncorrectCount() { return incorrectCount; }
    public void setIncorrectCount(int incorrectCount) { this.incorrectCount = incorrectCount; }

    public int getAccuracyPercentage() { return accuracyPercentage; }
    public void setAccuracyPercentage(int accuracyPercentage) { this.accuracyPercentage = accuracyPercentage; }

    public int getCommunicationScore() { return communicationScore; }
    public void setCommunicationScore(int communicationScore) { this.communicationScore = communicationScore; }

    public int getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(int confidenceScore) { this.confidenceScore = confidenceScore; }

    public int getTechnicalScore() { return technicalScore; }
    public void setTechnicalScore(int technicalScore) { this.technicalScore = technicalScore; }

    public int getClarityScore() { return clarityScore; }
    public void setClarityScore(int clarityScore) { this.clarityScore = clarityScore; }

    public int getAnswerStructureScore() { return answerStructureScore; }
    public void setAnswerStructureScore(int answerStructureScore) { this.answerStructureScore = answerStructureScore; }

    public String getOverallFeedback() { return overallFeedback; }
    public void setOverallFeedback(String overallFeedback) { this.overallFeedback = overallFeedback; }

    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }

    public List<String> getImprovements() { return improvements; }
    public void setImprovements(List<String> improvements) { this.improvements = improvements; }

    public String getStarEvaluation() { return starEvaluation; }
    public void setStarEvaluation(String starEvaluation) { this.starEvaluation = starEvaluation; }

    public List<QuestionEvaluationDto> getQuestionEvaluations() { return questionEvaluations; }
    public void setQuestionEvaluations(List<QuestionEvaluationDto> questionEvaluations) { this.questionEvaluations = questionEvaluations; }
}

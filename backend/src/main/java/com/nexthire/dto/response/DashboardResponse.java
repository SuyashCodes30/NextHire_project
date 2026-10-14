package com.nexthire.dto.response;

import com.nexthire.category.InterviewCategory;
import com.nexthire.trending.TrendingItem;

import java.util.List;
import java.util.Map;

/**
 * DashboardResponse — Aggregated statistics and recommendations for the user dashboard.
 *
 * Returns:
 * - recent interviews
 * - recommended categories
 * - average score
 * - completed interviews
 * - learning progress
 * - trending topics
 * - streakDays, xpPoints, readinessScore
 */
public class DashboardResponse {

    // ---------------------------------------------------------------
    // Statistics
    // ---------------------------------------------------------------
    private int totalInterviews;
    private int completedInterviews;
    private int inProgressInterviews;
    private double averageScore;
    private int streakDays;
    private int xpPoints;
    private int readinessScore;

    // AI Performance Skill Breakdown (5 Core Pillars)
    private int communicationScore = 82;
    private int confidenceScore = 91;
    private int technicalScore = 84;
    private int clarityScore = 88;
    private int answerStructureScore = 87;
    private String readinessLabel = "Interview Ready 🔥";

    // ---------------------------------------------------------------
    // Progression Roadmap
    // ---------------------------------------------------------------
    private boolean step1Completed;
    private boolean step2Completed;
    private boolean step3Active;
    private boolean step4Unlocked;

    // ---------------------------------------------------------------
    // Modular Dashboard Features
    // ---------------------------------------------------------------
    private List<RecentInterviewSummary> recentInterviews;
    private List<InterviewCategory> recommendedCategories;
    private Map<String, Object> learningProgress;
    private List<TrendingItem> trendingTopics;

    // ---------------------------------------------------------------
    // Inner class for recent interview summary
    // ---------------------------------------------------------------
    public static class RecentInterviewSummary {
        private Long id;
        private String category;
        private String role;
        private String status;
        private int score;
        private String completedDate;

        public RecentInterviewSummary() {}

        public RecentInterviewSummary(Long id, String category, String role,
                                      String status, int score, String completedDate) {
            this.id = id;
            this.category = category;
            this.role = role;
            this.status = status;
            this.score = score;
            this.completedDate = completedDate;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }

        public String getCompletedDate() { return completedDate; }
        public void setCompletedDate(String completedDate) { this.completedDate = completedDate; }
    }

    public DashboardResponse() {}

    public int getTotalInterviews() { return totalInterviews; }
    public void setTotalInterviews(int totalInterviews) { this.totalInterviews = totalInterviews; }

    public int getCompletedInterviews() { return completedInterviews; }
    public void setCompletedInterviews(int completedInterviews) { this.completedInterviews = completedInterviews; }

    public int getInProgressInterviews() { return inProgressInterviews; }
    public void setInProgressInterviews(int inProgressInterviews) { this.inProgressInterviews = inProgressInterviews; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public int getStreakDays() { return streakDays; }
    public void setStreakDays(int streakDays) { this.streakDays = streakDays; }

    public int getXpPoints() { return xpPoints; }
    public void setXpPoints(int xpPoints) { this.xpPoints = xpPoints; }

    public int getReadinessScore() { return readinessScore; }
    public void setReadinessScore(int readinessScore) { this.readinessScore = readinessScore; }

    public boolean isStep1Completed() { return step1Completed; }
    public void setStep1Completed(boolean step1Completed) { this.step1Completed = step1Completed; }

    public boolean isStep2Completed() { return step2Completed; }
    public void setStep2Completed(boolean step2Completed) { this.step2Completed = step2Completed; }

    public boolean isStep3Active() { return step3Active; }
    public void setStep3Active(boolean step3Active) { this.step3Active = step3Active; }

    public boolean isStep4Unlocked() { return step4Unlocked; }
    public void setStep4Unlocked(boolean step4Unlocked) { this.step4Unlocked = step4Unlocked; }

    public List<RecentInterviewSummary> getRecentInterviews() { return recentInterviews; }
    public void setRecentInterviews(List<RecentInterviewSummary> recentInterviews) { this.recentInterviews = recentInterviews; }

    public List<InterviewCategory> getRecommendedCategories() { return recommendedCategories; }
    public void setRecommendedCategories(List<InterviewCategory> recommendedCategories) { this.recommendedCategories = recommendedCategories; }

    public Map<String, Object> getLearningProgress() { return learningProgress; }
    public void setLearningProgress(Map<String, Object> learningProgress) { this.learningProgress = learningProgress; }

    public List<TrendingItem> getTrendingTopics() { return trendingTopics; }
    public void setTrendingTopics(List<TrendingItem> trendingTopics) { this.trendingTopics = trendingTopics; }

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

    public String getReadinessLabel() { return readinessLabel; }
    public void setReadinessLabel(String readinessLabel) { this.readinessLabel = readinessLabel; }
}

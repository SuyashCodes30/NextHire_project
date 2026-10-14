package com.nexthire.service.impl;

import com.nexthire.category.CategoryService;
import com.nexthire.dto.response.DashboardResponse;
import com.nexthire.learning.LearningService;
import com.nexthire.model.Interview;
import com.nexthire.model.InterviewResult;
import com.nexthire.model.User;
import com.nexthire.repository.InterviewRepository;
import com.nexthire.repository.InterviewResultRepository;
import com.nexthire.repository.UserRepository;
import com.nexthire.service.DashboardService;
import com.nexthire.trending.TrendingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DashboardServiceImpl — Aggregates user information, recommended categories,
 * learning progress, and trending topics from MySQL repositories with graceful demo fallbacks.
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private final CategoryService categoryService;
    private final LearningService learningService;
    private final TrendingService trendingService;
    private final InterviewRepository interviewRepository;
    private final InterviewResultRepository interviewResultRepository;
    private final UserRepository userRepository;

    @Autowired
    public DashboardServiceImpl(CategoryService categoryService,
                                LearningService learningService,
                                TrendingService trendingService,
                                InterviewRepository interviewRepository,
                                InterviewResultRepository interviewResultRepository,
                                UserRepository userRepository) {
        this.categoryService = categoryService;
        this.learningService = learningService;
        this.trendingService = trendingService;
        this.interviewRepository = interviewRepository;
        this.interviewResultRepository = interviewResultRepository;
        this.userRepository = userRepository;
    }

    @Override
    public DashboardResponse getDashboard(Long userId) {
        DashboardResponse dashboard = new DashboardResponse();

        int totalInterviews = interviewRepository.countByUserId(userId);
        if (totalInterviews > 0) {
            int completed = interviewRepository.countByUserIdAndStatus(userId, "COMPLETED");
            int inProgress = interviewRepository.countByUserIdAndStatus(userId, "IN_PROGRESS");

            dashboard.setTotalInterviews(totalInterviews);
            dashboard.setCompletedInterviews(completed);
            dashboard.setInProgressInterviews(inProgress);

            // Compute real average score and skill breakdown from results
            List<Interview> userInterviews = interviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
            double totalScore = 0;
            double totalComm = 0;
            double totalConf = 0;
            double totalTech = 0;
            double totalClarity = 0;
            double totalStruct = 0;
            int scoredCount = 0;
            String latestReadiness = null;
            List<DashboardResponse.RecentInterviewSummary> recents = new ArrayList<>();

            for (Interview i : userInterviews) {
                int score = 0;
                Optional<InterviewResult> res = interviewResultRepository.findByInterviewId(i.getId());
                if (res.isPresent()) {
                    InterviewResult r = res.get();
                    score = r.getOverallScore();
                    totalScore += score;
                    totalComm += r.getCommunicationScore();
                    totalConf += r.getConfidenceScore();
                    totalTech += r.getTechnicalScore();
                    totalClarity += r.getClarityScore();
                    totalStruct += r.getAnswerStructureScore();
                    if (latestReadiness == null && r.getReadinessLabel() != null && !r.getReadinessLabel().isBlank()) {
                        latestReadiness = r.getReadinessLabel();
                    }
                    scoredCount++;
                }
                if (recents.size() < 5) {
                    recents.add(new DashboardResponse.RecentInterviewSummary(
                            i.getId(),
                            i.getCategory(),
                            i.getRole(),
                            i.getStatus(),
                            score,
                            "Recently"
                    ));
                }
            }

            if (scoredCount > 0) {
                double avgScore = Math.round((totalScore / scoredCount) * 10.0) / 10.0;
                dashboard.setAverageScore(avgScore);
                dashboard.setReadinessScore((int) Math.round(avgScore));
                dashboard.setCommunicationScore((int) Math.round(totalComm / scoredCount));
                dashboard.setConfidenceScore((int) Math.round(totalConf / scoredCount));
                dashboard.setTechnicalScore((int) Math.round(totalTech / scoredCount));
                dashboard.setClarityScore((int) Math.round(totalClarity / scoredCount));
                dashboard.setAnswerStructureScore((int) Math.round(totalStruct / scoredCount));
                if (latestReadiness != null) {
                    dashboard.setReadinessLabel(latestReadiness);
                } else {
                    dashboard.setReadinessLabel(avgScore >= 80 ? "Interview Ready 🔥" : "Practice Recommended 🎯");
                }
            } else {
                dashboard.setAverageScore(0.0);
                dashboard.setReadinessScore(0);
                dashboard.setCommunicationScore(82);
                dashboard.setConfidenceScore(91);
                dashboard.setTechnicalScore(84);
                dashboard.setClarityScore(88);
                dashboard.setAnswerStructureScore(87);
                dashboard.setReadinessLabel("Interview Ready 🔥");
            }
            dashboard.setRecentInterviews(recents);
        } else {
            // Default demo statistics
            dashboard.setTotalInterviews(12);
            dashboard.setCompletedInterviews(9);
            dashboard.setInProgressInterviews(1);
            dashboard.setAverageScore(86.0);
            dashboard.setReadinessScore(86);
            dashboard.setCommunicationScore(82);
            dashboard.setConfidenceScore(91);
            dashboard.setTechnicalScore(84);
            dashboard.setClarityScore(88);
            dashboard.setAnswerStructureScore(87);
            dashboard.setReadinessLabel("Interview Ready 🔥");
            dashboard.setRecentInterviews(List.of(
                    new DashboardResponse.RecentInterviewSummary(1001L, "software-developer", "Software Engineer", "COMPLETED", 86, "Today"),
                    new DashboardResponse.RecentInterviewSummary(1002L, "data-analyst", "BI Data Analyst", "COMPLETED", 84, "Yesterday"),
                    new DashboardResponse.RecentInterviewSummary(1003L, "general-interview", "Product Manager", "COMPLETED", 88, "2 days ago"),
                    new DashboardResponse.RecentInterviewSummary(1004L, "english-communication", "Technical Consultant", "IN_PROGRESS", 0, "Today")
            ));
        }

        // Gamification (streak & XP) from User entity if found
        int streak = 7;
        int xp = 250;
        if (userId != null) {
            Optional<User> u = userRepository.findById(userId);
            if (u.isPresent()) {
                streak = u.get().getStreakDays();
                xp = u.get().getXpPoints();
            }
        }
        dashboard.setStreakDays(streak);
        dashboard.setXpPoints(xp);

        // Progression path
        dashboard.setStep1Completed(true);
        dashboard.setStep2Completed(true);
        dashboard.setStep3Active(true);
        dashboard.setStep4Unlocked(false);

        // Recommended Categories, Learning Progress, Trending Topics
        dashboard.setRecommendedCategories(categoryService.getAllCategories());
        dashboard.setLearningProgress(learningService.getLearningOverview());
        dashboard.setTrendingTopics(trendingService.getAllTrending());

        return dashboard;
    }

    @Override
    public DashboardResponse getStatistics(Long userId) {
        DashboardResponse stats = new DashboardResponse();
        int totalInterviews = interviewRepository.countByUserId(userId);
        if (totalInterviews > 0) {
            stats.setTotalInterviews(totalInterviews);
            stats.setCompletedInterviews(interviewRepository.countByUserIdAndStatus(userId, "COMPLETED"));
            stats.setInProgressInterviews(interviewRepository.countByUserIdAndStatus(userId, "IN_PROGRESS"));

            List<Interview> userInterviews = interviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
            double totalScore = 0;
            double totalComm = 0;
            double totalConf = 0;
            double totalTech = 0;
            double totalClarity = 0;
            double totalStruct = 0;
            int scoredCount = 0;
            String latestReadiness = null;

            for (Interview i : userInterviews) {
                Optional<InterviewResult> res = interviewResultRepository.findByInterviewId(i.getId());
                if (res.isPresent()) {
                    InterviewResult r = res.get();
                    totalScore += r.getOverallScore();
                    totalComm += r.getCommunicationScore();
                    totalConf += r.getConfidenceScore();
                    totalTech += r.getTechnicalScore();
                    totalClarity += r.getClarityScore();
                    totalStruct += r.getAnswerStructureScore();
                    if (latestReadiness == null && r.getReadinessLabel() != null) {
                        latestReadiness = r.getReadinessLabel();
                    }
                    scoredCount++;
                }
            }

            if (scoredCount > 0) {
                double avgScore = Math.round((totalScore / scoredCount) * 10.0) / 10.0;
                stats.setAverageScore(avgScore);
                stats.setReadinessScore((int) Math.round(avgScore));
                stats.setCommunicationScore((int) Math.round(totalComm / scoredCount));
                stats.setConfidenceScore((int) Math.round(totalConf / scoredCount));
                stats.setTechnicalScore((int) Math.round(totalTech / scoredCount));
                stats.setClarityScore((int) Math.round(totalClarity / scoredCount));
                stats.setAnswerStructureScore((int) Math.round(totalStruct / scoredCount));
                stats.setReadinessLabel(latestReadiness != null ? latestReadiness : "Interview Ready 🔥");
            } else {
                stats.setAverageScore(0.0);
                stats.setReadinessScore(0);
                stats.setCommunicationScore(82);
                stats.setConfidenceScore(91);
                stats.setTechnicalScore(84);
                stats.setClarityScore(88);
                stats.setAnswerStructureScore(87);
                stats.setReadinessLabel("Interview Ready 🔥");
            }
        } else {
            stats.setTotalInterviews(12);
            stats.setCompletedInterviews(9);
            stats.setInProgressInterviews(1);
            stats.setAverageScore(86.0);
            stats.setReadinessScore(86);
            stats.setCommunicationScore(82);
            stats.setConfidenceScore(91);
            stats.setTechnicalScore(84);
            stats.setClarityScore(88);
            stats.setAnswerStructureScore(87);
            stats.setReadinessLabel("Interview Ready 🔥");
        }

        int streak = 7;
        int xp = 250;
        if (userId != null) {
            userRepository.findById(userId).ifPresent(u -> {
                stats.setStreakDays(u.getStreakDays());
                stats.setXpPoints(u.getXpPoints());
            });
        }
        stats.setStreakDays(streak);
        stats.setXpPoints(xp);
        return stats;
    }
}

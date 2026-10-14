package com.nexthire.ai;

import com.nexthire.dto.response.InterviewResultResponse;
import com.nexthire.model.InterviewResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AIInterviewServiceImpl — Mock implementation of the AI interview engine.
 *
 * Uses heuristic scoring and structured evaluation algorithms for testing
 * before connecting external LLM APIs (e.g. Google Gemini or OpenAI).
 */
@Service
public class AIInterviewServiceImpl implements AIInterviewService {

    @Override
    public String generateQuestion(String category, String difficulty, String role, List<String> previousQuestions) {
        return "Can you describe a challenging project in your " + role + " experience where you optimized performance under strict constraints?";
    }

    @Override
    public int evaluateAnswer(String questionText, String answerText, String category) {
        if (answerText == null || answerText.isBlank()) return 20;

        int words = answerText.trim().split("\\s+").length;
        int score = 65;

        // Depth & substance
        if (words >= 20) score += 10;
        if (words >= 50) score += 10;
        if (words >= 100) score += 5;

        // Structured patterns (STAR keywords, technical buzzwords)
        String lower = answerText.toLowerCase();
        if (lower.contains("result") || lower.contains("action") || lower.contains("metric") || lower.contains("because")) {
            score += 5;
        }
        if (lower.contains("team") || lower.contains("stakeholder") || lower.contains("design") || lower.contains("latency")) {
            score += 5;
        }

        return Math.min(score, 98);
    }

    @Override
    public String generateAnswerFeedback(String questionText, String answerText, int score) {
        if (score >= 85) {
            return "Exceptional answer! You demonstrated solid depth, articulated your thought process clearly, and supported conclusions with structured rationale.";
        } else if (score >= 70) {
            return "Good response. Your core concepts were accurate. To elevate your answer to senior level, quantify your business impact with specific metrics.";
        } else {
            return "Your answer covered the basics, but needed more concrete examples. Practice structuring responses using the STAR method (Situation, Task, Action, Result).";
        }
    }

    @Override
    public String generateInterviewSummary(Long interviewId, int overallScore, String category) {
        return String.format(
                "You completed a %s interview evaluation and achieved a composite score of %d/100. " +
                "Your verbal clarity and foundational domain competence showed notable strength. " +
                "Continuous focus on trade-off analysis and metric-driven storytelling will elevate your performance to top-tier readiness.",
                category.toUpperCase(), overallScore
        );
    }

    @Override
    public List<String> generateImprovementSuggestions(String category, int score, List<InterviewResponse> responses) {
        List<String> list = new ArrayList<>();
        list.add("Adopt the STAR framework (Situation, Task, Action, Result) consistently for all behavioral and operational questions.");
        list.add("Quantify your project outcomes: mention percentages, latency reductions, or revenue metrics.");
        list.add("Pause for 2-3 seconds to outline your answer before speaking to avoid filler words and tangents.");
        return list;
    }

    @Override
    public List<String> identifyStrengths(String category, int score, List<InterviewResponse> responses) {
        List<String> list = new ArrayList<>();
        list.add("Clear, articulate communication with appropriate technical terminology.");
        list.add("Strong problem-solving framework and methodical breakdown of core concepts.");
        list.add("Professional tone and executive presence maintained throughout the interview.");
        return list;
    }

    @Override
    public InterviewResultResponse generateInterviewResult(Long interviewId, List<InterviewResponse> responses,
                                                            String category, String role) {
        int avgScore = 80;
        if (responses != null && !responses.isEmpty()) {
            int total = 0;
            for (InterviewResponse r : responses) {
                total += evaluateAnswer(null, r.getResponseText(), category);
            }
            avgScore = total / responses.size();
        }

        int commScore = Math.min(100, avgScore + 4);
        int techScore = Math.min(100, avgScore + 2);
        int confScore = Math.min(100, avgScore - 2);
        int clarityScore = Math.min(100, avgScore + 3);
        int structScore = Math.min(100, avgScore - 1);

        String readiness = avgScore >= 85 ? "Interview Ready" : (avgScore >= 70 ? "Proficient" : "Needs Practice");

        InterviewResultResponse result = new InterviewResultResponse();
        result.setInterviewId(interviewId);
        result.setOverallScore(avgScore);
        result.setReadinessLabel(readiness);
        result.setCommunicationScore(commScore);
        result.setConfidenceScore(confScore);
        result.setTechnicalScore(techScore);
        result.setClarityScore(clarityScore);
        result.setAnswerStructureScore(structScore);
        result.setOverallFeedback(generateInterviewSummary(interviewId, avgScore, category));
        result.setStrengths(identifyStrengths(category, avgScore, responses));
        result.setImprovements(generateImprovementSuggestions(category, avgScore, responses));
        result.setStarEvaluation("Situation & Task clearly defined (85%). Action demonstrated personal ownership (90%). Result included quantifiable impact (78%).");

        return result;
    }
}

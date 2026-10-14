package com.nexthire.ai;

import com.nexthire.dto.response.InterviewResultResponse;
import com.nexthire.model.InterviewResponse;
import com.nexthire.model.Question;

import java.util.List;

/**
 * AIInterviewService — Clean architectural abstraction for AI operations.
 *
 * Responsibilities:
 *   - Generate interview questions (dynamic prompting)
 *   - Evaluate candidate answers (semantic scoring & rubric analysis)
 *   - Generate contextual feedback
 *   - Generate interview summary
 *   - Generate improvement suggestions
 *
 * Pluggable design: Future implementations (Gemini, OpenAI, Anthropic) can be injected
 * without rewriting controllers or interview session workflows.
 */
public interface AIInterviewService {

    String generateQuestion(String category, String difficulty, String role, List<String> previousQuestions);

    int evaluateAnswer(String questionText, String answerText, String category);

    String generateAnswerFeedback(String questionText, String answerText, int score);

    String generateInterviewSummary(Long interviewId, int overallScore, String category);

    List<String> generateImprovementSuggestions(String category, int score, List<InterviewResponse> responses);

    List<String> identifyStrengths(String category, int score, List<InterviewResponse> responses);

    InterviewResultResponse generateInterviewResult(Long interviewId, List<InterviewResponse> responses,
                                                     String category, String role);
}

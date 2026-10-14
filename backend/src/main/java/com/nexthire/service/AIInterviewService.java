package com.nexthire.service;

import com.nexthire.dto.response.InterviewResultResponse;
import com.nexthire.dto.response.QuestionEvaluationDto;
import com.nexthire.model.InterviewResponse;
import com.nexthire.model.Question;

import java.util.List;

/**
 * AIInterviewService — Interface for AI-powered interview evaluation.
 */
public interface AIInterviewService {

    /**
     * Generate the next interview question for a given context.
     */
    String generateQuestion(String category, String difficulty, String role,
                            List<String> previousQuestions);

    /**
     * Evaluate a candidate's answer to a specific question.
     */
    int evaluateAnswer(String questionText, String answerText, String category);

    /**
     * Generate natural language feedback for a single answer.
     */
    String generateAnswerFeedback(String questionText, String answerText, int score);

    /**
     * Generate a comprehensive interview result after all questions are answered.
     */
    InterviewResultResponse generateInterviewResult(Long interviewId,
                                                     List<InterviewResponse> responses,
                                                     String category, String role);

    /**
     * Generate a comprehensive interview result checking all questions (Right vs Not Right).
     */
    InterviewResultResponse generateInterviewResult(Long interviewId,
                                                     List<Question> questions,
                                                     List<InterviewResponse> responses,
                                                     String category, String role);

    /**
     * Evaluate a single question answer with full concept matching (Right / Partial / Incorrect).
     */
    QuestionEvaluationDto evaluateQuestionAnswer(Question question,
                                                String answerText,
                                                int questionNumber,
                                                Integer durationSecs,
                                                Integer wordCount);

    /**
     * Generate an adaptive follow-up question based on the previous answer.
     */
    String generateFollowUpQuestion(String previousQuestion, String previousAnswer, String category);

    /**
     * Generate an overall interview summary for the candidate.
     */
    String generateInterviewSummary(Long interviewId, int overallScore, String category);
}

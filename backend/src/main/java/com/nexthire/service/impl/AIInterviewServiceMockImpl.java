package com.nexthire.service.impl;

import com.nexthire.dto.response.InterviewResultResponse;
import com.nexthire.dto.response.QuestionEvaluationDto;
import com.nexthire.model.InterviewResponse;
import com.nexthire.model.Question;
import com.nexthire.service.AIInterviewService;
import com.nexthire.service.AnswerEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AIInterviewServiceMockImpl — Implementation of AIInterviewService.
 *
 * Uses AnswerEvaluationService to check each interview question answer (Right vs Not Right),
 * score answers, evaluate concepts, and create realistic, accurate results.
 */
@Service
public class AIInterviewServiceMockImpl implements AIInterviewService {

    private final AnswerEvaluationService answerEvaluationService;

    @Autowired
    public AIInterviewServiceMockImpl(AnswerEvaluationService answerEvaluationService) {
        this.answerEvaluationService = answerEvaluationService;
    }

    private static final List<String> TECHNICAL_QUESTIONS = List.of(
            "Explain the difference between HashMap and TreeMap in Java.",
            "What is the difference between an abstract class and an interface?",
            "Explain the SOLID principles with an example.",
            "What is REST and what are its key constraints?",
            "How does garbage collection work in the JVM?",
            "Describe the difference between SQL JOINs (INNER, LEFT, RIGHT, FULL)."
    );

    private static final List<String> HR_QUESTIONS = List.of(
            "Tell me about yourself.",
            "Where do you see yourself in 5 years?",
            "Why do you want to work at this company?",
            "What is your greatest professional strength?",
            "Describe a challenging situation you faced at work and how you handled it.",
            "What are your salary expectations?"
    );

    private static final List<String> BEHAVIORAL_QUESTIONS = List.of(
            "Tell me about a time you had a conflict with a team member. How did you resolve it?",
            "Describe a situation where you had to meet a tight deadline.",
            "Give an example of a time you took initiative beyond your responsibilities.",
            "Tell me about a project you led and what was the outcome?",
            "Describe a time you failed and what you learned from it."
    );

    private static final List<String> CODING_QUESTIONS = List.of(
            "Explain how you would reverse a linked list.",
            "What is the time complexity of binary search and why?",
            "How would you detect a cycle in a linked list?",
            "Explain dynamic programming with a simple example.",
            "What data structure would you use to implement a browser's back button?"
    );

    private static final List<String> MANAGERIAL_QUESTIONS = List.of(
            "How do you prioritize tasks when everything seems equally important?",
            "How do you handle underperforming team members?",
            "Describe your approach to setting team goals.",
            "How do you handle disagreements with senior management?",
            "What metrics do you use to measure team productivity?"
    );

    @Override
    public String generateQuestion(String category, String difficulty, String role,
                                   List<String> previousQuestions) {
        List<String> pool = switch (category.toUpperCase()) {
            case "TECHNICAL"   -> TECHNICAL_QUESTIONS;
            case "HR"          -> HR_QUESTIONS;
            case "BEHAVIORAL"  -> BEHAVIORAL_QUESTIONS;
            case "CODING"      -> CODING_QUESTIONS;
            case "MANAGERIAL"  -> MANAGERIAL_QUESTIONS;
            default -> HR_QUESTIONS;
        };

        return pool.stream()
                .filter(q -> !previousQuestions.contains(q))
                .findFirst()
                .orElse("Tell me about your most recent project.");
    }

    @Override
    public int evaluateAnswer(String questionText, String answerText, String category) {
        Question q = new Question();
        q.setText(questionText);
        q.setCategory(category);
        QuestionEvaluationDto eval = answerEvaluationService.evaluateSingleAnswer(q, answerText, 1, 30, null);
        return eval.getScore();
    }

    @Override
    public String generateAnswerFeedback(String questionText, String answerText, int score) {
        Question q = new Question();
        q.setText(questionText);
        QuestionEvaluationDto eval = answerEvaluationService.evaluateSingleAnswer(q, answerText, 1, 30, null);
        return eval.getFeedback();
    }

    @Override
    public QuestionEvaluationDto evaluateQuestionAnswer(Question question,
                                                        String answerText,
                                                        int questionNumber,
                                                        Integer durationSecs,
                                                        Integer wordCount) {
        return answerEvaluationService.evaluateSingleAnswer(question, answerText, questionNumber, durationSecs, wordCount);
    }

    @Override
    public InterviewResultResponse generateInterviewResult(Long interviewId,
                                                           List<InterviewResponse> responses,
                                                           String category, String role) {
        // Fallback delegating to questions version
        return answerEvaluationService.generateInterviewResult(interviewId, List.of(), responses, category, role);
    }

    @Override
    public InterviewResultResponse generateInterviewResult(Long interviewId,
                                                           List<Question> questions,
                                                           List<InterviewResponse> responses,
                                                           String category, String role) {
        return answerEvaluationService.generateInterviewResult(interviewId, questions, responses, category, role);
    }

    @Override
    public String generateFollowUpQuestion(String previousQuestion, String previousAnswer, String category) {
        return "That's interesting. Can you elaborate more on that with a specific example from your experience?";
    }

    @Override
    public String generateInterviewSummary(Long interviewId, int overallScore, String category) {
        return String.format(
                "You completed a %s interview and scored %d/100 overall. " +
                "Your performance has been evaluated across all technical questions with detailed accuracy scoring.",
                category, overallScore
        );
    }
}

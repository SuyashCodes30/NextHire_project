package com.nexthire.service.impl;

import com.nexthire.dto.request.CreateInterviewRequest;
import com.nexthire.dto.request.SubmitResponseRequest;
import com.nexthire.dto.response.InterviewResponseDto;
import com.nexthire.dto.response.InterviewResultResponse;
import com.nexthire.exception.BadRequestException;
import com.nexthire.exception.ResourceNotFoundException;
import com.nexthire.model.Interview;
import com.nexthire.model.InterviewResponse;
import com.nexthire.model.InterviewResult;
import com.nexthire.model.Question;
import com.nexthire.repository.InterviewRepository;
import com.nexthire.repository.InterviewResponseRepository;
import com.nexthire.repository.InterviewResultRepository;
import com.nexthire.service.AIInterviewService;
import com.nexthire.service.InterviewService;
import com.nexthire.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * InterviewServiceImpl — MySQL persistent implementation of InterviewService.
 *
 * Manages the full interview lifecycle:
 * CREATED → IN_PROGRESS → COMPLETED (or ABANDONED)
 * Persisting interviews, answers, and evaluations into MySQL database.
 */
@Service
public class InterviewServiceImpl implements InterviewService {

    // ---------------------------------------------------------------
    // Allowed values for validation — supports new categories + legacy
    // ---------------------------------------------------------------
    private static final Set<String> VALID_CATEGORIES = Set.of(
            "general-interview", "data-analyst", "data-scientist", "software-developer", "english-communication",
            "general interview", "data analyst", "data scientist", "software developer", "english / communication interview",
            "TECHNICAL", "HR", "BEHAVIORAL", "CODING", "MANAGERIAL"
    );
    private static final Set<String> VALID_DIFFICULTIES = Set.of("EASY", "MEDIUM", "HARD");

    // In-memory cache for interview questions to optimize repeated reads
    private final Map<Long, List<Question>> interviewQuestionsMap = new ConcurrentHashMap<>();

    // Repositories
    private final InterviewRepository interviewRepository;
    private final InterviewResponseRepository interviewResponseRepository;
    private final InterviewResultRepository interviewResultRepository;

    // Injected services
    private final QuestionService questionService;
    private final AIInterviewService aiInterviewService;

    @Autowired
    public InterviewServiceImpl(InterviewRepository interviewRepository,
                                InterviewResponseRepository interviewResponseRepository,
                                InterviewResultRepository interviewResultRepository,
                                QuestionService questionService,
                                AIInterviewService aiInterviewService) {
        this.interviewRepository = interviewRepository;
        this.interviewResponseRepository = interviewResponseRepository;
        this.interviewResultRepository = interviewResultRepository;
        this.questionService = questionService;
        this.aiInterviewService = aiInterviewService;
    }

    /**
     * Create and start a new interview session and persist it to MySQL.
     * Validates category and difficulty, assigns random/diverse questions.
     */
    @Override
    @Transactional
    public InterviewResponseDto createInterview(CreateInterviewRequest request) {
        // Validate category (case-insensitive check)
        String rawCategory = request.getCategory().trim();
        boolean isValidCategory = VALID_CATEGORIES.stream()
                .anyMatch(c -> c.equalsIgnoreCase(rawCategory) || c.replace("-", " ").equalsIgnoreCase(rawCategory.replace("-", " ")));
        if (!isValidCategory) {
            throw new BadRequestException(
                    "Invalid category: '" + request.getCategory() + "'. " +
                    "Allowed values: " + VALID_CATEGORIES
            );
        }
        String category = rawCategory;

        // Validate difficulty
        String difficulty = request.getDifficulty().toUpperCase();
        if (!VALID_DIFFICULTIES.contains(difficulty)) {
            throw new BadRequestException(
                    "Invalid difficulty: '" + request.getDifficulty() + "'. " +
                    "Allowed values: " + VALID_DIFFICULTIES
            );
        }

        LocalDateTime now = LocalDateTime.now();

        // Determine requested question count (defaults to 5 if not provided or <= 0)
        int targetCount = (request.getNumberOfQuestions() != null && request.getNumberOfQuestions() > 0)
                ? request.getNumberOfQuestions()
                : 5;

        // Assign diverse, topic-balanced questions for this interview
        List<Question> questions = questionService.getDiverseQuestions(category, difficulty, targetCount);
        String questionIds = questions.stream()
                .map(q -> String.valueOf(q.getId()))
                .collect(Collectors.joining(","));

        Interview interview = new Interview(
                null,
                request.getUserId(),
                category,
                request.getRole(),
                difficulty,
                "IN_PROGRESS",
                now,
                null,
                null,
                now
        );
        interview.setQuestionIds(questionIds);

        Interview saved = interviewRepository.save(interview);
        interviewQuestionsMap.put(saved.getId(), questions);

        InterviewResponseDto dto = mapToDto(saved, questions.size());
        dto.setQuestions(questions);
        return dto;
    }

    @Override
    public InterviewResponseDto getInterviewById(Long interviewId) {
        Interview interview = findInterviewOrThrow(interviewId);
        List<Question> questions = getOrLoadQuestions(interview);
        InterviewResponseDto dto = mapToDto(interview, questions.size());
        dto.setQuestions(questions);
        return dto;
    }

    @Override
    public List<InterviewResponseDto> getInterviewsByUserId(Long userId) {
        return interviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(i -> {
                    List<Question> questions = getOrLoadQuestions(i);
                    InterviewResponseDto dto = mapToDto(i, questions.size());
                    dto.setQuestions(questions);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Question> getQuestionsForInterview(Long interviewId) {
        Interview interview = findInterviewOrThrow(interviewId);
        return getOrLoadQuestions(interview);
    }

    @Override
    @Transactional
    public InterviewResponse submitResponse(Long interviewId, SubmitResponseRequest request) {
        Interview interview = findInterviewOrThrow(interviewId);

        if ("COMPLETED".equals(interview.getStatus()) || "ABANDONED".equals(interview.getStatus())) {
            throw new BadRequestException(
                    "Cannot submit a response to an interview with status: " + interview.getStatus()
            );
        }

        InterviewResponse response = new InterviewResponse(
                null,
                interviewId,
                request.getQuestionId(),
                request.getResponseText(),
                request.getDurationSecs(),
                request.getWordCount() != null
                        ? request.getWordCount()
                        : (request.getResponseText() != null
                            ? request.getResponseText().split("\\s+").length
                            : 0),
                request.getAnswerMetadata(),
                LocalDateTime.now()
        );

        // Find the matching question to evaluate accuracy
        List<Question> questions = getOrLoadQuestions(interview);
        Question matchingQ = questions.stream()
                .filter(q -> q.getId() != null && q.getId().equals(request.getQuestionId()))
                .findFirst()
                .orElse(null);

        if (matchingQ == null && request.getQuestionId() != null) {
            try {
                matchingQ = questionService.getQuestionById(request.getQuestionId());
            } catch (Exception ignored) {}
        }

        int qNum = interviewResponseRepository.countByInterviewId(interviewId) + 1;
        if (matchingQ != null) {
            com.nexthire.dto.response.QuestionEvaluationDto eval = aiInterviewService.evaluateQuestionAnswer(
                    matchingQ,
                    request.getResponseText(),
                    qNum,
                    request.getDurationSecs(),
                    response.getWordCount()
            );
            response.setScore(eval.getScore());
            response.setIsCorrect(eval.isCorrect());
            response.setStatus(eval.getStatus());
            response.setStatusLabel(eval.getStatusLabel());
            response.setFeedback(eval.getFeedback());
            response.setExpectedAnswer(eval.getExpectedAnswer());
        }

        return interviewResponseRepository.save(response);
    }

    @Override
    @Transactional
    public InterviewResponseDto completeInterview(Long interviewId) {
        Interview interview = findInterviewOrThrow(interviewId);

        if ("COMPLETED".equals(interview.getStatus())) {
            throw new BadRequestException("Interview " + interviewId + " is already completed.");
        }

        LocalDateTime now = LocalDateTime.now();
        interview.setStatus("COMPLETED");
        interview.setCompletedAt(now);

        // Calculate duration
        if (interview.getStartedAt() != null) {
            long seconds = java.time.Duration.between(interview.getStartedAt(), now).getSeconds();
            interview.setDurationSecs((int) seconds);
        }

        interview = interviewRepository.save(interview);

        // Generate AI result derived directly from all question evaluations
        List<InterviewResponse> responses = interviewResponseRepository.findByInterviewIdOrderBySubmittedAtAsc(interviewId);
        List<Question> questions = getOrLoadQuestions(interview);
        InterviewResultResponse result = aiInterviewService.generateInterviewResult(
                interviewId, questions, responses, interview.getCategory(), interview.getRole()
        );

        // Persist InterviewResult in MySQL
        InterviewResult entity = interviewResultRepository.findByInterviewId(interviewId).orElseGet(InterviewResult::new);
        entity.setInterviewId(interviewId);
        entity.setOverallScore(result.getOverallScore());
        entity.setCommunicationScore(result.getCommunicationScore());
        entity.setConfidenceScore(result.getConfidenceScore());
        entity.setTechnicalScore(result.getTechnicalScore());
        entity.setClarityScore(result.getClarityScore());
        entity.setAnswerStructureScore(result.getAnswerStructureScore());
        entity.setOverallFeedback(result.getOverallFeedback());
        entity.setStrengths(result.getStrengths() != null ? String.join(";;", result.getStrengths()) : "");
        entity.setImprovements(result.getImprovements() != null ? String.join(";;", result.getImprovements()) : "");
        entity.setStarEvaluation(result.getStarEvaluation());
        entity.setReadinessLabel(result.getReadinessLabel());
        entity.setGeneratedAt(now);
        interviewResultRepository.save(entity);

        int totalQ = questions.size();
        return mapToDto(interview, totalQ);
    }

    @Override
    public InterviewResultResponse getInterviewResult(Long interviewId) {
        Interview interview = findInterviewOrThrow(interviewId);

        if (!"COMPLETED".equals(interview.getStatus())) {
            throw new BadRequestException(
                    "Results are only available for completed interviews. " +
                    "Current status: " + interview.getStatus()
            );
        }

        List<InterviewResponse> responses = interviewResponseRepository.findByInterviewIdOrderBySubmittedAtAsc(interviewId);
        List<Question> questions = getOrLoadQuestions(interview);
        InterviewResultResponse result = aiInterviewService.generateInterviewResult(
                interviewId, questions, responses, interview.getCategory(), interview.getRole()
        );

        // Ensure InterviewResult is stored if not yet saved
        if (!interviewResultRepository.existsByInterviewId(interviewId)) {
            InterviewResult entity = new InterviewResult();
            entity.setInterviewId(interviewId);
            entity.setOverallScore(result.getOverallScore());
            entity.setCommunicationScore(result.getCommunicationScore());
            entity.setConfidenceScore(result.getConfidenceScore());
            entity.setTechnicalScore(result.getTechnicalScore());
            entity.setClarityScore(result.getClarityScore());
            entity.setAnswerStructureScore(result.getAnswerStructureScore());
            entity.setOverallFeedback(result.getOverallFeedback());
            entity.setStrengths(result.getStrengths() != null ? String.join(";;", result.getStrengths()) : "");
            entity.setImprovements(result.getImprovements() != null ? String.join(";;", result.getImprovements()) : "");
            entity.setStarEvaluation(result.getStarEvaluation());
            entity.setReadinessLabel(result.getReadinessLabel());
            entity.setGeneratedAt(LocalDateTime.now());
            interviewResultRepository.save(entity);
        }

        return result;
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    private List<Question> getOrLoadQuestions(Interview interview) {
        if (interview == null || interview.getId() == null) {
            return List.of();
        }
        return interviewQuestionsMap.computeIfAbsent(interview.getId(), id -> {
            if (interview.getQuestionIds() == null || interview.getQuestionIds().isBlank()) {
                return List.of();
            }
            List<Question> list = new ArrayList<>();
            for (String qIdStr : interview.getQuestionIds().split(",")) {
                try {
                    Long qId = Long.parseLong(qIdStr.trim());
                    Question q = questionService.getQuestionById(qId);
                    if (q != null) {
                        list.add(q);
                    }
                } catch (Exception ignored) {}
            }
            return list;
        });
    }

    private Interview findInterviewOrThrow(Long interviewId) {
        return interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", interviewId));
    }

    private InterviewResponseDto mapToDto(Interview interview, int totalQuestions) {
        InterviewResponseDto dto = new InterviewResponseDto();
        dto.setId(interview.getId());
        dto.setInterviewId(interview.getId());
        dto.setUserId(interview.getUserId());
        dto.setCategory(interview.getCategory());
        dto.setRole(interview.getRole());
        dto.setDifficulty(interview.getDifficulty());
        dto.setStatus(interview.getStatus());
        dto.setTotalQuestions(totalQuestions);
        dto.setNumberOfQuestions(totalQuestions);
        dto.setStartedAt(interview.getStartedAt());
        dto.setCompletedAt(interview.getCompletedAt());
        dto.setDurationSecs(interview.getDurationSecs());
        dto.setCreatedAt(interview.getCreatedAt());
        return dto;
    }
}

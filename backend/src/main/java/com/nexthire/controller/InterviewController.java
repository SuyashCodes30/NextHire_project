package com.nexthire.controller;

import com.nexthire.dto.request.CreateInterviewRequest;
import com.nexthire.dto.request.SubmitResponseRequest;
import com.nexthire.dto.response.ApiResponse;
import com.nexthire.dto.response.InterviewResponseDto;
import com.nexthire.dto.response.InterviewResultResponse;
import com.nexthire.model.InterviewResponse;
import com.nexthire.model.Question;
import com.nexthire.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * InterviewController — Manages the full interview session lifecycle.
 *
 * Base URL: /api/interviews
 *
 * Endpoints:
 *   POST   /api/interviews                        — Create/start a new interview
 *   GET    /api/interviews/{id}                   — Get interview details
 *   GET    /api/interviews?userId={userId}         — Get all interviews for a user
 *   GET    /api/interviews/{id}/questions          — Get questions for an interview
 *   POST   /api/interviews/{id}/responses          — Submit an answer
 *   POST   /api/interviews/{id}/complete           — Complete the interview
 *   GET    /api/interviews/{id}/result             — Get AI-evaluated result
 *
 * Frontend connection:
 *   "Start practicing" button → POST /api/interviews
 *   AI interview session page → GET /api/interviews/{id}/questions
 *   Answer submission         → POST /api/interviews/{id}/responses
 *   Interview end             → POST /api/interviews/{id}/complete
 *   Results/analytics page    → GET /api/interviews/{id}/result
 */
@RestController
@RequestMapping("/api/interviews")
@Tag(name = "Interviews", description = "Interview session management endpoints")
public class InterviewController {

    private final InterviewService interviewService;

    @Autowired
    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    /**
     * POST /api/interviews
     * Create and start a new interview session.
     */
    @PostMapping
    @Operation(summary = "Create interview", description = "Start a new interview session")
    public ResponseEntity<ApiResponse<InterviewResponseDto>> createInterview(
            @Valid @RequestBody CreateInterviewRequest request) {
        InterviewResponseDto interview = interviewService.createInterview(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Interview started successfully.", interview, 201));
    }

    /**
     * GET /api/interviews/{id}
     * Get details of a specific interview.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get interview by ID", description = "Retrieve a specific interview session")
    public ResponseEntity<ApiResponse<InterviewResponseDto>> getInterviewById(@PathVariable Long id) {
        InterviewResponseDto interview = interviewService.getInterviewById(id);
        return ResponseEntity.ok(ApiResponse.ok("Interview retrieved successfully.", interview));
    }

    /**
     * GET /api/interviews?userId={userId}
     * Get all interviews for a specific user.
     */
    @GetMapping
    @Operation(summary = "Get interviews by user", description = "Retrieve all interview sessions for a user")
    public ResponseEntity<ApiResponse<List<InterviewResponseDto>>> getInterviewsByUser(
            @RequestParam(required = false) Long userId) {

        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.ok("No userId provided.", List.of()));
        }
        List<InterviewResponseDto> interviews = interviewService.getInterviewsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(
                "Found " + interviews.size() + " interview(s).", interviews
        ));
    }

    /**
     * GET /api/interviews/{id}/questions
     * Get the questions assigned to this interview session.
     */
    @GetMapping("/{id}/questions")
    @Operation(summary = "Get interview questions", description = "Get all questions for a specific interview")
    public ResponseEntity<ApiResponse<List<Question>>> getQuestionsForInterview(@PathVariable Long id) {
        List<Question> questions = interviewService.getQuestionsForInterview(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Fetched " + questions.size() + " question(s) for interview " + id + ".",
                questions
        ));
    }

    /**
     * POST /api/interviews/{id}/responses
     * Submit a candidate's answer to one question.
     */
    @PostMapping("/{id}/responses")
    @Operation(summary = "Submit answer", description = "Save a candidate's response to a question")
    public ResponseEntity<ApiResponse<InterviewResponse>> submitResponse(
            @PathVariable Long id,
            @Valid @RequestBody SubmitResponseRequest request) {
        InterviewResponse savedResponse = interviewService.submitResponse(id, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Answer submitted successfully.", savedResponse, 201));
    }

    /**
     * POST /api/interviews/{id}/complete
     * Mark the interview as complete and trigger AI result generation.
     */
    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete interview", description = "Mark interview as complete and generate results")
    public ResponseEntity<ApiResponse<InterviewResponseDto>> completeInterview(@PathVariable Long id) {
        InterviewResponseDto interview = interviewService.completeInterview(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Interview completed. Your results are ready!",
                interview
        ));
    }

    /**
     * GET /api/interviews/{id}/result
     * Get the AI-generated result for a completed interview.
     * This powers the analytics/results page shown on the frontend.
     */
    @GetMapping("/{id}/result")
    @Operation(summary = "Get interview result", description = "Get AI-evaluated scores and feedback")
    public ResponseEntity<ApiResponse<InterviewResultResponse>> getInterviewResult(@PathVariable Long id) {
        InterviewResultResponse result = interviewService.getInterviewResult(id);
        return ResponseEntity.ok(ApiResponse.ok("Interview result retrieved successfully.", result));
    }
}

package com.nexthire.service;

import com.nexthire.dto.request.CreateInterviewRequest;
import com.nexthire.dto.request.SubmitResponseRequest;
import com.nexthire.dto.response.InterviewResponseDto;
import com.nexthire.dto.response.InterviewResultResponse;
import com.nexthire.model.InterviewResponse;
import com.nexthire.model.Question;

import java.util.List;

/**
 * InterviewService — Interface defining all interview-related operations.
 *
 * This service handles the full interview lifecycle:
 * CREATED → IN_PROGRESS → COMPLETED (or ABANDONED)
 */
public interface InterviewService {

    /**
     * Create and start a new interview session.
     * Sets status to IN_PROGRESS and records start time.
     *
     * @param request Interview parameters (category, role, difficulty, userId)
     * @return Created interview details
     *
     * POST /api/interviews
     */
    InterviewResponseDto createInterview(CreateInterviewRequest request);

    /**
     * Get details of a specific interview.
     *
     * @param interviewId The interview ID
     * @return Interview session details
     *
     * GET /api/interviews/{id}
     */
    InterviewResponseDto getInterviewById(Long interviewId);

    /**
     * Get all interviews for a specific user.
     *
     * @param userId The user's ID
     * @return List of interview sessions
     *
     * GET /api/interviews?userId={userId}
     */
    List<InterviewResponseDto> getInterviewsByUserId(Long userId);

    /**
     * Get the questions assigned to a specific interview session.
     *
     * @param interviewId The interview ID
     * @return List of questions for this interview
     *
     * GET /api/interviews/{id}/questions
     */
    List<Question> getQuestionsForInterview(Long interviewId);

    /**
     * Save a candidate's response to a question during an interview.
     *
     * @param interviewId The interview ID
     * @param request     The response data (questionId, responseText, etc.)
     * @return The saved response record
     *
     * POST /api/interviews/{id}/responses
     */
    InterviewResponse submitResponse(Long interviewId, SubmitResponseRequest request);

    /**
     * Mark an interview as complete and trigger result generation.
     *
     * @param interviewId The interview ID
     * @return The completed interview details
     *
     * POST /api/interviews/{id}/complete
     */
    InterviewResponseDto completeInterview(Long interviewId);

    /**
     * Get the AI-generated result of a completed interview.
     *
     * @param interviewId The interview ID
     * @return Detailed result with scores, feedback, strengths, improvements
     *
     * GET /api/interviews/{id}/result
     */
    InterviewResultResponse getInterviewResult(Long interviewId);
}

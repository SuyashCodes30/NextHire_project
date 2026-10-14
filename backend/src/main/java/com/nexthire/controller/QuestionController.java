package com.nexthire.controller;

import com.nexthire.dto.response.ApiResponse;
import com.nexthire.model.Question;
import com.nexthire.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "Questions", description = "Interview question bank endpoints")
public class QuestionController {

    private final QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * GET /api/questions
     * Query parameters:
     *   - category (e.g. data-analyst, data-scientist, software-developer, english-communication, general-interview)
     *   - difficulty (EASY, MEDIUM, HARD)
     *   - topic (SQL, Python, DSA, Grammar, etc.)
     *   - type (TECHNICAL, HR, COMMUNICATION, etc.)
     */
    @GetMapping
    @Operation(summary = "Get questions", description = "Get all questions or filter by category, difficulty, topic, or question type")
    public ResponseEntity<ApiResponse<List<Question>>> getQuestions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String type) {

        List<Question> questions = questionService.searchQuestions(category, difficulty, topic, type);

        return ResponseEntity.ok(ApiResponse.ok(
                "Found " + questions.size() + " question(s).", questions
        ));
    }

    /**
     * GET /api/questions/{id}
     * Get a specific question by its ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get question by ID", description = "Retrieve a specific interview question")
    public ResponseEntity<ApiResponse<Question>> getQuestionById(@PathVariable Long id) {
        Question question = questionService.getQuestionById(id);
        return ResponseEntity.ok(ApiResponse.ok("Question retrieved successfully.", question));
    }
}

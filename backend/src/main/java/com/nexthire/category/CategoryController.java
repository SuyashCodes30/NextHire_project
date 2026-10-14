package com.nexthire.category;

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
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Interview category management endpoints")
public class CategoryController {

    private final CategoryService categoryService;
    private final QuestionService questionService;

    @Autowired
    public CategoryController(CategoryService categoryService, QuestionService questionService) {
        this.categoryService = categoryService;
        this.questionService = questionService;
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve list of all active interview categories")
    public ResponseEntity<ApiResponse<List<InterviewCategory>>> getAllCategories() {
        List<InterviewCategory> list = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.ok("Retrieved " + list.size() + " category(ies).", list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve a specific interview category by ID or slug")
    public ResponseEntity<ApiResponse<InterviewCategory>> getCategoryById(@PathVariable String id) {
        InterviewCategory category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.ok("Category found.", category));
    }

    @GetMapping("/{id}/questions")
    @Operation(summary = "Get questions for category", description = "Retrieve all questions belonging to this category")
    public ResponseEntity<ApiResponse<List<Question>>> getQuestionsForCategory(
            @PathVariable String id,
            @RequestParam(required = false) String difficulty) {
        // Ensure category exists
        InterviewCategory category = categoryService.getCategoryById(id);

        List<Question> questions;
        if (difficulty != null && !difficulty.isBlank()) {
            questions = questionService.getQuestionsByCategoryAndDifficulty(category.getCategoryId(), difficulty);
        } else {
            questions = questionService.getQuestionsByCategory(category.getCategoryId());
        }

        return ResponseEntity.ok(ApiResponse.ok(
                "Found " + questions.size() + " question(s) for category '" + category.getCategoryName() + "'.",
                questions
        ));
    }
}

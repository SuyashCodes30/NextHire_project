package com.nexthire.learning;

import com.nexthire.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning")
@Tag(name = "Learning Module", description = "English and career preparation learning academy endpoints")
public class LearningController {

    private final LearningService learningService;

    @Autowired
    public LearningController(LearningService learningService) {
        this.learningService = learningService;
    }

    @GetMapping
    @Operation(summary = "Get learning overview", description = "Overview of all educational modules available in NextHire")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLearningOverview() {
        return ResponseEntity.ok(ApiResponse.ok("Learning overview retrieved.", learningService.getLearningOverview()));
    }

    @GetMapping("/english")
    @Operation(summary = "Get English track overview", description = "Overview of the English & Communication track")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEnglishOverview() {
        return ResponseEntity.ok(ApiResponse.ok("English track overview retrieved.", learningService.getEnglishOverview()));
    }

    @GetMapping("/english/topics")
    @Operation(summary = "Get all English learning topics", description = "Lists all 6 English communication topics")
    public ResponseEntity<ApiResponse<List<LearningTopic>>> getEnglishTopics() {
        List<LearningTopic> topics = learningService.getEnglishTopics();
        return ResponseEntity.ok(ApiResponse.ok("Retrieved " + topics.size() + " English topics.", topics));
    }

    @GetMapping("/english/{topicId}")
    @Operation(summary = "Get single English topic with lessons and practice", description = "Returns lessons, examples, and practice questions for a topic")
    public ResponseEntity<ApiResponse<LearningTopic>> getEnglishTopicById(@PathVariable String topicId) {
        LearningTopic topic = learningService.getEnglishTopicById(topicId);
        return ResponseEntity.ok(ApiResponse.ok("Topic retrieved: " + topic.getTitle(), topic));
    }
}

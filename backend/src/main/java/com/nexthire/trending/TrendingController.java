package com.nexthire.trending;

import com.nexthire.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trending")
@Tag(name = "Trending", description = "Trending interview questions, industry skills and popular topics")
public class TrendingController {

    private final TrendingService trendingService;

    @Autowired
    public TrendingController(TrendingService trendingService) {
        this.trendingService = trendingService;
    }

    @GetMapping
    @Operation(summary = "Get all trending topics", description = "Retrieve list of popular interview topics and frequent skills")
    public ResponseEntity<ApiResponse<List<TrendingItem>>> getAllTrending() {
        List<TrendingItem> items = trendingService.getAllTrending();
        return ResponseEntity.ok(ApiResponse.ok("Retrieved " + items.size() + " trending item(s).", items));
    }

    @GetMapping("/categories/{category}")
    @Operation(summary = "Get trending topics for category", description = "Retrieve trending questions and topics for a specific domain")
    public ResponseEntity<ApiResponse<List<TrendingItem>>> getTrendingByCategory(@PathVariable String category) {
        List<TrendingItem> items = trendingService.getTrendingByCategory(category);
        return ResponseEntity.ok(ApiResponse.ok("Retrieved " + items.size() + " trending item(s) for " + category + ".", items));
    }
}

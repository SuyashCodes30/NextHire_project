package com.nexthire.controller;

import com.nexthire.dto.response.ApiResponse;
import com.nexthire.dto.response.DashboardResponse;
import com.nexthire.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * DashboardController — Provides aggregated data for the user dashboard.
 *
 * Base URL: /api/dashboard
 *
 * Endpoints:
 *   GET /api/dashboard                  — Full dashboard (streak, XP, stats, recent interviews)
 *   GET /api/dashboard/statistics       — Stats only (lighter endpoint)
 *
 * Frontend connection:
 *   - Navbar streak count (🔥) ← streakDays from dashboard
 *   - Navbar XP count (⚡)     ← xpPoints from dashboard
 *   - Gamification progress    ← step1-4 completion status
 *   - Achievement banner       ← streakDays + xpPoints
 *   - Recent interview list    ← recentInterviews array
 */
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "User dashboard statistics and activity endpoints")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * GET /api/dashboard?userId={userId}
     *
     * Get the full dashboard for a user.
     * TODO: When auth is implemented, extract userId from JWT token instead of query param.
     */
    @GetMapping
    @Operation(summary = "Get dashboard", description = "Get full dashboard data including stats and recent interviews")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @RequestParam(defaultValue = "1") Long userId) {
        DashboardResponse dashboard = dashboardService.getDashboard(userId);
        return ResponseEntity.ok(ApiResponse.ok("Dashboard data retrieved successfully.", dashboard));
    }

    /**
     * GET /api/dashboard/statistics?userId={userId}
     *
     * Get only statistics without recent interviews list.
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get statistics", description = "Get only interview statistics (lighter endpoint)")
    public ResponseEntity<ApiResponse<DashboardResponse>> getStatistics(
            @RequestParam(defaultValue = "1") Long userId) {
        DashboardResponse stats = dashboardService.getStatistics(userId);
        return ResponseEntity.ok(ApiResponse.ok("Statistics retrieved successfully.", stats));
    }
}

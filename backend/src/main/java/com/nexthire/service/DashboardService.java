package com.nexthire.service;

import com.nexthire.dto.response.DashboardResponse;

/**
 * DashboardService — Interface for providing dashboard statistics.
 *
 * The dashboard powers the gamification section of the frontend:
 * - Streak count (🔥) shown in the navbar
 * - XP points (⚡) shown in the navbar
 * - Progression path (Steps 1-4)
 * - Achievement banner
 * - Recent interviews list
 */
public interface DashboardService {

    /**
     * Get full dashboard data for a user.
     *
     * @param userId The user's ID (from session/token)
     * @return Complete dashboard statistics and recent activity
     *
     * GET /api/dashboard?userId={userId}
     * GET /api/dashboard (when auth extracts userId from token)
     */
    DashboardResponse getDashboard(Long userId);

    /**
     * Get only interview statistics for a user.
     * Lighter endpoint when only stats are needed (not recent interviews).
     *
     * GET /api/dashboard/statistics
     */
    DashboardResponse getStatistics(Long userId);
}

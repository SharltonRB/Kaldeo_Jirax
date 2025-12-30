package com.issuetracker.controller;

import com.issuetracker.dto.DashboardMetricsDto;
import com.issuetracker.entity.User;
import com.issuetracker.service.DashboardService;
import com.issuetracker.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for dashboard metrics and reporting operations.
 * Provides endpoints for retrieving aggregated statistics and real-time metrics
 * with caching for performance optimization.
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;
    private final UserService userService;

    @Autowired
    public DashboardController(DashboardService dashboardService, UserService userService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
    }

    /**
     * Retrieves comprehensive dashboard metrics for the authenticated user.
     * Includes project, issue, and sprint statistics with caching for performance.
     *
     * @return dashboard metrics DTO
     */
    @GetMapping("/metrics")
    @Cacheable(value = "dashboardMetrics", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public ResponseEntity<DashboardMetricsDto> getDashboardMetrics() {
        logger.debug("Retrieving dashboard metrics for authenticated user");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + authentication.getName()));

        DashboardMetricsDto metrics = dashboardService.calculateDashboardMetrics(user);

        logger.info("Retrieved dashboard metrics for user {}: {} projects, {} issues, {} sprints",
                   user.getId(), metrics.getProjectMetrics().getTotalProjects(),
                   metrics.getIssueMetrics().getTotalIssues(), metrics.getSprintMetrics().getTotalSprints());

        return ResponseEntity.ok(metrics);
    }

    /**
     * Retrieves real-time statistics for a specific project.
     * Provides detailed project metrics including issue distribution and counts.
     *
     * @param projectId the project ID
     * @return project statistics map
     */
    @GetMapping("/projects/{projectId}/statistics")
    @Cacheable(value = "projectStatistics", key = "#projectId + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public ResponseEntity<Map<String, Object>> getProjectStatistics(@PathVariable Long projectId) {
        logger.debug("Retrieving project statistics for project {} and authenticated user", projectId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + authentication.getName()));

        Map<String, Object> statistics = dashboardService.getProjectStatistics(projectId, user);

        if (statistics.isEmpty()) {
            logger.warn("Project {} not found or not accessible for user {}", projectId, user.getId());
            return ResponseEntity.notFound().build();
        }

        logger.info("Retrieved project statistics for project {} and user {}: {} total issues",
                   projectId, user.getId(), statistics.get("totalIssues"));

        return ResponseEntity.ok(statistics);
    }

    /**
     * Retrieves real-time statistics for a specific sprint.
     * Provides detailed sprint metrics including completion percentage and story points.
     *
     * @param sprintId the sprint ID
     * @return sprint statistics map
     */
    @GetMapping("/sprints/{sprintId}/statistics")
    @Cacheable(value = "sprintStatistics", key = "#sprintId + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public ResponseEntity<Map<String, Object>> getSprintStatistics(@PathVariable Long sprintId) {
        logger.debug("Retrieving sprint statistics for sprint {} and authenticated user", sprintId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + authentication.getName()));

        Map<String, Object> statistics = dashboardService.getSprintStatistics(sprintId, user);

        if (statistics.isEmpty()) {
            logger.warn("Sprint {} not found or not accessible for user {}", sprintId, user.getId());
            return ResponseEntity.notFound().build();
        }

        logger.info("Retrieved sprint statistics for sprint {} and user {}: {} total issues, {}% complete",
                   sprintId, user.getId(), statistics.get("totalIssues"), statistics.get("completionPercentage"));

        return ResponseEntity.ok(statistics);
    }

    /**
     * Retrieves summary metrics for quick dashboard overview.
     * Provides essential counts and percentages for dashboard widgets.
     *
     * @return summary metrics map
     */
    @GetMapping("/summary")
    @Cacheable(value = "dashboardSummary", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        logger.debug("Retrieving dashboard summary for authenticated user");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + authentication.getName()));

        DashboardMetricsDto metrics = dashboardService.calculateDashboardMetrics(user);

        // Create summary with key metrics
        Map<String, Object> summary = Map.of(
            "totalProjects", metrics.getProjectMetrics().getTotalProjects(),
            "activeProjects", metrics.getProjectMetrics().getActiveProjects(),
            "totalIssues", metrics.getIssueMetrics().getTotalIssues(),
            "backlogIssues", metrics.getIssueMetrics().getBacklogIssues(),
            "completedIssues", metrics.getIssueMetrics().getCompletedIssues(),
            "totalSprints", metrics.getSprintMetrics().getTotalSprints(),
            "activeSprint", metrics.getSprintMetrics().getActiveSprint() != null ? 
                           metrics.getSprintMetrics().getActiveSprint() : null,
            "averageSprintCompletion", metrics.getSprintMetrics().getAverageSprintCompletion()
        );

        logger.info("Retrieved dashboard summary for user {}", user.getId());

        return ResponseEntity.ok(summary);
    }

    /**
     * Forces cache refresh for dashboard metrics.
     * Useful for ensuring real-time updates when needed.
     *
     * @return updated dashboard metrics
     */
    @PostMapping("/refresh")
    public ResponseEntity<DashboardMetricsDto> refreshDashboardMetrics() {
        logger.debug("Refreshing dashboard metrics for authenticated user");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + authentication.getName()));

        // Clear cache and recalculate metrics
        DashboardMetricsDto metrics = dashboardService.calculateDashboardMetrics(user);

        logger.info("Refreshed dashboard metrics for user {}", user.getId());

        return ResponseEntity.ok(metrics);
    }
}
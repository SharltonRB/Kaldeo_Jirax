package com.issuetracker.dto;

import com.issuetracker.entity.IssueStatus;
import com.issuetracker.entity.Priority;
import com.issuetracker.entity.SprintStatus;

import java.util.Map;

/**
 * DTO for dashboard metrics data.
 * Contains aggregated statistics for projects, issues, and sprints.
 */
public class DashboardMetricsDto {

    private final ProjectMetrics projectMetrics;
    private final IssueMetrics issueMetrics;
    private final SprintMetrics sprintMetrics;

    public DashboardMetricsDto(ProjectMetrics projectMetrics, 
                              IssueMetrics issueMetrics, 
                              SprintMetrics sprintMetrics) {
        this.projectMetrics = projectMetrics;
        this.issueMetrics = issueMetrics;
        this.sprintMetrics = sprintMetrics;
    }

    public ProjectMetrics getProjectMetrics() {
        return projectMetrics;
    }

    public IssueMetrics getIssueMetrics() {
        return issueMetrics;
    }

    public SprintMetrics getSprintMetrics() {
        return sprintMetrics;
    }

    /**
     * Project-related metrics.
     */
    public static class ProjectMetrics {
        private final long totalProjects;
        private final long activeProjects;
        private final Map<String, Long> issuesPerProject;

        public ProjectMetrics(long totalProjects, long activeProjects, Map<String, Long> issuesPerProject) {
            this.totalProjects = totalProjects;
            this.activeProjects = activeProjects;
            this.issuesPerProject = issuesPerProject;
        }

        public long getTotalProjects() {
            return totalProjects;
        }

        public long getActiveProjects() {
            return activeProjects;
        }

        public Map<String, Long> getIssuesPerProject() {
            return issuesPerProject;
        }
    }

    /**
     * Issue-related metrics.
     */
    public static class IssueMetrics {
        private final long totalIssues;
        private final Map<IssueStatus, Long> issuesByStatus;
        private final Map<Priority, Long> issuesByPriority;
        private final long backlogIssues;
        private final long completedIssues;

        public IssueMetrics(long totalIssues, 
                           Map<IssueStatus, Long> issuesByStatus,
                           Map<Priority, Long> issuesByPriority,
                           long backlogIssues,
                           long completedIssues) {
            this.totalIssues = totalIssues;
            this.issuesByStatus = issuesByStatus;
            this.issuesByPriority = issuesByPriority;
            this.backlogIssues = backlogIssues;
            this.completedIssues = completedIssues;
        }

        public long getTotalIssues() {
            return totalIssues;
        }

        public Map<IssueStatus, Long> getIssuesByStatus() {
            return issuesByStatus;
        }

        public Map<Priority, Long> getIssuesByPriority() {
            return issuesByPriority;
        }

        public long getBacklogIssues() {
            return backlogIssues;
        }

        public long getCompletedIssues() {
            return completedIssues;
        }
    }

    /**
     * Sprint-related metrics.
     */
    public static class SprintMetrics {
        private final long totalSprints;
        private final Map<SprintStatus, Long> sprintsByStatus;
        private final SprintProgressDto activeSprint;
        private final double averageSprintCompletion;

        public SprintMetrics(long totalSprints, 
                            Map<SprintStatus, Long> sprintsByStatus,
                            SprintProgressDto activeSprint,
                            double averageSprintCompletion) {
            this.totalSprints = totalSprints;
            this.sprintsByStatus = sprintsByStatus;
            this.activeSprint = activeSprint;
            this.averageSprintCompletion = averageSprintCompletion;
        }

        public long getTotalSprints() {
            return totalSprints;
        }

        public Map<SprintStatus, Long> getSprintsByStatus() {
            return sprintsByStatus;
        }

        public SprintProgressDto getActiveSprint() {
            return activeSprint;
        }

        public double getAverageSprintCompletion() {
            return averageSprintCompletion;
        }
    }
}
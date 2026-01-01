package com.issuetracker.dto;

import java.time.LocalDate;

/**
 * DTO for sprint progress information.
 * Contains detailed progress metrics for an active sprint.
 */
public class SprintProgressDto {

    private final Long sprintId;
    private final String sprintName;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final long totalIssues;
    private final long completedIssues;
    private final long inProgressIssues;
    private final long remainingIssues;
    private final double completionPercentage;
    private final long totalStoryPoints;
    private final long completedStoryPoints;
    private final int daysRemaining;
    private final int totalDays;

    public SprintProgressDto(Long sprintId, String sprintName, LocalDate startDate, LocalDate endDate,
                            long totalIssues, long completedIssues, long inProgressIssues, long remainingIssues,
                            double completionPercentage, long totalStoryPoints, long completedStoryPoints,
                            int daysRemaining, int totalDays) {
        this.sprintId = sprintId;
        this.sprintName = sprintName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalIssues = totalIssues;
        this.completedIssues = completedIssues;
        this.inProgressIssues = inProgressIssues;
        this.remainingIssues = remainingIssues;
        this.completionPercentage = completionPercentage;
        this.totalStoryPoints = totalStoryPoints;
        this.completedStoryPoints = completedStoryPoints;
        this.daysRemaining = daysRemaining;
        this.totalDays = totalDays;
    }

    public Long getSprintId() {
        return sprintId;
    }

    public String getSprintName() {
        return sprintName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public long getTotalIssues() {
        return totalIssues;
    }

    public long getCompletedIssues() {
        return completedIssues;
    }

    public long getInProgressIssues() {
        return inProgressIssues;
    }

    public long getRemainingIssues() {
        return remainingIssues;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public long getTotalStoryPoints() {
        return totalStoryPoints;
    }

    public long getCompletedStoryPoints() {
        return completedStoryPoints;
    }

    public int getDaysRemaining() {
        return daysRemaining;
    }

    public int getTotalDays() {
        return totalDays;
    }
}
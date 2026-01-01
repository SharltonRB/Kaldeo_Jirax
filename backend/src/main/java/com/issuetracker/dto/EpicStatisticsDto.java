package com.issuetracker.dto;

/**
 * Data Transfer Object for epic statistics.
 * Contains aggregated information about epics and their child issues.
 */
public class EpicStatisticsDto {

    private long totalEpics;
    private long totalChildIssues;

    // Constructors
    public EpicStatisticsDto() {}

    public EpicStatisticsDto(long totalEpics, long totalChildIssues) {
        this.totalEpics = totalEpics;
        this.totalChildIssues = totalChildIssues;
    }

    // Getters and Setters
    public long getTotalEpics() {
        return totalEpics;
    }

    public void setTotalEpics(long totalEpics) {
        this.totalEpics = totalEpics;
    }

    public long getTotalChildIssues() {
        return totalChildIssues;
    }

    public void setTotalChildIssues(long totalChildIssues) {
        this.totalChildIssues = totalChildIssues;
    }

    @Override
    public String toString() {
        return "EpicStatisticsDto{" +
                "totalEpics=" + totalEpics +
                ", totalChildIssues=" + totalChildIssues +
                '}';
    }
}
package com.issuetracker.entity;

/**
 * Status values for issues following the fixed workflow progression.
 */
public enum IssueStatus {
    BACKLOG,
    SELECTED_FOR_DEVELOPMENT,
    IN_PROGRESS,
    IN_REVIEW,
    DONE
}
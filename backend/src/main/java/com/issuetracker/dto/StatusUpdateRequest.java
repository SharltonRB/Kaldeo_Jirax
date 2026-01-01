package com.issuetracker.dto;

import com.issuetracker.entity.IssueStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating issue status.
 * Contains validation rules for status transitions.
 */
public class StatusUpdateRequest {

    @NotNull(message = "New status is required")
    private IssueStatus newStatus;

    // Constructors
    public StatusUpdateRequest() {}

    public StatusUpdateRequest(IssueStatus newStatus) {
        this.newStatus = newStatus;
    }

    // Getters and Setters
    public IssueStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(IssueStatus newStatus) {
        this.newStatus = newStatus;
    }

    @Override
    public String toString() {
        return "StatusUpdateRequest{" +
                "newStatus=" + newStatus +
                '}';
    }
}
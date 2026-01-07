package com.issuetracker.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for adding issues to a sprint.
 */
public class AddIssuesToSprintRequest {

    @NotNull(message = "Issue IDs cannot be null")
    @NotEmpty(message = "At least one issue ID must be provided")
    private List<Long> issueIds;

    public AddIssuesToSprintRequest() {}

    public AddIssuesToSprintRequest(List<Long> issueIds) {
        this.issueIds = issueIds;
    }

    public List<Long> getIssueIds() {
        return issueIds;
    }

    public void setIssueIds(List<Long> issueIds) {
        this.issueIds = issueIds;
    }
}
package com.issuetracker.dto;

import com.issuetracker.entity.Priority;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for creating a new issue.
 * Contains validation rules for issue creation.
 */
public class CreateIssueRequest {

    @NotBlank(message = "Issue title is required")
    @Size(min = 1, max = 255, message = "Issue title must be between 1 and 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Issue priority is required")
    private Priority priority;

    @Min(value = 0, message = "Story points must be non-negative")
    private Integer storyPoints;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Issue type ID is required")
    private Long issueTypeId;

    private Long sprintId;

    private List<Long> labelIds;

    // Constructors
    public CreateIssueRequest() {}

    public CreateIssueRequest(String title, String description, Priority priority, Long projectId, Long issueTypeId) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.projectId = projectId;
        this.issueTypeId = issueTypeId;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Integer getStoryPoints() {
        return storyPoints;
    }

    public void setStoryPoints(Integer storyPoints) {
        this.storyPoints = storyPoints;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getIssueTypeId() {
        return issueTypeId;
    }

    public void setIssueTypeId(Long issueTypeId) {
        this.issueTypeId = issueTypeId;
    }

    public Long getSprintId() {
        return sprintId;
    }

    public void setSprintId(Long sprintId) {
        this.sprintId = sprintId;
    }

    public List<Long> getLabelIds() {
        return labelIds;
    }

    public void setLabelIds(List<Long> labelIds) {
        this.labelIds = labelIds;
    }

    @Override
    public String toString() {
        return "CreateIssueRequest{" +
                "title='" + title + '\'' +
                ", priority=" + priority +
                ", projectId=" + projectId +
                '}';
    }
}
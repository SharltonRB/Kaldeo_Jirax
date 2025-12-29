package com.issuetracker.dto;

import com.issuetracker.entity.Priority;
import com.issuetracker.validation.ValidStoryPoints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for updating an existing issue.
 * Contains validation rules for issue updates.
 */
public class UpdateIssueRequest {

    @NotBlank(message = "Issue title is required")
    @Size(min = 1, max = 255, message = "Issue title must be between 1 and 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Priority priority;

    @ValidStoryPoints
    private Integer storyPoints;

    private Long sprintId;

    private List<Long> labelIds;

    // Constructors
    public UpdateIssueRequest() {}

    public UpdateIssueRequest(String title, String description, Priority priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
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
        return "UpdateIssueRequest{" +
                "title='" + title + '\'' +
                ", priority=" + priority +
                '}';
    }
}
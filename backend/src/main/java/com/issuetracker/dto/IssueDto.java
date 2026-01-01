package com.issuetracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.issuetracker.entity.IssueStatus;
import com.issuetracker.entity.Priority;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object for Issue entity.
 * Used for API responses and data transfer.
 */
public class IssueDto {

    private Long id;

    @NotBlank(message = "Issue title is required")
    @Size(max = 255, message = "Issue title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Issue status is required")
    private IssueStatus status;

    @NotNull(message = "Issue priority is required")
    private Priority priority;

    @Min(value = 0, message = "Story points must be non-negative")
    private Integer storyPoints;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;

    // Related entity information
    private Long projectId;
    private String projectName;
    private String projectKey;
    
    private Long sprintId;
    private String sprintName;
    
    private Long issueTypeId;
    private String issueTypeName;
    
    private List<LabelDto> labels;
    private Long commentCount;
    
    // Epic hierarchy information
    private Long parentIssueId;
    private String parentIssueTitle;
    private boolean isEpic;
    private Long childIssueCount;

    // Constructors
    public IssueDto() {}

    public IssueDto(Long id, String title, String description, IssueStatus status, Priority priority, 
                   Integer storyPoints, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.storyPoints = storyPoints;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public Long getSprintId() {
        return sprintId;
    }

    public void setSprintId(Long sprintId) {
        this.sprintId = sprintId;
    }

    public String getSprintName() {
        return sprintName;
    }

    public void setSprintName(String sprintName) {
        this.sprintName = sprintName;
    }

    public Long getIssueTypeId() {
        return issueTypeId;
    }

    public void setIssueTypeId(Long issueTypeId) {
        this.issueTypeId = issueTypeId;
    }

    public String getIssueTypeName() {
        return issueTypeName;
    }

    public void setIssueTypeName(String issueTypeName) {
        this.issueTypeName = issueTypeName;
    }

    public List<LabelDto> getLabels() {
        return labels;
    }

    public void setLabels(List<LabelDto> labels) {
        this.labels = labels;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Long getParentIssueId() {
        return parentIssueId;
    }

    public void setParentIssueId(Long parentIssueId) {
        this.parentIssueId = parentIssueId;
    }

    public String getParentIssueTitle() {
        return parentIssueTitle;
    }

    public void setParentIssueTitle(String parentIssueTitle) {
        this.parentIssueTitle = parentIssueTitle;
    }

    public boolean isEpic() {
        return isEpic;
    }

    public void setEpic(boolean epic) {
        isEpic = epic;
    }

    public Long getChildIssueCount() {
        return childIssueCount;
    }

    public void setChildIssueCount(Long childIssueCount) {
        this.childIssueCount = childIssueCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IssueDto issueDto = (IssueDto) o;
        return Objects.equals(id, issueDto.id) &&
               Objects.equals(title, issueDto.title) &&
               Objects.equals(description, issueDto.description) &&
               status == issueDto.status &&
               priority == issueDto.priority &&
               Objects.equals(storyPoints, issueDto.storyPoints) &&
               Objects.equals(createdAt, issueDto.createdAt) &&
               Objects.equals(updatedAt, issueDto.updatedAt) &&
               Objects.equals(projectId, issueDto.projectId) &&
               Objects.equals(sprintId, issueDto.sprintId) &&
               Objects.equals(issueTypeId, issueDto.issueTypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, status, priority, storyPoints, 
                           createdAt, updatedAt, projectId, sprintId, issueTypeId);
    }

    @Override
    public String toString() {
        return "IssueDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", projectKey='" + projectKey + '\'' +
                '}';
    }
}
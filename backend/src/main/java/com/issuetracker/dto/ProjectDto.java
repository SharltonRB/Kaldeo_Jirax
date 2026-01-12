package com.issuetracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.issuetracker.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Data Transfer Object for Project entity.
 * Used for API responses and data transfer.
 */
public class ProjectDto {

    private Long id;

    @NotBlank(message = "Project name is required")
    @Size(max = 100, message = "Project name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Project key is required")
    @Size(max = 10, message = "Project key must not exceed 10 characters")
    private String key;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private ProjectStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;

    private Long issueCount;

    // Constructors
    public ProjectDto() {}

    public ProjectDto(Long id, String name, String key, String description, ProjectStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.key = key;
        this.description = description;
        this.status = status;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
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

    public Long getIssueCount() {
        return issueCount;
    }

    public void setIssueCount(Long issueCount) {
        this.issueCount = issueCount;
    }

    @Override
    public String toString() {
        return "ProjectDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", key='" + key + '\'' +
                ", status=" + status +
                ", issueCount=" + issueCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectDto that = (ProjectDto) o;
        return java.util.Objects.equals(id, that.id) &&
               java.util.Objects.equals(name, that.name) &&
               java.util.Objects.equals(key, that.key) &&
               java.util.Objects.equals(description, that.description) &&
               java.util.Objects.equals(status, that.status) &&
               java.util.Objects.equals(createdAt, that.createdAt) &&
               java.util.Objects.equals(updatedAt, that.updatedAt) &&
               java.util.Objects.equals(issueCount, that.issueCount);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, key, description, status, createdAt, updatedAt, issueCount);
    }
}
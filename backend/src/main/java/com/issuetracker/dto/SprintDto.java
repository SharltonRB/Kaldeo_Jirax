package com.issuetracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.issuetracker.entity.SprintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Data Transfer Object for Sprint entity.
 * Used for API responses and data transfer.
 */
public class SprintDto {

    private Long id;

    @NotBlank(message = "Sprint name is required")
    @Size(max = 100, message = "Sprint name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Start date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotNull(message = "Sprint status is required")
    private SprintStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;

    private Long issueCount;
    private Long completedIssueCount;

    // Constructors
    public SprintDto() {}

    public SprintDto(Long id, String name, LocalDate startDate, LocalDate endDate, 
                    SprintStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public SprintStatus getStatus() {
        return status;
    }

    public void setStatus(SprintStatus status) {
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

    public Long getCompletedIssueCount() {
        return completedIssueCount;
    }

    public void setCompletedIssueCount(Long completedIssueCount) {
        this.completedIssueCount = completedIssueCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SprintDto sprintDto = (SprintDto) o;
        return Objects.equals(id, sprintDto.id) &&
               Objects.equals(name, sprintDto.name) &&
               Objects.equals(startDate, sprintDto.startDate) &&
               Objects.equals(endDate, sprintDto.endDate) &&
               status == sprintDto.status &&
               Objects.equals(createdAt, sprintDto.createdAt) &&
               Objects.equals(updatedAt, sprintDto.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, startDate, endDate, status, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "SprintDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", issueCount=" + issueCount +
                '}';
    }
}
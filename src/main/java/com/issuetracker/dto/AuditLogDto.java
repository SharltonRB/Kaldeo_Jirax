package com.issuetracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.Objects;

/**
 * Data Transfer Object for AuditLog entity.
 * Used for API responses and data transfer.
 */
public class AuditLogDto {

    private Long id;
    private String action;
    private String details;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;
    
    // User information
    private Long userId;
    private String userName;
    private String userEmail;
    
    // Issue information
    private Long issueId;
    private String issueTitle;

    // Constructors
    public AuditLogDto() {}

    public AuditLogDto(Long id, String action, String details, Instant createdAt) {
        this.id = id;
        this.action = action;
        this.details = details;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getIssueTitle() {
        return issueTitle;
    }

    public void setIssueTitle(String issueTitle) {
        this.issueTitle = issueTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLogDto that = (AuditLogDto) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(action, that.action) &&
               Objects.equals(details, that.details) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(issueId, that.issueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, action, details, createdAt, userId, issueId);
    }

    @Override
    public String toString() {
        return "AuditLogDto{" +
                "id=" + id +
                ", action='" + action + '\'' +
                ", createdAt=" + createdAt +
                ", userName='" + userName + '\'' +
                '}';
    }
}
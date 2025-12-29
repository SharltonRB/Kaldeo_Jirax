package com.issuetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new comment.
 * Contains validation rules for comment creation.
 */
public class CreateCommentRequest {

    @NotBlank(message = "Comment content is required")
    @Size(min = 1, max = 5000, message = "Comment content must be between 1 and 5000 characters")
    private String content;

    @NotNull(message = "Issue ID is required")
    private Long issueId;

    // Constructors
    public CreateCommentRequest() {}

    public CreateCommentRequest(String content, Long issueId) {
        this.content = content;
        this.issueId = issueId;
    }

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    @Override
    public String toString() {
        return "CreateCommentRequest{" +
                "content='" + (content != null && content.length() > 50 ? 
                    content.substring(0, 50) + "..." : content) + '\'' +
                ", issueId=" + issueId +
                '}';
    }
}
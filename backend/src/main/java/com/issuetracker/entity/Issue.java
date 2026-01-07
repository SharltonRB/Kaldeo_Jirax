package com.issuetracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Issue entity representing individual work items with workflow states.
 * Issues belong to projects and can be assigned to sprints.
 */
@Entity
@Table(name = "issues", indexes = {
    @Index(name = "idx_issue_user_project", columnList = "user_id, project_id"),
    @Index(name = "idx_issue_status", columnList = "status"),
    @Index(name = "idx_issue_priority", columnList = "priority"),
    @Index(name = "idx_issue_sprint", columnList = "sprint_id"),
    @Index(name = "idx_issue_type", columnList = "issue_type_id")
})
@EntityListeners(AuditingEntityListener.class)
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_completed_sprint_id")
    private Sprint lastCompletedSprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_type_id", nullable = false)
    private IssueType issueType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_issue_id")
    private Issue parentIssue;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String title;

    @Size(max = 5000)
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status = IssueStatus.BACKLOG;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Min(0)
    @Column(name = "story_points")
    private Integer storyPoints;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Relationships
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "issue_labels",
        joinColumns = @JoinColumn(name = "issue_id"),
        inverseJoinColumns = @JoinColumn(name = "label_id"),
        indexes = {
            @Index(name = "idx_issue_labels_issue", columnList = "issue_id"),
            @Index(name = "idx_issue_labels_label", columnList = "label_id")
        }
    )
    private List<Label> labels = new ArrayList<>();

    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "parentIssue", cascade = CascadeType.ALL)
    private List<Issue> childIssues = new ArrayList<>();

    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuditLog> auditLogs = new ArrayList<>();

    // Constructors
    public Issue() {}

    public Issue(User user, Project project, IssueType issueType, String title, String description, Priority priority) {
        this.user = user;
        this.project = project;
        this.issueType = issueType;
        this.title = title;
        this.description = description;
        this.priority = priority;
    }

    public Issue(User user, Project project, IssueType issueType, String title, String description, Priority priority, Issue parentIssue) {
        this.user = user;
        this.project = project;
        this.issueType = issueType;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.parentIssue = parentIssue;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Sprint getSprint() {
        return sprint;
    }

    public void setSprint(Sprint sprint) {
        this.sprint = sprint;
    }

    public Sprint getLastCompletedSprint() {
        return lastCompletedSprint;
    }

    public void setLastCompletedSprint(Sprint lastCompletedSprint) {
        this.lastCompletedSprint = lastCompletedSprint;
    }

    public IssueType getIssueType() {
        return issueType;
    }

    public void setIssueType(IssueType issueType) {
        this.issueType = issueType;
    }

    public Issue getParentIssue() {
        return parentIssue;
    }

    public void setParentIssue(Issue parentIssue) {
        this.parentIssue = parentIssue;
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

    public List<Label> getLabels() {
        return labels;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Issue> getChildIssues() {
        return childIssues;
    }

    public void setChildIssues(List<Issue> childIssues) {
        this.childIssues = childIssues;
    }

    public List<AuditLog> getAuditLogs() {
        return auditLogs;
    }

    public void setAuditLogs(List<AuditLog> auditLogs) {
        this.auditLogs = auditLogs;
    }

    // Utility methods for epic hierarchy
    
    /**
     * Checks if this issue is an epic based on its issue type.
     * @return true if this is an epic issue
     */
    public boolean isEpic() {
        return this.issueType != null && "EPIC".equals(this.issueType.getName());
    }
    
    /**
     * Checks if this issue has child issues.
     * @return true if this issue has children
     */
    public boolean hasChildren() {
        return !this.childIssues.isEmpty();
    }
    
    /**
     * Gets the root epic for this issue.
     * @return the root epic, or this issue if it's already an epic
     */
    public Issue getRootEpic() {
        if (this.parentIssue == null) {
            return this; // This is already an epic
        }
        return this.parentIssue.getRootEpic();
    }
    
    /**
     * Adds a child issue to this epic.
     * @param childIssue the child issue to add
     * @throws IllegalStateException if this issue is not an epic
     */
    public void addChildIssue(Issue childIssue) {
        if (!this.isEpic()) {
            throw new IllegalStateException("Only epic issues can have children");
        }
        childIssue.setParentIssue(this);
        this.childIssues.add(childIssue);
    }
    
    /**
     * Removes a child issue from this epic.
     * @param childIssue the child issue to remove
     */
    public void removeChildIssue(Issue childIssue) {
        childIssue.setParentIssue(null);
        this.childIssues.remove(childIssue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Issue issue = (Issue) o;
        return Objects.equals(id, issue.id) && 
               Objects.equals(title, issue.title) && 
               Objects.equals(user, issue.user) && 
               Objects.equals(project, issue.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, user, project);
    }

    @Override
    public String toString() {
        return "Issue{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", createdAt=" + createdAt +
                '}';
    }
}
package com.issuetracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * IssueType entity representing both global and project-specific issue types.
 * Global types (BUG, STORY, TASK, EPIC) are available to all users.
 * Custom types are project-specific.
 */
@Entity
@Table(name = "issue_types", indexes = {
    @Index(name = "idx_issue_type_project", columnList = "project_id"),
    @Index(name = "idx_issue_type_name", columnList = "project_id, name", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
public class IssueType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    private String name;

    @Size(max = 255)
    private String description;

    @Column(name = "is_global", nullable = false)
    private Boolean isGlobal = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Relationships
    @OneToMany(mappedBy = "issueType", cascade = CascadeType.ALL)
    private List<Issue> issues = new ArrayList<>();

    // Constructors
    public IssueType() {}

    public IssueType(String name, String description, Boolean isGlobal) {
        this.name = name;
        this.description = description;
        this.isGlobal = isGlobal;
    }

    public IssueType(Project project, String name, String description) {
        this.project = project;
        this.name = name;
        this.description = description;
        this.isGlobal = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsGlobal() {
        return isGlobal;
    }

    public void setIsGlobal(Boolean isGlobal) {
        this.isGlobal = isGlobal;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IssueType issueType = (IssueType) o;
        return Objects.equals(id, issueType.id) && 
               Objects.equals(name, issueType.name) && 
               Objects.equals(project, issueType.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, project);
    }

    @Override
    public String toString() {
        return "IssueType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isGlobal=" + isGlobal +
                '}';
    }
}
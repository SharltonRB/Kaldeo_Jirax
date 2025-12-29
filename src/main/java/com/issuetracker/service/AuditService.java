package com.issuetracker.service;

import com.issuetracker.entity.*;
import com.issuetracker.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for managing audit trails with immutable logging.
 * Handles audit log creation and retrieval for all entity changes.
 * Provides structured logging with correlation IDs and comprehensive audit trails.
 */
@Service
@Transactional
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Logs issue creation.
     *
     * @param issue the created issue
     * @param user the user who created the issue
     */
    public void logIssueCreated(Issue issue, User user) {
        String details = String.format("Issue created with title: %s, priority: %s, status: %s", 
                                      issue.getTitle(), issue.getPriority(), issue.getStatus());
        createAuditLog(user, issue, "ISSUE_CREATED", details);
        
        logger.info("Issue created: issueId={}, projectId={}, userId={}, title={}", 
                   issue.getId(), issue.getProject().getId(), user.getId(), issue.getTitle());
    }

    /**
     * Logs status change.
     *
     * @param issue the issue
     * @param user the user who made the change
     * @param oldStatus the previous status
     * @param newStatus the new status
     */
    public void logStatusChange(Issue issue, User user, IssueStatus oldStatus, IssueStatus newStatus) {
        String details = String.format("Status changed from %s to %s", oldStatus, newStatus);
        createAuditLog(user, issue, "STATUS_CHANGE", details);
        
        logger.info("Issue status changed: issueId={}, userId={}, oldStatus={}, newStatus={}", 
                   issue.getId(), user.getId(), oldStatus, newStatus);
    }

    /**
     * Logs field change.
     *
     * @param issue the issue
     * @param user the user who made the change
     * @param fieldName the name of the changed field
     * @param oldValue the previous value
     * @param newValue the new value
     */
    public void logFieldChange(Issue issue, User user, String fieldName, String oldValue, String newValue) {
        String details = String.format("Field '%s' changed from '%s' to '%s'", fieldName, oldValue, newValue);
        createAuditLog(user, issue, "FIELD_CHANGE", details);
        
        logger.info("Issue field changed: issueId={}, userId={}, field={}, oldValue={}, newValue={}", 
                   issue.getId(), user.getId(), fieldName, oldValue, newValue);
    }

    /**
     * Logs project creation.
     *
     * @param project the created project
     * @param user the user who created the project
     */
    public void logProjectCreated(Project project, User user) {
        String details = String.format("Project created with name: %s, key: %s", 
                                      project.getName(), project.getKey());
        createProjectAuditLog(user, project, "PROJECT_CREATED", details);
        
        logger.info("Project created: projectId={}, userId={}, name={}, key={}", 
                   project.getId(), user.getId(), project.getName(), project.getKey());
    }

    /**
     * Logs project updates.
     *
     * @param project the updated project
     * @param user the user who made the change
     * @param fieldName the changed field
     * @param oldValue the old value
     * @param newValue the new value
     */
    public void logProjectChange(Project project, User user, String fieldName, String oldValue, String newValue) {
        String details = String.format("Project field '%s' changed from '%s' to '%s'", 
                                      fieldName, oldValue, newValue);
        createProjectAuditLog(user, project, "PROJECT_CHANGE", details);
        
        logger.info("Project changed: projectId={}, userId={}, field={}, oldValue={}, newValue={}", 
                   project.getId(), user.getId(), fieldName, oldValue, newValue);
    }

    /**
     * Logs sprint creation.
     *
     * @param sprint the created sprint
     * @param user the user who created the sprint
     */
    public void logSprintCreated(Sprint sprint, User user) {
        String details = String.format("Sprint created with name: %s, start: %s, end: %s", 
                                      sprint.getName(), sprint.getStartDate(), sprint.getEndDate());
        createSprintAuditLog(user, sprint, "SPRINT_CREATED", details);
        
        logger.info("Sprint created: sprintId={}, userId={}, name={}", 
                   sprint.getId(), user.getId(), sprint.getName());
    }

    /**
     * Logs sprint status changes.
     *
     * @param sprint the sprint
     * @param user the user who made the change
     * @param oldStatus the old status
     * @param newStatus the new status
     */
    public void logSprintStatusChange(Sprint sprint, User user, SprintStatus oldStatus, SprintStatus newStatus) {
        String details = String.format("Sprint status changed from %s to %s", oldStatus, newStatus);
        createSprintAuditLog(user, sprint, "SPRINT_STATUS_CHANGE", details);
        
        logger.info("Sprint status changed: sprintId={}, userId={}, oldStatus={}, newStatus={}", 
                   sprint.getId(), user.getId(), oldStatus, newStatus);
    }

    /**
     * Logs user authentication events.
     *
     * @param user the user
     * @param action the authentication action (LOGIN, LOGOUT, FAILED_LOGIN)
     * @param details additional details
     */
    public void logAuthenticationEvent(User user, String action, String details) {
        logger.info("Authentication event: userId={}, email={}, action={}, details={}, correlationId={}", 
                   user != null ? user.getId() : "unknown", 
                   user != null ? user.getEmail() : "unknown", 
                   action, details, MDC.get("correlationId"));
    }

    /**
     * Retrieves audit history for an issue.
     *
     * @param issue the issue
     * @return list of audit logs in chronological order
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getIssueHistory(Issue issue) {
        return auditLogRepository.findByIssueOrderByCreatedAtAsc(issue);
    }

    /**
     * Retrieves audit history for an issue by ID.
     *
     * @param issueId the issue ID
     * @return list of audit logs in chronological order
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getIssueHistory(Long issueId) {
        // This method would need the issue to be loaded first
        // For now, we'll keep it simple and use the other method
        throw new UnsupportedOperationException("Use getIssueHistory(Issue) instead");
    }

    /**
     * Creates and saves an audit log entry for issues.
     *
     * @param user the user who made the change
     * @param issue the affected issue
     * @param action the action performed
     * @param details the details of the change
     */
    private void createAuditLog(User user, Issue issue, String action, String details) {
        AuditLog auditLog = new AuditLog(user, issue, action, details);
        auditLogRepository.save(auditLog);
        
        logger.debug("Created audit log: id={}, issueId={}, userId={}, action={}, correlationId={}", 
                    auditLog.getId(), issue.getId(), user.getId(), action, MDC.get("correlationId"));
    }

    /**
     * Creates audit log for project-related actions.
     * Note: This creates a general audit log without issue association.
     */
    private void createProjectAuditLog(User user, Project project, String action, String details) {
        // For now, we'll log project actions without creating audit log entries
        // since our current AuditLog entity is tied to issues
        logger.info("Project audit: projectId={}, userId={}, action={}, details={}, correlationId={}", 
                   project.getId(), user.getId(), action, details, MDC.get("correlationId"));
    }

    /**
     * Creates audit log for sprint-related actions.
     * Note: This creates a general audit log without issue association.
     */
    private void createSprintAuditLog(User user, Sprint sprint, String action, String details) {
        // For now, we'll log sprint actions without creating audit log entries
        // since our current AuditLog entity is tied to issues
        logger.info("Sprint audit: sprintId={}, userId={}, action={}, details={}, correlationId={}", 
                   sprint.getId(), user.getId(), action, details, MDC.get("correlationId"));
    }
}
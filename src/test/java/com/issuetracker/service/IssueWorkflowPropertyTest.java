package com.issuetracker.service;

import com.issuetracker.base.BasePostgreSQLTest;
import com.issuetracker.dto.CreateIssueRequest;
import com.issuetracker.dto.IssueDto;
import com.issuetracker.dto.StatusUpdateRequest;
import com.issuetracker.entity.*;
import com.issuetracker.exception.InvalidWorkflowTransitionException;
import com.issuetracker.repository.UserRepository;
import com.issuetracker.repository.ProjectRepository;
import com.issuetracker.repository.IssueTypeRepository;
import org.junit.jupiter.api.Test;
import org.quicktheories.QuickTheory;
import org.quicktheories.core.Gen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.quicktheories.generators.SourceDSL.*;

/**
 * Property-based tests for issue workflow integrity.
 * Feature: personal-issue-tracker, Property 5: Issue Workflow Integrity
 * Validates: Requirements 3.3, 7.1
 * Uses Testcontainers with PostgreSQL for production parity.
 */
@Transactional
public class IssueWorkflowPropertyTest extends BasePostgreSQLTest {

    // QuickTheory instance for property testing
    private static final QuickTheory qt = QuickTheory.qt();

    @Autowired
    private IssueService issueService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private IssueTypeRepository issueTypeRepository;

    /**
     * Property 5: Issue Workflow Integrity
     * For any issue status transition, the system should only allow valid workflow 
     * progressions (BACKLOG → SELECTED_FOR_DEVELOPMENT → IN_PROGRESS → IN_REVIEW → DONE) 
     * and record all changes in the audit trail.
     * 
     * Validates: Requirements 3.3, 7.1
     */
    @Test
    void issueWorkflowIntegrityProperty() {
        qt.forAll(
            userGenerator(),
            projectDataGenerator(),
            issueDataGenerator(),
            validWorkflowTransitionGenerator()
        ).checkAssert((user, projectData, issueData, transition) -> {
            // Persist user
            User savedUser = userRepository.save(user);
            
            // Create project
            Project project = new Project(savedUser, projectData.name, projectData.key, projectData.description);
            Project savedProject = projectRepository.save(project);
            
            // Create global issue type
            IssueType globalIssueType = issueTypeRepository.findByNameAndIsGlobalTrue("TASK")
                    .orElseGet(() -> {
                        IssueType newType = new IssueType("TASK", "Global task type", true);
                        return issueTypeRepository.save(newType);
                    });
            
            // Create issue
            CreateIssueRequest createRequest = new CreateIssueRequest(
                issueData.title, 
                issueData.description, 
                issueData.priority, 
                savedProject.getId(), 
                globalIssueType.getId()
            );
            
            IssueDto createdIssue = issueService.createIssue(createRequest, savedUser);
            
            // Verify issue starts in BACKLOG status
            assertThat(createdIssue.getStatus()).isEqualTo(IssueStatus.BACKLOG);
            
            // Test valid workflow transition
            IssueStatus fromStatus = transition.from;
            IssueStatus toStatus = transition.to;
            
            // Set issue to the 'from' status first (if not already BACKLOG)
            if (fromStatus != IssueStatus.BACKLOG) {
                // Navigate to the from status through valid transitions
                navigateToStatus(createdIssue.getId(), fromStatus, savedUser);
            }
            
            // Perform the transition
            StatusUpdateRequest statusRequest = new StatusUpdateRequest(toStatus);
            IssueDto updatedIssue = issueService.updateIssueStatus(createdIssue.getId(), statusRequest, savedUser);
            
            // Verify the transition was successful
            assertThat(updatedIssue.getStatus()).isEqualTo(toStatus);
            assertThat(updatedIssue.getId()).isEqualTo(createdIssue.getId());
            assertThat(updatedIssue.getUpdatedAt()).isAfterOrEqualTo(createdIssue.getUpdatedAt());
            
            // Verify audit trail was created
            List<AuditLog> auditLogs = auditService.getIssueHistory(
                new Issue(savedUser, savedProject, globalIssueType, issueData.title, issueData.description, issueData.priority) {{
                    setId(createdIssue.getId());
                }}
            );
            
            // Should have at least creation log and status change log
            assertThat(auditLogs).hasSizeGreaterThanOrEqualTo(2);
            
            // Verify there's a status change audit log
            boolean hasStatusChangeLog = auditLogs.stream()
                    .anyMatch(log -> log.getAction().equals("STATUS_CHANGE"));
            assertThat(hasStatusChangeLog).isTrue();
        });
    }

    /**
     * Test invalid workflow transitions are rejected.
     */
    @Test
    void invalidWorkflowTransitionsRejectedProperty() {
        qt.forAll(
            userGenerator(),
            projectDataGenerator(),
            issueDataGenerator(),
            invalidWorkflowTransitionGenerator()
        ).checkAssert((user, projectData, issueData, transition) -> {
            // Persist user
            User savedUser = userRepository.save(user);
            
            // Create project
            Project project = new Project(savedUser, projectData.name, projectData.key, projectData.description);
            Project savedProject = projectRepository.save(project);
            
            // Create global issue type
            IssueType globalIssueType = issueTypeRepository.findByNameAndIsGlobalTrue("TASK")
                    .orElseGet(() -> {
                        IssueType newType = new IssueType("TASK", "Global task type", true);
                        return issueTypeRepository.save(newType);
                    });
            
            // Create issue
            CreateIssueRequest createRequest = new CreateIssueRequest(
                issueData.title, 
                issueData.description, 
                issueData.priority, 
                savedProject.getId(), 
                globalIssueType.getId()
            );
            
            IssueDto createdIssue = issueService.createIssue(createRequest, savedUser);
            
            // Set issue to the 'from' status first (if not already BACKLOG)
            if (transition.from != IssueStatus.BACKLOG) {
                navigateToStatus(createdIssue.getId(), transition.from, savedUser);
            }
            
            // Attempt invalid transition - should throw exception
            StatusUpdateRequest statusRequest = new StatusUpdateRequest(transition.to);
            assertThatThrownBy(() -> issueService.updateIssueStatus(createdIssue.getId(), statusRequest, savedUser))
                .isInstanceOf(InvalidWorkflowTransitionException.class);
            
            // Verify issue status hasn't changed
            IssueDto unchangedIssue = issueService.getIssue(createdIssue.getId(), savedUser);
            assertThat(unchangedIssue.getStatus()).isEqualTo(transition.from);
        });
    }

    /**
     * Helper method to navigate an issue to a specific status through valid transitions.
     */
    private void navigateToStatus(Long issueId, IssueStatus targetStatus, User user) {
        IssueDto currentIssue = issueService.getIssue(issueId, user);
        IssueStatus currentStatus = currentIssue.getStatus();
        
        while (currentStatus != targetStatus) {
            IssueStatus nextStatus = getNextValidStatus(currentStatus, targetStatus);
            if (nextStatus == null) {
                throw new IllegalStateException("Cannot navigate from " + currentStatus + " to " + targetStatus);
            }
            
            StatusUpdateRequest request = new StatusUpdateRequest(nextStatus);
            currentIssue = issueService.updateIssueStatus(issueId, request, user);
            currentStatus = currentIssue.getStatus();
        }
    }

    /**
     * Gets the next valid status in the path to the target status.
     */
    private IssueStatus getNextValidStatus(IssueStatus current, IssueStatus target) {
        return switch (current) {
            case BACKLOG -> target == IssueStatus.SELECTED_FOR_DEVELOPMENT || 
                          target == IssueStatus.IN_PROGRESS || 
                          target == IssueStatus.IN_REVIEW || 
                          target == IssueStatus.DONE ? IssueStatus.SELECTED_FOR_DEVELOPMENT : null;
            case SELECTED_FOR_DEVELOPMENT -> target == IssueStatus.IN_PROGRESS || 
                                           target == IssueStatus.IN_REVIEW || 
                                           target == IssueStatus.DONE ? IssueStatus.IN_PROGRESS : null;
            case IN_PROGRESS -> target == IssueStatus.IN_REVIEW || 
                              target == IssueStatus.DONE ? IssueStatus.IN_REVIEW : null;
            case IN_REVIEW -> target == IssueStatus.DONE ? IssueStatus.DONE : null;
            case DONE -> null; // Already at final status
        };
    }

    // Generator methods
    private Gen<User> userGenerator() {
        return integers().between(1, 1000000)
            .zip(strings().ascii().ofLengthBetween(5, 20),
                 strings().allPossible().ofLengthBetween(8, 50),
                 strings().ascii().ofLengthBetween(2, 30),
                 integers().between(1, Integer.MAX_VALUE),
                 (uniqueId, emailPrefix, password, name, randomSuffix) -> {
                     String cleanEmailPrefix = emailPrefix.replaceAll("[^a-zA-Z0-9]", "a");
                     if (cleanEmailPrefix.isEmpty()) {
                         cleanEmailPrefix = "user";
                     }
                     
                     String cleanName = name.replaceAll("[^a-zA-Z0-9 ]", "a").trim();
                     if (cleanName.isEmpty() || cleanName.isBlank()) {
                         cleanName = "TestUser";
                     }
                     
                     String cleanPassword = password.replaceAll("[^a-zA-Z0-9!@#$%^&*]", "a").trim();
                     if (cleanPassword.isEmpty() || cleanPassword.isBlank()) {
                         cleanPassword = "TestPassword123!";
                     }
                     
                     return new User(
                         cleanEmailPrefix + uniqueId + "_" + System.nanoTime() + "_" + randomSuffix + "@test.com",
                         cleanPassword,
                         cleanName
                     );
                 });
    }

    private Gen<ProjectData> projectDataGenerator() {
        return strings().allPossible().ofLengthBetween(3, 50)
            .zip(strings().ascii().ofLengthBetween(2, 5),
                 strings().allPossible().ofLengthBetween(10, 200),
                 integers().between(1, 1000000),
                 (name, keyPrefix, description, uniqueId) -> {
                     String cleanName = name.replaceAll("[^a-zA-Z0-9 ]", "a").trim();
                     if (cleanName.isEmpty() || cleanName.isBlank()) {
                         cleanName = "TestProject";
                     }
                     
                     String cleanKeyPrefix = keyPrefix.replaceAll("[^a-zA-Z0-9]", "A");
                     if (cleanKeyPrefix.isEmpty()) {
                         cleanKeyPrefix = "TEST";
                     }
                     String cleanKey = (cleanKeyPrefix + uniqueId).toUpperCase();
                     if (cleanKey.length() > 10) {
                         cleanKey = cleanKey.substring(0, 10);
                     }
                     
                     String cleanDescription = description.replaceAll("[^a-zA-Z0-9 .,!?-]", "a").trim();
                     if (cleanDescription.isEmpty() || cleanDescription.isBlank()) {
                         cleanDescription = "Test project description";
                     }
                     
                     return new ProjectData(cleanName, cleanKey, cleanDescription);
                 });
    }

    private Gen<IssueData> issueDataGenerator() {
        return strings().allPossible().ofLengthBetween(5, 100)
            .zip(strings().allPossible().ofLengthBetween(10, 500),
                 integers().between(0, Priority.values().length - 1),
                 (title, description, priorityIndex) -> {
                     String cleanTitle = title.replaceAll("[^a-zA-Z0-9 .,!?-]", "a").trim();
                     if (cleanTitle.isEmpty() || cleanTitle.isBlank()) {
                         cleanTitle = "Test Issue";
                     }
                     
                     String cleanDescription = description.replaceAll("[^a-zA-Z0-9 .,!?-]", "a").trim();
                     if (cleanDescription.isEmpty() || cleanDescription.isBlank()) {
                         cleanDescription = "Test issue description";
                     }
                     
                     return new IssueData(cleanTitle, cleanDescription, Priority.values()[priorityIndex]);
                 });
    }

    private Gen<WorkflowTransition> validWorkflowTransitionGenerator() {
        List<WorkflowTransition> validTransitions = Arrays.asList(
            new WorkflowTransition(IssueStatus.BACKLOG, IssueStatus.SELECTED_FOR_DEVELOPMENT),
            new WorkflowTransition(IssueStatus.SELECTED_FOR_DEVELOPMENT, IssueStatus.IN_PROGRESS),
            new WorkflowTransition(IssueStatus.SELECTED_FOR_DEVELOPMENT, IssueStatus.BACKLOG),
            new WorkflowTransition(IssueStatus.IN_PROGRESS, IssueStatus.IN_REVIEW),
            new WorkflowTransition(IssueStatus.IN_PROGRESS, IssueStatus.SELECTED_FOR_DEVELOPMENT),
            new WorkflowTransition(IssueStatus.IN_REVIEW, IssueStatus.DONE),
            new WorkflowTransition(IssueStatus.IN_REVIEW, IssueStatus.IN_PROGRESS),
            new WorkflowTransition(IssueStatus.DONE, IssueStatus.IN_REVIEW)
        );
        
        return integers().between(0, validTransitions.size() - 1)
                .map(validTransitions::get);
    }

    private Gen<WorkflowTransition> invalidWorkflowTransitionGenerator() {
        List<WorkflowTransition> invalidTransitions = Arrays.asList(
            new WorkflowTransition(IssueStatus.BACKLOG, IssueStatus.IN_PROGRESS),
            new WorkflowTransition(IssueStatus.BACKLOG, IssueStatus.IN_REVIEW),
            new WorkflowTransition(IssueStatus.BACKLOG, IssueStatus.DONE),
            new WorkflowTransition(IssueStatus.SELECTED_FOR_DEVELOPMENT, IssueStatus.IN_REVIEW),
            new WorkflowTransition(IssueStatus.SELECTED_FOR_DEVELOPMENT, IssueStatus.DONE),
            new WorkflowTransition(IssueStatus.IN_PROGRESS, IssueStatus.BACKLOG),
            new WorkflowTransition(IssueStatus.IN_PROGRESS, IssueStatus.DONE),
            new WorkflowTransition(IssueStatus.IN_REVIEW, IssueStatus.BACKLOG),
            new WorkflowTransition(IssueStatus.IN_REVIEW, IssueStatus.SELECTED_FOR_DEVELOPMENT),
            new WorkflowTransition(IssueStatus.DONE, IssueStatus.BACKLOG),
            new WorkflowTransition(IssueStatus.DONE, IssueStatus.SELECTED_FOR_DEVELOPMENT),
            new WorkflowTransition(IssueStatus.DONE, IssueStatus.IN_PROGRESS)
        );
        
        return integers().between(0, invalidTransitions.size() - 1)
                .map(invalidTransitions::get);
    }

    // Helper classes for test data
    private static class ProjectData {
        final String name;
        final String key;
        final String description;

        ProjectData(String name, String key, String description) {
            this.name = name;
            this.key = key;
            this.description = description;
        }
    }

    private static class IssueData {
        final String title;
        final String description;
        final Priority priority;

        IssueData(String title, String description, Priority priority) {
            this.title = title;
            this.description = description;
            this.priority = priority;
        }
    }

    private static class WorkflowTransition {
        final IssueStatus from;
        final IssueStatus to;

        WorkflowTransition(IssueStatus from, IssueStatus to) {
            this.from = from;
            this.to = to;
        }
    }
}
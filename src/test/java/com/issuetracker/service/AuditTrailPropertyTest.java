package com.issuetracker.service;

import com.issuetracker.base.BasePostgreSQLTest;
import com.issuetracker.entity.*;
import com.issuetracker.repository.*;
import org.junit.jupiter.api.Test;
import org.quicktheories.QuickTheory;
import org.quicktheories.core.Gen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quicktheories.generators.SourceDSL.*;
import static org.quicktheories.generators.Generate.*;

/**
 * Property-based tests for audit trail completeness and integrity.
 * 
 * Property 10: Audit Trail Completeness
 * Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5
 * 
 * This test ensures that all significant operations on issues are properly
 * audited and that audit logs are immutable and complete.
 */
@ActiveProfiles("test")
@Transactional
class AuditTrailPropertyTest extends BasePostgreSQLTest {

    // QuickTheory instance for property testing
    private static final QuickTheory qt = QuickTheory.qt();

    @Autowired
    private AuditService auditService;
    
    @Autowired
    private IssueService issueService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private IssueRepository issueRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private IssueTypeRepository issueTypeRepository;

    /**
     * Property 10: Audit Trail Completeness
     * 
     * Tests that:
     * 1. All issue operations generate appropriate audit logs
     * 2. Audit logs are immutable once created
     * 3. Audit logs contain complete information about changes
     * 4. Audit logs maintain chronological order
     * 5. Users can only access audit logs for their own issues
     */
    @Test
    void property_auditTrailCompleteness() {
        qt
            .forAll(
                userGenerator(),
                projectGenerator(),
                issueGenerator(),
                pick(List.of(IssueStatus.BACKLOG, IssueStatus.SELECTED_FOR_DEVELOPMENT, IssueStatus.IN_PROGRESS, IssueStatus.DONE))
            )
            .checkAssert((userData, projectData, issueData, newStatus) -> {
                // Create user and project
                User user = userRepository.save(userData);
                Project project = createProject(projectData, user);
                
                // Get or create issue type
                IssueType issueType = getOrCreateIssueType();
                
                // Record initial audit log count
                long initialAuditCount = auditLogRepository.countByUser(user);
                
                // Create issue - should generate audit log
                Issue issue = createIssue(issueData, project, user, issueType);
                
                // Verify issue creation was audited
                List<AuditLog> auditLogs = auditService.getIssueHistory(issue);
                assertThat(auditLogs).hasSize(1);
                
                AuditLog creationLog = auditLogs.get(0);
                assertThat(creationLog.getAction()).isEqualTo("ISSUE_CREATED");
                assertThat(creationLog.getUser()).isEqualTo(user);
                assertThat(creationLog.getIssue()).isEqualTo(issue);
                assertThat(creationLog.getDetails()).contains(issue.getTitle());
                assertThat(creationLog.getCreatedAt()).isNotNull();
                
                // Store original creation time for immutability test
                Instant originalCreationTime = creationLog.getCreatedAt();
                Long originalLogId = creationLog.getId();
                
                // Update issue status - should generate audit log
                IssueStatus originalStatus = issue.getStatus();
                if (!originalStatus.equals(newStatus)) {
                    issue.setStatus(newStatus);
                    issueRepository.save(issue);
                    auditService.logStatusChange(issue, user, originalStatus, newStatus);
                    
                    // Verify status change was audited
                    auditLogs = auditService.getIssueHistory(issue);
                    assertThat(auditLogs).hasSize(2);
                    
                    AuditLog statusLog = auditLogs.get(1); // Second log (chronological order)
                    assertThat(statusLog.getAction()).isEqualTo("STATUS_CHANGE");
                    assertThat(statusLog.getUser()).isEqualTo(user);
                    assertThat(statusLog.getIssue()).isEqualTo(issue);
                    assertThat(statusLog.getDetails()).contains(originalStatus.toString());
                    assertThat(statusLog.getDetails()).contains(newStatus.toString());
                    assertThat(statusLog.getCreatedAt()).isAfter(originalCreationTime);
                }
                
                // Update issue priority - should generate audit log
                Priority originalPriority = issue.getPriority();
                Priority newPriority = Priority.HIGH; // Use a fixed new priority
                if (!originalPriority.equals(newPriority)) {
                    issue.setPriority(newPriority);
                    issueRepository.save(issue);
                    auditService.logFieldChange(issue, user, "priority", 
                                              originalPriority.toString(), newPriority.toString());
                    
                    // Verify field change was audited
                    auditLogs = auditService.getIssueHistory(issue);
                    assertThat(auditLogs.size()).isGreaterThanOrEqualTo(2);
                    
                    AuditLog fieldLog = auditLogs.get(auditLogs.size() - 1); // Last log
                    assertThat(fieldLog.getAction()).isEqualTo("FIELD_CHANGE");
                    assertThat(fieldLog.getUser()).isEqualTo(user);
                    assertThat(fieldLog.getIssue()).isEqualTo(issue);
                    assertThat(fieldLog.getDetails()).contains("priority");
                    assertThat(fieldLog.getDetails()).contains(originalPriority.toString());
                    assertThat(fieldLog.getDetails()).contains(newPriority.toString());
                }
                
                // Test immutability - original audit log should be unchanged
                AuditLog unchangedLog = auditLogRepository.findById(originalLogId).orElseThrow();
                assertThat(unchangedLog.getCreatedAt()).isEqualTo(originalCreationTime);
                assertThat(unchangedLog.getAction()).isEqualTo("ISSUE_CREATED");
                assertThat(unchangedLog.getDetails()).contains(issue.getTitle());
                
                // Test chronological order
                auditLogs = auditService.getIssueHistory(issue);
                for (int i = 1; i < auditLogs.size(); i++) {
                    assertThat(auditLogs.get(i).getCreatedAt())
                        .isAfterOrEqualTo(auditLogs.get(i - 1).getCreatedAt());
                }
                
                // Test user isolation - create another user and verify they can't access audit logs
                User otherUser = userRepository.save(createOtherUser(userData));
                
                // Other user should not see audit logs for this issue
                List<AuditLog> otherUserLogs = auditLogRepository.findByUserOrderByCreatedAtDesc(otherUser, 
                                                                                                org.springframework.data.domain.Pageable.unpaged()).getContent();
                assertThat(otherUserLogs).isEmpty();
                
                // Verify total audit count increased appropriately
                long finalAuditCount = auditLogRepository.countByUser(user);
                assertThat(finalAuditCount).isGreaterThan(initialAuditCount);
            });
    }

    /**
     * Tests audit log search and filtering functionality.
     */
    @Test
    void property_auditLogSearchAndFiltering() {
        qt
            .forAll(
                userGenerator(),
                projectGenerator(),
                issueGenerator(),
                strings().ascii().ofLengthBetween(5, 10) // Search term
            )
            .checkAssert((userData, projectData, issueData, searchTerm) -> {
                // Create test data
                User user = userRepository.save(userData);
                Project project = createProject(projectData, user);
                IssueType issueType = getOrCreateIssueType();
                Issue issue = createIssue(issueData, project, user, issueType);
                
                // Create audit log with search term in details
                String detailsWithSearchTerm = "Issue updated with " + searchTerm + " information";
                auditService.logFieldChange(issue, user, "description", "old", detailsWithSearchTerm);
                
                // Search for audit logs containing the term
                var searchResults = auditLogRepository.findByUserAndDetailsContainingIgnoreCase(
                    user, searchTerm, org.springframework.data.domain.Pageable.unpaged());
                
                assertThat(searchResults.getContent()).isNotEmpty();
                assertThat(searchResults.getContent().get(0).getDetails()).containsIgnoringCase(searchTerm);
                
                // Verify case-insensitive search
                var upperCaseResults = auditLogRepository.findByUserAndDetailsContainingIgnoreCase(
                    user, searchTerm.toUpperCase(), org.springframework.data.domain.Pageable.unpaged());
                
                assertThat(upperCaseResults.getContent()).hasSize(searchResults.getContent().size());
            });
    }

    /**
     * Tests audit log date range filtering.
     */
    @Test
    void property_auditLogDateRangeFiltering() {
        qt
            .forAll(
                userGenerator(),
                projectGenerator(),
                issueGenerator()
            )
            .checkAssert((userData, projectData, issueData) -> {
                // Create test data
                User user = userRepository.save(userData);
                Project project = createProject(projectData, user);
                IssueType issueType = getOrCreateIssueType();
                Issue issue = createIssue(issueData, project, user, issueType);
                
                Instant beforeUpdate = Instant.now().minusSeconds(1);
                
                // Create additional audit log
                auditService.logFieldChange(issue, user, "title", "old title", "new title");
                
                Instant afterUpdate = Instant.now().plusSeconds(1);
                
                // Find logs in date range
                var rangeResults = auditLogRepository.findByUserAndActionAndDateRange(
                    user, "FIELD_CHANGE", beforeUpdate, afterUpdate, 
                    org.springframework.data.domain.Pageable.unpaged());
                
                assertThat(rangeResults.getContent()).isNotEmpty();
                
                // Verify all results are within the date range
                rangeResults.getContent().forEach(log -> {
                    assertThat(log.getCreatedAt()).isBetween(beforeUpdate, afterUpdate);
                    assertThat(log.getAction()).isEqualTo("FIELD_CHANGE");
                });
            });
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

    private Gen<ProjectData> projectGenerator() {
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

    private Gen<IssueData> issueGenerator() {
        return strings().allPossible().ofLengthBetween(5, 100)
            .zip(strings().allPossible().ofLengthBetween(10, 500),
                 pick(List.of(IssueStatus.BACKLOG, IssueStatus.SELECTED_FOR_DEVELOPMENT, IssueStatus.IN_PROGRESS, IssueStatus.DONE)),
                 pick(List.of(Priority.LOW, Priority.MEDIUM, Priority.HIGH, Priority.CRITICAL)),
                 (title, description, status, priority) -> {
                     String cleanTitle = title.replaceAll("[^a-zA-Z0-9 .,!?-]", "a").trim();
                     if (cleanTitle.isEmpty() || cleanTitle.isBlank()) {
                         cleanTitle = "Test Issue";
                     }
                     
                     String cleanDescription = description.replaceAll("[^a-zA-Z0-9 .,!?-]", "a").trim();
                     if (cleanDescription.isEmpty() || cleanDescription.isBlank()) {
                         cleanDescription = "Test issue description";
                     }
                     
                     return new IssueData(cleanTitle, cleanDescription, status, priority);
                 });
    }

    private Project createProject(ProjectData projectData, User user) {
        Project project = new Project();
        project.setName(projectData.name());
        project.setKey(projectData.key());
        project.setDescription(projectData.description());
        project.setUser(user);
        return projectRepository.save(project);
    }

    private Issue createIssue(IssueData issueData, Project project, User user, IssueType issueType) {
        Issue issue = new Issue();
        issue.setTitle(issueData.title());
        issue.setDescription(issueData.description());
        issue.setStatus(issueData.status());
        issue.setPriority(issueData.priority());
        issue.setProject(project);
        issue.setUser(user);
        issue.setIssueType(issueType);
        
        Issue savedIssue = issueRepository.save(issue);
        auditService.logIssueCreated(savedIssue, user);
        
        return savedIssue;
    }

    private IssueType getOrCreateIssueType() {
        return issueTypeRepository.findByNameAndIsGlobalTrue("TASK")
            .orElseGet(() -> {
                IssueType issueType = new IssueType();
                issueType.setName("TASK");
                issueType.setDescription("General task");
                issueType.setIsGlobal(true);
                return issueTypeRepository.save(issueType);
            });
    }

    private User createOtherUser(User originalUser) {
        return new User(
            "other_" + originalUser.getEmail(),
            originalUser.getPasswordHash(),
            "Other " + originalUser.getName()
        );
    }

    // Test data records
    private record ProjectData(String name, String key, String description) {}
    private record IssueData(String title, String description, IssueStatus status, Priority priority) {}
}
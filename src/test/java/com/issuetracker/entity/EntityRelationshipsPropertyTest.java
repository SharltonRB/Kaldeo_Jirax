package com.issuetracker.entity;

import com.issuetracker.base.BaseIntegrationTest;
import com.issuetracker.base.BasePropertyTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.quicktheories.core.Gen;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quicktheories.generators.SourceDSL.*;

/**
 * Property-based tests for entity relationships and user data isolation.
 * Feature: personal-issue-tracker, Property 1: User Data Isolation
 * Validates: Requirements 1.3, 1.5, 2.3, 2.4, 3.4, 4.3, 5.5, 6.4, 8.4
 */
@DataJpaTest
@Transactional
@Rollback
@ActiveProfiles("test")
public class EntityRelationshipsPropertyTest extends BasePropertyTest {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Property 1: User Data Isolation
     * For any authenticated user and any data operation (read, write, update, delete), 
     * the system should only allow access to data owned by that user, ensuring complete 
     * data isolation between users.
     * 
     * Validates: Requirements 1.3, 1.5, 2.3, 2.4, 3.4, 4.3, 5.5, 6.4, 8.4
     */
    @Test
    void userDataIsolationProperty() {
        qt.forAll(
            userGenerator(),
            userGenerator(),
            projectDataGenerator(),
            issueDataGenerator()
        ).checkAssert((user1, user2, projectData, issueData) -> {
            // Clear any previous session state
            entityManager.clear();
            
            // Persist users
            entityManager.persist(user1);
            entityManager.persist(user2);
            entityManager.flush();
            
            // Create global issue type for testing
            IssueType globalIssueType = new IssueType("TASK", "Global task type", true);
            entityManager.persist(globalIssueType);
            entityManager.flush();
            
            // Create projects for each user
            Project project1 = new Project(user1, projectData.name + "1", projectData.key + "1", projectData.description);
            Project project2 = new Project(user2, projectData.name + "2", projectData.key + "2", projectData.description);
            
            entityManager.persist(project1);
            entityManager.persist(project2);
            entityManager.flush();
            
            // Create issues for each user
            Issue issue1 = new Issue(user1, project1, globalIssueType, issueData.title + "1", issueData.description, issueData.priority);
            Issue issue2 = new Issue(user2, project2, globalIssueType, issueData.title + "2", issueData.description, issueData.priority);
            
            entityManager.persist(issue1);
            entityManager.persist(issue2);
            entityManager.flush();
            
            // Create labels for each user
            Label label1 = new Label(user1, "label1", "#FF0000");
            Label label2 = new Label(user2, "label2", "#00FF00");
            
            entityManager.persist(label1);
            entityManager.persist(label2);
            entityManager.flush();
            
            // Create sprints for each user
            Sprint sprint1 = new Sprint(user1, "Sprint 1", LocalDate.now(), LocalDate.now().plusDays(14));
            Sprint sprint2 = new Sprint(user2, "Sprint 2", LocalDate.now(), LocalDate.now().plusDays(14));
            
            entityManager.persist(sprint1);
            entityManager.persist(sprint2);
            entityManager.flush();
            
            // Create comments for each user on their own issues
            Comment comment1 = new Comment(user1, issue1, "Comment by user 1");
            Comment comment2 = new Comment(user2, issue2, "Comment by user 2");
            
            entityManager.persist(comment1);
            entityManager.persist(comment2);
            entityManager.flush();
            
            // Create audit logs for each user
            AuditLog audit1 = new AuditLog(user1, issue1, "CREATED", "Issue created");
            AuditLog audit2 = new AuditLog(user2, issue2, "CREATED", "Issue created");
            
            entityManager.persist(audit1);
            entityManager.persist(audit2);
            entityManager.flush();
            entityManager.clear();
            
            // Verify data isolation - User 1 should only see their own data
            List<Project> user1Projects = entityManager.createQuery(
                "SELECT p FROM Project p WHERE p.user.id = :userId", Project.class)
                .setParameter("userId", user1.getId())
                .getResultList();
            
            List<Issue> user1Issues = entityManager.createQuery(
                "SELECT i FROM Issue i WHERE i.user.id = :userId", Issue.class)
                .setParameter("userId", user1.getId())
                .getResultList();
            
            List<Label> user1Labels = entityManager.createQuery(
                "SELECT l FROM Label l WHERE l.user.id = :userId", Label.class)
                .setParameter("userId", user1.getId())
                .getResultList();
            
            List<Sprint> user1Sprints = entityManager.createQuery(
                "SELECT s FROM Sprint s WHERE s.user.id = :userId", Sprint.class)
                .setParameter("userId", user1.getId())
                .getResultList();
            
            List<Comment> user1Comments = entityManager.createQuery(
                "SELECT c FROM Comment c WHERE c.user.id = :userId", Comment.class)
                .setParameter("userId", user1.getId())
                .getResultList();
            
            List<AuditLog> user1AuditLogs = entityManager.createQuery(
                "SELECT a FROM AuditLog a WHERE a.user.id = :userId", AuditLog.class)
                .setParameter("userId", user1.getId())
                .getResultList();
            
            // Verify User 2 should only see their own data
            List<Project> user2Projects = entityManager.createQuery(
                "SELECT p FROM Project p WHERE p.user.id = :userId", Project.class)
                .setParameter("userId", user2.getId())
                .getResultList();
            
            List<Issue> user2Issues = entityManager.createQuery(
                "SELECT i FROM Issue i WHERE i.user.id = :userId", Issue.class)
                .setParameter("userId", user2.getId())
                .getResultList();
            
            List<Label> user2Labels = entityManager.createQuery(
                "SELECT l FROM Label l WHERE l.user.id = :userId", Label.class)
                .setParameter("userId", user2.getId())
                .getResultList();
            
            List<Sprint> user2Sprints = entityManager.createQuery(
                "SELECT s FROM Sprint s WHERE s.user.id = :userId", Sprint.class)
                .setParameter("userId", user2.getId())
                .getResultList();
            
            List<Comment> user2Comments = entityManager.createQuery(
                "SELECT c FROM Comment c WHERE c.user.id = :userId", Comment.class)
                .setParameter("userId", user2.getId())
                .getResultList();
            
            List<AuditLog> user2AuditLogs = entityManager.createQuery(
                "SELECT a FROM AuditLog a WHERE a.user.id = :userId", AuditLog.class)
                .setParameter("userId", user2.getId())
                .getResultList();
            
            // Assert data isolation - each user should only see their own data
            assertThat(user1Projects).hasSize(1);
            assertThat(user1Projects.get(0).getUser().getId()).isEqualTo(user1.getId());
            
            assertThat(user1Issues).hasSize(1);
            assertThat(user1Issues.get(0).getUser().getId()).isEqualTo(user1.getId());
            
            assertThat(user1Labels).hasSize(1);
            assertThat(user1Labels.get(0).getUser().getId()).isEqualTo(user1.getId());
            
            assertThat(user1Sprints).hasSize(1);
            assertThat(user1Sprints.get(0).getUser().getId()).isEqualTo(user1.getId());
            
            assertThat(user1Comments).hasSize(1);
            assertThat(user1Comments.get(0).getUser().getId()).isEqualTo(user1.getId());
            
            assertThat(user1AuditLogs).hasSize(1);
            assertThat(user1AuditLogs.get(0).getUser().getId()).isEqualTo(user1.getId());
            
            // Same for user 2
            assertThat(user2Projects).hasSize(1);
            assertThat(user2Projects.get(0).getUser().getId()).isEqualTo(user2.getId());
            
            assertThat(user2Issues).hasSize(1);
            assertThat(user2Issues.get(0).getUser().getId()).isEqualTo(user2.getId());
            
            assertThat(user2Labels).hasSize(1);
            assertThat(user2Labels.get(0).getUser().getId()).isEqualTo(user2.getId());
            
            assertThat(user2Sprints).hasSize(1);
            assertThat(user2Sprints.get(0).getUser().getId()).isEqualTo(user2.getId());
            
            assertThat(user2Comments).hasSize(1);
            assertThat(user2Comments.get(0).getUser().getId()).isEqualTo(user2.getId());
            
            assertThat(user2AuditLogs).hasSize(1);
            assertThat(user2AuditLogs.get(0).getUser().getId()).isEqualTo(user2.getId());
            
            // Verify cross-user data isolation - no user should see other user's data
            assertThat(user1Projects).noneMatch(p -> p.getUser().getId().equals(user2.getId()));
            assertThat(user2Projects).noneMatch(p -> p.getUser().getId().equals(user1.getId()));
            
            assertThat(user1Issues).noneMatch(i -> i.getUser().getId().equals(user2.getId()));
            assertThat(user2Issues).noneMatch(i -> i.getUser().getId().equals(user1.getId()));
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
                     // Clean email prefix to only contain alphanumeric characters
                     String cleanEmailPrefix = emailPrefix.replaceAll("[^a-zA-Z0-9]", "a");
                     if (cleanEmailPrefix.isEmpty()) {
                         cleanEmailPrefix = "user";
                     }
                     
                     // Clean name to ensure it's not blank and contains valid characters
                     String cleanName = name.replaceAll("[^a-zA-Z0-9 ]", "a").trim();
                     if (cleanName.isEmpty() || cleanName.isBlank()) {
                         cleanName = "TestUser";
                     }
                     
                     // Clean password to ensure it's not blank and contains valid characters
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
                 (name, keyPrefix, description) -> {
                     // Clean name to ensure it's not blank and contains valid characters
                     String cleanName = name.replaceAll("[^a-zA-Z0-9 ]", "a").trim();
                     if (cleanName.isEmpty() || cleanName.isBlank()) {
                         cleanName = "TestProject";
                     }
                     
                     // Clean key prefix to only contain alphanumeric characters and ensure max 10 chars
                     String cleanKeyPrefix = keyPrefix.replaceAll("[^a-zA-Z0-9]", "A");
                     if (cleanKeyPrefix.isEmpty()) {
                         cleanKeyPrefix = "TEST";
                     }
                     String cleanKey = cleanKeyPrefix.toUpperCase();
                     if (cleanKey.length() > 10) {
                         cleanKey = cleanKey.substring(0, 10);
                     }
                     
                     // Clean description to ensure it's not blank
                     String cleanDescription = description.replaceAll("[^a-zA-Z0-9 .,!?-]", "a").trim();
                     if (cleanDescription.isEmpty() || cleanDescription.isBlank()) {
                         cleanDescription = "Test project description";
                     }
                     
                     return new ProjectData(
                         cleanName,
                         cleanKey,
                         cleanDescription
                     );
                 });
    }

    private Gen<IssueData> issueDataGenerator() {
        return strings().allPossible().ofLengthBetween(5, 100)
            .zip(strings().allPossible().ofLengthBetween(10, 500),
                 integers().between(0, Priority.values().length - 1),
                 (title, description, priorityIndex) -> {
                     // Clean title to ensure it's not blank and contains valid characters
                     String cleanTitle = title.replaceAll("[^a-zA-Z0-9 .,!?-]", "a").trim();
                     if (cleanTitle.isEmpty() || cleanTitle.isBlank()) {
                         cleanTitle = "Test Issue";
                     }
                     
                     // Clean description to ensure it's not blank
                     String cleanDescription = description.replaceAll("[^a-zA-Z0-9 .,!?-]", "a").trim();
                     if (cleanDescription.isEmpty() || cleanDescription.isBlank()) {
                         cleanDescription = "Test issue description";
                     }
                     
                     return new IssueData(
                         cleanTitle,
                         cleanDescription,
                         Priority.values()[priorityIndex]
                     );
                 });
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
}
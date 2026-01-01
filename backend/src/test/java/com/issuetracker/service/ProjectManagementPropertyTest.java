package com.issuetracker.service;

import com.issuetracker.base.BasePostgreSQLTest;
import com.issuetracker.dto.CreateProjectRequest;
import com.issuetracker.dto.ProjectDto;
import com.issuetracker.dto.UpdateProjectRequest;
import com.issuetracker.entity.User;
import com.issuetracker.exception.DuplicateResourceException;
import com.issuetracker.exception.ResourceNotFoundException;
import com.issuetracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.quicktheories.QuickTheory;
import org.quicktheories.core.Gen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.quicktheories.generators.SourceDSL.*;

/**
 * Property-based tests for project management lifecycle.
 * Feature: personal-issue-tracker, Property 4: Project Management Lifecycle
 * Validates: Requirements 2.1, 2.5
 * Uses Testcontainers with PostgreSQL for production parity.
 */
@Transactional
public class ProjectManagementPropertyTest extends BasePostgreSQLTest {

    // QuickTheory instance for property testing
    private static final QuickTheory qt = QuickTheory.qt();

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Property 4: Project Management Lifecycle
     * For any project operations (create, update, delete), the system should maintain 
     * referential integrity, generate unique project keys, and properly handle cascade 
     * operations for associated issues.
     * 
     * Validates: Requirements 2.1, 2.5
     */
    @Test
    void projectManagementLifecycleProperty() {
        qt.forAll(
            userGenerator(),
            projectRequestGenerator(),
            projectUpdateGenerator()
        ).checkAssert((user, createRequest, updateRequest) -> {
            // Persist user
            User savedUser = userRepository.save(user);
            
            // Test project creation
            ProjectDto createdProject = projectService.createProject(createRequest, savedUser);
            
            // Verify project creation properties
            assertThat(createdProject).isNotNull();
            assertThat(createdProject.getId()).isNotNull();
            assertThat(createdProject.getName()).isEqualTo(createRequest.getName());
            assertThat(createdProject.getKey()).isEqualTo(createRequest.getKey());
            assertThat(createdProject.getDescription()).isEqualTo(createRequest.getDescription());
            assertThat(createdProject.getCreatedAt()).isNotNull();
            assertThat(createdProject.getUpdatedAt()).isNotNull();
            assertThat(createdProject.getIssueCount()).isEqualTo(0L);
            
            // Test project retrieval by ID
            ProjectDto retrievedById = projectService.getProject(createdProject.getId(), savedUser);
            assertThat(retrievedById).isEqualTo(createdProject);
            
            // Test project retrieval by key
            ProjectDto retrievedByKey = projectService.getProjectByKey(createdProject.getKey(), savedUser);
            assertThat(retrievedByKey).isEqualTo(createdProject);
            
            // Test project key uniqueness - attempting to create another project with same key should fail
            CreateProjectRequest duplicateKeyRequest = new CreateProjectRequest(
                createRequest.getName() + "_duplicate",
                createRequest.getKey(), // Same key
                createRequest.getDescription()
            );
            assertThatThrownBy(() -> projectService.createProject(duplicateKeyRequest, savedUser))
                .isInstanceOf(DuplicateResourceException.class);
            
            // Test project update
            ProjectDto updatedProject = projectService.updateProject(
                createdProject.getId(), 
                updateRequest, 
                savedUser
            );
            
            // Verify update properties
            assertThat(updatedProject.getId()).isEqualTo(createdProject.getId());
            assertThat(updatedProject.getName()).isEqualTo(updateRequest.getName());
            assertThat(updatedProject.getKey()).isEqualTo(createdProject.getKey()); // Key should not change
            assertThat(updatedProject.getDescription()).isEqualTo(updateRequest.getDescription());
            assertThat(updatedProject.getCreatedAt()).isEqualTo(createdProject.getCreatedAt());
            assertThat(updatedProject.getUpdatedAt()).isAfterOrEqualTo(createdProject.getUpdatedAt());
            
            // Test project count
            long projectCount = projectService.getProjectCount(savedUser);
            assertThat(projectCount).isEqualTo(1L);
            
            // Test project key availability check
            assertThat(projectService.isProjectKeyAvailable(createdProject.getKey(), savedUser)).isFalse();
            assertThat(projectService.isProjectKeyAvailable("NONEXISTENT", savedUser)).isTrue();
            
            // Test project deletion
            projectService.deleteProject(createdProject.getId(), savedUser);
            
            // Verify project is deleted
            assertThatThrownBy(() -> projectService.getProject(createdProject.getId(), savedUser))
                .isInstanceOf(ResourceNotFoundException.class);
            
            // Verify project count after deletion
            long projectCountAfterDeletion = projectService.getProjectCount(savedUser);
            assertThat(projectCountAfterDeletion).isEqualTo(0L);
            
            // Verify project key is now available after deletion
            assertThat(projectService.isProjectKeyAvailable(createdProject.getKey(), savedUser)).isTrue();
        });
    }

    /**
     * Test user isolation in project operations.
     * Ensures users can only access their own projects.
     */
    @Test
    void projectUserIsolationProperty() {
        qt.forAll(
            userGenerator(),
            userGenerator(),
            projectRequestGenerator()
        ).checkAssert((user1, user2, projectRequest) -> {
            // Persist users
            User savedUser1 = userRepository.save(user1);
            User savedUser2 = userRepository.save(user2);
            
            // Create project for user1
            ProjectDto project1 = projectService.createProject(projectRequest, savedUser1);
            
            // User2 should not be able to access user1's project
            assertThatThrownBy(() -> projectService.getProject(project1.getId(), savedUser2))
                .isInstanceOf(ResourceNotFoundException.class);
            
            assertThatThrownBy(() -> projectService.getProjectByKey(project1.getKey(), savedUser2))
                .isInstanceOf(ResourceNotFoundException.class);
            
            // User2 should not be able to update user1's project
            UpdateProjectRequest updateRequest = new UpdateProjectRequest("Updated Name", "Updated Description");
            assertThatThrownBy(() -> projectService.updateProject(project1.getId(), updateRequest, savedUser2))
                .isInstanceOf(ResourceNotFoundException.class);
            
            // User2 should not be able to delete user1's project
            assertThatThrownBy(() -> projectService.deleteProject(project1.getId(), savedUser2))
                .isInstanceOf(ResourceNotFoundException.class);
            
            // User2 should be able to use the same project key (keys are unique per user)
            CreateProjectRequest sameKeyRequest = new CreateProjectRequest(
                "Different Project",
                projectRequest.getKey(),
                "Different Description"
            );
            ProjectDto project2 = projectService.createProject(sameKeyRequest, savedUser2);
            assertThat(project2.getKey()).isEqualTo(project1.getKey());
            assertThat(project2.getId()).isNotEqualTo(project1.getId());
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

    private Gen<CreateProjectRequest> projectRequestGenerator() {
        return strings().allPossible().ofLengthBetween(3, 50)
            .zip(strings().ascii().ofLengthBetween(2, 5),
                 strings().allPossible().ofLengthBetween(10, 200),
                 integers().between(1, 1000000),
                 (name, keyPrefix, description, uniqueId) -> {
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
                     String cleanKey = (cleanKeyPrefix + uniqueId).toUpperCase();
                     if (cleanKey.length() > 10) {
                         cleanKey = cleanKey.substring(0, 10);
                     }
                     
                     // Clean description to ensure it's not blank
                     String cleanDescription = description.replaceAll("[^a-zA-Z0-9 .,!?-]", "a").trim();
                     if (cleanDescription.isEmpty() || cleanDescription.isBlank()) {
                         cleanDescription = "Test project description";
                     }
                     
                     return new CreateProjectRequest(cleanName, cleanKey, cleanDescription);
                 });
    }

    private Gen<UpdateProjectRequest> projectUpdateGenerator() {
        return strings().allPossible().ofLengthBetween(3, 50)
            .zip(strings().allPossible().ofLengthBetween(10, 200),
                 (name, description) -> {
                     // Clean name to ensure it's not blank and contains valid characters
                     String cleanName = name.replaceAll("[^a-zA-Z0-9 ]", "a").trim();
                     if (cleanName.isEmpty() || cleanName.isBlank()) {
                         cleanName = "UpdatedProject";
                     }
                     
                     // Clean description to ensure it's not blank
                     String cleanDescription = description.replaceAll("[^a-zA-Z0-9 .,!?-]", "a").trim();
                     if (cleanDescription.isEmpty() || cleanDescription.isBlank()) {
                         cleanDescription = "Updated project description";
                     }
                     
                     return new UpdateProjectRequest(cleanName, cleanDescription);
                 });
    }
}
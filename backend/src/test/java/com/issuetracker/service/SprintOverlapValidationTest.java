package com.issuetracker.service;

import com.issuetracker.dto.CreateSprintRequest;
import com.issuetracker.dto.SprintDto;
import com.issuetracker.entity.*;
import com.issuetracker.exception.InvalidSprintOperationException;
import com.issuetracker.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for sprint overlap validation functionality.
 * Verifies that completed sprints don't block creation of new sprints with overlapping dates.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SprintOverlapValidationTest {

    @Autowired
    private SprintService sprintService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SprintRepository sprintRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User("test@example.com", "Test User", "password");
        testUser = userRepository.save(testUser);
    }

    @Test
    void shouldAllowCreatingSprintWithSameDatesAsCompletedSprint() {
        // Given: A completed sprint with specific dates
        LocalDate startDate = LocalDate.of(2026, 1, 15);
        LocalDate endDate = LocalDate.of(2026, 1, 29);
        
        Sprint completedSprint = new Sprint(testUser, "Completed Sprint", startDate, endDate, "Completed sprint goal");
        completedSprint.setStatus(SprintStatus.COMPLETED);
        sprintRepository.save(completedSprint);

        // When: Creating a new sprint with the same dates
        CreateSprintRequest newSprintRequest = new CreateSprintRequest(
                "New Sprint", startDate, endDate, "New sprint goal");

        // Then: Should succeed without throwing exception
        SprintDto newSprint = sprintService.createSprint(newSprintRequest, testUser);
        
        assertThat(newSprint).isNotNull();
        assertThat(newSprint.getName()).isEqualTo("New Sprint");
        assertThat(newSprint.getStartDate()).isEqualTo(startDate);
        assertThat(newSprint.getEndDate()).isEqualTo(endDate);
        assertThat(newSprint.getStatus()).isEqualTo(SprintStatus.PLANNED);
    }

    @Test
    void shouldBlockCreatingSprintWithSameDatesAsActiveSprint() {
        // Given: An active sprint with specific dates
        LocalDate startDate = LocalDate.of(2026, 1, 15);
        LocalDate endDate = LocalDate.of(2026, 1, 29);
        
        Sprint activeSprint = new Sprint(testUser, "Active Sprint", startDate, endDate, "Active sprint goal");
        activeSprint.setStatus(SprintStatus.ACTIVE);
        sprintRepository.save(activeSprint);

        // When: Trying to create a new sprint with overlapping dates
        CreateSprintRequest newSprintRequest = new CreateSprintRequest(
                "Overlapping Sprint", startDate, endDate, "Overlapping sprint goal");

        // Then: Should throw exception
        assertThatThrownBy(() -> sprintService.createSprint(newSprintRequest, testUser))
                .isInstanceOf(InvalidSprintOperationException.class)
                .hasMessageContaining("Sprint dates overlap with existing sprint");
    }

    @Test
    void shouldBlockCreatingSprintWithSameDatesAsPlannedSprint() {
        // Given: A planned sprint with specific dates
        LocalDate startDate = LocalDate.of(2026, 1, 15);
        LocalDate endDate = LocalDate.of(2026, 1, 29);
        
        Sprint plannedSprint = new Sprint(testUser, "Planned Sprint", startDate, endDate, "Planned sprint goal");
        plannedSprint.setStatus(SprintStatus.PLANNED);
        sprintRepository.save(plannedSprint);

        // When: Trying to create a new sprint with overlapping dates
        CreateSprintRequest newSprintRequest = new CreateSprintRequest(
                "Overlapping Sprint", startDate, endDate, "Overlapping sprint goal");

        // Then: Should throw exception
        assertThatThrownBy(() -> sprintService.createSprint(newSprintRequest, testUser))
                .isInstanceOf(InvalidSprintOperationException.class)
                .hasMessageContaining("Sprint dates overlap with existing sprint");
    }

    @Test
    void shouldAllowCreatingSprintWithPartialOverlapWithCompletedSprint() {
        // Given: A completed sprint
        LocalDate completedStart = LocalDate.of(2026, 1, 10);
        LocalDate completedEnd = LocalDate.of(2026, 1, 20);
        
        Sprint completedSprint = new Sprint(testUser, "Completed Sprint", completedStart, completedEnd, "Completed goal");
        completedSprint.setStatus(SprintStatus.COMPLETED);
        sprintRepository.save(completedSprint);

        // When: Creating a new sprint with partial overlap
        LocalDate newStart = LocalDate.of(2026, 1, 15);
        LocalDate newEnd = LocalDate.of(2026, 1, 25);
        
        CreateSprintRequest newSprintRequest = new CreateSprintRequest(
                "Partially Overlapping Sprint", newStart, newEnd, "New sprint goal");

        // Then: Should succeed
        SprintDto newSprint = sprintService.createSprint(newSprintRequest, testUser);
        
        assertThat(newSprint).isNotNull();
        assertThat(newSprint.getName()).isEqualTo("Partially Overlapping Sprint");
        assertThat(newSprint.getStatus()).isEqualTo(SprintStatus.PLANNED);
    }

    @Test
    void shouldAllowMultipleSprintsWithSameDatesAsCompletedSprints() {
        // Given: Multiple completed sprints with same dates
        LocalDate startDate = LocalDate.of(2026, 1, 15);
        LocalDate endDate = LocalDate.of(2026, 1, 29);
        
        Sprint completedSprint1 = new Sprint(testUser, "Completed Sprint 1", startDate, endDate, "Goal 1");
        completedSprint1.setStatus(SprintStatus.COMPLETED);
        sprintRepository.save(completedSprint1);
        
        Sprint completedSprint2 = new Sprint(testUser, "Completed Sprint 2", startDate, endDate, "Goal 2");
        completedSprint2.setStatus(SprintStatus.COMPLETED);
        sprintRepository.save(completedSprint2);

        // When: Creating new sprints with non-overlapping dates (but same as completed ones)
        CreateSprintRequest newSprintRequest1 = new CreateSprintRequest(
                "New Sprint 1", startDate, endDate, "New goal 1");
        CreateSprintRequest newSprintRequest2 = new CreateSprintRequest(
                "New Sprint 2", startDate.plusDays(30), endDate.plusDays(30), "New goal 2");

        // Then: Both should succeed
        SprintDto newSprint1 = sprintService.createSprint(newSprintRequest1, testUser);
        SprintDto newSprint2 = sprintService.createSprint(newSprintRequest2, testUser);
        
        assertThat(newSprint1).isNotNull();
        assertThat(newSprint2).isNotNull();
        assertThat(newSprint1.getStatus()).isEqualTo(SprintStatus.PLANNED);
        assertThat(newSprint2.getStatus()).isEqualTo(SprintStatus.PLANNED);
    }

    @Test
    void shouldIncludeConflictingSprintDetailsInErrorMessage() {
        // Given: A planned sprint with specific dates and name
        LocalDate startDate = LocalDate.of(2026, 1, 15);
        LocalDate endDate = LocalDate.of(2026, 1, 29);
        String conflictingSprintName = "Existing Development Sprint";
        
        CreateSprintRequest existingSprintRequest = new CreateSprintRequest();
        existingSprintRequest.setName(conflictingSprintName);
        existingSprintRequest.setStartDate(startDate);
        existingSprintRequest.setEndDate(endDate);
        existingSprintRequest.setGoal("Existing sprint goal");
        
        sprintService.createSprint(existingSprintRequest, testUser);

        // When: Trying to create a new sprint with overlapping dates
        CreateSprintRequest newSprintRequest = new CreateSprintRequest();
        newSprintRequest.setName("New Conflicting Sprint");
        newSprintRequest.setStartDate(startDate); // Same start date
        newSprintRequest.setEndDate(endDate.plusDays(5)); // Overlapping end date
        newSprintRequest.setGoal("New sprint goal");

        // Then: Should throw exception with specific sprint details
        assertThatThrownBy(() -> sprintService.createSprint(newSprintRequest, testUser))
                .isInstanceOf(InvalidSprintOperationException.class)
                .hasMessageContaining("Sprint dates overlap with existing sprint '" + conflictingSprintName + "'")
                .hasMessageContaining("2026-01-15 to 2026-01-29");
    }
}
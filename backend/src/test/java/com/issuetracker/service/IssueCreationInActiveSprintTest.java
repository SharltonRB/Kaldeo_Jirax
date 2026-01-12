package com.issuetracker.service;

import com.issuetracker.dto.CreateIssueRequest;
import com.issuetracker.dto.CreateSprintRequest;
import com.issuetracker.dto.IssueDto;
import com.issuetracker.dto.SprintDto;
import com.issuetracker.entity.*;
import com.issuetracker.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to verify that issues created in active sprints are created with SELECTED_FOR_DEVELOPMENT status.
 * NEW RULE: Issues created in active sprint board should start in SELECTED_FOR_DEVELOPMENT status.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class IssueCreationInActiveSprintTest {

    @Autowired
    private IssueService issueService;

    @Autowired
    private SprintService sprintService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private IssueTypeRepository issueTypeRepository;

    private User testUser;
    private Project testProject;
    private IssueType storyType;
    private IssueType epicType;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User("test@example.com", "password", "Test User");
        testUser = userRepository.save(testUser);

        // Create test project
        testProject = new Project(testUser, "Test Project", "TP", "Test project description");
        testProject = projectRepository.save(testProject);

        // Create issue types (global types)
        storyType = new IssueType("STORY", "Story issue type", true);
        storyType = issueTypeRepository.save(storyType);
        
        epicType = new IssueType("EPIC", "Epic issue type", true);
        epicType = issueTypeRepository.save(epicType);
    }

    @Test
    void testIssueCreatedInActiveSprintHasSelectedForDevelopmentStatus() {
        // Create and activate a sprint
        CreateSprintRequest sprintRequest = new CreateSprintRequest(
                "Active Sprint",
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );
        SprintDto sprint = sprintService.createSprint(sprintRequest, testUser);
        sprintService.activateSprint(sprint.getId(), testUser);

        // Create an epic first
        CreateIssueRequest epicRequest = new CreateIssueRequest(
                "Test Epic",
                "Epic for active sprint testing",
                Priority.HIGH,
                testProject.getId(),
                epicType.getId()
        );
        IssueDto epic = issueService.createIssue(epicRequest, testUser);

        // Create an issue in the active sprint
        CreateIssueRequest issueRequest = new CreateIssueRequest(
                "Story in Active Sprint",
                "This story should be created in SELECTED_FOR_DEVELOPMENT status",
                Priority.HIGH,
                testProject.getId(),
                storyType.getId()
        );
        issueRequest.setSprintId(sprint.getId());
        issueRequest.setParentIssueId(epic.getId());
        
        IssueDto createdIssue = issueService.createIssue(issueRequest, testUser);

        // NEW RULE: Issue created in active sprint should have SELECTED_FOR_DEVELOPMENT status
        assertThat(createdIssue.getStatus()).isEqualTo(IssueStatus.SELECTED_FOR_DEVELOPMENT);
        assertThat(createdIssue.getSprintId()).isEqualTo(sprint.getId());
    }

    @Test
    void testIssueCreatedInPlannedSprintHasBacklogStatus() {
        // Create a planned sprint (not activated)
        CreateSprintRequest sprintRequest = new CreateSprintRequest(
                "Planned Sprint",
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(21)
        );
        SprintDto sprint = sprintService.createSprint(sprintRequest, testUser);
        // Note: Not activating the sprint, so it remains PLANNED

        // Create an epic first
        CreateIssueRequest epicRequest = new CreateIssueRequest(
                "Test Epic",
                "Epic for planned sprint testing",
                Priority.HIGH,
                testProject.getId(),
                epicType.getId()
        );
        IssueDto epic = issueService.createIssue(epicRequest, testUser);

        // Create an issue in the planned sprint
        CreateIssueRequest issueRequest = new CreateIssueRequest(
                "Story in Planned Sprint",
                "This story should be created in BACKLOG status",
                Priority.HIGH,
                testProject.getId(),
                storyType.getId()
        );
        issueRequest.setSprintId(sprint.getId());
        issueRequest.setParentIssueId(epic.getId());
        
        IssueDto createdIssue = issueService.createIssue(issueRequest, testUser);

        // Issue created in planned sprint should have BACKLOG status
        assertThat(createdIssue.getStatus()).isEqualTo(IssueStatus.BACKLOG);
        assertThat(createdIssue.getSprintId()).isEqualTo(sprint.getId());
    }

    @Test
    void testIssueCreatedWithoutSprintHasBacklogStatus() {
        // Create an epic first
        CreateIssueRequest epicRequest = new CreateIssueRequest(
                "Test Epic",
                "Epic for no sprint testing",
                Priority.HIGH,
                testProject.getId(),
                epicType.getId()
        );
        IssueDto epic = issueService.createIssue(epicRequest, testUser);

        // Create an issue without assigning to any sprint
        CreateIssueRequest issueRequest = new CreateIssueRequest(
                "Story without Sprint",
                "This story should be created in BACKLOG status",
                Priority.HIGH,
                testProject.getId(),
                storyType.getId()
        );
        issueRequest.setParentIssueId(epic.getId());
        
        IssueDto createdIssue = issueService.createIssue(issueRequest, testUser);

        // Issue created without sprint should have BACKLOG status
        assertThat(createdIssue.getStatus()).isEqualTo(IssueStatus.BACKLOG);
        assertThat(createdIssue.getSprintId()).isNull();
    }

    @Test
    void testDirectStatusTransitionsWork() {
        // Create an epic first
        CreateIssueRequest epicRequest = new CreateIssueRequest(
                "Test Epic",
                "Epic for transition testing",
                Priority.HIGH,
                testProject.getId(),
                epicType.getId()
        );
        IssueDto epic = issueService.createIssue(epicRequest, testUser);

        // Create an issue
        CreateIssueRequest issueRequest = new CreateIssueRequest(
                "Story for Transition Test",
                "Testing direct transitions",
                Priority.HIGH,
                testProject.getId(),
                storyType.getId()
        );
        issueRequest.setParentIssueId(epic.getId());
        
        IssueDto issue = issueService.createIssue(issueRequest, testUser);

        // Verify it starts in BACKLOG
        assertThat(issue.getStatus()).isEqualTo(IssueStatus.BACKLOG);

        // NEW RULE: Test direct transitions from any status to any status
        
        // BACKLOG -> DONE (skip all intermediate steps)
        issueService.updateIssueStatus(issue.getId(), 
                new com.issuetracker.dto.StatusUpdateRequest(IssueStatus.DONE), testUser);
        
        IssueDto updatedIssue = issueService.getIssue(issue.getId(), testUser);
        assertThat(updatedIssue.getStatus()).isEqualTo(IssueStatus.DONE);

        // DONE -> IN_PROGRESS (backwards transition)
        issueService.updateIssueStatus(issue.getId(), 
                new com.issuetracker.dto.StatusUpdateRequest(IssueStatus.IN_PROGRESS), testUser);
        
        updatedIssue = issueService.getIssue(issue.getId(), testUser);
        assertThat(updatedIssue.getStatus()).isEqualTo(IssueStatus.IN_PROGRESS);

        // IN_PROGRESS -> BACKLOG (back to start)
        issueService.updateIssueStatus(issue.getId(), 
                new com.issuetracker.dto.StatusUpdateRequest(IssueStatus.BACKLOG), testUser);
        
        updatedIssue = issueService.getIssue(issue.getId(), testUser);
        assertThat(updatedIssue.getStatus()).isEqualTo(IssueStatus.BACKLOG);
    }
}
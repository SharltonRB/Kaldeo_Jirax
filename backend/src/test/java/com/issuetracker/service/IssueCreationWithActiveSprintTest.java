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
 * Test to verify that issues created with an active sprint assignment
 * get the correct status (SELECTED_FOR_DEVELOPMENT instead of BACKLOG).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class IssueCreationWithActiveSprintTest {

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
    private IssueType epicType;
    private IssueType storyType;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User("test@example.com", "password", "Test User");
        testUser = userRepository.save(testUser);

        // Create test project
        testProject = new Project(testUser, "Test Project", "TP", "Test project description");
        testProject = projectRepository.save(testProject);

        // Create issue types
        epicType = new IssueType("EPIC", "Epic issue type", true);
        epicType = issueTypeRepository.save(epicType);

        storyType = new IssueType("STORY", "Story issue type", true);
        storyType = issueTypeRepository.save(storyType);
    }

    @Test
    void testIssueCreatedWithActiveSprintGetsSelectedForDevelopmentStatus() {
        // Create an epic first
        CreateIssueRequest epicRequest = new CreateIssueRequest(
                "Test Epic",
                "Epic description",
                Priority.MEDIUM,
                testProject.getId(),
                epicType.getId()
        );
        IssueDto epic = issueService.createIssue(epicRequest, testUser);

        // Create an active sprint
        CreateSprintRequest sprintRequest = new CreateSprintRequest(
                "Active Sprint",
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );
        SprintDto sprint = sprintService.createSprint(sprintRequest, testUser);

        // Activate the sprint to make it active
        sprintService.activateSprint(sprint.getId(), testUser);

        // Create an issue assigned to the active sprint
        CreateIssueRequest issueRequest = new CreateIssueRequest(
                "Test Story",
                "Story description",
                Priority.HIGH,
                testProject.getId(),
                storyType.getId()
        );
        issueRequest.setParentIssueId(epic.getId());
        issueRequest.setSprintId(sprint.getId());

        // Create the issue
        IssueDto createdIssue = issueService.createIssue(issueRequest, testUser);

        // Verify the issue has SELECTED_FOR_DEVELOPMENT status instead of BACKLOG
        assertThat(createdIssue.getStatus()).isEqualTo(IssueStatus.SELECTED_FOR_DEVELOPMENT);
        assertThat(createdIssue.getSprintId()).isEqualTo(sprint.getId());
        assertThat(createdIssue.getParentIssueId()).isEqualTo(epic.getId());
    }

    @Test
    void testIssueCreatedWithoutSprintGetsBacklogStatus() {
        // Create an epic first
        CreateIssueRequest epicRequest = new CreateIssueRequest(
                "Test Epic",
                "Epic description",
                Priority.MEDIUM,
                testProject.getId(),
                epicType.getId()
        );
        IssueDto epic = issueService.createIssue(epicRequest, testUser);

        // Create an issue without sprint assignment
        CreateIssueRequest issueRequest = new CreateIssueRequest(
                "Test Story",
                "Story description",
                Priority.HIGH,
                testProject.getId(),
                storyType.getId()
        );
        issueRequest.setParentIssueId(epic.getId());

        // Create the issue
        IssueDto createdIssue = issueService.createIssue(issueRequest, testUser);

        // Verify the issue has BACKLOG status (default)
        assertThat(createdIssue.getStatus()).isEqualTo(IssueStatus.BACKLOG);
        assertThat(createdIssue.getSprintId()).isNull();
        assertThat(createdIssue.getParentIssueId()).isEqualTo(epic.getId());
    }

    @Test
    void testIssueCreatedWithInactiveSprintGetsBacklogStatus() {
        // Create an epic first
        CreateIssueRequest epicRequest = new CreateIssueRequest(
                "Test Epic",
                "Epic description",
                Priority.MEDIUM,
                testProject.getId(),
                epicType.getId()
        );
        IssueDto epic = issueService.createIssue(epicRequest, testUser);

        // Create a sprint but don't activate it (remains PLANNED)
        CreateSprintRequest sprintRequest = new CreateSprintRequest(
                "Planned Sprint",
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(21)
        );
        SprintDto sprint = sprintService.createSprint(sprintRequest, testUser);

        // Create an issue assigned to the inactive sprint
        CreateIssueRequest issueRequest = new CreateIssueRequest(
                "Test Story",
                "Story description",
                Priority.HIGH,
                testProject.getId(),
                storyType.getId()
        );
        issueRequest.setParentIssueId(epic.getId());
        issueRequest.setSprintId(sprint.getId());

        // Create the issue
        IssueDto createdIssue = issueService.createIssue(issueRequest, testUser);

        // Verify the issue has BACKLOG status (since sprint is not active)
        assertThat(createdIssue.getStatus()).isEqualTo(IssueStatus.BACKLOG);
        assertThat(createdIssue.getSprintId()).isEqualTo(sprint.getId());
        assertThat(createdIssue.getParentIssueId()).isEqualTo(epic.getId());
    }
}
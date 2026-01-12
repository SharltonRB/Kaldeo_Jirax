package com.issuetracker.service;

import com.issuetracker.dto.CreateIssueRequest;
import com.issuetracker.dto.CreateSprintRequest;
import com.issuetracker.dto.IssueDto;
import com.issuetracker.dto.SprintDto;
import com.issuetracker.dto.StatusUpdateRequest;
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
 * Test to verify that sprint completion works correctly:
 * - Issues in DONE status remain in the sprint
 * - Issues not in DONE status are moved to BACKLOG and removed from sprint
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SprintCompletionTest {

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
    private IssueDto testEpic;

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

        // Create a test epic
        CreateIssueRequest epicRequest = new CreateIssueRequest(
                "Test Epic",
                "Epic for testing",
                Priority.HIGH,
                testProject.getId(),
                epicType.getId()
        );
        testEpic = issueService.createIssue(epicRequest, testUser);
    }

    @Test
    void testSprintCompletionMovesIncompleteIssuesToBacklog() {
        // Create and activate a sprint
        CreateSprintRequest sprintRequest = new CreateSprintRequest(
                "Test Sprint",
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );
        SprintDto sprint = sprintService.createSprint(sprintRequest, testUser);
        sprintService.activateSprint(sprint.getId(), testUser);

        // Create issues in different states
        CreateIssueRequest issueRequest1 = new CreateIssueRequest(
                "Completed Story",
                "This story is done",
                Priority.HIGH,
                testProject.getId(),
                storyType.getId()
        );
        issueRequest1.setSprintId(sprint.getId());
        issueRequest1.setParentIssueId(testEpic.getId());
        IssueDto completedIssue = issueService.createIssue(issueRequest1, testUser);

        CreateIssueRequest issueRequest2 = new CreateIssueRequest(
                "In Review Story",
                "This story is in review",
                Priority.HIGH,
                testProject.getId(),
                storyType.getId()
        );
        issueRequest2.setSprintId(sprint.getId());
        issueRequest2.setParentIssueId(testEpic.getId());
        IssueDto inReviewIssue = issueService.createIssue(issueRequest2, testUser);

        CreateIssueRequest issueRequest3 = new CreateIssueRequest(
                "In Progress Story",
                "This story is in progress",
                Priority.HIGH,
                testProject.getId(),
                storyType.getId()
        );
        issueRequest3.setSprintId(sprint.getId());
        issueRequest3.setParentIssueId(testEpic.getId());
        IssueDto inProgressIssue = issueService.createIssue(issueRequest3, testUser);

        // Move issues to different states (NEW RULE: direct transitions allowed)
        // Move first issue to DONE
        issueService.updateIssueStatus(completedIssue.getId(), 
                new StatusUpdateRequest(IssueStatus.DONE), testUser);

        // Move second issue to IN_REVIEW
        issueService.updateIssueStatus(inReviewIssue.getId(), 
                new StatusUpdateRequest(IssueStatus.IN_REVIEW), testUser);

        // Move third issue to IN_PROGRESS
        issueService.updateIssueStatus(inProgressIssue.getId(), 
                new StatusUpdateRequest(IssueStatus.IN_PROGRESS), testUser);

        // Complete the sprint
        SprintDto completedSprint = sprintService.completeSprint(sprint.getId(), testUser);

        // Verify sprint is completed
        assertThat(completedSprint.getStatus()).isEqualTo(SprintStatus.COMPLETED);

        // Verify issue states after sprint completion
        IssueDto completedIssueAfter = issueService.getIssue(completedIssue.getId(), testUser);
        IssueDto inReviewIssueAfter = issueService.getIssue(inReviewIssue.getId(), testUser);
        IssueDto inProgressIssueAfter = issueService.getIssue(inProgressIssue.getId(), testUser);

        // Completed issue should remain in sprint and keep DONE status
        assertThat(completedIssueAfter.getStatus()).isEqualTo(IssueStatus.DONE);
        assertThat(completedIssueAfter.getSprintId()).isEqualTo(sprint.getId());
        assertThat(completedIssueAfter.getLastCompletedSprintId()).isNull(); // Should not be set for completed issues

        // In-review issue should be moved to backlog and removed from sprint
        assertThat(inReviewIssueAfter.getStatus()).isEqualTo(IssueStatus.BACKLOG);
        assertThat(inReviewIssueAfter.getSprintId()).isNull();
        assertThat(inReviewIssueAfter.getLastCompletedSprintId()).isEqualTo(sprint.getId());

        // In-progress issue should be moved to backlog and removed from sprint
        assertThat(inProgressIssueAfter.getStatus()).isEqualTo(IssueStatus.BACKLOG);
        assertThat(inProgressIssueAfter.getSprintId()).isNull();
        assertThat(inProgressIssueAfter.getLastCompletedSprintId()).isEqualTo(sprint.getId());
    }

    @Test
    void testSprintCompletionWithAllIssuesDone() {
        // Create and activate a sprint
        CreateSprintRequest sprintRequest = new CreateSprintRequest(
                "All Done Sprint",
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );
        SprintDto sprint = sprintService.createSprint(sprintRequest, testUser);
        sprintService.activateSprint(sprint.getId(), testUser);

        // Create an issue and complete it
        CreateIssueRequest issueRequest = new CreateIssueRequest(
                "Completed Story",
                "This story is done",
                Priority.HIGH,
                testProject.getId(),
                storyType.getId()
        );
        issueRequest.setSprintId(sprint.getId());
        issueRequest.setParentIssueId(testEpic.getId());
        IssueDto issue = issueService.createIssue(issueRequest, testUser);

        // Move issue to DONE (NEW RULE: direct transition allowed)
        issueService.updateIssueStatus(issue.getId(), 
                new StatusUpdateRequest(IssueStatus.DONE), testUser);

        // Complete the sprint
        SprintDto completedSprint = sprintService.completeSprint(sprint.getId(), testUser);

        // Verify sprint is completed
        assertThat(completedSprint.getStatus()).isEqualTo(SprintStatus.COMPLETED);

        // Verify issue remains in sprint
        IssueDto issueAfter = issueService.getIssue(issue.getId(), testUser);
        assertThat(issueAfter.getStatus()).isEqualTo(IssueStatus.DONE);
        assertThat(issueAfter.getSprintId()).isEqualTo(sprint.getId());
        assertThat(issueAfter.getLastCompletedSprintId()).isNull(); // Should not be set for completed issues
    }
}
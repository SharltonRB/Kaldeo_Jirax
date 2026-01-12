package com.issuetracker.service;

import com.issuetracker.dto.CreateIssueRequest;
import com.issuetracker.dto.IssueDto;
import com.issuetracker.dto.StatusUpdateRequest;
import com.issuetracker.entity.*;
import com.issuetracker.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to verify that epic auto-completion works correctly:
 * - When all child issues are DONE, the epic should automatically become DONE
 * - When an epic is DONE but a child issue is moved back, the epic should revert to IN_PROGRESS
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EpicAutoCompletionTest {

    @Autowired
    private IssueService issueService;

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
    void testEpicAutoCompletionWhenAllChildrenAreDone() {
        // Create an epic
        CreateIssueRequest epicRequest = new CreateIssueRequest(
                "Test Epic",
                "Epic for testing auto-completion",
                Priority.HIGH,
                testProject.getId(),
                epicType.getId()
        );
        IssueDto epic = issueService.createIssue(epicRequest, testUser);

        // Create child issues
        CreateIssueRequest childRequest1 = new CreateIssueRequest(
                "Child Story 1",
                "First child story",
                Priority.HIGH,
                testProject.getId(),
                storyType.getId()
        );
        childRequest1.setParentIssueId(epic.getId());
        IssueDto child1 = issueService.createIssue(childRequest1, testUser);

        CreateIssueRequest childRequest2 = new CreateIssueRequest(
                "Child Story 2",
                "Second child story",
                Priority.HIGH,
                testProject.getId(),
                storyType.getId()
        );
        childRequest2.setParentIssueId(epic.getId());
        IssueDto child2 = issueService.createIssue(childRequest2, testUser);

        // Initially, epic should not be DONE
        IssueDto currentEpic = issueService.getIssue(epic.getId(), testUser);
        assertThat(currentEpic.getStatus()).isNotEqualTo(IssueStatus.DONE);

        // Complete first child issue (NEW RULE: can go directly to any status)
        issueService.updateIssueStatus(child1.getId(), 
                new StatusUpdateRequest(IssueStatus.DONE), testUser);

        // Epic should still not be DONE (only one child completed)
        currentEpic = issueService.getIssue(epic.getId(), testUser);
        assertThat(currentEpic.getStatus()).isNotEqualTo(IssueStatus.DONE);

        // Complete second child issue (NEW RULE: can go directly to DONE)
        issueService.updateIssueStatus(child2.getId(), 
                new StatusUpdateRequest(IssueStatus.DONE), testUser);

        // Now epic should be automatically DONE
        currentEpic = issueService.getIssue(epic.getId(), testUser);
        assertThat(currentEpic.getStatus()).isEqualTo(IssueStatus.DONE);
    }

    @Test
    void testEpicRevertWhenChildIsMovedBack() {
        // Create an epic
        CreateIssueRequest epicRequest = new CreateIssueRequest(
                "Test Epic",
                "Epic for testing revert",
                Priority.HIGH,
                testProject.getId(),
                epicType.getId()
        );
        IssueDto epic = issueService.createIssue(epicRequest, testUser);

        // Create and complete a child issue
        CreateIssueRequest childRequest = new CreateIssueRequest(
                "Child Story",
                "Child story",
                Priority.HIGH,
                testProject.getId(),
                storyType.getId()
        );
        childRequest.setParentIssueId(epic.getId());
        IssueDto child = issueService.createIssue(childRequest, testUser);

        // Complete the child issue (this should auto-complete the epic)
        // NEW RULE: can go directly from BACKLOG to DONE
        issueService.updateIssueStatus(child.getId(), 
                new StatusUpdateRequest(IssueStatus.DONE), testUser);

        // Epic should be DONE
        IssueDto currentEpic = issueService.getIssue(epic.getId(), testUser);
        assertThat(currentEpic.getStatus()).isEqualTo(IssueStatus.DONE);

        // Move child back to IN_REVIEW
        issueService.updateIssueStatus(child.getId(), 
                new StatusUpdateRequest(IssueStatus.IN_REVIEW), testUser);

        // Epic should revert to IN_PROGRESS
        currentEpic = issueService.getIssue(epic.getId(), testUser);
        assertThat(currentEpic.getStatus()).isEqualTo(IssueStatus.IN_PROGRESS);
    }

    @Test
    void testEpicWithoutChildrenIsNotAutoCompleted() {
        // Create an epic without children
        CreateIssueRequest epicRequest = new CreateIssueRequest(
                "Empty Epic",
                "Epic without children",
                Priority.HIGH,
                testProject.getId(),
                epicType.getId()
        );
        IssueDto epic = issueService.createIssue(epicRequest, testUser);

        // Epic should remain in its initial status (not auto-completed)
        IssueDto currentEpic = issueService.getIssue(epic.getId(), testUser);
        assertThat(currentEpic.getStatus()).isNotEqualTo(IssueStatus.DONE);
    }

    @Test
    void testNonEpicIssueDoesNotTriggerAutoCompletion() {
        // Create an epic first
        CreateIssueRequest epicRequest = new CreateIssueRequest(
                "Parent Epic",
                "Epic for regular issue",
                Priority.HIGH,
                testProject.getId(),
                epicType.getId()
        );
        IssueDto epic = issueService.createIssue(epicRequest, testUser);

        // Create a regular issue (child of epic)
        CreateIssueRequest issueRequest = new CreateIssueRequest(
                "Regular Issue",
                "Not an epic",
                Priority.HIGH,
                testProject.getId(),
                storyType.getId()
        );
        issueRequest.setParentIssueId(epic.getId());
        IssueDto issue = issueService.createIssue(issueRequest, testUser);

        // Complete the issue (NEW RULE: can go directly to DONE)
        issueService.updateIssueStatus(issue.getId(), 
                new StatusUpdateRequest(IssueStatus.DONE), testUser);

        // The issue should be DONE
        IssueDto currentIssue = issueService.getIssue(issue.getId(), testUser);
        assertThat(currentIssue.getStatus()).isEqualTo(IssueStatus.DONE);

        // The parent epic should also be auto-completed since all children are done
        IssueDto currentEpic = issueService.getIssue(epic.getId(), testUser);
        assertThat(currentEpic.getStatus()).isEqualTo(IssueStatus.DONE);
    }
}
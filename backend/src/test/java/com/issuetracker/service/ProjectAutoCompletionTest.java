package com.issuetracker.service;

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
 * Test class for project auto-completion functionality.
 * Verifies that projects automatically change status based on epic completion.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProjectAutoCompletionTest {

    @Autowired
    private IssueService issueService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private IssueTypeRepository issueTypeRepository;

    private User testUser;
    private Project testProject;
    private IssueType epicType;
    private IssueType storyType;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User("test@example.com", "Test User", "password");
        testUser = userRepository.save(testUser);

        // Create test project
        testProject = new Project(testUser, "Test Project", "TEST", "Test project for auto-completion");
        testProject = projectRepository.save(testProject);

        // Create issue types (global types)
        epicType = new IssueType("EPIC", "Epic issue type", true);
        epicType = issueTypeRepository.save(epicType);
        
        storyType = new IssueType("STORY", "Story issue type", true);
        storyType = issueTypeRepository.save(storyType);
    }

    @Test
    void shouldCompleteProjectWhenAllEpicsAreDone() {
        // Given: A project with two epics, each with child issues
        Issue epic1 = createEpic("Epic 1", "First epic");
        Issue epic2 = createEpic("Epic 2", "Second epic");

        Issue story1 = createStory("Story 1", "First story", epic1);
        Issue story2 = createStory("Story 2", "Second story", epic1);
        Issue story3 = createStory("Story 3", "Third story", epic2);

        // Initially project should be IN_PROGRESS
        assertThat(testProject.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);

        // When: Complete all stories in epic1
        issueService.updateIssueStatus(story1.getId(), new StatusUpdateRequest(IssueStatus.DONE), testUser);
        issueService.updateIssueStatus(story2.getId(), new StatusUpdateRequest(IssueStatus.DONE), testUser);

        // Then: Epic1 should be DONE, but project should still be IN_PROGRESS
        epic1 = issueRepository.findById(epic1.getId()).orElseThrow();
        testProject = projectRepository.findById(testProject.getId()).orElseThrow();
        assertThat(epic1.getStatus()).isEqualTo(IssueStatus.DONE);
        assertThat(testProject.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);

        // When: Complete the story in epic2
        issueService.updateIssueStatus(story3.getId(), new StatusUpdateRequest(IssueStatus.DONE), testUser);

        // Then: Epic2 should be DONE and project should be DONE
        epic2 = issueRepository.findById(epic2.getId()).orElseThrow();
        testProject = projectRepository.findById(testProject.getId()).orElseThrow();
        assertThat(epic2.getStatus()).isEqualTo(IssueStatus.DONE);
        assertThat(testProject.getStatus()).isEqualTo(ProjectStatus.DONE);
    }

    @Test
    void shouldRevertProjectWhenEpicIsReopened() {
        // Given: A completed project with all epics DONE
        Issue epic1 = createEpic("Epic 1", "First epic");
        Issue epic2 = createEpic("Epic 2", "Second epic");

        Issue story1 = createStory("Story 1", "First story", epic1);
        Issue story2 = createStory("Story 2", "Second story", epic2);

        // Complete all stories to make epics and project DONE
        issueService.updateIssueStatus(story1.getId(), new StatusUpdateRequest(IssueStatus.DONE), testUser);
        issueService.updateIssueStatus(story2.getId(), new StatusUpdateRequest(IssueStatus.DONE), testUser);

        // Verify project is DONE
        testProject = projectRepository.findById(testProject.getId()).orElseThrow();
        assertThat(testProject.getStatus()).isEqualTo(ProjectStatus.DONE);

        // When: Reopen one story
        issueService.updateIssueStatus(story1.getId(), new StatusUpdateRequest(IssueStatus.IN_PROGRESS), testUser);

        // Then: Epic1 should revert to IN_PROGRESS and project should revert to IN_PROGRESS
        epic1 = issueRepository.findById(epic1.getId()).orElseThrow();
        testProject = projectRepository.findById(testProject.getId()).orElseThrow();
        assertThat(epic1.getStatus()).isEqualTo(IssueStatus.IN_PROGRESS);
        assertThat(testProject.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
    }

    @Test
    void shouldKeepProjectInProgressWhenNoEpicsExist() {
        // Given: A project with no epics (only direct stories - which shouldn't happen in real scenario)
        // This test ensures the system handles edge cases gracefully

        // When: Check project status
        testProject = projectRepository.findById(testProject.getId()).orElseThrow();

        // Then: Project should remain IN_PROGRESS
        assertThat(testProject.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
    }

    @Test
    void shouldHandleEpicWithoutChildren() {
        // Given: A project with an epic that has no child issues
        Issue epic1 = createEpic("Epic 1", "Epic without children");

        // When: Check project status
        testProject = projectRepository.findById(testProject.getId()).orElseThrow();

        // Then: Project should remain IN_PROGRESS (epics without children don't auto-complete)
        assertThat(testProject.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
        assertThat(epic1.getStatus()).isEqualTo(IssueStatus.BACKLOG);
    }

    @Test
    void shouldCompleteProjectWithMixedEpicStates() {
        // Given: A project with multiple epics in different states
        Issue epic1 = createEpic("Epic 1", "First epic");
        Issue epic2 = createEpic("Epic 2", "Second epic");
        Issue epic3 = createEpic("Epic 3", "Third epic - no children");

        Issue story1 = createStory("Story 1", "First story", epic1);
        Issue story2 = createStory("Story 2", "Second story", epic2);

        // When: Complete all stories
        issueService.updateIssueStatus(story1.getId(), new StatusUpdateRequest(IssueStatus.DONE), testUser);
        issueService.updateIssueStatus(story2.getId(), new StatusUpdateRequest(IssueStatus.DONE), testUser);

        // Then: Project should be DONE because epic1 and epic2 are DONE (auto-completed)
        // and epic3 has no children so it doesn't block project completion
        testProject = projectRepository.findById(testProject.getId()).orElseThrow();
        assertThat(testProject.getStatus()).isEqualTo(ProjectStatus.DONE);

        // When: Manually complete the epic without children (optional, doesn't change project status)
        issueService.updateIssueStatus(epic3.getId(), new StatusUpdateRequest(IssueStatus.DONE), testUser);

        // Then: Project should still be DONE
        testProject = projectRepository.findById(testProject.getId()).orElseThrow();
        assertThat(testProject.getStatus()).isEqualTo(ProjectStatus.DONE);
    }

    private Issue createEpic(String title, String description) {
        Issue epic = new Issue(testUser, testProject, epicType, title, description, Priority.MEDIUM);
        return issueRepository.save(epic);
    }

    private Issue createStory(String title, String description, Issue parentEpic) {
        Issue story = new Issue(testUser, testProject, storyType, title, description, Priority.MEDIUM, parentEpic);
        return issueRepository.save(story);
    }
}
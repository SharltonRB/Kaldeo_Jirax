package com.issuetracker.service;

import com.issuetracker.dto.CreateIssueRequest;
import com.issuetracker.dto.IssueDto;
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
 * Test to verify that epic mapping works correctly in the DTO conversion.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EpicMappingTest {

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
    void testEpicIsCorrectlyMappedAsEpic() {
        // Create an epic
        CreateIssueRequest epicRequest = new CreateIssueRequest(
                "Test Epic",
                "Epic description",
                Priority.MEDIUM,
                testProject.getId(),
                epicType.getId()
        );
        IssueDto epic = issueService.createIssue(epicRequest, testUser);

        // Verify the epic is correctly identified
        assertThat(epic.isEpic()).isTrue();
        assertThat(epic.getIssueTypeName()).isEqualTo("EPIC");
        assertThat(epic.getParentIssueId()).isNull();
    }

    @Test
    void testStoryWithEpicParentIsNotEpic() {
        // Create an epic first
        CreateIssueRequest epicRequest = new CreateIssueRequest(
                "Test Epic",
                "Epic description",
                Priority.MEDIUM,
                testProject.getId(),
                epicType.getId()
        );
        IssueDto epic = issueService.createIssue(epicRequest, testUser);

        // Create a story under the epic
        CreateIssueRequest storyRequest = new CreateIssueRequest(
                "Test Story",
                "Story description",
                Priority.HIGH,
                testProject.getId(),
                storyType.getId()
        );
        storyRequest.setParentIssueId(epic.getId());
        IssueDto story = issueService.createIssue(storyRequest, testUser);

        // Verify the story is not an epic
        assertThat(story.isEpic()).isFalse();
        assertThat(story.getIssueTypeName()).isEqualTo("STORY");
        assertThat(story.getParentIssueId()).isEqualTo(epic.getId());
    }
}
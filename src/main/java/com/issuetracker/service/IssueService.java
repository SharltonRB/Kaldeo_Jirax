package com.issuetracker.service;

import com.issuetracker.dto.*;
import com.issuetracker.entity.*;
import com.issuetracker.exception.InvalidWorkflowTransitionException;
import com.issuetracker.exception.ResourceNotFoundException;
import com.issuetracker.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing issues with user isolation and workflow validation.
 * Handles issue CRUD operations, status transitions, and business logic.
 */
@Service
@Transactional
public class IssueService {

    private static final Logger logger = LoggerFactory.getLogger(IssueService.class);

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final IssueTypeRepository issueTypeRepository;
    private final SprintRepository sprintRepository;
    private final LabelRepository labelRepository;
    private final CommentRepository commentRepository;
    private final AuditService auditService;

    public IssueService(IssueRepository issueRepository, 
                       ProjectRepository projectRepository,
                       IssueTypeRepository issueTypeRepository,
                       SprintRepository sprintRepository,
                       LabelRepository labelRepository,
                       CommentRepository commentRepository,
                       AuditService auditService) {
        this.issueRepository = issueRepository;
        this.projectRepository = projectRepository;
        this.issueTypeRepository = issueTypeRepository;
        this.sprintRepository = sprintRepository;
        this.labelRepository = labelRepository;
        this.commentRepository = commentRepository;
        this.auditService = auditService;
    }

    /**
     * Creates a new issue for the specified user.
     *
     * @param request the issue creation request
     * @param user the issue owner
     * @return the created issue DTO
     * @throws ResourceNotFoundException if project or issue type not found
     */
    public IssueDto createIssue(CreateIssueRequest request, User user) {
        logger.debug("Creating issue '{}' for user {}", request.getTitle(), user.getId());

        // Validate project ownership
        Project project = projectRepository.findByIdAndUser(request.getProjectId(), user)
                .orElseThrow(() -> ResourceNotFoundException.project(request.getProjectId()));

        // Validate issue type availability for project
        IssueType issueType = issueTypeRepository.findById(request.getIssueTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Issue type not found with id: " + request.getIssueTypeId()));

        // Validate issue type is available for this project (global or project-specific)
        if (!issueType.getIsGlobal() && !issueType.getProject().equals(project)) {
            throw new ResourceNotFoundException("Issue type not available for this project");
        }

        // Create issue
        Issue issue = new Issue(user, project, issueType, request.getTitle(), 
                               request.getDescription(), request.getPriority());
        issue.setStoryPoints(request.getStoryPoints());

        // Set sprint if provided
        if (request.getSprintId() != null) {
            Sprint sprint = sprintRepository.findByIdAndUser(request.getSprintId(), user)
                    .orElseThrow(() -> ResourceNotFoundException.sprint(request.getSprintId()));
            issue.setSprint(sprint);
        }

        // Set labels if provided
        if (request.getLabelIds() != null && !request.getLabelIds().isEmpty()) {
            List<Label> labels = request.getLabelIds().stream()
                    .map(labelId -> labelRepository.findByIdAndUser(labelId, user)
                            .orElseThrow(() -> ResourceNotFoundException.label(labelId)))
                    .collect(Collectors.toList());
            issue.setLabels(labels);
        }

        Issue savedIssue = issueRepository.save(issue);

        // Create audit log
        auditService.logIssueCreated(savedIssue, user);

        logger.info("Created issue '{}' (ID: {}) for user {}", 
                   savedIssue.getTitle(), savedIssue.getId(), user.getId());

        return convertToDto(savedIssue);
    }

    /**
     * Updates an existing issue.
     *
     * @param issueId the issue ID
     * @param request the update request
     * @param user the issue owner
     * @return the updated issue DTO
     * @throws ResourceNotFoundException if issue not found or not owned by user
     */
    public IssueDto updateIssue(Long issueId, UpdateIssueRequest request, User user) {
        logger.debug("Updating issue {} for user {}", issueId, user.getId());

        Issue issue = issueRepository.findByIdAndUser(issueId, user)
                .orElseThrow(() -> ResourceNotFoundException.issue(issueId));

        // Track changes for audit
        String oldTitle = issue.getTitle();
        Priority oldPriority = issue.getPriority();
        Integer oldStoryPoints = issue.getStoryPoints();
        Sprint oldSprint = issue.getSprint();

        // Update issue fields
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        
        if (request.getPriority() != null) {
            issue.setPriority(request.getPriority());
        }
        
        issue.setStoryPoints(request.getStoryPoints());

        // Update sprint if provided
        if (request.getSprintId() != null) {
            Sprint sprint = sprintRepository.findByIdAndUser(request.getSprintId(), user)
                    .orElseThrow(() -> ResourceNotFoundException.sprint(request.getSprintId()));
            issue.setSprint(sprint);
        } else if (request.getSprintId() == null) {
            issue.setSprint(null); // Remove from sprint
        }

        // Update labels if provided
        if (request.getLabelIds() != null) {
            if (request.getLabelIds().isEmpty()) {
                issue.getLabels().clear();
            } else {
                List<Label> labels = request.getLabelIds().stream()
                        .map(labelId -> labelRepository.findByIdAndUser(labelId, user)
                                .orElseThrow(() -> ResourceNotFoundException.label(labelId)))
                        .collect(Collectors.toList());
                issue.getLabels().clear();
                issue.getLabels().addAll(labels);
            }
        }

        Issue updatedIssue = issueRepository.save(issue);

        // Create audit logs for changes
        if (!oldTitle.equals(updatedIssue.getTitle())) {
            auditService.logFieldChange(updatedIssue, user, "title", oldTitle, updatedIssue.getTitle());
        }
        if (oldPriority != updatedIssue.getPriority()) {
            auditService.logFieldChange(updatedIssue, user, "priority", 
                                       oldPriority.toString(), updatedIssue.getPriority().toString());
        }
        if (!java.util.Objects.equals(oldStoryPoints, updatedIssue.getStoryPoints())) {
            auditService.logFieldChange(updatedIssue, user, "storyPoints", 
                                       String.valueOf(oldStoryPoints), String.valueOf(updatedIssue.getStoryPoints()));
        }
        if (!java.util.Objects.equals(oldSprint, updatedIssue.getSprint())) {
            String oldSprintName = oldSprint != null ? oldSprint.getName() : "None";
            String newSprintName = updatedIssue.getSprint() != null ? updatedIssue.getSprint().getName() : "None";
            auditService.logFieldChange(updatedIssue, user, "sprint", oldSprintName, newSprintName);
        }

        logger.info("Updated issue '{}' (ID: {}) for user {}", 
                   updatedIssue.getTitle(), updatedIssue.getId(), user.getId());

        return convertToDto(updatedIssue);
    }

    /**
     * Updates issue status with workflow validation.
     *
     * @param issueId the issue ID
     * @param request the status update request
     * @param user the issue owner
     * @return the updated issue DTO
     * @throws InvalidWorkflowTransitionException if transition is invalid
     */
    public IssueDto updateIssueStatus(Long issueId, StatusUpdateRequest request, User user) {
        logger.debug("Updating status of issue {} to {} for user {}", 
                    issueId, request.getNewStatus(), user.getId());

        Issue issue = issueRepository.findByIdAndUser(issueId, user)
                .orElseThrow(() -> ResourceNotFoundException.issue(issueId));

        IssueStatus oldStatus = issue.getStatus();
        IssueStatus newStatus = request.getNewStatus();

        // Validate workflow transition
        if (!isValidTransition(oldStatus, newStatus)) {
            throw InvalidWorkflowTransitionException.transition(oldStatus, newStatus);
        }

        issue.setStatus(newStatus);
        Issue updatedIssue = issueRepository.save(issue);

        // Create audit log for status change
        auditService.logStatusChange(updatedIssue, user, oldStatus, newStatus);

        logger.info("Updated status of issue '{}' (ID: {}) from {} to {} for user {}", 
                   updatedIssue.getTitle(), updatedIssue.getId(), oldStatus, newStatus, user.getId());

        return convertToDto(updatedIssue);
    }

    /**
     * Retrieves an issue by ID with user isolation.
     *
     * @param issueId the issue ID
     * @param user the issue owner
     * @return the issue DTO
     * @throws ResourceNotFoundException if issue not found or not owned by user
     */
    @Transactional(readOnly = true)
    public IssueDto getIssue(Long issueId, User user) {
        logger.debug("Retrieving issue {} for user {}", issueId, user.getId());

        Issue issue = issueRepository.findByIdAndUser(issueId, user)
                .orElseThrow(() -> ResourceNotFoundException.issue(issueId));

        return convertToDto(issue);
    }

    /**
     * Retrieves all issues for a user with pagination.
     *
     * @param user the issue owner
     * @param pageable pagination information
     * @return page of issue DTOs
     */
    @Transactional(readOnly = true)
    public Page<IssueDto> getIssues(User user, Pageable pageable) {
        logger.debug("Retrieving issues for user {} with pagination", user.getId());

        Page<Issue> issues = issueRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return issues.map(this::convertToDto);
    }

    /**
     * Searches issues with filters.
     *
     * @param user the issue owner
     * @param projectId optional project filter
     * @param status optional status filter
     * @param priority optional priority filter
     * @param sprintId optional sprint filter
     * @param pageable pagination information
     * @return page of filtered issue DTOs
     */
    @Transactional(readOnly = true)
    public Page<IssueDto> searchIssues(User user, Long projectId, IssueStatus status, 
                                      Priority priority, Long sprintId, Pageable pageable) {
        logger.debug("Searching issues for user {} with filters", user.getId());

        Project project = null;
        if (projectId != null) {
            project = projectRepository.findByIdAndUser(projectId, user).orElse(null);
        }

        Sprint sprint = null;
        if (sprintId != null) {
            sprint = sprintRepository.findByIdAndUser(sprintId, user).orElse(null);
        }

        Page<Issue> issues = issueRepository.findByUserWithFilters(user, project, status, priority, sprint, pageable);
        return issues.map(this::convertToDto);
    }

    /**
     * Deletes an issue and all associated data.
     *
     * @param issueId the issue ID
     * @param user the issue owner
     * @throws ResourceNotFoundException if issue not found or not owned by user
     */
    public void deleteIssue(Long issueId, User user) {
        logger.debug("Deleting issue {} for user {}", issueId, user.getId());

        Issue issue = issueRepository.findByIdAndUser(issueId, user)
                .orElseThrow(() -> ResourceNotFoundException.issue(issueId));

        // Delete issue (cascade will handle related entities)
        issueRepository.delete(issue);

        logger.info("Deleted issue '{}' (ID: {}) for user {}", 
                   issue.getTitle(), issue.getId(), user.getId());
    }

    /**
     * Gets issue statistics for a user.
     *
     * @param user the issue owner
     * @return issue count
     */
    @Transactional(readOnly = true)
    public long getIssueCount(User user) {
        return issueRepository.countByUser(user);
    }

    /**
     * Validates if a status transition is allowed according to the workflow.
     * Workflow: BACKLOG → SELECTED_FOR_DEVELOPMENT → IN_PROGRESS → IN_REVIEW → DONE
     *
     * @param from current status
     * @param to target status
     * @return true if transition is valid
     */
    private boolean isValidTransition(IssueStatus from, IssueStatus to) {
        if (from == to) {
            return true; // Same status is always valid
        }

        return switch (from) {
            case BACKLOG -> to == IssueStatus.SELECTED_FOR_DEVELOPMENT;
            case SELECTED_FOR_DEVELOPMENT -> to == IssueStatus.IN_PROGRESS || to == IssueStatus.BACKLOG;
            case IN_PROGRESS -> to == IssueStatus.IN_REVIEW || to == IssueStatus.SELECTED_FOR_DEVELOPMENT;
            case IN_REVIEW -> to == IssueStatus.DONE || to == IssueStatus.IN_PROGRESS;
            case DONE -> to == IssueStatus.IN_REVIEW; // Allow reopening
        };
    }

    /**
     * Converts an Issue entity to IssueDto.
     *
     * @param issue the issue entity
     * @return the issue DTO
     */
    private IssueDto convertToDto(Issue issue) {
        IssueDto dto = new IssueDto(
                issue.getId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getStatus(),
                issue.getPriority(),
                issue.getStoryPoints(),
                issue.getCreatedAt(),
                issue.getUpdatedAt()
        );

        // Set project information
        dto.setProjectId(issue.getProject().getId());
        dto.setProjectName(issue.getProject().getName());
        dto.setProjectKey(issue.getProject().getKey());

        // Set sprint information
        if (issue.getSprint() != null) {
            dto.setSprintId(issue.getSprint().getId());
            dto.setSprintName(issue.getSprint().getName());
        }

        // Set issue type information
        dto.setIssueTypeId(issue.getIssueType().getId());
        dto.setIssueTypeName(issue.getIssueType().getName());

        // Set labels
        if (issue.getLabels() != null && !issue.getLabels().isEmpty()) {
            List<LabelDto> labelDtos = issue.getLabels().stream()
                    .map(label -> new LabelDto(label.getId(), label.getName(), label.getColor(), label.getCreatedAt()))
                    .collect(Collectors.toList());
            dto.setLabels(labelDtos);
        }

        // Set comment count
        long commentCount = commentRepository.countByIssue(issue);
        dto.setCommentCount(commentCount);

        return dto;
    }
}
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

        // Handle epic hierarchy
        validateAndSetEpicHierarchy(issue, request.getParentIssueId(), user);

        Issue savedIssue = issueRepository.save(issue);

        // Create audit log
        auditService.logIssueCreated(savedIssue, user);

        logger.info("📝 Created issue '{}' (ID: {}) for user: {}", 
                   savedIssue.getTitle(), savedIssue.getId(), user.getEmail());

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
        logger.info("📝 Updating issue {} for user: {}", issueId, user.getEmail());

        Issue issue = issueRepository.findByIdAndUser(issueId, user)
                .orElseThrow(() -> ResourceNotFoundException.issue(issueId));

        // Track changes for audit
        String oldTitle = issue.getTitle();
        Priority oldPriority = issue.getPriority();
        Integer oldStoryPoints = issue.getStoryPoints();
        Sprint oldSprint = issue.getSprint();
        Issue oldParentIssue = issue.getParentIssue();

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

        // Handle epic hierarchy changes
        if (request.getParentIssueId() != null || 
            (oldParentIssue != null && request.getParentIssueId() == null)) {
            validateAndSetEpicHierarchy(issue, request.getParentIssueId(), user);
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
        if (!java.util.Objects.equals(oldParentIssue, updatedIssue.getParentIssue())) {
            String oldParentName = oldParentIssue != null ? oldParentIssue.getTitle() : "None";
            String newParentName = updatedIssue.getParentIssue() != null ? updatedIssue.getParentIssue().getTitle() : "None";
            auditService.logFieldChange(updatedIssue, user, "parentEpic", oldParentName, newParentName);
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
        logger.info("🔄 Updating status of issue {} to {} for user: {}", 
                    issueId, request.getNewStatus(), user.getEmail());

        Issue issue = issueRepository.findByIdAndUser(issueId, user)
                .orElseThrow(() -> ResourceNotFoundException.issue(issueId));

        IssueStatus oldStatus = issue.getStatus();
        IssueStatus newStatus = request.getNewStatus();

        // Validate workflow transition
        if (!isValidTransition(oldStatus, newStatus)) {
            logger.warn("❌ Invalid transition from {} to {} for issue {} by user: {}", 
                       oldStatus, newStatus, issueId, user.getEmail());
            throw InvalidWorkflowTransitionException.transition(oldStatus, newStatus);
        }

        issue.setStatus(newStatus);
        Issue updatedIssue = issueRepository.save(issue);

        // Create audit log for status change
        auditService.logStatusChange(updatedIssue, user, oldStatus, newStatus);

        logger.info("✅ Status updated for '{}' (ID: {}) from {} to {} by user: {}", 
                   updatedIssue.getTitle(), updatedIssue.getId(), oldStatus, newStatus, user.getEmail());

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
            case BACKLOG -> to == IssueStatus.SELECTED_FOR_DEVELOPMENT || 
                           to == IssueStatus.IN_PROGRESS || 
                           to == IssueStatus.DONE; // Allow direct completion from backlog
            case SELECTED_FOR_DEVELOPMENT -> to == IssueStatus.IN_PROGRESS || 
                                           to == IssueStatus.BACKLOG || 
                                           to == IssueStatus.DONE; // Allow direct completion
            case IN_PROGRESS -> to == IssueStatus.IN_REVIEW || 
                              to == IssueStatus.SELECTED_FOR_DEVELOPMENT || 
                              to == IssueStatus.BACKLOG || 
                              to == IssueStatus.DONE; // Allow direct completion
            case IN_REVIEW -> to == IssueStatus.DONE || 
                            to == IssueStatus.IN_PROGRESS || 
                            to == IssueStatus.BACKLOG; // Allow moving back to backlog
            case DONE -> to == IssueStatus.IN_REVIEW || 
                       to == IssueStatus.BACKLOG; // Allow reopening to review or backlog
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

        // Set epic hierarchy information
        if (issue.getParentIssue() != null) {
            dto.setParentIssueId(issue.getParentIssue().getId());
            dto.setParentIssueTitle(issue.getParentIssue().getTitle());
            dto.setEpic(false);
        } else {
            dto.setEpic(true);
        }

        // Set child issue count for epics
        if (issue.isEpic()) {
            long childCount = issueRepository.countByParentIssueAndUser(issue, issue.getUser());
            dto.setChildIssueCount(childCount);
        } else {
            dto.setChildIssueCount(0L);
        }

        return dto;
    }

    // Epic hierarchy methods

    /**
     * Validates and sets the epic hierarchy for an issue.
     *
     * @param issue the issue to validate
     * @param parentIssueId the parent issue ID (can be null for epics)
     * @param user the user
     * @throws IllegalArgumentException if hierarchy rules are violated
     */
    private void validateAndSetEpicHierarchy(Issue issue, Long parentIssueId, User user) {
        boolean isEpicType = "EPIC".equals(issue.getIssueType().getName());

        if (isEpicType) {
            // Epic issues cannot have a parent
            if (parentIssueId != null) {
                throw new IllegalArgumentException("Epic issues cannot have a parent issue");
            }
            issue.setParentIssue(null);
        } else {
            // Non-epic issues MUST have a parent epic
            if (parentIssueId == null) {
                throw new IllegalArgumentException("Non-epic issues must be assigned to an epic");
            }

            Issue parentIssue = issueRepository.findByIdAndUser(parentIssueId, user)
                    .orElseThrow(() -> ResourceNotFoundException.issue(parentIssueId));

            // Validate parent is an epic
            if (!parentIssue.isEpic()) {
                throw new IllegalArgumentException("Parent issue must be an epic");
            }

            // For issue updates, we need to be more flexible with project validation
            // The issue might be moving between epics in the same project
            // So we validate that the parent epic belongs to a project the user has access to
            // and we'll update the issue's project to match the epic's project if needed
            
            // Update the issue's project to match the epic's project
            issue.setProject(parentIssue.getProject());
            issue.setParentIssue(parentIssue);
        }
    }

    /**
     * Gets all epic issues for a user.
     *
     * @param user the user
     * @param pageable pagination information
     * @return page of epic issues
     */
    @Transactional(readOnly = true)
    public Page<IssueDto> getEpics(User user, Pageable pageable) {
        logger.debug("Retrieving epics for user {}", user.getId());

        Page<Issue> epics = issueRepository.findByUserAndParentIssueIsNullOrderByCreatedAtDesc(user, pageable);
        return epics.map(this::convertToDto);
    }

    /**
     * Gets all epic issues for a user without pagination.
     *
     * @param user the user
     * @return list of epic issues
     */
    @Transactional(readOnly = true)
    public List<IssueDto> getAllEpics(User user) {
        logger.debug("Retrieving all epics for user {}", user.getId());

        List<Issue> epics = issueRepository.findByUserAndParentIssueIsNullOrderByCreatedAtDesc(user);
        return epics.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets child issues of a specific epic.
     *
     * @param epicId the epic ID
     * @param user the user
     * @param pageable pagination information
     * @return page of child issues
     */
    @Transactional(readOnly = true)
    public Page<IssueDto> getEpicChildren(Long epicId, User user, Pageable pageable) {
        logger.debug("Retrieving children of epic {} for user {}", epicId, user.getId());

        Issue epic = issueRepository.findByIdAndUser(epicId, user)
                .orElseThrow(() -> ResourceNotFoundException.issue(epicId));

        if (!epic.isEpic()) {
            throw new IllegalArgumentException("Issue is not an epic");
        }

        Page<Issue> children = issueRepository.findByParentIssueAndUserOrderByCreatedAtDesc(epic, user, pageable);
        return children.map(this::convertToDto);
    }

    /**
     * Gets child issues of a specific epic without pagination.
     *
     * @param epicId the epic ID
     * @param user the user
     * @return list of child issues
     */
    @Transactional(readOnly = true)
    public List<IssueDto> getAllEpicChildren(Long epicId, User user) {
        logger.debug("Retrieving all children of epic {} for user {}", epicId, user.getId());

        Issue epic = issueRepository.findByIdAndUser(epicId, user)
                .orElseThrow(() -> ResourceNotFoundException.issue(epicId));

        if (!epic.isEpic()) {
            throw new IllegalArgumentException("Issue is not an epic");
        }

        List<Issue> children = issueRepository.findByParentIssueAndUserOrderByCreatedAtDesc(epic, user);
        return children.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Moves an issue to a different epic.
     *
     * @param issueId the issue ID
     * @param newParentEpicId the new parent epic ID
     * @param user the user
     * @return updated issue DTO
     */
    public IssueDto moveIssueToEpic(Long issueId, Long newParentEpicId, User user) {
        logger.debug("Moving issue {} to epic {} for user {}", issueId, newParentEpicId, user.getId());

        Issue issue = issueRepository.findByIdAndUser(issueId, user)
                .orElseThrow(() -> ResourceNotFoundException.issue(issueId));

        if (issue.isEpic()) {
            throw new IllegalArgumentException("Cannot move an epic issue to another epic");
        }

        Issue oldParent = issue.getParentIssue();
        
        Issue newParentEpic = issueRepository.findByIdAndUser(newParentEpicId, user)
                .orElseThrow(() -> ResourceNotFoundException.issue(newParentEpicId));

        if (!newParentEpic.isEpic()) {
            throw new IllegalArgumentException("Target issue is not an epic");
        }

        // Validate same project
        if (!newParentEpic.getProject().equals(issue.getProject())) {
            throw new IllegalArgumentException("Target epic must belong to the same project");
        }

        issue.setParentIssue(newParentEpic);
        Issue updatedIssue = issueRepository.save(issue);

        // Create audit log
        String details = String.format("Moved from epic '%s' to epic '%s'", 
                                      oldParent != null ? oldParent.getTitle() : "None", 
                                      newParentEpic.getTitle());
        auditService.logFieldChange(updatedIssue, user, "parentEpic", 
                                   oldParent != null ? oldParent.getTitle() : "None", 
                                   newParentEpic.getTitle());

        logger.info("Moved issue '{}' (ID: {}) to epic '{}' for user {}", 
                   updatedIssue.getTitle(), updatedIssue.getId(), newParentEpic.getTitle(), user.getId());

        return convertToDto(updatedIssue);
    }

    /**
     * Gets epic statistics for a user.
     *
     * @param user the user
     * @return epic statistics
     */
    @Transactional(readOnly = true)
    public EpicStatisticsDto getEpicStatistics(User user) {
        logger.debug("Calculating epic statistics for user {}", user.getId());

        long totalEpics = issueRepository.countByUserAndParentIssueIsNull(user);
        long totalChildIssues = issueRepository.countByUserAndParentIssueIsNotNull(user);

        return new EpicStatisticsDto(totalEpics, totalChildIssues);
    }
}
package com.issuetracker.service;

import com.issuetracker.dto.CreateSprintRequest;
import com.issuetracker.dto.SprintDto;
import com.issuetracker.dto.SprintActivationResponse;
import com.issuetracker.dto.UpdateSprintRequest;
import com.issuetracker.entity.*;
import com.issuetracker.exception.InvalidSprintOperationException;
import com.issuetracker.exception.ResourceNotFoundException;
import com.issuetracker.repository.IssueRepository;
import com.issuetracker.repository.SprintRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing sprints with user isolation and business rule validation.
 * Handles sprint CRUD operations, activation, completion, and date validation.
 */
@Service
@Transactional
public class SprintService {

    private static final Logger logger = LoggerFactory.getLogger(SprintService.class);

    private final SprintRepository sprintRepository;
    private final IssueRepository issueRepository;
    private final AuditService auditService;

    public SprintService(SprintRepository sprintRepository, IssueRepository issueRepository, AuditService auditService) {
        this.sprintRepository = sprintRepository;
        this.issueRepository = issueRepository;
        this.auditService = auditService;
    }

    /**
     * Creates a new sprint for the specified user.
     *
     * @param request the sprint creation request
     * @param user the sprint owner
     * @return the created sprint DTO
     * @throws InvalidSprintOperationException if dates are invalid or overlap with existing sprints
     */
    public SprintDto createSprint(CreateSprintRequest request, User user) {
        logger.info("🏃 Creating sprint '{}' for user: {}", request.getName(), user.getEmail());

        // Validate dates
        validateSprintDates(request.getStartDate(), request.getEndDate());

        // Check for overlapping sprints
        List<Sprint> overlappingSprints = sprintRepository.findOverlappingSprints(
                user, request.getStartDate(), request.getEndDate(), null);
        if (!overlappingSprints.isEmpty()) {
            logger.warn("❌ Sprint creation failed - overlapping dates for user: {}", user.getEmail());
            throw InvalidSprintOperationException.overlappingSprints();
        }

        // Create sprint
        Sprint sprint = new Sprint(user, request.getName(), request.getStartDate(), request.getEndDate(), request.getGoal());
        Sprint savedSprint = sprintRepository.save(sprint);

        logger.info("✅ Created sprint '{}' (ID: {}) for user: {}", 
                   savedSprint.getName(), savedSprint.getId(), user.getEmail());

        return convertToDto(savedSprint);
    }

    /**
     * Updates an existing sprint.
     *
     * @param sprintId the sprint ID
     * @param request the update request
     * @param user the sprint owner
     * @return the updated sprint DTO
     * @throws ResourceNotFoundException if sprint not found or not owned by user
     * @throws InvalidSprintOperationException if dates are invalid or overlap with existing sprints
     */
    public SprintDto updateSprint(Long sprintId, UpdateSprintRequest request, User user) {
        logger.debug("Updating sprint {} for user {}", sprintId, user.getId());

        Sprint sprint = sprintRepository.findByIdAndUser(sprintId, user)
                .orElseThrow(() -> ResourceNotFoundException.sprint(sprintId));

        // Validate dates
        validateSprintDates(request.getStartDate(), request.getEndDate());

        // Check for overlapping sprints (excluding current sprint)
        List<Sprint> overlappingSprints = sprintRepository.findOverlappingSprints(
                user, request.getStartDate(), request.getEndDate(), sprintId);
        if (!overlappingSprints.isEmpty()) {
            throw InvalidSprintOperationException.overlappingSprints();
        }

        // Update sprint fields
        sprint.setName(request.getName());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        sprint.setGoal(request.getGoal());

        Sprint updatedSprint = sprintRepository.save(sprint);

        logger.info("Updated sprint '{}' (ID: {}) for user {}", 
                   updatedSprint.getName(), updatedSprint.getId(), user.getId());

        return convertToDto(updatedSprint);
    }

    /**
     * Activates a sprint, ensuring only one active sprint per user.
     *
     * @param sprintId the sprint ID
     * @param user the sprint owner
     * @return the sprint activation response with updated sprint and affected issues
     * @throws InvalidSprintOperationException if another sprint is already active
     */
    public SprintActivationResponse activateSprint(Long sprintId, User user) {
        return activateSprint(sprintId, user, null, null);
    }

    /**
     * Activates a sprint with optional date updates, ensuring only one active sprint per user.
     *
     * @param sprintId the sprint ID
     * @param user the sprint owner
     * @param newStartDate optional new start date
     * @param newEndDate optional new end date
     * @return the sprint activation response with updated sprint and affected issues
     * @throws InvalidSprintOperationException if another sprint is already active
     */
    public SprintActivationResponse activateSprint(Long sprintId, User user, LocalDate newStartDate, LocalDate newEndDate) {
        logger.info("🚀 Activating sprint {} for user: {}", sprintId, user.getEmail());

        Sprint sprint = sprintRepository.findByIdAndUser(sprintId, user)
                .orElseThrow(() -> ResourceNotFoundException.sprint(sprintId));

        // Check if another sprint is already active
        Optional<Sprint> activeSprint = sprintRepository.findByUserAndStatus(user, SprintStatus.ACTIVE);
        if (activeSprint.isPresent() && !activeSprint.get().getId().equals(sprintId)) {
            logger.warn("❌ Sprint activation failed - another sprint already active for user: {}", user.getEmail());
            throw InvalidSprintOperationException.activeSprintExists();
        }

        // Update dates if provided
        if (newStartDate != null && newEndDate != null) {
            logger.info("📅 Updating sprint dates: {} to {} -> {} to {}", 
                       sprint.getStartDate(), sprint.getEndDate(), newStartDate, newEndDate);
            
            sprint.setStartDate(newStartDate);
            sprint.setEndDate(newEndDate);
            
            // Resolve conflicts with planned sprints
            resolveSprintConflicts(user, sprintId, newStartDate, newEndDate);
        }

        sprint.setStatus(SprintStatus.ACTIVE);
        Sprint activatedSprint = sprintRepository.save(sprint);

        // Move all BACKLOG issues in this sprint to SELECTED_FOR_DEVELOPMENT
        List<Issue> sprintIssues = issueRepository.findByUserAndSprint(user, activatedSprint);
        logger.info("🔍 Found {} issues in sprint {} for user {}", sprintIssues.size(), sprintId, user.getEmail());
        
        List<Long> updatedIssueIds = new ArrayList<>();
        int movedIssuesCount = 0;
        for (Issue issue : sprintIssues) {
            logger.debug("📋 Processing issue {} with status {}", issue.getId(), issue.getStatus());
            if (issue.getStatus() == IssueStatus.BACKLOG) {
                logger.info("🔄 Moving issue {} from BACKLOG to SELECTED_FOR_DEVELOPMENT", issue.getId());
                issue.setStatus(IssueStatus.SELECTED_FOR_DEVELOPMENT);
                issueRepository.save(issue);
                updatedIssueIds.add(issue.getId());
                movedIssuesCount++;
                
                // Log the status change for audit
                auditService.logStatusChange(issue, user, IssueStatus.BACKLOG, IssueStatus.SELECTED_FOR_DEVELOPMENT);
            }
        }

        logger.info("✅ Activated sprint '{}' (ID: {}) for user: {} and moved {} issues to SELECTED", 
                   activatedSprint.getName(), activatedSprint.getId(), user.getEmail(), movedIssuesCount);

        return new SprintActivationResponse(convertToDto(activatedSprint), updatedIssueIds, movedIssuesCount);
    }

    /**
     * Resolves conflicts with planned sprints by clearing their dates if they overlap.
     *
     * @param user the user
     * @param excludeSprintId the sprint ID to exclude from conflict resolution
     * @param newStartDate the new start date
     * @param newEndDate the new end date
     */
    private void resolveSprintConflicts(User user, Long excludeSprintId, LocalDate newStartDate, LocalDate newEndDate) {
        logger.info("🔍 Checking for sprint conflicts between {} and {}", newStartDate, newEndDate);
        
        // Find overlapping planned sprints
        List<Sprint> overlappingSprints = sprintRepository.findOverlappingSprints(
                user, newStartDate, newEndDate, excludeSprintId);
        
        List<Sprint> conflictingSprints = overlappingSprints.stream()
                .filter(s -> s.getStatus() == SprintStatus.PLANNED)
                .toList();
        
        if (!conflictingSprints.isEmpty()) {
            logger.info("⚠️ Found {} conflicting planned sprints, clearing their dates", conflictingSprints.size());
            
            for (Sprint conflictingSprint : conflictingSprints) {
                logger.info("📅 Clearing dates for sprint '{}' (ID: {}) due to conflict", 
                           conflictingSprint.getName(), conflictingSprint.getId());
                
                // Clear the dates but keep the sprint
                conflictingSprint.setStartDate(null);
                conflictingSprint.setEndDate(null);
                sprintRepository.save(conflictingSprint);
                
                // Log the conflict resolution
                auditService.logSprintStatusChange(conflictingSprint, user, 
                    SprintStatus.PLANNED, SprintStatus.PLANNED);
            }
        }
    }

    /**
     * Completes a sprint and moves incomplete issues back to backlog.
     *
     * @param sprintId the sprint ID
     * @param user the sprint owner
     * @return the completed sprint DTO
     * @throws InvalidSprintOperationException if sprint is not active
     */
    public SprintDto completeSprint(Long sprintId, User user) {
        logger.info("🏁 Completing sprint {} for user: {}", sprintId, user.getEmail());

        Sprint sprint = sprintRepository.findByIdAndUser(sprintId, user)
                .orElseThrow(() -> ResourceNotFoundException.sprint(sprintId));

        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            logger.warn("❌ Sprint completion failed - sprint not active for user: {}", user.getEmail());
            throw InvalidSprintOperationException.sprintNotActive();
        }

        // Move incomplete issues back to backlog
        List<Issue> sprintIssues = issueRepository.findByUserAndSprint(user, sprint);
        int movedIssues = 0;
        for (Issue issue : sprintIssues) {
            if (issue.getStatus() != IssueStatus.DONE) {
                issue.setSprint(null); // Remove from sprint (back to backlog)
                issueRepository.save(issue);
                movedIssues++;
            }
        }

        sprint.setStatus(SprintStatus.COMPLETED);
        Sprint completedSprint = sprintRepository.save(sprint);

        logger.info("✅ Completed sprint '{}' (ID: {}) for user: {} - moved {} incomplete issues to backlog", 
                   completedSprint.getName(), completedSprint.getId(), user.getEmail(), movedIssues);

        return convertToDto(completedSprint);
    }

    /**
     * Retrieves a sprint by ID with user isolation.
     *
     * @param sprintId the sprint ID
     * @param user the sprint owner
     * @return the sprint DTO
     * @throws ResourceNotFoundException if sprint not found or not owned by user
     */
    @Transactional(readOnly = true)
    public SprintDto getSprint(Long sprintId, User user) {
        logger.debug("Retrieving sprint {} for user {}", sprintId, user.getId());

        Sprint sprint = sprintRepository.findByIdAndUser(sprintId, user)
                .orElseThrow(() -> ResourceNotFoundException.sprint(sprintId));

        return convertToDto(sprint);
    }

    /**
     * Retrieves all sprints for a user with pagination.
     *
     * @param user the sprint owner
     * @param pageable pagination information
     * @return page of sprint DTOs
     */
    @Transactional(readOnly = true)
    public Page<SprintDto> getSprints(User user, Pageable pageable) {
        logger.debug("Retrieving sprints for user {} with pagination", user.getId());

        Page<Sprint> sprints = sprintRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return sprints.map(this::convertToDto);
    }

    /**
     * Retrieves all sprints for a user without pagination.
     *
     * @param user the sprint owner
     * @return list of sprint DTOs
     */
    @Transactional(readOnly = true)
    public List<SprintDto> getAllSprints(User user) {
        logger.debug("Retrieving all sprints for user {}", user.getId());

        List<Sprint> sprints = sprintRepository.findByUserOrderByCreatedAtDesc(user);
        return sprints.stream()
                .map(this::convertToDto)
                .toList();
    }

    /**
     * Retrieves the active sprint for a user.
     *
     * @param user the sprint owner
     * @return optional active sprint DTO
     */
    @Transactional(readOnly = true)
    public Optional<SprintDto> getActiveSprint(User user) {
        logger.debug("Retrieving active sprint for user {}", user.getId());

        Optional<Sprint> activeSprint = sprintRepository.findByUserAndStatus(user, SprintStatus.ACTIVE);
        return activeSprint.map(this::convertToDto);
    }

    /**
     * Retrieves sprints by status for a user.
     *
     * @param user the sprint owner
     * @param status the sprint status
     * @return list of sprint DTOs
     */
    @Transactional(readOnly = true)
    public List<SprintDto> getSprintsByStatus(User user, SprintStatus status) {
        logger.debug("Retrieving sprints with status {} for user {}", status, user.getId());

        List<Sprint> sprints = sprintRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status);
        return sprints.stream()
                .map(this::convertToDto)
                .toList();
    }

    /**
     * Deletes a sprint and removes it from all associated issues.
     *
     * @param sprintId the sprint ID
     * @param user the sprint owner
     * @throws ResourceNotFoundException if sprint not found or not owned by user
     */
    public void deleteSprint(Long sprintId, User user) {
        logger.debug("Deleting sprint {} for user {}", sprintId, user.getId());

        Sprint sprint = sprintRepository.findByIdAndUser(sprintId, user)
                .orElseThrow(() -> ResourceNotFoundException.sprint(sprintId));

        // Remove sprint from all associated issues
        List<Issue> sprintIssues = issueRepository.findByUserAndSprint(user, sprint);
        for (Issue issue : sprintIssues) {
            issue.setSprint(null);
            issueRepository.save(issue);
        }

        // Delete sprint
        sprintRepository.delete(sprint);

        logger.info("Deleted sprint '{}' (ID: {}) for user {} - removed from {} issues", 
                   sprint.getName(), sprint.getId(), user.getId(), sprintIssues.size());
    }

    /**
     * Gets sprint statistics for a user.
     *
     * @param user the sprint owner
     * @return sprint count
     */
    @Transactional(readOnly = true)
    public long getSprintCount(User user) {
        return sprintRepository.countByUser(user);
    }

    /**
     * Validates sprint dates.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @throws InvalidSprintOperationException if dates are invalid
     */
    private void validateSprintDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw InvalidSprintOperationException.dateValidation("Start date and end date are required");
        }

        if (endDate.isBefore(startDate)) {
            throw InvalidSprintOperationException.dateValidation("End date must be after start date");
        }

        if (startDate.isBefore(LocalDate.now().minusDays(1))) {
            throw InvalidSprintOperationException.dateValidation("Start date cannot be in the past");
        }
    }

    /**
     * Converts a Sprint entity to SprintDto.
     *
     * @param sprint the sprint entity
     * @return the sprint DTO
     */
    private SprintDto convertToDto(Sprint sprint) {
        SprintDto dto = new SprintDto(
                sprint.getId(),
                sprint.getName(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                sprint.getStatus(),
                sprint.getGoal(),
                sprint.getCreatedAt(),
                sprint.getUpdatedAt()
        );

        // Add issue counts
        long totalIssues = issueRepository.countByUserAndSprint(sprint.getUser(), sprint);
        long completedIssues = issueRepository.countByUserAndSprintAndStatus(sprint.getUser(), sprint, IssueStatus.DONE);
        
        dto.setIssueCount(totalIssues);
        dto.setCompletedIssueCount(completedIssues);

        return dto;
    }
}
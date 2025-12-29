package com.issuetracker.service;

import com.issuetracker.dto.CreateSprintRequest;
import com.issuetracker.dto.SprintDto;
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

    public SprintService(SprintRepository sprintRepository, IssueRepository issueRepository) {
        this.sprintRepository = sprintRepository;
        this.issueRepository = issueRepository;
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
        logger.debug("Creating sprint '{}' for user {}", request.getName(), user.getId());

        // Validate dates
        validateSprintDates(request.getStartDate(), request.getEndDate());

        // Check for overlapping sprints
        List<Sprint> overlappingSprints = sprintRepository.findOverlappingSprints(
                user, request.getStartDate(), request.getEndDate(), null);
        if (!overlappingSprints.isEmpty()) {
            throw InvalidSprintOperationException.overlappingSprints();
        }

        // Create sprint
        Sprint sprint = new Sprint(user, request.getName(), request.getStartDate(), request.getEndDate());
        Sprint savedSprint = sprintRepository.save(sprint);

        logger.info("Created sprint '{}' (ID: {}) for user {}", 
                   savedSprint.getName(), savedSprint.getId(), user.getId());

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
     * @return the activated sprint DTO
     * @throws InvalidSprintOperationException if another sprint is already active
     */
    public SprintDto activateSprint(Long sprintId, User user) {
        logger.debug("Activating sprint {} for user {}", sprintId, user.getId());

        Sprint sprint = sprintRepository.findByIdAndUser(sprintId, user)
                .orElseThrow(() -> ResourceNotFoundException.sprint(sprintId));

        // Check if another sprint is already active
        Optional<Sprint> activeSprint = sprintRepository.findByUserAndStatus(user, SprintStatus.ACTIVE);
        if (activeSprint.isPresent() && !activeSprint.get().getId().equals(sprintId)) {
            throw InvalidSprintOperationException.activeSprintExists();
        }

        sprint.setStatus(SprintStatus.ACTIVE);
        Sprint activatedSprint = sprintRepository.save(sprint);

        logger.info("Activated sprint '{}' (ID: {}) for user {}", 
                   activatedSprint.getName(), activatedSprint.getId(), user.getId());

        return convertToDto(activatedSprint);
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
        logger.debug("Completing sprint {} for user {}", sprintId, user.getId());

        Sprint sprint = sprintRepository.findByIdAndUser(sprintId, user)
                .orElseThrow(() -> ResourceNotFoundException.sprint(sprintId));

        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw InvalidSprintOperationException.sprintNotActive();
        }

        // Move incomplete issues back to backlog
        List<Issue> sprintIssues = issueRepository.findByUserAndSprint(user, sprint);
        for (Issue issue : sprintIssues) {
            if (issue.getStatus() != IssueStatus.DONE) {
                issue.setSprint(null); // Remove from sprint (back to backlog)
                issueRepository.save(issue);
            }
        }

        sprint.setStatus(SprintStatus.COMPLETED);
        Sprint completedSprint = sprintRepository.save(sprint);

        logger.info("Completed sprint '{}' (ID: {}) for user {} - moved {} incomplete issues to backlog", 
                   completedSprint.getName(), completedSprint.getId(), user.getId(),
                   sprintIssues.stream().mapToLong(i -> i.getStatus() != IssueStatus.DONE ? 1 : 0).sum());

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
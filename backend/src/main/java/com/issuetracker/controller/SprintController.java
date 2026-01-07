package com.issuetracker.controller;

import com.issuetracker.dto.CreateSprintRequest;
import com.issuetracker.dto.SprintDto;
import com.issuetracker.dto.SprintActivationResponse;
import com.issuetracker.dto.SprintActivationRequest;
import com.issuetracker.dto.UpdateSprintRequest;
import com.issuetracker.dto.AddIssuesToSprintRequest;
import com.issuetracker.dto.IssueDto;
import com.issuetracker.entity.SprintStatus;
import com.issuetracker.entity.User;
import com.issuetracker.entity.Issue;
import com.issuetracker.service.SprintService;
import com.issuetracker.service.UserService;
import com.issuetracker.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for sprint management operations.
 * Handles sprint CRUD operations, activation, completion, and planning endpoints.
 */
@RestController
@RequestMapping("/sprints")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SprintController {

    private static final Logger logger = LoggerFactory.getLogger(SprintController.class);

    private final SprintService sprintService;
    private final UserService userService;
    private final IssueService issueService;

    @Autowired
    public SprintController(SprintService sprintService, UserService userService, IssueService issueService) {
        this.sprintService = sprintService;
        this.userService = userService;
        this.issueService = issueService;
    }

    /**
     * Retrieves all sprints for the authenticated user with pagination.
     *
     * @param pageable pagination parameters
     * @param status optional status filter
     * @return page of sprint DTOs
     */
    @GetMapping
    public ResponseEntity<Page<SprintDto>> getSprints(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) SprintStatus status) {
        
        User currentUser = getCurrentUser();
        
        Page<SprintDto> sprints;
        if (status != null) {
            // Convert list to page for consistency
            List<SprintDto> sprintList = sprintService.getSprintsByStatus(currentUser, status);
            sprints = new org.springframework.data.domain.PageImpl<>(
                sprintList, pageable, sprintList.size());
        } else {
            sprints = sprintService.getSprints(currentUser, pageable);
        }
        
        return ResponseEntity.ok(sprints);
    }

    /**
     * Retrieves all sprints for the authenticated user without pagination.
     *
     * @return list of all sprint DTOs
     */
    @GetMapping("/all")
    public ResponseEntity<List<SprintDto>> getAllSprints() {
        User currentUser = getCurrentUser();
        List<SprintDto> sprints = sprintService.getAllSprints(currentUser);
        return ResponseEntity.ok(sprints);
    }

    /**
     * Retrieves a specific sprint by ID.
     *
     * @param id sprint ID
     * @return sprint DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<SprintDto> getSprint(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        SprintDto sprint = sprintService.getSprint(id, currentUser);
        return ResponseEntity.ok(sprint);
    }

    /**
     * Retrieves the currently active sprint for the user.
     *
     * @return active sprint DTO or 404 if no active sprint
     */
    @GetMapping("/active")
    public ResponseEntity<SprintDto> getActiveSprint() {
        User currentUser = getCurrentUser();
        Optional<SprintDto> activeSprint = sprintService.getActiveSprint(currentUser);
        
        if (activeSprint.isPresent()) {
            return ResponseEntity.ok(activeSprint.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Creates a new sprint.
     *
     * @param request sprint creation request
     * @return created sprint DTO
     */
    @PostMapping
    public ResponseEntity<SprintDto> createSprint(@Valid @RequestBody CreateSprintRequest request) {
        User currentUser = getCurrentUser();
        SprintDto sprint = sprintService.createSprint(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(sprint);
    }

    /**
     * Updates an existing sprint.
     *
     * @param id sprint ID
     * @param request sprint update request
     * @return updated sprint DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<SprintDto> updateSprint(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSprintRequest request) {
        
        User currentUser = getCurrentUser();
        SprintDto sprint = sprintService.updateSprint(id, request, currentUser);
        return ResponseEntity.ok(sprint);
    }

    /**
     * Activates a sprint, ensuring only one active sprint per user.
     * Optionally accepts new dates for the sprint.
     *
     * @param id sprint ID
     * @param request optional activation request with new dates
     * @return sprint activation response with updated sprint and affected issues
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<SprintActivationResponse> activateSprint(
            @PathVariable Long id,
            @RequestBody(required = false) SprintActivationRequest request) {
        
        User currentUser = getCurrentUser();
        
        // Add logging to debug the request
        if (request != null) {
            System.out.println("🔍 DEBUG: Received activation request with dates: " + 
                             "startDate=" + request.getNewStartDate() + 
                             ", endDate=" + request.getNewEndDate());
        } else {
            System.out.println("🔍 DEBUG: Received activation request without dates");
        }
        
        SprintActivationResponse response;
        if (request != null && request.getNewStartDate() != null && request.getNewEndDate() != null) {
            System.out.println("🔍 DEBUG: Calling activateSprint with new dates");
            response = sprintService.activateSprint(id, currentUser, request.getNewStartDate(), request.getNewEndDate());
        } else {
            System.out.println("🔍 DEBUG: Calling activateSprint without dates");
            response = sprintService.activateSprint(id, currentUser);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Completes a sprint and moves incomplete issues back to backlog.
     *
     * @param id sprint ID
     * @return completed sprint DTO
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<SprintDto> completeSprint(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        SprintDto sprint = sprintService.completeSprint(id, currentUser);
        return ResponseEntity.ok(sprint);
    }

    /**
     * Deletes a sprint and removes it from all associated issues.
     *
     * @param id sprint ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSprint(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        sprintService.deleteSprint(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets sprints by status.
     *
     * @param status sprint status
     * @return list of sprint DTOs with the specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SprintDto>> getSprintsByStatus(@PathVariable SprintStatus status) {
        User currentUser = getCurrentUser();
        List<SprintDto> sprints = sprintService.getSprintsByStatus(currentUser, status);
        return ResponseEntity.ok(sprints);
    }

    /**
     * Gets sprint statistics for the current user.
     *
     * @return sprint count
     */
    @GetMapping("/stats/count")
    public ResponseEntity<Long> getSprintCount() {
        User currentUser = getCurrentUser();
        long count = sprintService.getSprintCount(currentUser);
        return ResponseEntity.ok(count);
    }

    /**
     * Gets planned sprints (not yet started).
     *
     * @return list of planned sprint DTOs
     */
    @GetMapping("/planned")
    public ResponseEntity<List<SprintDto>> getPlannedSprints() {
        User currentUser = getCurrentUser();
        List<SprintDto> sprints = sprintService.getSprintsByStatus(currentUser, SprintStatus.PLANNED);
        return ResponseEntity.ok(sprints);
    }

    /**
     * Gets completed sprints.
     *
     * @return list of completed sprint DTOs
     */
    @GetMapping("/completed")
    public ResponseEntity<List<SprintDto>> getCompletedSprints() {
        User currentUser = getCurrentUser();
        List<SprintDto> sprints = sprintService.getSprintsByStatus(currentUser, SprintStatus.COMPLETED);
        return ResponseEntity.ok(sprints);
    }

    /**
     * Adds issues to a sprint.
     * If the sprint is ACTIVE, issues are moved to SELECTED_FOR_DEVELOPMENT status.
     * If the sprint is PLANNED, issues remain in BACKLOG status.
     *
     * @param id sprint ID
     * @param request request containing issue IDs to add
     * @return list of updated issue DTOs
     */
    @PostMapping("/{id}/issues")
    public ResponseEntity<List<IssueDto>> addIssuesToSprint(
            @PathVariable Long id,
            @Valid @RequestBody AddIssuesToSprintRequest request) {
        
        User currentUser = getCurrentUser();
        logger.info("📋 Adding {} issues to sprint {} for user: {}", 
                   request.getIssueIds().size(), id, currentUser.getEmail());
        
        List<Issue> updatedIssues = sprintService.addIssuesToSprint(id, request.getIssueIds(), currentUser);
        
        // Convert to DTOs using IssueService
        List<IssueDto> issueDtos = updatedIssues.stream()
                .map(issue -> issueService.convertIssueToDto(issue))
                .collect(Collectors.toList());
        
        logger.info("✅ Successfully added {} issues to sprint {}", issueDtos.size(), id);
        return ResponseEntity.ok(issueDtos);
    }

    /**
     * Gets all issues that were part of a completed sprint.
     * This includes both completed issues (still in sprint) and incomplete issues 
     * (moved to backlog but marked with lastCompletedSprint).
     *
     * @param id completed sprint ID
     * @return list of issue DTOs that were part of the sprint
     */
    @GetMapping("/{id}/issues")
    public ResponseEntity<List<IssueDto>> getCompletedSprintIssues(@PathVariable Long id) {
        logger.info("🔍 Getting issues for sprint {} - endpoint called", id);
        
        User currentUser = getCurrentUser();
        logger.info("🔍 Current user: {}", currentUser.getEmail());
        
        List<Issue> issues = sprintService.getCompletedSprintIssues(id, currentUser);
        logger.info("🔍 Found {} issues for sprint {}", issues.size(), id);
        
        // Convert to DTOs using IssueService
        List<IssueDto> issueDtos = issues.stream()
                .map(issue -> {
                    IssueDto dto = issueService.convertIssueToDto(issue);
                    logger.info("🔍 Issue DTO: id={}, title={}, status={}, sprintId={}, lastCompletedSprintId={}", 
                               dto.getId(), dto.getTitle(), dto.getStatus(), dto.getSprintId(), dto.getLastCompletedSprintId());
                    return dto;
                })
                .collect(Collectors.toList());
        
        logger.info("🔍 Returning {} issue DTOs", issueDtos.size());
        return ResponseEntity.ok(issueDtos);
    }

    /**
     * Gets the current authenticated user.
     *
     * @return current user
     * @throws RuntimeException if user is not authenticated or not found
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String email = authentication.getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
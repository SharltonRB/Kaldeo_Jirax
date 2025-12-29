package com.issuetracker.controller;

import com.issuetracker.dto.CreateSprintRequest;
import com.issuetracker.dto.SprintDto;
import com.issuetracker.dto.UpdateSprintRequest;
import com.issuetracker.entity.SprintStatus;
import com.issuetracker.entity.User;
import com.issuetracker.service.SprintService;
import com.issuetracker.service.UserService;
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

import java.util.List;
import java.util.Optional;

/**
 * REST controller for sprint management operations.
 * Handles sprint CRUD operations, activation, completion, and planning endpoints.
 */
@RestController
@RequestMapping("/api/sprints")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SprintController {

    private final SprintService sprintService;
    private final UserService userService;

    @Autowired
    public SprintController(SprintService sprintService, UserService userService) {
        this.sprintService = sprintService;
        this.userService = userService;
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
     *
     * @param id sprint ID
     * @return activated sprint DTO
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<SprintDto> activateSprint(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        SprintDto sprint = sprintService.activateSprint(id, currentUser);
        return ResponseEntity.ok(sprint);
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
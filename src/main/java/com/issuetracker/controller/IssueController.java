package com.issuetracker.controller;

import com.issuetracker.dto.*;
import com.issuetracker.entity.IssueStatus;
import com.issuetracker.entity.Priority;
import com.issuetracker.entity.User;
import com.issuetracker.service.AuditService;
import com.issuetracker.service.IssueService;
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

/**
 * REST controller for issue management operations.
 * Handles issue CRUD operations, status transitions, search and filtering capabilities.
 */
@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*", maxAge = 3600)
public class IssueController {

    private final IssueService issueService;
    private final AuditService auditService;
    private final UserService userService;

    @Autowired
    public IssueController(IssueService issueService, AuditService auditService, UserService userService) {
        this.issueService = issueService;
        this.auditService = auditService;
        this.userService = userService;
    }

    /**
     * Retrieves all issues for the authenticated user with pagination and filtering.
     *
     * @param pageable pagination parameters
     * @param projectId optional project filter
     * @param status optional status filter
     * @param priority optional priority filter
     * @param sprintId optional sprint filter
     * @return page of issue DTOs
     */
    @GetMapping
    public ResponseEntity<Page<IssueDto>> getIssues(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Long sprintId) {
        
        User currentUser = getCurrentUser();
        
        Page<IssueDto> issues;
        if (projectId != null || status != null || priority != null || sprintId != null) {
            issues = issueService.searchIssues(currentUser, projectId, status, priority, sprintId, pageable);
        } else {
            issues = issueService.getIssues(currentUser, pageable);
        }
        
        return ResponseEntity.ok(issues);
    }

    /**
     * Retrieves a specific issue by ID.
     *
     * @param id issue ID
     * @return issue DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<IssueDto> getIssue(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        IssueDto issue = issueService.getIssue(id, currentUser);
        return ResponseEntity.ok(issue);
    }

    /**
     * Creates a new issue.
     *
     * @param request issue creation request
     * @return created issue DTO
     */
    @PostMapping
    public ResponseEntity<IssueDto> createIssue(@Valid @RequestBody CreateIssueRequest request) {
        User currentUser = getCurrentUser();
        IssueDto issue = issueService.createIssue(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(issue);
    }

    /**
     * Updates an existing issue.
     *
     * @param id issue ID
     * @param request issue update request
     * @return updated issue DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<IssueDto> updateIssue(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIssueRequest request) {
        
        User currentUser = getCurrentUser();
        IssueDto issue = issueService.updateIssue(id, request, currentUser);
        return ResponseEntity.ok(issue);
    }

    /**
     * Updates issue status with workflow validation.
     *
     * @param id issue ID
     * @param request status update request
     * @return updated issue DTO
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<IssueDto> updateIssueStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        
        User currentUser = getCurrentUser();
        IssueDto issue = issueService.updateIssueStatus(id, request, currentUser);
        return ResponseEntity.ok(issue);
    }

    /**
     * Deletes an issue and all associated data.
     *
     * @param id issue ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIssue(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        issueService.deleteIssue(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves audit history for a specific issue.
     *
     * @param id issue ID
     * @return list of audit log DTOs
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<AuditLogDto>> getIssueHistory(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        
        // First verify the user owns the issue
        issueService.getIssue(id, currentUser);
        
        // Then get the audit history
        List<AuditLogDto> history = auditService.getIssueHistory(id, currentUser);
        return ResponseEntity.ok(history);
    }

    /**
     * Gets issue statistics for the current user.
     *
     * @return issue count
     */
    @GetMapping("/stats/count")
    public ResponseEntity<Long> getIssueCount() {
        User currentUser = getCurrentUser();
        long count = issueService.getIssueCount(currentUser);
        return ResponseEntity.ok(count);
    }

    /**
     * Gets issues by project with pagination.
     *
     * @param projectId project ID
     * @param pageable pagination parameters
     * @return page of issue DTOs for the project
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<Page<IssueDto>> getIssuesByProject(
            @PathVariable Long projectId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        User currentUser = getCurrentUser();
        Page<IssueDto> issues = issueService.searchIssues(currentUser, projectId, null, null, null, pageable);
        return ResponseEntity.ok(issues);
    }

    /**
     * Gets issues by sprint with pagination.
     *
     * @param sprintId sprint ID
     * @param pageable pagination parameters
     * @return page of issue DTOs for the sprint
     */
    @GetMapping("/sprint/{sprintId}")
    public ResponseEntity<Page<IssueDto>> getIssuesBySprint(
            @PathVariable Long sprintId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        User currentUser = getCurrentUser();
        Page<IssueDto> issues = issueService.searchIssues(currentUser, null, null, null, sprintId, pageable);
        return ResponseEntity.ok(issues);
    }

    /**
     * Gets issues by status with pagination.
     *
     * @param status issue status
     * @param pageable pagination parameters
     * @return page of issue DTOs with the specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<IssueDto>> getIssuesByStatus(
            @PathVariable IssueStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        User currentUser = getCurrentUser();
        Page<IssueDto> issues = issueService.searchIssues(currentUser, null, status, null, null, pageable);
        return ResponseEntity.ok(issues);
    }

    /**
     * Gets issues by priority with pagination.
     *
     * @param priority issue priority
     * @param pageable pagination parameters
     * @return page of issue DTOs with the specified priority
     */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<Page<IssueDto>> getIssuesByPriority(
            @PathVariable Priority priority,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        User currentUser = getCurrentUser();
        Page<IssueDto> issues = issueService.searchIssues(currentUser, null, null, priority, null, pageable);
        return ResponseEntity.ok(issues);
    }

    /**
     * Gets backlog issues (not assigned to any sprint) with pagination.
     *
     * @param pageable pagination parameters
     * @return page of backlog issue DTOs
     */
    @GetMapping("/backlog")
    public ResponseEntity<Page<IssueDto>> getBacklogIssues(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        User currentUser = getCurrentUser();
        // Filter for issues with null sprint (backlog issues)
        Page<IssueDto> issues = issueService.searchIssues(currentUser, null, null, null, null, pageable);
        return ResponseEntity.ok(issues);
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
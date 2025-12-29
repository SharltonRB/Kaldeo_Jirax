package com.issuetracker.controller;

import com.issuetracker.dto.CreateProjectRequest;
import com.issuetracker.dto.ProjectDto;
import com.issuetracker.dto.UpdateProjectRequest;
import com.issuetracker.entity.User;
import com.issuetracker.service.ProjectService;
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
 * REST controller for project management operations.
 * Handles project CRUD operations with user isolation and pagination support.
 */
@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    @Autowired
    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    /**
     * Retrieves all projects for the authenticated user with pagination.
     *
     * @param pageable pagination parameters
     * @param search optional search term for filtering projects by name
     * @return page of project DTOs
     */
    @GetMapping
    public ResponseEntity<Page<ProjectDto>> getProjects(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) String search) {
        
        User currentUser = getCurrentUser();
        
        Page<ProjectDto> projects;
        if (search != null && !search.trim().isEmpty()) {
            projects = projectService.searchProjects(currentUser, search.trim(), pageable);
        } else {
            projects = projectService.getProjects(currentUser, pageable);
        }
        
        return ResponseEntity.ok(projects);
    }

    /**
     * Retrieves all projects for the authenticated user without pagination.
     *
     * @return list of all project DTOs
     */
    @GetMapping("/all")
    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        User currentUser = getCurrentUser();
        List<ProjectDto> projects = projectService.getAllProjects(currentUser);
        return ResponseEntity.ok(projects);
    }

    /**
     * Retrieves a specific project by ID.
     *
     * @param id project ID
     * @return project DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getProject(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        ProjectDto project = projectService.getProject(id, currentUser);
        return ResponseEntity.ok(project);
    }

    /**
     * Retrieves a specific project by key.
     *
     * @param key project key
     * @return project DTO
     */
    @GetMapping("/key/{key}")
    public ResponseEntity<ProjectDto> getProjectByKey(@PathVariable String key) {
        User currentUser = getCurrentUser();
        ProjectDto project = projectService.getProjectByKey(key, currentUser);
        return ResponseEntity.ok(project);
    }

    /**
     * Creates a new project.
     *
     * @param request project creation request
     * @return created project DTO
     */
    @PostMapping
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody CreateProjectRequest request) {
        User currentUser = getCurrentUser();
        ProjectDto project = projectService.createProject(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    /**
     * Updates an existing project.
     *
     * @param id project ID
     * @param request project update request
     * @return updated project DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request) {
        
        User currentUser = getCurrentUser();
        ProjectDto project = projectService.updateProject(id, request, currentUser);
        return ResponseEntity.ok(project);
    }

    /**
     * Deletes a project and all associated data.
     *
     * @param id project ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        projectService.deleteProject(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Checks if a project key is available for the current user.
     *
     * @param key project key to check
     * @return availability status
     */
    @GetMapping("/key-available/{key}")
    public ResponseEntity<Boolean> isProjectKeyAvailable(@PathVariable String key) {
        User currentUser = getCurrentUser();
        boolean available = projectService.isProjectKeyAvailable(key, currentUser);
        return ResponseEntity.ok(available);
    }

    /**
     * Gets project statistics for the current user.
     *
     * @return project count
     */
    @GetMapping("/stats/count")
    public ResponseEntity<Long> getProjectCount() {
        User currentUser = getCurrentUser();
        long count = projectService.getProjectCount(currentUser);
        return ResponseEntity.ok(count);
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
package com.issuetracker.controller;

import com.issuetracker.dto.CreateLabelRequest;
import com.issuetracker.dto.LabelDto;
import com.issuetracker.dto.UpdateLabelRequest;
import com.issuetracker.entity.User;
import com.issuetracker.service.LabelService;
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
 * REST controller for label management operations.
 * Handles label CRUD operations with proper authorization and validation.
 */
@RestController
@RequestMapping("/api/labels")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LabelController {

    private final LabelService labelService;
    private final UserService userService;

    @Autowired
    public LabelController(LabelService labelService, UserService userService) {
        this.labelService = labelService;
        this.userService = userService;
    }

    /**
     * Retrieves all labels for the authenticated user with pagination.
     *
     * @param pageable pagination parameters
     * @param search optional search term for filtering labels by name
     * @return page of label DTOs
     */
    @GetMapping
    public ResponseEntity<Page<LabelDto>> getLabels(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) String search) {
        
        User currentUser = getCurrentUser();
        
        Page<LabelDto> labels;
        if (search != null && !search.trim().isEmpty()) {
            labels = labelService.searchLabels(currentUser, search.trim(), pageable);
        } else {
            labels = labelService.getLabels(currentUser, pageable);
        }
        
        return ResponseEntity.ok(labels);
    }

    /**
     * Retrieves all labels for the authenticated user without pagination.
     *
     * @return list of all label DTOs
     */
    @GetMapping("/all")
    public ResponseEntity<List<LabelDto>> getAllLabels() {
        User currentUser = getCurrentUser();
        List<LabelDto> labels = labelService.getAllLabels(currentUser);
        return ResponseEntity.ok(labels);
    }

    /**
     * Retrieves a specific label by ID.
     *
     * @param id label ID
     * @return label DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<LabelDto> getLabel(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        LabelDto label = labelService.getLabel(id, currentUser);
        return ResponseEntity.ok(label);
    }

    /**
     * Creates a new label.
     *
     * @param request label creation request
     * @return created label DTO
     */
    @PostMapping
    public ResponseEntity<LabelDto> createLabel(@Valid @RequestBody CreateLabelRequest request) {
        User currentUser = getCurrentUser();
        LabelDto label = labelService.createLabel(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(label);
    }

    /**
     * Updates an existing label.
     *
     * @param id label ID
     * @param request label update request
     * @return updated label DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<LabelDto> updateLabel(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLabelRequest request) {
        
        User currentUser = getCurrentUser();
        LabelDto label = labelService.updateLabel(id, request, currentUser);
        return ResponseEntity.ok(label);
    }

    /**
     * Deletes a label and removes it from all associated issues.
     *
     * @param id label ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLabel(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        labelService.deleteLabel(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets labels by color.
     *
     * @param color label color (hex format)
     * @return list of label DTOs with the specified color
     */
    @GetMapping("/color/{color}")
    public ResponseEntity<List<LabelDto>> getLabelsByColor(@PathVariable String color) {
        User currentUser = getCurrentUser();
        
        // Ensure color starts with # if not provided
        if (!color.startsWith("#")) {
            color = "#" + color;
        }
        
        List<LabelDto> labels = labelService.getLabelsByColor(currentUser, color);
        return ResponseEntity.ok(labels);
    }

    /**
     * Gets unused labels for cleanup purposes.
     *
     * @return list of unused label DTOs
     */
    @GetMapping("/unused")
    public ResponseEntity<List<LabelDto>> getUnusedLabels() {
        User currentUser = getCurrentUser();
        List<LabelDto> labels = labelService.getUnusedLabels(currentUser);
        return ResponseEntity.ok(labels);
    }

    /**
     * Checks if a label name is available for the current user.
     *
     * @param name label name to check
     * @return availability status
     */
    @GetMapping("/name-available/{name}")
    public ResponseEntity<Boolean> isLabelNameAvailable(@PathVariable String name) {
        User currentUser = getCurrentUser();
        boolean available = labelService.isLabelNameAvailable(name, currentUser);
        return ResponseEntity.ok(available);
    }

    /**
     * Gets label statistics for the current user.
     *
     * @return label count
     */
    @GetMapping("/stats/count")
    public ResponseEntity<Long> getLabelCount() {
        User currentUser = getCurrentUser();
        long count = labelService.getLabelCount(currentUser);
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
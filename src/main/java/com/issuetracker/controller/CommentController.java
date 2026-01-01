package com.issuetracker.controller;

import com.issuetracker.dto.CommentDto;
import com.issuetracker.dto.CreateCommentRequest;
import com.issuetracker.dto.UpdateCommentRequest;
import com.issuetracker.entity.User;
import com.issuetracker.service.CommentService;
import com.issuetracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST controller for comment management operations.
 * Handles comment CRUD operations with proper authorization and validation.
 */
@RestController
@RequestMapping("/comments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    @Autowired
    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    /**
     * Retrieves a specific comment by ID.
     *
     * @param id comment ID
     * @return comment DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommentDto> getComment(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        CommentDto comment = commentService.getComment(id, currentUser);
        return ResponseEntity.ok(comment);
    }

    /**
     * Creates a new comment.
     *
     * @param request comment creation request
     * @return created comment DTO
     */
    @PostMapping
    public ResponseEntity<CommentDto> createComment(@Valid @RequestBody CreateCommentRequest request) {
        User currentUser = getCurrentUser();
        CommentDto comment = commentService.createComment(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    /**
     * Updates an existing comment.
     *
     * @param id comment ID
     * @param request comment update request
     * @return updated comment DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCommentRequest request) {
        
        User currentUser = getCurrentUser();
        CommentDto comment = commentService.updateComment(id, request, currentUser);
        return ResponseEntity.ok(comment);
    }

    /**
     * Deletes a comment.
     *
     * @param id comment ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        commentService.deleteComment(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves comments for a specific issue in chronological order.
     *
     * @param issueId issue ID
     * @param pageable optional pagination parameters
     * @return list or page of comment DTOs
     */
    @GetMapping("/issue/{issueId}")
    public ResponseEntity<?> getIssueComments(
            @PathVariable Long issueId,
            @RequestParam(required = false, defaultValue = "false") boolean paginated,
            Pageable pageable) {
        
        User currentUser = getCurrentUser();
        
        if (paginated) {
            Page<CommentDto> comments = commentService.getIssueComments(issueId, currentUser, pageable);
            return ResponseEntity.ok(comments);
        } else {
            List<CommentDto> comments = commentService.getIssueComments(issueId, currentUser);
            return ResponseEntity.ok(comments);
        }
    }

    /**
     * Retrieves recent comments for an issue after a specific timestamp.
     *
     * @param issueId issue ID
     * @param since timestamp to get comments after
     * @return list of recent comment DTOs
     */
    @GetMapping("/issue/{issueId}/recent")
    public ResponseEntity<List<CommentDto>> getRecentIssueComments(
            @PathVariable Long issueId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since) {
        
        User currentUser = getCurrentUser();
        List<CommentDto> comments = commentService.getRecentIssueComments(issueId, currentUser, since);
        return ResponseEntity.ok(comments);
    }

    /**
     * Retrieves all comments authored by the current user with pagination.
     *
     * @param pageable pagination parameters
     * @param search optional search term for filtering comments by content
     * @return page of comment DTOs
     */
    @GetMapping("/my-comments")
    public ResponseEntity<Page<CommentDto>> getUserComments(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) String search) {
        
        User currentUser = getCurrentUser();
        
        Page<CommentDto> comments;
        if (search != null && !search.trim().isEmpty()) {
            comments = commentService.searchComments(currentUser, search.trim(), pageable);
        } else {
            comments = commentService.getUserComments(currentUser, pageable);
        }
        
        return ResponseEntity.ok(comments);
    }

    /**
     * Gets comment count for a specific issue.
     *
     * @param issueId issue ID
     * @return comment count
     */
    @GetMapping("/issue/{issueId}/count")
    public ResponseEntity<Long> getIssueCommentCount(@PathVariable Long issueId) {
        User currentUser = getCurrentUser();
        long count = commentService.getIssueCommentCount(issueId, currentUser);
        return ResponseEntity.ok(count);
    }

    /**
     * Gets total comment count for the current user.
     *
     * @return comment count
     */
    @GetMapping("/stats/count")
    public ResponseEntity<Long> getUserCommentCount() {
        User currentUser = getCurrentUser();
        long count = commentService.getUserCommentCount(currentUser);
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
package com.issuetracker.service;

import com.issuetracker.dto.CommentDto;
import com.issuetracker.dto.CreateCommentRequest;
import com.issuetracker.dto.UpdateCommentRequest;
import com.issuetracker.entity.Comment;
import com.issuetracker.entity.Issue;
import com.issuetracker.entity.User;
import com.issuetracker.exception.ResourceNotFoundException;
import com.issuetracker.repository.CommentRepository;
import com.issuetracker.repository.IssueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing comments with user isolation and proper authorization.
 * Handles comment CRUD operations and chronological ordering.
 */
@Service
@Transactional
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final IssueRepository issueRepository;

    public CommentService(CommentRepository commentRepository, IssueRepository issueRepository) {
        this.commentRepository = commentRepository;
        this.issueRepository = issueRepository;
    }

    /**
     * Creates a new comment for the specified user and issue.
     *
     * @param request the comment creation request
     * @param user the comment author
     * @return the created comment DTO
     * @throws ResourceNotFoundException if issue not found or not owned by user
     */
    public CommentDto createComment(CreateCommentRequest request, User user) {
        logger.debug("Creating comment for issue {} by user {}", request.getIssueId(), user.getId());

        // Validate issue ownership - users can only comment on their own issues
        Issue issue = issueRepository.findByIdAndUser(request.getIssueId(), user)
                .orElseThrow(() -> ResourceNotFoundException.issue(request.getIssueId()));

        // Create and save comment
        Comment comment = new Comment(user, issue, request.getContent());
        Comment savedComment = commentRepository.save(comment);

        logger.info("Created comment (ID: {}) for issue '{}' by user {}", 
                   savedComment.getId(), issue.getTitle(), user.getId());

        return convertToDto(savedComment);
    }

    /**
     * Updates an existing comment.
     *
     * @param commentId the comment ID
     * @param request the update request
     * @param user the comment author
     * @return the updated comment DTO
     * @throws ResourceNotFoundException if comment not found or not authored by user
     */
    public CommentDto updateComment(Long commentId, UpdateCommentRequest request, User user) {
        logger.debug("Updating comment {} by user {}", commentId, user.getId());

        Comment comment = commentRepository.findByIdAndUser(commentId, user)
                .orElseThrow(() -> ResourceNotFoundException.comment(commentId));

        // Update comment content
        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);

        logger.info("Updated comment (ID: {}) by user {}", 
                   updatedComment.getId(), user.getId());

        return convertToDto(updatedComment);
    }

    /**
     * Retrieves a comment by ID with user authorization.
     *
     * @param commentId the comment ID
     * @param user the requesting user
     * @return the comment DTO
     * @throws ResourceNotFoundException if comment not found or not accessible by user
     */
    @Transactional(readOnly = true)
    public CommentDto getComment(Long commentId, User user) {
        logger.debug("Retrieving comment {} for user {}", commentId, user.getId());

        // Users can only access comments they authored or comments on their issues
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> ResourceNotFoundException.comment(commentId));

        // Check if user is the author or the issue owner
        if (!comment.getUser().equals(user) && !comment.getIssue().getUser().equals(user)) {
            throw ResourceNotFoundException.comment(commentId);
        }

        return convertToDto(comment);
    }

    /**
     * Retrieves comments for a specific issue in chronological order.
     *
     * @param issueId the issue ID
     * @param user the requesting user
     * @return list of comment DTOs
     * @throws ResourceNotFoundException if issue not found or not owned by user
     */
    @Transactional(readOnly = true)
    public List<CommentDto> getIssueComments(Long issueId, User user) {
        logger.debug("Retrieving comments for issue {} by user {}", issueId, user.getId());

        // Validate issue ownership
        Issue issue = issueRepository.findByIdAndUser(issueId, user)
                .orElseThrow(() -> ResourceNotFoundException.issue(issueId));

        List<Comment> comments = commentRepository.findByIssueOrderByCreatedAtAsc(issue);
        return comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves comments for a specific issue with pagination.
     *
     * @param issueId the issue ID
     * @param user the requesting user
     * @param pageable pagination information
     * @return page of comment DTOs
     * @throws ResourceNotFoundException if issue not found or not owned by user
     */
    @Transactional(readOnly = true)
    public Page<CommentDto> getIssueComments(Long issueId, User user, Pageable pageable) {
        logger.debug("Retrieving comments for issue {} by user {} with pagination", issueId, user.getId());

        // Validate issue ownership
        Issue issue = issueRepository.findByIdAndUser(issueId, user)
                .orElseThrow(() -> ResourceNotFoundException.issue(issueId));

        Page<Comment> comments = commentRepository.findByIssueOrderByCreatedAtAsc(issue, pageable);
        return comments.map(this::convertToDto);
    }

    /**
     * Retrieves all comments authored by a user with pagination.
     *
     * @param user the comment author
     * @param pageable pagination information
     * @return page of comment DTOs
     */
    @Transactional(readOnly = true)
    public Page<CommentDto> getUserComments(User user, Pageable pageable) {
        logger.debug("Retrieving comments authored by user {} with pagination", user.getId());

        Page<Comment> comments = commentRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return comments.map(this::convertToDto);
    }

    /**
     * Retrieves recent comments for an issue after a specific timestamp.
     *
     * @param issueId the issue ID
     * @param user the requesting user
     * @param since the timestamp to get comments after
     * @return list of recent comment DTOs
     * @throws ResourceNotFoundException if issue not found or not owned by user
     */
    @Transactional(readOnly = true)
    public List<CommentDto> getRecentIssueComments(Long issueId, User user, Instant since) {
        logger.debug("Retrieving recent comments for issue {} since {} by user {}", issueId, since, user.getId());

        // Validate issue ownership
        Issue issue = issueRepository.findByIdAndUser(issueId, user)
                .orElseThrow(() -> ResourceNotFoundException.issue(issueId));

        List<Comment> comments = commentRepository.findByIssueAndCreatedAtAfterOrderByCreatedAtAsc(issue, since);
        return comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Searches comments by content with user isolation.
     *
     * @param user the comment author
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of matching comment DTOs
     */
    @Transactional(readOnly = true)
    public Page<CommentDto> searchComments(User user, String searchTerm, Pageable pageable) {
        logger.debug("Searching comments for user {} with term '{}'", user.getId(), searchTerm);

        Page<Comment> comments = commentRepository.findByUserAndContentContainingIgnoreCase(user, searchTerm, pageable);
        return comments.map(this::convertToDto);
    }

    /**
     * Deletes a comment.
     *
     * @param commentId the comment ID
     * @param user the comment author
     * @throws ResourceNotFoundException if comment not found or not authored by user
     */
    public void deleteComment(Long commentId, User user) {
        logger.debug("Deleting comment {} by user {}", commentId, user.getId());

        Comment comment = commentRepository.findByIdAndUser(commentId, user)
                .orElseThrow(() -> ResourceNotFoundException.comment(commentId));

        // Delete comment
        commentRepository.delete(comment);

        logger.info("Deleted comment (ID: {}) by user {}", commentId, user.getId());
    }

    /**
     * Gets comment count for an issue.
     *
     * @param issueId the issue ID
     * @param user the requesting user
     * @return comment count
     * @throws ResourceNotFoundException if issue not found or not owned by user
     */
    @Transactional(readOnly = true)
    public long getIssueCommentCount(Long issueId, User user) {
        // Validate issue ownership
        Issue issue = issueRepository.findByIdAndUser(issueId, user)
                .orElseThrow(() -> ResourceNotFoundException.issue(issueId));

        return commentRepository.countByIssue(issue);
    }

    /**
     * Gets total comment count for a user.
     *
     * @param user the comment author
     * @return comment count
     */
    @Transactional(readOnly = true)
    public long getUserCommentCount(User user) {
        return commentRepository.countByUser(user);
    }

    /**
     * Converts a Comment entity to CommentDto.
     *
     * @param comment the comment entity
     * @return the comment DTO
     */
    private CommentDto convertToDto(Comment comment) {
        CommentDto dto = new CommentDto(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );

        // Set issue information
        dto.setIssueId(comment.getIssue().getId());
        dto.setIssueTitle(comment.getIssue().getTitle());

        // Set author information
        dto.setAuthorName(comment.getUser().getName());

        return dto;
    }
}
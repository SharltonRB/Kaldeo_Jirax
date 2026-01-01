package com.issuetracker.repository;

import com.issuetracker.entity.Comment;
import com.issuetracker.entity.Issue;
import com.issuetracker.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Comment entity operations.
 * Provides data access methods for comment management with user isolation.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Finds all comments by a specific user with pagination.
     *
     * @param user the comment author
     * @param pageable pagination information
     * @return page of comments
     */
    Page<Comment> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Finds a comment by ID and user for data isolation.
     *
     * @param id comment ID
     * @param user comment author
     * @return optional comment
     */
    Optional<Comment> findByIdAndUser(Long id, User user);

    /**
     * Finds comments for a specific issue in chronological order.
     *
     * @param issue the issue
     * @return list of comments ordered by creation time
     */
    List<Comment> findByIssueOrderByCreatedAtAsc(Issue issue);

    /**
     * Finds comments for a specific issue with pagination.
     *
     * @param issue the issue
     * @param pageable pagination information
     * @return page of comments
     */
    Page<Comment> findByIssueOrderByCreatedAtAsc(Issue issue, Pageable pageable);

    /**
     * Finds comments by issue and user (for authorization checks).
     *
     * @param issue the issue
     * @param user the comment author
     * @return list of comments
     */
    List<Comment> findByIssueAndUserOrderByCreatedAtAsc(Issue issue, User user);

    /**
     * Counts comments for a specific issue.
     *
     * @param issue the issue
     * @return comment count
     */
    long countByIssue(Issue issue);

    /**
     * Counts total comments by a user.
     *
     * @param user the comment author
     * @return comment count
     */
    long countByUser(User user);

    /**
     * Finds comments created after a specific timestamp for an issue.
     *
     * @param issue the issue
     * @param timestamp the timestamp
     * @return list of recent comments
     */
    List<Comment> findByIssueAndCreatedAtAfterOrderByCreatedAtAsc(Issue issue, Instant timestamp);

    /**
     * Finds comments containing search term (case-insensitive).
     *
     * @param user the comment author
     * @param searchTerm search term
     * @param pageable pagination information
     * @return page of matching comments
     */
    @Query("SELECT c FROM Comment c WHERE c.user = :user AND " +
           "LOWER(c.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY c.createdAt DESC")
    Page<Comment> findByUserAndContentContainingIgnoreCase(@Param("user") User user, 
                                                           @Param("searchTerm") String searchTerm, 
                                                           Pageable pageable);

    /**
     * Finds comments for issues owned by a user (for accessing comments on user's issues).
     *
     * @param user the issue owner
     * @param pageable pagination information
     * @return page of comments on user's issues
     */
    @Query("SELECT c FROM Comment c WHERE c.issue.user = :user " +
           "ORDER BY c.createdAt DESC")
    Page<Comment> findCommentsOnUserIssues(@Param("user") User user, Pageable pageable);

    /**
     * Deletes a comment by ID and user for data isolation.
     *
     * @param id comment ID
     * @param user comment author
     */
    void deleteByIdAndUser(Long id, User user);
}
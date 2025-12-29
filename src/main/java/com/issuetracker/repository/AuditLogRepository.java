package com.issuetracker.repository;

import com.issuetracker.entity.AuditLog;
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

/**
 * Repository interface for AuditLog entity operations.
 * Provides data access methods for audit trail management with user isolation.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Finds all audit logs for a specific user with pagination.
     *
     * @param user the user
     * @param pageable pagination information
     * @return page of audit logs
     */
    Page<AuditLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Finds audit logs for a specific issue in chronological order.
     *
     * @param issue the issue
     * @return list of audit logs ordered by creation time
     */
    List<AuditLog> findByIssueOrderByCreatedAtAsc(Issue issue);

    /**
     * Finds audit logs for a specific issue with pagination.
     *
     * @param issue the issue
     * @param pageable pagination information
     * @return page of audit logs
     */
    Page<AuditLog> findByIssueOrderByCreatedAtAsc(Issue issue, Pageable pageable);

    /**
     * Finds audit logs by issue and user.
     *
     * @param issue the issue
     * @param user the user who made the changes
     * @return list of audit logs
     */
    List<AuditLog> findByIssueAndUserOrderByCreatedAtAsc(Issue issue, User user);

    /**
     * Finds audit logs by action type for a user.
     *
     * @param user the user
     * @param action the action type
     * @param pageable pagination information
     * @return page of audit logs
     */
    Page<AuditLog> findByUserAndActionOrderByCreatedAtDesc(User user, String action, Pageable pageable);

    /**
     * Counts audit logs for a specific issue.
     *
     * @param issue the issue
     * @return audit log count
     */
    long countByIssue(Issue issue);

    /**
     * Counts total audit logs by a user.
     *
     * @param user the user
     * @return audit log count
     */
    long countByUser(User user);

    /**
     * Finds audit logs created after a specific timestamp for an issue.
     *
     * @param issue the issue
     * @param timestamp the timestamp
     * @return list of recent audit logs
     */
    List<AuditLog> findByIssueAndCreatedAtAfterOrderByCreatedAtAsc(Issue issue, Instant timestamp);

    /**
     * Finds audit logs by action type and date range.
     *
     * @param user the user
     * @param action the action type
     * @param startDate range start
     * @param endDate range end
     * @param pageable pagination information
     * @return page of audit logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user = :user AND a.action = :action AND " +
           "a.createdAt >= :startDate AND a.createdAt <= :endDate " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> findByUserAndActionAndDateRange(@Param("user") User user,
                                                   @Param("action") String action,
                                                   @Param("startDate") Instant startDate,
                                                   @Param("endDate") Instant endDate,
                                                   Pageable pageable);

    /**
     * Finds audit logs containing search term in details (case-insensitive).
     *
     * @param user the user
     * @param searchTerm search term
     * @param pageable pagination information
     * @return page of matching audit logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user = :user AND " +
           "LOWER(a.details) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> findByUserAndDetailsContainingIgnoreCase(@Param("user") User user, 
                                                            @Param("searchTerm") String searchTerm, 
                                                            Pageable pageable);

    /**
     * Finds audit logs for issues owned by a user (for accessing audit logs on user's issues).
     *
     * @param user the issue owner
     * @param pageable pagination information
     * @return page of audit logs on user's issues
     */
    @Query("SELECT a FROM AuditLog a WHERE a.issue.user = :user " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> findAuditLogsOnUserIssues(@Param("user") User user, Pageable pageable);
}
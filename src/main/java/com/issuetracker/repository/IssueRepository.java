package com.issuetracker.repository;

import com.issuetracker.entity.Issue;
import com.issuetracker.entity.IssueStatus;
import com.issuetracker.entity.Priority;
import com.issuetracker.entity.Project;
import com.issuetracker.entity.Sprint;
import com.issuetracker.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Issue entity operations.
 * Provides data access methods for issue management with user isolation.
 */
@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

    /**
     * Finds all issues owned by a specific user with pagination.
     *
     * @param user the issue owner
     * @param pageable pagination information
     * @return page of issues
     */
    Page<Issue> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Finds an issue by ID and user for data isolation.
     *
     * @param id issue ID
     * @param user issue owner
     * @return optional issue
     */
    Optional<Issue> findByIdAndUser(Long id, User user);

    /**
     * Finds issues by project and user.
     *
     * @param project the project
     * @param user the issue owner
     * @param pageable pagination information
     * @return page of issues
     */
    Page<Issue> findByProjectAndUserOrderByCreatedAtDesc(Project project, User user, Pageable pageable);

    /**
     * Finds issues by sprint and user.
     *
     * @param sprint the sprint
     * @param user the issue owner
     * @param pageable pagination information
     * @return page of issues
     */
    Page<Issue> findBySprintAndUserOrderByCreatedAtDesc(Sprint sprint, User user, Pageable pageable);

    /**
     * Finds issues by status and user.
     *
     * @param status issue status
     * @param user the issue owner
     * @param pageable pagination information
     * @return page of issues
     */
    Page<Issue> findByStatusAndUserOrderByCreatedAtDesc(IssueStatus status, User user, Pageable pageable);

    /**
     * Finds issues by priority and user.
     *
     * @param priority issue priority
     * @param user the issue owner
     * @param pageable pagination information
     * @return page of issues
     */
    Page<Issue> findByPriorityAndUserOrderByCreatedAtDesc(Priority priority, User user, Pageable pageable);

    /**
     * Finds issues in backlog (not assigned to any sprint).
     *
     * @param user the issue owner
     * @param pageable pagination information
     * @return page of backlog issues
     */
    Page<Issue> findByUserAndSprintIsNullOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Counts issues by status for a user.
     *
     * @param user the issue owner
     * @param status issue status
     * @return issue count
     */
    long countByUserAndStatus(User user, IssueStatus status);

    /**
     * Counts issues by priority for a user.
     *
     * @param user the issue owner
     * @param priority issue priority
     * @return issue count
     */
    long countByUserAndPriority(User user, Priority priority);

    /**
     * Counts total issues for a user.
     *
     * @param user the issue owner
     * @return issue count
     */
    long countByUser(User user);

    /**
     * Counts issues by user and project.
     *
     * @param user the issue owner
     * @param project the project
     * @return issue count
     */
    long countByUserAndProject(User user, Project project);

    /**
     * Counts issues by user and sprint.
     *
     * @param user the issue owner
     * @param sprint the sprint
     * @return issue count
     */
    long countByUserAndSprint(User user, Sprint sprint);

    /**
     * Counts issues by user, sprint, and status.
     *
     * @param user the issue owner
     * @param sprint the sprint
     * @param status the issue status
     * @return issue count
     */
    long countByUserAndSprintAndStatus(User user, Sprint sprint, IssueStatus status);

    /**
     * Finds issues by title containing search term (case-insensitive).
     *
     * @param user the issue owner
     * @param searchTerm search term
     * @param pageable pagination information
     * @return page of matching issues
     */
    @Query("SELECT i FROM Issue i WHERE i.user = :user AND " +
           "LOWER(i.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY i.createdAt DESC")
    Page<Issue> findByUserAndTitleContainingIgnoreCase(@Param("user") User user, 
                                                       @Param("searchTerm") String searchTerm, 
                                                       Pageable pageable);

    /**
     * Finds issues with complex filtering.
     *
     * @param user the issue owner
     * @param project optional project filter
     * @param status optional status filter
     * @param priority optional priority filter
     * @param sprint optional sprint filter
     * @param pageable pagination information
     * @return page of filtered issues
     */
    @Query("SELECT i FROM Issue i WHERE i.user = :user " +
           "AND (:project IS NULL OR i.project = :project) " +
           "AND (:status IS NULL OR i.status = :status) " +
           "AND (:priority IS NULL OR i.priority = :priority) " +
           "AND (:sprint IS NULL OR i.sprint = :sprint) " +
           "ORDER BY i.createdAt DESC")
    Page<Issue> findByUserWithFilters(@Param("user") User user,
                                     @Param("project") Project project,
                                     @Param("status") IssueStatus status,
                                     @Param("priority") Priority priority,
                                     @Param("sprint") Sprint sprint,
                                     Pageable pageable);

    /**
     * Finds issues assigned to a sprint for a user.
     *
     * @param user the issue owner
     * @param sprint the sprint
     * @return list of issues
     */
    List<Issue> findByUserAndSprint(User user, Sprint sprint);

    /**
     * Deletes an issue by ID and user for data isolation.
     *
     * @param id issue ID
     * @param user issue owner
     */
    void deleteByIdAndUser(Long id, User user);
}
package com.issuetracker.repository;

import com.issuetracker.entity.Sprint;
import com.issuetracker.entity.SprintStatus;
import com.issuetracker.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Sprint entity operations.
 * Provides data access methods for sprint management with user isolation.
 */
@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {

    /**
     * Finds all sprints owned by a specific user with pagination.
     *
     * @param user the sprint owner
     * @param pageable pagination information
     * @return page of sprints
     */
    Page<Sprint> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Finds all sprints owned by a specific user.
     *
     * @param user the sprint owner
     * @return list of sprints
     */
    List<Sprint> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Finds a sprint by ID and user for data isolation.
     *
     * @param id sprint ID
     * @param user sprint owner
     * @return optional sprint
     */
    Optional<Sprint> findByIdAndUser(Long id, User user);

    /**
     * Finds sprints by status and user.
     *
     * @param user the sprint owner
     * @param status sprint status
     * @return list of sprints
     */
    List<Sprint> findByUserAndStatusOrderByCreatedAtDesc(User user, SprintStatus status);

    /**
     * Finds the active sprint for a user (only one should exist).
     *
     * @param user the sprint owner
     * @param status sprint status
     * @return optional active sprint
     */
    Optional<Sprint> findByUserAndStatus(User user, SprintStatus status);

    /**
     * Finds sprints by date range and user.
     *
     * @param user the sprint owner
     * @param startDate range start date
     * @param endDate range end date
     * @param pageable pagination information
     * @return page of sprints
     */
    @Query("SELECT s FROM Sprint s WHERE s.user = :user AND " +
           "s.startDate >= :startDate AND s.endDate <= :endDate " +
           "ORDER BY s.startDate DESC")
    Page<Sprint> findByUserAndDateRange(@Param("user") User user,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate,
                                       Pageable pageable);

    /**
     * Finds overlapping sprints for a user (for validation).
     *
     * @param user the sprint owner
     * @param startDate new sprint start date
     * @param endDate new sprint end date
     * @param excludeId optional sprint ID to exclude (for updates)
     * @return list of overlapping sprints
     */
    @Query("SELECT s FROM Sprint s WHERE s.user = :user AND " +
           "(:excludeId IS NULL OR s.id != :excludeId) AND " +
           "((s.startDate <= :startDate AND s.endDate >= :startDate) OR " +
           "(s.startDate <= :endDate AND s.endDate >= :endDate) OR " +
           "(s.startDate >= :startDate AND s.endDate <= :endDate))")
    List<Sprint> findOverlappingSprints(@Param("user") User user,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate,
                                       @Param("excludeId") Long excludeId);

    /**
     * Counts sprints by status for a user.
     *
     * @param user the sprint owner
     * @param status sprint status
     * @return sprint count
     */
    long countByUserAndStatus(User user, SprintStatus status);

    /**
     * Counts total sprints for a user.
     *
     * @param user the sprint owner
     * @return sprint count
     */
    long countByUser(User user);

    /**
     * Finds sprints by name containing search term (case-insensitive).
     *
     * @param user the sprint owner
     * @param searchTerm search term
     * @param pageable pagination information
     * @return page of matching sprints
     */
    @Query("SELECT s FROM Sprint s WHERE s.user = :user AND " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY s.createdAt DESC")
    Page<Sprint> findByUserAndNameContainingIgnoreCase(@Param("user") User user, 
                                                       @Param("searchTerm") String searchTerm, 
                                                       Pageable pageable);

    /**
     * Deletes a sprint by ID and user for data isolation.
     *
     * @param id sprint ID
     * @param user sprint owner
     */
    void deleteByIdAndUser(Long id, User user);
}
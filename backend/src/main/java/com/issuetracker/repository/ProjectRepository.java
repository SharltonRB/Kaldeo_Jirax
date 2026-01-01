package com.issuetracker.repository;

import com.issuetracker.entity.Project;
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
 * Repository interface for Project entity operations.
 * Provides data access methods for project management with user isolation.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Finds all projects owned by a specific user with pagination.
     *
     * @param user the project owner
     * @param pageable pagination information
     * @return page of projects
     */
    Page<Project> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Finds all projects owned by a specific user.
     *
     * @param user the project owner
     * @return list of projects
     */
    List<Project> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Finds a project by ID and user for data isolation.
     *
     * @param id project ID
     * @param user project owner
     * @return optional project
     */
    Optional<Project> findByIdAndUser(Long id, User user);

    /**
     * Finds a project by key and user for uniqueness validation.
     *
     * @param key project key
     * @param user project owner
     * @return optional project
     */
    Optional<Project> findByKeyAndUser(String key, User user);

    /**
     * Checks if a project key exists for a specific user.
     *
     * @param key project key
     * @param user project owner
     * @return true if key exists
     */
    boolean existsByKeyAndUser(String key, User user);

    /**
     * Counts total projects for a user.
     *
     * @param user project owner
     * @return project count
     */
    long countByUser(User user);

    /**
     * Finds projects by name containing search term (case-insensitive).
     *
     * @param user project owner
     * @param searchTerm search term
     * @param pageable pagination information
     * @return page of matching projects
     */
    @Query("SELECT p FROM Project p WHERE p.user = :user AND " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY p.createdAt DESC")
    Page<Project> findByUserAndNameContainingIgnoreCase(@Param("user") User user, 
                                                        @Param("searchTerm") String searchTerm, 
                                                        Pageable pageable);

    /**
     * Deletes a project by ID and user for data isolation.
     *
     * @param id project ID
     * @param user project owner
     */
    void deleteByIdAndUser(Long id, User user);
}
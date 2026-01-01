package com.issuetracker.repository;

import com.issuetracker.entity.IssueType;
import com.issuetracker.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for IssueType entity operations.
 * Provides data access methods for both global and project-specific issue types.
 */
@Repository
public interface IssueTypeRepository extends JpaRepository<IssueType, Long> {

    /**
     * Finds all global issue types available to all users.
     *
     * @return list of global issue types
     */
    List<IssueType> findByIsGlobalTrueOrderByName();

    /**
     * Finds issue types for a specific project.
     *
     * @param project the project
     * @return list of project-specific issue types
     */
    List<IssueType> findByProjectOrderByName(Project project);

    /**
     * Finds all available issue types for a project (global + project-specific).
     *
     * @param project the project
     * @return list of available issue types
     */
    @Query("SELECT it FROM IssueType it WHERE it.isGlobal = true OR it.project = :project ORDER BY it.name")
    List<IssueType> findAvailableForProject(@Param("project") Project project);

    /**
     * Finds an issue type by name and project for uniqueness validation.
     *
     * @param name issue type name
     * @param project the project
     * @return optional issue type
     */
    Optional<IssueType> findByNameAndProject(String name, Project project);

    /**
     * Finds a global issue type by name.
     *
     * @param name issue type name
     * @return optional global issue type
     */
    Optional<IssueType> findByNameAndIsGlobalTrue(String name);

    /**
     * Checks if an issue type name exists for a specific project.
     *
     * @param name issue type name
     * @param project the project
     * @return true if name exists
     */
    boolean existsByNameAndProject(String name, Project project);

    /**
     * Checks if a global issue type name exists.
     *
     * @param name issue type name
     * @return true if global name exists
     */
    boolean existsByNameAndIsGlobalTrue(String name);

    /**
     * Counts issue types for a project (excluding global types).
     *
     * @param project the project
     * @return issue type count
     */
    long countByProject(Project project);

    /**
     * Finds issue types that are not used by any issues for cleanup.
     *
     * @param project the project
     * @return list of unused issue types
     */
    @Query("SELECT it FROM IssueType it WHERE it.project = :project AND it.issues IS EMPTY")
    List<IssueType> findUnusedByProject(@Param("project") Project project);
}
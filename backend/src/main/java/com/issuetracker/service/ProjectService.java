package com.issuetracker.service;

import com.issuetracker.dto.CreateProjectRequest;
import com.issuetracker.dto.ProjectDto;
import com.issuetracker.dto.UpdateProjectRequest;
import com.issuetracker.entity.Project;
import com.issuetracker.entity.User;
import com.issuetracker.exception.DuplicateResourceException;
import com.issuetracker.exception.ResourceNotFoundException;
import com.issuetracker.repository.IssueRepository;
import com.issuetracker.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing projects with user isolation.
 * Handles project CRUD operations, validation, and business logic.
 */
@Service
@Transactional
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;

    public ProjectService(ProjectRepository projectRepository, IssueRepository issueRepository) {
        this.projectRepository = projectRepository;
        this.issueRepository = issueRepository;
    }

    /**
     * Creates a new project for the specified user.
     *
     * @param request the project creation request
     * @param user the project owner
     * @return the created project DTO
     * @throws DuplicateResourceException if project key already exists for the user
     */
    public ProjectDto createProject(CreateProjectRequest request, User user) {
        logger.info("📁 Creating project '{}' for user: {}", request.getKey(), user.getEmail());

        // Validate project key uniqueness within user scope
        if (projectRepository.existsByKeyAndUser(request.getKey(), user)) {
            logger.warn("❌ Project key '{}' already exists for user: {}", request.getKey(), user.getEmail());
            throw DuplicateResourceException.projectKey(request.getKey());
        }

        // Create and save project
        Project project = new Project(user, request.getName(), request.getKey(), request.getDescription());
        Project savedProject = projectRepository.save(project);

        logger.info("✅ Created project '{}' with key '{}' for user: {}", 
                   savedProject.getName(), savedProject.getKey(), user.getEmail());

        return convertToDto(savedProject);
    }

    /**
     * Updates an existing project.
     *
     * @param projectId the project ID
     * @param request the update request
     * @param user the project owner
     * @return the updated project DTO
     * @throws ResourceNotFoundException if project not found or not owned by user
     */
    public ProjectDto updateProject(Long projectId, UpdateProjectRequest request, User user) {
        logger.info("📁 Updating project {} for user: {}", projectId, user.getEmail());

        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> ResourceNotFoundException.project(projectId));

        // Update project fields
        project.setName(request.getName());
        project.setDescription(request.getDescription());

        Project updatedProject = projectRepository.save(project);

        logger.info("Updated project '{}' (ID: {}) for user {}", 
                   updatedProject.getName(), updatedProject.getId(), user.getId());

        return convertToDto(updatedProject);
    }

    /**
     * Retrieves a project by ID with user isolation.
     *
     * @param projectId the project ID
     * @param user the project owner
     * @return the project DTO
     * @throws ResourceNotFoundException if project not found or not owned by user
     */
    @Transactional(readOnly = true)
    public ProjectDto getProject(Long projectId, User user) {
        logger.debug("Retrieving project {} for user {}", projectId, user.getId());

        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> ResourceNotFoundException.project(projectId));

        return convertToDto(project);
    }

    /**
     * Retrieves a project by key with user isolation.
     *
     * @param projectKey the project key
     * @param user the project owner
     * @return the project DTO
     * @throws ResourceNotFoundException if project not found or not owned by user
     */
    @Transactional(readOnly = true)
    public ProjectDto getProjectByKey(String projectKey, User user) {
        logger.debug("Retrieving project with key '{}' for user {}", projectKey, user.getId());

        Project project = projectRepository.findByKeyAndUser(projectKey, user)
                .orElseThrow(() -> ResourceNotFoundException.projectByKey(projectKey));

        return convertToDto(project);
    }

    /**
     * Retrieves all projects for a user with pagination.
     *
     * @param user the project owner
     * @param pageable pagination information
     * @return page of project DTOs
     */
    @Transactional(readOnly = true)
    public Page<ProjectDto> getProjects(User user, Pageable pageable) {
        logger.debug("Retrieving projects for user {} with pagination", user.getId());

        Page<Project> projects = projectRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return projects.map(this::convertToDto);
    }

    /**
     * Retrieves all projects for a user without pagination.
     *
     * @param user the project owner
     * @return list of project DTOs
     */
    @Transactional(readOnly = true)
    public List<ProjectDto> getAllProjects(User user) {
        logger.debug("Retrieving all projects for user {}", user.getId());

        List<Project> projects = projectRepository.findByUserOrderByCreatedAtDesc(user);
        return projects.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Searches projects by name with user isolation.
     *
     * @param user the project owner
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of matching project DTOs
     */
    @Transactional(readOnly = true)
    public Page<ProjectDto> searchProjects(User user, String searchTerm, Pageable pageable) {
        logger.debug("Searching projects for user {} with term '{}'", user.getId(), searchTerm);

        Page<Project> projects = projectRepository.findByUserAndNameContainingIgnoreCase(user, searchTerm, pageable);
        return projects.map(this::convertToDto);
    }

    /**
     * Deletes a project and all associated data.
     *
     * @param projectId the project ID
     * @param user the project owner
     * @throws ResourceNotFoundException if project not found or not owned by user
     */
    public void deleteProject(Long projectId, User user) {
        logger.debug("Deleting project {} for user {}", projectId, user.getId());

        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> ResourceNotFoundException.project(projectId));

        // Delete project (cascade will handle related entities)
        projectRepository.delete(project);

        logger.info("Deleted project '{}' (ID: {}) for user {}", 
                   project.getName(), project.getId(), user.getId());
    }

    /**
     * Gets project statistics for a user.
     *
     * @param user the project owner
     * @return project count
     */
    @Transactional(readOnly = true)
    public long getProjectCount(User user) {
        return projectRepository.countByUser(user);
    }

    /**
     * Validates if a project key is available for a user.
     *
     * @param projectKey the project key to validate
     * @param user the user
     * @return true if key is available
     */
    @Transactional(readOnly = true)
    public boolean isProjectKeyAvailable(String projectKey, User user) {
        return !projectRepository.existsByKeyAndUser(projectKey, user);
    }

    /**
     * Converts a Project entity to ProjectDto.
     *
     * @param project the project entity
     * @return the project DTO
     */
    private ProjectDto convertToDto(Project project) {
        ProjectDto dto = new ProjectDto(
                project.getId(),
                project.getName(),
                project.getKey(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );

        // Add issue count
        long issueCount = issueRepository.countByUserAndProject(project.getUser(), project);
        dto.setIssueCount(issueCount);

        return dto;
    }
}
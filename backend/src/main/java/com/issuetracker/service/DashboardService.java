package com.issuetracker.service;

import com.issuetracker.dto.DashboardMetricsDto;
import com.issuetracker.dto.SprintProgressDto;
import com.issuetracker.entity.*;
import com.issuetracker.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for dashboard metrics calculation and real-time data aggregation.
 * Provides comprehensive statistics for projects, issues, and sprints.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final SprintRepository sprintRepository;

    public DashboardService(ProjectRepository projectRepository,
                           IssueRepository issueRepository,
                           SprintRepository sprintRepository) {
        this.projectRepository = projectRepository;
        this.issueRepository = issueRepository;
        this.sprintRepository = sprintRepository;
    }

    /**
     * Calculates comprehensive dashboard metrics for a user.
     * Aggregates data from projects, issues, and sprints to provide real-time insights.
     *
     * @param user the user for whom to calculate metrics
     * @return comprehensive dashboard metrics
     */
    public DashboardMetricsDto calculateDashboardMetrics(User user) {
        logger.debug("Calculating dashboard metrics for user {}", user.getId());

        // Calculate project metrics
        DashboardMetricsDto.ProjectMetrics projectMetrics = calculateProjectMetrics(user);

        // Calculate issue metrics
        DashboardMetricsDto.IssueMetrics issueMetrics = calculateIssueMetrics(user);

        // Calculate sprint metrics
        DashboardMetricsDto.SprintMetrics sprintMetrics = calculateSprintMetrics(user);

        logger.info("Calculated dashboard metrics for user {}: {} projects, {} issues, {} sprints",
                   user.getId(), projectMetrics.getTotalProjects(), 
                   issueMetrics.getTotalIssues(), sprintMetrics.getTotalSprints());

        return new DashboardMetricsDto(projectMetrics, issueMetrics, sprintMetrics);
    }

    /**
     * Calculates project-related metrics.
     *
     * @param user the user
     * @return project metrics
     */
    private DashboardMetricsDto.ProjectMetrics calculateProjectMetrics(User user) {
        logger.debug("Calculating project metrics for user {}", user.getId());

        long totalProjects = projectRepository.countByUser(user);
        
        // Calculate active projects (projects with issues)
        List<Project> allProjects = projectRepository.findByUserOrderByCreatedAtDesc(user);
        long activeProjects = allProjects.stream()
                .mapToLong(project -> issueRepository.countByUserAndProject(user, project))
                .filter(count -> count > 0)
                .count();

        // Calculate issues per project
        Map<String, Long> issuesPerProject = allProjects.stream()
                .collect(Collectors.toMap(
                        project -> project.getName(),
                        project -> issueRepository.countByUserAndProject(user, project),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        return new DashboardMetricsDto.ProjectMetrics(totalProjects, activeProjects, issuesPerProject);
    }

    /**
     * Calculates issue-related metrics.
     *
     * @param user the user
     * @return issue metrics
     */
    private DashboardMetricsDto.IssueMetrics calculateIssueMetrics(User user) {
        logger.debug("Calculating issue metrics for user {}", user.getId());

        long totalIssues = issueRepository.countByUser(user);

        // Calculate issues by status
        Map<IssueStatus, Long> issuesByStatus = Arrays.stream(IssueStatus.values())
                .collect(Collectors.toMap(
                        status -> status,
                        status -> issueRepository.countByUserAndStatus(user, status),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        // Calculate issues by priority
        Map<Priority, Long> issuesByPriority = Arrays.stream(Priority.values())
                .collect(Collectors.toMap(
                        priority -> priority,
                        priority -> issueRepository.countByUserAndPriority(user, priority),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        long backlogIssues = issueRepository.countByUserAndStatus(user, IssueStatus.BACKLOG);
        long completedIssues = issueRepository.countByUserAndStatus(user, IssueStatus.DONE);

        // Epic metrics
        long totalEpics = issueRepository.countByUserAndParentIssueIsNull(user);
        long totalChildIssues = issueRepository.countByUserAndParentIssueIsNotNull(user);

        return new DashboardMetricsDto.IssueMetrics(
                totalIssues, issuesByStatus, issuesByPriority, backlogIssues, completedIssues, totalEpics, totalChildIssues);
    }

    /**
     * Calculates sprint-related metrics.
     *
     * @param user the user
     * @return sprint metrics
     */
    private DashboardMetricsDto.SprintMetrics calculateSprintMetrics(User user) {
        logger.debug("Calculating sprint metrics for user {}", user.getId());

        long totalSprints = sprintRepository.countByUser(user);

        // Calculate sprints by status
        Map<SprintStatus, Long> sprintsByStatus = Arrays.stream(SprintStatus.values())
                .collect(Collectors.toMap(
                        status -> status,
                        status -> sprintRepository.countByUserAndStatus(user, status),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        // Get active sprint progress
        SprintProgressDto activeSprint = calculateActiveSprintProgress(user);

        // Calculate average sprint completion rate
        double averageSprintCompletion = calculateAverageSprintCompletion(user);

        return new DashboardMetricsDto.SprintMetrics(
                totalSprints, sprintsByStatus, activeSprint, averageSprintCompletion);
    }

    /**
     * Calculates progress for the currently active sprint.
     *
     * @param user the user
     * @return active sprint progress or null if no active sprint
     */
    private SprintProgressDto calculateActiveSprintProgress(User user) {
        logger.debug("Calculating active sprint progress for user {}", user.getId());

        Optional<Sprint> activeSprintOpt = sprintRepository.findByUserAndStatus(user, SprintStatus.ACTIVE);
        if (activeSprintOpt.isEmpty()) {
            return null;
        }

        Sprint activeSprint = activeSprintOpt.get();
        List<Issue> sprintIssues = issueRepository.findByUserAndSprint(user, activeSprint);

        long totalIssues = sprintIssues.size();
        long completedIssues = sprintIssues.stream()
                .mapToLong(issue -> issue.getStatus() == IssueStatus.DONE ? 1 : 0)
                .sum();
        long inProgressIssues = sprintIssues.stream()
                .mapToLong(issue -> issue.getStatus() == IssueStatus.IN_PROGRESS ? 1 : 0)
                .sum();
        long remainingIssues = totalIssues - completedIssues;

        double completionPercentage = totalIssues > 0 ? (double) completedIssues / totalIssues * 100 : 0;

        // Calculate story points
        long totalStoryPoints = sprintIssues.stream()
                .mapToLong(issue -> issue.getStoryPoints() != null ? issue.getStoryPoints() : 0)
                .sum();
        long completedStoryPoints = sprintIssues.stream()
                .filter(issue -> issue.getStatus() == IssueStatus.DONE)
                .mapToLong(issue -> issue.getStoryPoints() != null ? issue.getStoryPoints() : 0)
                .sum();

        // Calculate days
        LocalDate today = LocalDate.now();
        int daysRemaining = Math.max(0, (int) ChronoUnit.DAYS.between(today, activeSprint.getEndDate()));
        int totalDays = (int) ChronoUnit.DAYS.between(activeSprint.getStartDate(), activeSprint.getEndDate());

        return new SprintProgressDto(
                activeSprint.getId(),
                activeSprint.getName(),
                activeSprint.getStartDate(),
                activeSprint.getEndDate(),
                totalIssues,
                completedIssues,
                inProgressIssues,
                remainingIssues,
                completionPercentage,
                totalStoryPoints,
                completedStoryPoints,
                daysRemaining,
                totalDays
        );
    }

    /**
     * Calculates the average completion rate across all completed sprints.
     *
     * @param user the user
     * @return average completion percentage
     */
    private double calculateAverageSprintCompletion(User user) {
        logger.debug("Calculating average sprint completion for user {}", user.getId());

        List<Sprint> completedSprints = sprintRepository.findByUserAndStatusOrderByCreatedAtDesc(user, SprintStatus.COMPLETED);
        
        if (completedSprints.isEmpty()) {
            return 0.0;
        }

        double totalCompletionRate = 0.0;
        int validSprints = 0;

        for (Sprint sprint : completedSprints) {
            List<Issue> sprintIssues = issueRepository.findByUserAndSprint(user, sprint);
            if (!sprintIssues.isEmpty()) {
                long completedIssues = sprintIssues.stream()
                        .mapToLong(issue -> issue.getStatus() == IssueStatus.DONE ? 1 : 0)
                        .sum();
                double completionRate = (double) completedIssues / sprintIssues.size() * 100;
                totalCompletionRate += completionRate;
                validSprints++;
            }
        }

        return validSprints > 0 ? totalCompletionRate / validSprints : 0.0;
    }

    /**
     * Gets real-time project statistics for a specific project.
     *
     * @param projectId the project ID
     * @param user the user
     * @return project statistics
     */
    public Map<String, Object> getProjectStatistics(Long projectId, User user) {
        logger.debug("Getting project statistics for project {} and user {}", projectId, user.getId());

        Optional<Project> projectOpt = projectRepository.findByIdAndUser(projectId, user);
        if (projectOpt.isEmpty()) {
            return Collections.emptyMap();
        }

        Project project = projectOpt.get();
        Map<String, Object> stats = new HashMap<>();

        // Basic counts
        long totalIssues = issueRepository.countByUserAndProject(user, project);
        stats.put("totalIssues", totalIssues);

        // Issues by status
        Map<IssueStatus, Long> issuesByStatus = Arrays.stream(IssueStatus.values())
                .collect(Collectors.toMap(
                        status -> status,
                        status -> issueRepository.countByUserAndStatus(user, status),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
        stats.put("issuesByStatus", issuesByStatus);

        // Issues by priority
        Map<Priority, Long> issuesByPriority = Arrays.stream(Priority.values())
                .collect(Collectors.toMap(
                        priority -> priority,
                        priority -> issueRepository.countByUserAndPriority(user, priority),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
        stats.put("issuesByPriority", issuesByPriority);

        logger.info("Retrieved project statistics for project {} and user {}: {} total issues",
                   projectId, user.getId(), totalIssues);

        return stats;
    }

    /**
     * Gets real-time sprint statistics for a specific sprint.
     *
     * @param sprintId the sprint ID
     * @param user the user
     * @return sprint statistics
     */
    public Map<String, Object> getSprintStatistics(Long sprintId, User user) {
        logger.debug("Getting sprint statistics for sprint {} and user {}", sprintId, user.getId());

        Optional<Sprint> sprintOpt = sprintRepository.findByIdAndUser(sprintId, user);
        if (sprintOpt.isEmpty()) {
            return Collections.emptyMap();
        }

        Sprint sprint = sprintOpt.get();
        List<Issue> sprintIssues = issueRepository.findByUserAndSprint(user, sprint);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalIssues", sprintIssues.size());

        // Issues by status within sprint
        Map<IssueStatus, Long> issuesByStatus = sprintIssues.stream()
                .collect(Collectors.groupingBy(Issue::getStatus, Collectors.counting()));
        stats.put("issuesByStatus", issuesByStatus);

        // Story points
        long totalStoryPoints = sprintIssues.stream()
                .mapToLong(issue -> issue.getStoryPoints() != null ? issue.getStoryPoints() : 0)
                .sum();
        long completedStoryPoints = sprintIssues.stream()
                .filter(issue -> issue.getStatus() == IssueStatus.DONE)
                .mapToLong(issue -> issue.getStoryPoints() != null ? issue.getStoryPoints() : 0)
                .sum();
        stats.put("totalStoryPoints", totalStoryPoints);
        stats.put("completedStoryPoints", completedStoryPoints);

        // Completion percentage
        double completionPercentage = sprintIssues.size() > 0 ? 
                (double) issuesByStatus.getOrDefault(IssueStatus.DONE, 0L) / sprintIssues.size() * 100 : 0;
        stats.put("completionPercentage", completionPercentage);

        logger.info("Retrieved sprint statistics for sprint {} and user {}: {} total issues, {}% complete",
                   sprintId, user.getId(), sprintIssues.size(), completionPercentage);

        return stats;
    }
}
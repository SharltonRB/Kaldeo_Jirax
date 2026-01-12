package com.issuetracker.entity;

/**
 * Enumeration representing the possible states of a project.
 * Projects follow a simple workflow based on their epic completion status.
 */
public enum ProjectStatus {
    /**
     * Project is active with work in progress.
     * This is the default state and indicates that not all epics are completed.
     */
    IN_PROGRESS,
    
    /**
     * Project is completed.
     * This state is automatically set when all epics in the project are DONE.
     */
    DONE
}
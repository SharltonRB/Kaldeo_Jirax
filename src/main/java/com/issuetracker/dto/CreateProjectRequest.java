package com.issuetracker.dto;

import com.issuetracker.validation.SafeProjectName;
import com.issuetracker.validation.ValidProjectKey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new project.
 * Contains validation rules for project creation.
 */
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(min = 1, max = 100, message = "Project name must be between 1 and 100 characters")
    @SafeProjectName
    private String name;

    @NotBlank(message = "Project key is required")
    @Size(min = 2, max = 10, message = "Project key must be between 2 and 10 characters")
    @ValidProjectKey
    private String key;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    // Constructors
    public CreateProjectRequest() {}

    public CreateProjectRequest(String name, String key, String description) {
        this.name = name;
        this.key = key;
        this.description = description;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CreateProjectRequest{" +
                "name='" + name + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
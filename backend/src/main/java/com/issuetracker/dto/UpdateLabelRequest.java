package com.issuetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing label.
 * Contains validation rules for label updates.
 */
public class UpdateLabelRequest {

    @NotBlank(message = "Label name is required")
    @Size(min = 1, max = 50, message = "Label name must be between 1 and 50 characters")
    private String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code")
    private String color;

    // Constructors
    public UpdateLabelRequest() {}

    public UpdateLabelRequest(String name, String color) {
        this.name = name;
        this.color = color;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "UpdateLabelRequest{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
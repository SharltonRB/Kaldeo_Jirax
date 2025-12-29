package com.issuetracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Objects;

/**
 * Data Transfer Object for Label entity.
 * Used for API responses and data transfer.
 */
public class LabelDto {

    private Long id;

    @NotBlank(message = "Label name is required")
    @Size(max = 50, message = "Label name must not exceed 50 characters")
    private String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code")
    private String color;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    // Constructors
    public LabelDto() {}

    public LabelDto(Long id, String name, String color, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabelDto labelDto = (LabelDto) o;
        return Objects.equals(id, labelDto.id) &&
               Objects.equals(name, labelDto.name) &&
               Objects.equals(color, labelDto.color) &&
               Objects.equals(createdAt, labelDto.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, color, createdAt);
    }

    @Override
    public String toString() {
        return "LabelDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
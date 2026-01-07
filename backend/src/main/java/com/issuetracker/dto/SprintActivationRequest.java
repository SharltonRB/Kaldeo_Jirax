package com.issuetracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * Data Transfer Object for Sprint activation request.
 * Used when activating a sprint with optional date updates.
 */
public class SprintActivationRequest {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate newStartDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate newEndDate;

    // Constructors
    public SprintActivationRequest() {}

    public SprintActivationRequest(LocalDate newStartDate, LocalDate newEndDate) {
        this.newStartDate = newStartDate;
        this.newEndDate = newEndDate;
    }

    // Getters and Setters
    public LocalDate getNewStartDate() {
        return newStartDate;
    }

    public void setNewStartDate(LocalDate newStartDate) {
        this.newStartDate = newStartDate;
    }

    public LocalDate getNewEndDate() {
        return newEndDate;
    }

    public void setNewEndDate(LocalDate newEndDate) {
        this.newEndDate = newEndDate;
    }

    @Override
    public String toString() {
        return "SprintActivationRequest{" +
                "newStartDate=" + newStartDate +
                ", newEndDate=" + newEndDate +
                '}';
    }
}
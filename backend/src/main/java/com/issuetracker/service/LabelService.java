package com.issuetracker.service;

import com.issuetracker.dto.CreateLabelRequest;
import com.issuetracker.dto.LabelDto;
import com.issuetracker.dto.UpdateLabelRequest;
import com.issuetracker.entity.Label;
import com.issuetracker.entity.User;
import com.issuetracker.exception.DuplicateResourceException;
import com.issuetracker.exception.ResourceNotFoundException;
import com.issuetracker.repository.LabelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing labels with user isolation.
 * Handles label CRUD operations and cross-project usage.
 */
@Service
@Transactional
public class LabelService {

    private static final Logger logger = LoggerFactory.getLogger(LabelService.class);

    private final LabelRepository labelRepository;

    public LabelService(LabelRepository labelRepository) {
        this.labelRepository = labelRepository;
    }

    /**
     * Creates a new label for the specified user.
     *
     * @param request the label creation request
     * @param user the label owner
     * @return the created label DTO
     * @throws DuplicateResourceException if label name already exists for the user
     */
    public LabelDto createLabel(CreateLabelRequest request, User user) {
        logger.debug("Creating label '{}' for user {}", request.getName(), user.getId());

        // Validate label name uniqueness within user scope
        if (labelRepository.existsByNameAndUser(request.getName(), user)) {
            throw DuplicateResourceException.labelName(request.getName());
        }

        // Create and save label
        Label label = new Label(user, request.getName(), request.getColor());
        Label savedLabel = labelRepository.save(label);

        logger.info("Created label '{}' with color '{}' for user {}", 
                   savedLabel.getName(), savedLabel.getColor(), user.getId());

        return convertToDto(savedLabel);
    }

    /**
     * Updates an existing label.
     *
     * @param labelId the label ID
     * @param request the update request
     * @param user the label owner
     * @return the updated label DTO
     * @throws ResourceNotFoundException if label not found or not owned by user
     * @throws DuplicateResourceException if new name already exists for the user
     */
    public LabelDto updateLabel(Long labelId, UpdateLabelRequest request, User user) {
        logger.debug("Updating label {} for user {}", labelId, user.getId());

        Label label = labelRepository.findByIdAndUser(labelId, user)
                .orElseThrow(() -> ResourceNotFoundException.label(labelId));

        // Check name uniqueness if name is changing
        if (!label.getName().equals(request.getName()) && 
            labelRepository.existsByNameAndUser(request.getName(), user)) {
            throw DuplicateResourceException.labelName(request.getName());
        }

        // Update label fields
        label.setName(request.getName());
        label.setColor(request.getColor());

        Label updatedLabel = labelRepository.save(label);

        logger.info("Updated label '{}' (ID: {}) for user {}", 
                   updatedLabel.getName(), updatedLabel.getId(), user.getId());

        return convertToDto(updatedLabel);
    }

    /**
     * Retrieves a label by ID with user isolation.
     *
     * @param labelId the label ID
     * @param user the label owner
     * @return the label DTO
     * @throws ResourceNotFoundException if label not found or not owned by user
     */
    @Transactional(readOnly = true)
    public LabelDto getLabel(Long labelId, User user) {
        logger.debug("Retrieving label {} for user {}", labelId, user.getId());

        Label label = labelRepository.findByIdAndUser(labelId, user)
                .orElseThrow(() -> ResourceNotFoundException.label(labelId));

        return convertToDto(label);
    }

    /**
     * Retrieves all labels for a user with pagination.
     *
     * @param user the label owner
     * @param pageable pagination information
     * @return page of label DTOs
     */
    @Transactional(readOnly = true)
    public Page<LabelDto> getLabels(User user, Pageable pageable) {
        logger.debug("Retrieving labels for user {} with pagination", user.getId());

        Page<Label> labels = labelRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return labels.map(this::convertToDto);
    }

    /**
     * Retrieves all labels for a user without pagination.
     *
     * @param user the label owner
     * @return list of label DTOs
     */
    @Transactional(readOnly = true)
    public List<LabelDto> getAllLabels(User user) {
        logger.debug("Retrieving all labels for user {}", user.getId());

        List<Label> labels = labelRepository.findByUserOrderByCreatedAtDesc(user);
        return labels.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Searches labels by name with user isolation.
     *
     * @param user the label owner
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of matching label DTOs
     */
    @Transactional(readOnly = true)
    public Page<LabelDto> searchLabels(User user, String searchTerm, Pageable pageable) {
        logger.debug("Searching labels for user {} with term '{}'", user.getId(), searchTerm);

        Page<Label> labels = labelRepository.findByUserAndNameContainingIgnoreCase(user, searchTerm, pageable);
        return labels.map(this::convertToDto);
    }

    /**
     * Retrieves labels by color for a user.
     *
     * @param user the label owner
     * @param color the label color
     * @return list of label DTOs
     */
    @Transactional(readOnly = true)
    public List<LabelDto> getLabelsByColor(User user, String color) {
        logger.debug("Retrieving labels with color '{}' for user {}", color, user.getId());

        List<Label> labels = labelRepository.findByUserAndColor(user, color);
        return labels.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves unused labels for cleanup purposes.
     *
     * @param user the label owner
     * @return list of unused label DTOs
     */
    @Transactional(readOnly = true)
    public List<LabelDto> getUnusedLabels(User user) {
        logger.debug("Retrieving unused labels for user {}", user.getId());

        List<Label> unusedLabels = labelRepository.findUnusedLabelsByUser(user);
        return unusedLabels.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a label and removes it from all associated issues.
     *
     * @param labelId the label ID
     * @param user the label owner
     * @throws ResourceNotFoundException if label not found or not owned by user
     */
    public void deleteLabel(Long labelId, User user) {
        logger.debug("Deleting label {} for user {}", labelId, user.getId());

        Label label = labelRepository.findByIdAndUser(labelId, user)
                .orElseThrow(() -> ResourceNotFoundException.label(labelId));

        // Delete label (cascade will handle issue associations)
        labelRepository.delete(label);

        logger.info("Deleted label '{}' (ID: {}) for user {}", 
                   label.getName(), label.getId(), user.getId());
    }

    /**
     * Gets label statistics for a user.
     *
     * @param user the label owner
     * @return label count
     */
    @Transactional(readOnly = true)
    public long getLabelCount(User user) {
        return labelRepository.countByUser(user);
    }

    /**
     * Validates if a label name is available for a user.
     *
     * @param labelName the label name to validate
     * @param user the user
     * @return true if name is available
     */
    @Transactional(readOnly = true)
    public boolean isLabelNameAvailable(String labelName, User user) {
        return !labelRepository.existsByNameAndUser(labelName, user);
    }

    /**
     * Converts a Label entity to LabelDto.
     *
     * @param label the label entity
     * @return the label DTO
     */
    private LabelDto convertToDto(Label label) {
        return new LabelDto(
                label.getId(),
                label.getName(),
                label.getColor(),
                label.getCreatedAt()
        );
    }
}
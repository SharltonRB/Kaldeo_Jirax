package com.issuetracker.repository;

import com.issuetracker.entity.Label;
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
 * Repository interface for Label entity operations.
 * Provides data access methods for label management with user isolation.
 */
@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {

    /**
     * Finds all labels owned by a specific user with pagination.
     *
     * @param user the label owner
     * @param pageable pagination information
     * @return page of labels
     */
    Page<Label> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Finds all labels owned by a specific user.
     *
     * @param user the label owner
     * @return list of labels
     */
    List<Label> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Finds a label by ID and user for data isolation.
     *
     * @param id label ID
     * @param user label owner
     * @return optional label
     */
    Optional<Label> findByIdAndUser(Long id, User user);

    /**
     * Finds a label by name and user for uniqueness validation.
     *
     * @param name label name
     * @param user label owner
     * @return optional label
     */
    Optional<Label> findByNameAndUser(String name, User user);

    /**
     * Checks if a label name exists for a specific user.
     *
     * @param name label name
     * @param user label owner
     * @return true if name exists
     */
    boolean existsByNameAndUser(String name, User user);

    /**
     * Counts total labels for a user.
     *
     * @param user label owner
     * @return label count
     */
    long countByUser(User user);

    /**
     * Finds labels by name containing search term (case-insensitive).
     *
     * @param user the label owner
     * @param searchTerm search term
     * @param pageable pagination information
     * @return page of matching labels
     */
    @Query("SELECT l FROM Label l WHERE l.user = :user AND " +
           "LOWER(l.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY l.createdAt DESC")
    Page<Label> findByUserAndNameContainingIgnoreCase(@Param("user") User user, 
                                                      @Param("searchTerm") String searchTerm, 
                                                      Pageable pageable);

    /**
     * Finds labels by color for a user.
     *
     * @param user the label owner
     * @param color label color
     * @return list of labels with the specified color
     */
    List<Label> findByUserAndColor(User user, String color);

    /**
     * Finds labels that are not used by any issues for cleanup.
     *
     * @param user the label owner
     * @return list of unused labels
     */
    @Query("SELECT l FROM Label l WHERE l.user = :user AND l.issues IS EMPTY")
    List<Label> findUnusedLabelsByUser(@Param("user") User user);

    /**
     * Deletes a label by ID and user for data isolation.
     *
     * @param id label ID
     * @param user label owner
     */
    void deleteByIdAndUser(Long id, User user);
}
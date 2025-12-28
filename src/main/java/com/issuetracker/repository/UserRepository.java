package com.issuetracker.repository;

import com.issuetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Provides data access methods for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds user by email address.
     *
     * @param email user email
     * @return optional user
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if user exists by email.
     *
     * @param email user email
     * @return true if user exists
     */
    boolean existsByEmail(String email);
}
package com.issuetracker.base;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for fast integration tests using H2 database.
 * 
 * Features:
 * - H2 in-memory database (fast, no external dependencies)
 * - Perfect for local development and rapid feedback
 * - Automatic rollback after each test
 * 
 * Use this for:
 * - Daily development testing
 * - Property-based tests
 * - Unit and integration tests
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BasePostgreSQLTest {
    // Configuration handled by application-test.yml
}
package com.issuetracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for the main application class.
 * Verifies that the Spring Boot application context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class PersonalIssueTrackerApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring Boot application context loads successfully
        // with all configurations and dependencies properly wired
    }
}
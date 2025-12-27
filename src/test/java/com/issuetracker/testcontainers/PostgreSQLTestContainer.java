package com.issuetracker.testcontainers;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared PostgreSQL test container for integration tests.
 * Uses singleton pattern to reuse container across test classes.
 */
public class PostgreSQLTestContainer {
    
    private static final String POSTGRES_IMAGE = "postgres:15-alpine";
    private static final String DATABASE_NAME = "issue_tracker_test";
    private static final String USERNAME = "test";
    private static final String PASSWORD = "test";
    
    private static PostgreSQLContainer<?> container;
    
    public static PostgreSQLContainer<?> getInstance() {
        if (container == null) {
            container = new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE))
                    .withDatabaseName(DATABASE_NAME)
                    .withUsername(USERNAME)
                    .withPassword(PASSWORD)
                    .withReuse(true);
        }
        return container;
    }
    
    public static void start() {
        getInstance().start();
    }
    
    public static void stop() {
        if (container != null) {
            container.stop();
        }
    }
}
package com.issuetracker.base;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.util.logging.Logger;

/**
 * Base class for integration tests with Testcontainers PostgreSQL.
 * 
 * Use this class when you need production parity with PostgreSQL.
 * Requires Docker to be available.
 * 
 * Usage:
 * - CI/CD pipelines: Always use this for production parity
 * - Local development: Use when testing PostgreSQL-specific features
 * 
 * To run tests with Testcontainers:
 * mvn test -Dspring.profiles.active=testcontainers
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("testcontainers")
@Transactional
public abstract class BaseTestcontainersTest {

    private static final Logger logger = Logger.getLogger(BaseTestcontainersTest.class.getName());

    static {
        // Configure Docker BEFORE Testcontainers tries to connect
        configureDockerForMacOS();
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @BeforeAll
    static void setupDocker() {
        // Verify connection
        try {
            logger.fine("Docker Host: " + System.getProperty("DOCKER_HOST"));
            logger.info("PostgreSQL Container Started: " + postgres.getJdbcUrl());
        } catch (Exception e) {
            logger.severe("Error starting containers: " + e.getMessage());
            throw e;
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        
        logger.fine("Using Testcontainers PostgreSQL: " + postgres.getJdbcUrl());
    }

    /**
     * Configure Docker socket for macOS Docker Desktop compatibility.
     * This fixes the "Could not find a valid Docker environment" issue on macOS.
     */
    private static void configureDockerForMacOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("mac")) {
            logger.fine("Detected macOS - Configuring Docker socket");
            
            // First try with the standard symlink location
            String dockerHost = "unix:///var/run/docker.sock";
            
            // If it doesn't exist, try other locations
            String homeDir = System.getProperty("user.home");
            String[] socketPaths = {
                "/var/run/docker.sock",
                homeDir + "/.docker/run/docker.sock",
                homeDir + "/Library/Containers/com.docker.docker/Data/docker.sock"
            };
            
            for (String socketPath : socketPaths) {
                File socket = new File(socketPath);
                if (socket.exists()) {
                    dockerHost = "unix://" + socketPath;
                    logger.fine("Found Docker socket at: " + socketPath);
                    break;
                }
            }
            
            System.setProperty("DOCKER_HOST", dockerHost);
            System.setProperty("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", dockerHost.replace("unix://", ""));
            
            // Additional configuration for Testcontainers
            System.setProperty("testcontainers.reuse.enable", "true");
        }
    }
}
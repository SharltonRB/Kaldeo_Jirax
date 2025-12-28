package com.issuetracker.base;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        
        System.out.println("✅ Using Testcontainers PostgreSQL: " + postgres.getJdbcUrl());
    }
}
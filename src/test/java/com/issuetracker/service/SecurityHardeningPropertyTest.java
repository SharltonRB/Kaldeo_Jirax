package com.issuetracker.service;

import com.issuetracker.base.BasePostgreSQLTest;
import com.issuetracker.dto.CreateIssueRequest;
import com.issuetracker.dto.CreateProjectRequest;
import com.issuetracker.dto.ErrorResponse;
import com.issuetracker.dto.LoginRequest;
import com.issuetracker.entity.Priority;
import com.issuetracker.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quicktheories.QuickTheory;
import org.quicktheories.core.Gen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quicktheories.generators.SourceDSL.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Property-based tests for security hardening functionality.
 * Tests Property 12 from the design document.
 * Uses Testcontainers with PostgreSQL for production parity and full isolation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityHardeningPropertyTest extends BasePostgreSQLTest {

    // QuickTheory instance for property testing
    private static final QuickTheory qt = QuickTheory.qt();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    // Counter to ensure unique emails across test iterations
    private static final AtomicInteger emailCounter = new AtomicInteger(0);

    private User testUser;
    private String validJwtToken;

    @BeforeEach
    void setUp() {
        // Create a test user for authenticated requests
        String email = "security-test-" + UUID.randomUUID() + "@example.com";
        testUser = userService.registerUser(email, "testPassword123", "Security Test User");
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        validJwtToken = jwtService.generateToken(userDetails);
    }

    /**
     * Property 12: Security Hardening
     * For any API access, the system should require valid JWT tokens for protected endpoints,
     * prevent SQL injection through parameterized queries, and return consistent error responses
     * without exposing sensitive information.
     * 
     * Feature: personal-issue-tracker, Property 12: Security Hardening
     * Validates: Requirements 9.3, 9.4, 9.5
     */
    @Test
    @Transactional
    void securityHardeningProperty() {
        qt.withFixedSeed(98765L) // Use fixed seed for reproducible tests
                .withExamples(5) // Small number for fast execution
                .forAll(
                        maliciousInputs(),
                        validProjectNames(),
                        validIssueData()
                )
                .checkAssert((maliciousInput, projectName, issueData) -> {
                    try {
                        // Test 1: Protected endpoints require valid JWT tokens
                        testProtectedEndpointRequiresValidToken(projectName);
                        
                        // Test 2: Invalid tokens are rejected consistently
                        testInvalidTokensRejectedConsistently(projectName);
                        
                        // Test 3: SQL injection attempts are prevented
                        testSqlInjectionPrevention(maliciousInput, projectName);
                        
                        // Test 4: Error responses don't expose sensitive information
                        testErrorResponsesDoNotExposeSensitiveInfo(maliciousInput);
                        
                        // Test 5: Consistent error format across all endpoints
                        testConsistentErrorFormat(issueData);
                        
                    } catch (Exception e) {
                        // If any unexpected exception occurs, fail the test with details
                        throw new AssertionError("Unexpected exception during security hardening test: " + e.getMessage(), e);
                    }
                });
    }

    private void testProtectedEndpointRequiresValidToken(String projectName) throws Exception {
        CreateProjectRequest request = new CreateProjectRequest(projectName, "TEST", "Test project");
        String requestJson = objectMapper.writeValueAsString(request);

        // Request without token should be rejected with 403 (Forbidden) - Spring Security default
        MvcResult result = mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isForbidden())
                .andReturn();

        // Verify error response format - Spring Security returns empty body for 403
        String responseBody = result.getResponse().getContentAsString();
        if (!responseBody.isEmpty()) {
            ErrorResponse errorResponse = objectMapper.readValue(responseBody, ErrorResponse.class);
            assertThat(errorResponse.getMessage()).doesNotContain("password", "secret", "key", "token");
        }
    }

    private void testInvalidTokensRejectedConsistently(String projectName) throws Exception {
        CreateProjectRequest request = new CreateProjectRequest(projectName, "TEST", "Test project");
        String requestJson = objectMapper.writeValueAsString(request);

        // Test various invalid token formats
        String[] invalidTokens = {
            "invalid-token",
            "Bearer invalid",
            "Bearer " + validJwtToken + "tampered",
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature",
            ""
        };

        for (String invalidToken : invalidTokens) {
            MvcResult result = mockMvc.perform(post("/api/projects")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", invalidToken)
                    .content(requestJson))
                    .andReturn();

            // Should return either 401 (Unauthorized) or 403 (Forbidden) depending on token format
            int status = result.getResponse().getStatus();
            assertThat(status).isIn(401, 403);

            // Verify response doesn't expose sensitive information
            String responseBody = result.getResponse().getContentAsString();
            if (!responseBody.isEmpty()) {
                // Only parse if there's a response body
                try {
                    ErrorResponse errorResponse = objectMapper.readValue(responseBody, ErrorResponse.class);
                    assertThat(errorResponse.getMessage()).doesNotContain("password", "secret", "key", "JWT");
                } catch (Exception e) {
                    // If parsing fails, just check the raw response doesn't contain sensitive info
                    assertThat(responseBody.toLowerCase()).doesNotContain("password", "secret", "key", "jwt");
                }
            }
        }
    }

    private void testSqlInjectionPrevention(String maliciousInput, String projectName) throws Exception {
        // Test SQL injection in project creation
        CreateProjectRequest projectRequest = new CreateProjectRequest(
            projectName + maliciousInput, 
            "TEST", 
            "Description with " + maliciousInput
        );
        String requestJson = objectMapper.writeValueAsString(projectRequest);

        MvcResult result = mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + validJwtToken)
                .content(requestJson))
                .andReturn();

        // Should either succeed (if input is valid) or fail with validation error (not SQL error)
        int status = result.getResponse().getStatus();
        if (status != 201) { // If not successful creation
            assertThat(status).isIn(400, 422); // Should be validation error, not 500 (SQL error)
            
            String responseBody = result.getResponse().getContentAsString();
            ErrorResponse errorResponse = objectMapper.readValue(responseBody, ErrorResponse.class);
            
            // Error should not contain SQL-related information
            assertThat(errorResponse.getMessage().toLowerCase())
                .doesNotContain("sql", "database", "constraint", "foreign key", "primary key");
        }
    }

    private void testErrorResponsesDoNotExposeSensitiveInfo(String maliciousInput) throws Exception {
        // Test with malicious login attempt
        LoginRequest loginRequest = new LoginRequest(
            "nonexistent" + maliciousInput + "@example.com", 
            "wrongpassword" + maliciousInput
        );
        String requestJson = objectMapper.writeValueAsString(loginRequest);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andReturn();

        // Should return either 401 or 400 depending on validation
        int status = result.getResponse().getStatus();
        assertThat(status).isIn(400, 401);

        String responseBody = result.getResponse().getContentAsString();
        if (!responseBody.isEmpty()) {
            try {
                ErrorResponse errorResponse = objectMapper.readValue(responseBody, ErrorResponse.class);
                
                // Error response should not expose sensitive information
                String message = errorResponse.getMessage().toLowerCase();
                assertThat(message).doesNotContain(
                    "password", "hash", "bcrypt", "secret", "key", "database", 
                    "sql", "table", "column", "constraint", "stack", "exception"
                );
                
                // Should have consistent error structure
                assertThat(errorResponse.getTimestamp()).isNotNull();
                assertThat(errorResponse.getPath()).isNotNull();
            } catch (Exception e) {
                // If parsing fails, just check the raw response doesn't contain sensitive info
                assertThat(responseBody.toLowerCase()).doesNotContain(
                    "password", "hash", "bcrypt", "secret", "key", "database", 
                    "sql", "table", "column", "constraint", "stack", "exception"
                );
            }
        }
    }

    private void testConsistentErrorFormat(IssueTestData issueData) throws Exception {
        // Test with invalid issue creation to verify consistent error format
        CreateIssueRequest invalidRequest = new CreateIssueRequest(
            issueData.title,
            issueData.description,
            issueData.priority,
            999999L, // Non-existent project ID
            1L // Assuming issue type 1 exists
        );
        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        MvcResult result = mockMvc.perform(post("/api/issues")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + validJwtToken)
                .content(requestJson))
                .andReturn();

        // Should return error (not found or validation error)
        int status = result.getResponse().getStatus();
        assertThat(status).isIn(400, 404, 422);

        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(responseBody, ErrorResponse.class);

        // Verify consistent error response structure
        assertThat(errorResponse.getCode()).isNotNull();
        assertThat(errorResponse.getMessage()).isNotNull();
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getPath()).isNotNull();
        
        // Error message should not expose internal details
        String message = errorResponse.getMessage().toLowerCase();
        assertThat(message).doesNotContain(
            "hibernate", "jpa", "sql", "database", "constraint", "foreign key",
            "stack", "exception", "internal", "server error"
        );
    }

    // Generators for property testing - optimized for speed

    private Gen<String> maliciousInputs() {
        return strings().ascii()
                .ofLengthBetween(1, 20)
                .map(s -> {
                    // Return different malicious patterns based on string content
                    int hash = Math.abs(s.hashCode()) % 6;
                    return switch (hash) {
                        case 0 -> "'; DROP TABLE users; --";
                        case 1 -> "' OR '1'='1";
                        case 2 -> "<script>alert('xss')</script>";
                        case 3 -> "../../../etc/passwd";
                        case 4 -> "; rm -rf /";
                        default -> "'\"\\;--";
                    };
                });
    }

    private Gen<String> validProjectNames() {
        return strings().ascii()
                .ofLengthBetween(3, 10) // Short for fast generation
                .map(s -> s.replaceAll("[^a-zA-Z0-9 ]", "a")) // Replace special chars with 'a'
                .map(s -> s.trim().isEmpty() ? "TestProject" : s.trim()); // Ensure not empty
    }

    private Gen<IssueTestData> validIssueData() {
        return integers().between(0, 3)
                .zip(strings().ascii().ofLengthBetween(3, 10),
                     strings().ascii().ofLengthBetween(5, 20),
                     (priorityIndex, title, description) -> {
                         Priority[] priorities = Priority.values();
                         Priority priority = priorities[priorityIndex % priorities.length];
                         
                         String cleanTitle = title.replaceAll("[^a-zA-Z0-9 ]", "a").trim();
                         if (cleanTitle.isEmpty()) cleanTitle = "Test Issue";
                         
                         String cleanDescription = description.replaceAll("[^a-zA-Z0-9 ]", "a").trim();
                         if (cleanDescription.isEmpty()) cleanDescription = "Test Description";
                         
                         return new IssueTestData(cleanTitle, cleanDescription, priority);
                     });
    }

    // Helper class for issue test data
    private static class IssueTestData {
        final String title;
        final String description;
        final Priority priority;

        IssueTestData(String title, String description, Priority priority) {
            this.title = title;
            this.description = description;
            this.priority = priority;
        }
    }
}
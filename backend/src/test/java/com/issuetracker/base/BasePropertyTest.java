package com.issuetracker.base;

import org.quicktheories.QuickTheory;

/**
 * Base class for property-based tests using QuickTheories.
 * Provides common configuration and utilities for property testing.
 */
public abstract class BasePropertyTest {

    /**
     * QuickTheory instance configured for development speed.
     * Uses fewer iterations for faster feedback during development.
     * For CI/production, use system property quicktheories.examples to override.
     */
    protected static final QuickTheory qt = QuickTheory.qt()
        .withExamples(getPropertyTestIterations())
        .withShrinkCycles(50); // Reduce shrink cycles for speed

    /**
     * Number of iterations for property tests.
     * Can be overridden with system property quicktheories.examples
     */
    protected static final int PROPERTY_TEST_ITERATIONS = getPropertyTestIterations();
    
    private static int getPropertyTestIterations() {
        String examples = System.getProperty("quicktheories.examples");
        if (examples != null) {
            try {
                return Integer.parseInt(examples);
            } catch (NumberFormatException e) {
                // Fall back to default
            }
        }
        // Default to 25 for development, can be overridden for CI
        return 25;
    }
}
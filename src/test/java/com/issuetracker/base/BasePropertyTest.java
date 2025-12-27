package com.issuetracker.base;

import org.quicktheories.QuickTheory;

/**
 * Base class for property-based tests using QuickTheories.
 * Provides common configuration and utilities for property testing.
 */
public abstract class BasePropertyTest {

    /**
     * QuickTheory instance configured for comprehensive testing.
     * Uses 100 iterations minimum as specified in the design document.
     */
    protected static final QuickTheory qt = QuickTheory.qt();

    /**
     * Number of iterations for property tests.
     * Minimum 100 as per testing strategy requirements.
     */
    protected static final int PROPERTY_TEST_ITERATIONS = 100;
}
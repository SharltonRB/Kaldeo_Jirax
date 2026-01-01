/**
 * Custom validation annotations and validators for business rules.
 * 
 * This package contains custom Jakarta Bean Validation annotations and their
 * corresponding validators that enforce business rules beyond basic field validation.
 * 
 * Custom validators include:
 * - Sprint date validation (end date after start date, reasonable duration)
 * - Project key validation (format rules, reserved words)
 * - Story points validation (Fibonacci sequence values)
 * 
 * These validators work in conjunction with the standard Jakarta validation
 * annotations to provide comprehensive input validation across all DTOs.
 */
package com.issuetracker.validation;
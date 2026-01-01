import { IssueStatus, SprintStatus } from '@/types';

/**
 * Validation utilities for form inputs and API data
 */

/**
 * Validates email format
 */
export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

/**
 * Validates password strength
 */
export const isValidPassword = (password: string): {
  isValid: boolean;
  errors: string[];
} => {
  const errors: string[] = [];
  
  if (password.length < 8) {
    errors.push('Password must be at least 8 characters long');
  }
  
  if (!/[A-Z]/.test(password)) {
    errors.push('Password must contain at least one uppercase letter');
  }
  
  if (!/[a-z]/.test(password)) {
    errors.push('Password must contain at least one lowercase letter');
  }
  
  if (!/\d/.test(password)) {
    errors.push('Password must contain at least one number');
  }
  
  return {
    isValid: errors.length === 0,
    errors,
  };
};

/**
 * Validates project key format (uppercase letters and numbers, 2-10 chars)
 */
export const isValidProjectKey = (key: string): boolean => {
  const keyRegex = /^[A-Z0-9]{2,10}$/;
  return keyRegex.test(key);
};

/**
 * Validates project name
 */
export const isValidProjectName = (name: string): boolean => {
  return name.trim().length >= 2 && name.trim().length <= 100;
};

/**
 * Validates issue title
 */
export const isValidIssueTitle = (title: string): boolean => {
  return title.trim().length >= 3 && title.trim().length <= 200;
};

/**
 * Validates story points (must be positive integer or Fibonacci number)
 */
export const isValidStoryPoints = (points: number): boolean => {
  const fibonacciNumbers = [1, 2, 3, 5, 8, 13, 21, 34, 55, 89];
  return Number.isInteger(points) && points > 0 && fibonacciNumbers.includes(points);
};

/**
 * Validates sprint dates (end date must be after start date)
 */
export const isValidSprintDates = (startDate: string, endDate: string): {
  isValid: boolean;
  error?: string;
} => {
  const start = new Date(startDate);
  const end = new Date(endDate);
  
  if (isNaN(start.getTime()) || isNaN(end.getTime())) {
    return { isValid: false, error: 'Invalid date format' };
  }
  
  if (end <= start) {
    return { isValid: false, error: 'End date must be after start date' };
  }
  
  const diffInDays = (end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24);
  if (diffInDays > 30) {
    return { isValid: false, error: 'Sprint duration cannot exceed 30 days' };
  }
  
  return { isValid: true };
};

/**
 * Validates label name
 */
export const isValidLabelName = (name: string): boolean => {
  return name.trim().length >= 1 && name.trim().length <= 50;
};

/**
 * Validates hex color format
 */
export const isValidHexColor = (color: string): boolean => {
  const hexRegex = /^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$/;
  return hexRegex.test(color);
};

/**
 * Validates comment content
 */
export const isValidCommentContent = (content: string): boolean => {
  return content.trim().length >= 1 && content.trim().length <= 2000;
};

/**
 * Validates issue status transition
 */
export const isValidStatusTransition = (
  currentStatus: IssueStatus,
  newStatus: IssueStatus
): boolean => {
  const validTransitions: Record<IssueStatus, IssueStatus[]> = {
    [IssueStatus.BACKLOG]: [
      IssueStatus.SELECTED_FOR_DEVELOPMENT,
      IssueStatus.IN_PROGRESS, // Direct transition allowed
    ],
    [IssueStatus.SELECTED_FOR_DEVELOPMENT]: [
      IssueStatus.BACKLOG,
      IssueStatus.IN_PROGRESS,
    ],
    [IssueStatus.IN_PROGRESS]: [
      IssueStatus.SELECTED_FOR_DEVELOPMENT,
      IssueStatus.IN_REVIEW,
      IssueStatus.DONE, // Direct transition allowed for simple tasks
    ],
    [IssueStatus.IN_REVIEW]: [
      IssueStatus.IN_PROGRESS,
      IssueStatus.DONE,
    ],
    [IssueStatus.DONE]: [
      IssueStatus.IN_REVIEW, // Reopening allowed
      IssueStatus.IN_PROGRESS, // Direct reopening allowed
    ],
  };
  
  return validTransitions[currentStatus]?.includes(newStatus) || false;
};

/**
 * Validates sprint status transition
 */
export const isValidSprintStatusTransition = (
  currentStatus: SprintStatus,
  newStatus: SprintStatus
): boolean => {
  const validTransitions: Record<SprintStatus, SprintStatus[]> = {
    [SprintStatus.PLANNED]: [SprintStatus.ACTIVE],
    [SprintStatus.ACTIVE]: [SprintStatus.COMPLETED],
    [SprintStatus.COMPLETED]: [], // No transitions from completed
  };
  
  return validTransitions[currentStatus]?.includes(newStatus) || false;
};

/**
 * Sanitizes user input to prevent XSS
 */
export const sanitizeInput = (input: string): string => {
  return input
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;')
    .replace(/\//g, '&#x2F;');
};

/**
 * Validates file upload (for future use)
 */
export const isValidFileUpload = (file: File, maxSizeMB: number = 5): {
  isValid: boolean;
  error?: string;
} => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'application/pdf'];
  
  if (!allowedTypes.includes(file.type)) {
    return { 
      isValid: false, 
      error: 'File type not allowed. Please upload JPEG, PNG, GIF, or PDF files.' 
    };
  }
  
  const maxSizeBytes = maxSizeMB * 1024 * 1024;
  if (file.size > maxSizeBytes) {
    return { 
      isValid: false, 
      error: `File size must be less than ${maxSizeMB}MB` 
    };
  }
  
  return { isValid: true };
};

/**
 * Validates URL format
 */
export const isValidUrl = (url: string): boolean => {
  try {
    new URL(url);
    return true;
  } catch {
    return false;
  }
};

/**
 * Validates that a value is within a numeric range
 */
export const isInRange = (value: number, min: number, max: number): boolean => {
  return value >= min && value <= max;
};

/**
 * Validates that a string contains only alphanumeric characters and spaces
 */
export const isAlphanumericWithSpaces = (str: string): boolean => {
  const regex = /^[a-zA-Z0-9\s]+$/;
  return regex.test(str);
};

/**
 * Validates that a string is not empty after trimming
 */
export const isNotEmpty = (str: string): boolean => {
  return str.trim().length > 0;
};

/**
 * Validates that an array is not empty
 */
export const isNotEmptyArray = <T>(arr: T[]): boolean => {
  return Array.isArray(arr) && arr.length > 0;
};
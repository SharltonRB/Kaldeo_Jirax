/**
 * Application constants and configuration
 */

// API Configuration
export const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_BASE_URL || '/api',
  TIMEOUT: 10000,
  RETRY_ATTEMPTS: 1,
} as const;

// Authentication
export const AUTH_CONFIG = {
  TOKEN_KEY: 'access_token',
  REFRESH_TOKEN_KEY: 'refresh_token',
  TOKEN_EXPIRY_BUFFER: 5 * 60 * 1000, // 5 minutes before expiry
} as const;

// Pagination
export const PAGINATION_CONFIG = {
  DEFAULT_PAGE_SIZE: 20,
  MAX_PAGE_SIZE: 100,
  PAGE_SIZE_OPTIONS: [10, 20, 50, 100],
} as const;

// UI Configuration
export const UI_CONFIG = {
  DEBOUNCE_DELAY: 300,
  THROTTLE_DELAY: 1000,
  TOAST_DURATION: 5000,
  MODAL_ANIMATION_DURATION: 200,
} as const;

// Issue Configuration
export const ISSUE_CONFIG = {
  MAX_TITLE_LENGTH: 200,
  MAX_DESCRIPTION_LENGTH: 5000,
  FIBONACCI_STORY_POINTS: [1, 2, 3, 5, 8, 13, 21, 34, 55, 89],
  STATUS_COLORS: {
    BACKLOG: '#6b7280',
    SELECTED_FOR_DEVELOPMENT: '#3b82f6',
    IN_PROGRESS: '#f59e0b',
    IN_REVIEW: '#8b5cf6',
    DONE: '#10b981',
  },
  PRIORITY_COLORS: {
    LOW: '#10b981',
    MEDIUM: '#f59e0b',
    HIGH: '#ef4444',
    CRITICAL: '#dc2626',
  },
} as const;

// Project Configuration
export const PROJECT_CONFIG = {
  MAX_NAME_LENGTH: 100,
  MAX_DESCRIPTION_LENGTH: 1000,
  KEY_MIN_LENGTH: 2,
  KEY_MAX_LENGTH: 10,
  KEY_PATTERN: /^[A-Z0-9]{2,10}$/,
} as const;

// Sprint Configuration
export const SPRINT_CONFIG = {
  MAX_NAME_LENGTH: 100,
  MAX_DURATION_DAYS: 30,
  MIN_DURATION_DAYS: 1,
  STATUS_COLORS: {
    PLANNED: '#6b7280',
    ACTIVE: '#10b981',
    COMPLETED: '#3b82f6',
  },
} as const;

// Label Configuration
export const LABEL_CONFIG = {
  MAX_NAME_LENGTH: 50,
  DEFAULT_COLORS: [
    '#ef4444', // Red
    '#f97316', // Orange
    '#f59e0b', // Amber
    '#eab308', // Yellow
    '#84cc16', // Lime
    '#22c55e', // Green
    '#10b981', // Emerald
    '#14b8a6', // Teal
    '#06b6d4', // Cyan
    '#0ea5e9', // Sky
    '#3b82f6', // Blue
    '#6366f1', // Indigo
    '#8b5cf6', // Violet
    '#a855f7', // Purple
    '#d946ef', // Fuchsia
    '#ec4899', // Pink
    '#f43f5e', // Rose
  ],
} as const;

// Comment Configuration
export const COMMENT_CONFIG = {
  MAX_CONTENT_LENGTH: 2000,
  MIN_CONTENT_LENGTH: 1,
} as const;

// File Upload Configuration
export const FILE_CONFIG = {
  MAX_SIZE_MB: 5,
  ALLOWED_TYPES: ['image/jpeg', 'image/png', 'image/gif', 'application/pdf'],
  ALLOWED_EXTENSIONS: ['.jpg', '.jpeg', '.png', '.gif', '.pdf'],
} as const;

// Cache Configuration
export const CACHE_CONFIG = {
  STALE_TIME: 5 * 60 * 1000, // 5 minutes
  CACHE_TIME: 10 * 60 * 1000, // 10 minutes
  BACKGROUND_REFETCH_INTERVAL: 30 * 60 * 1000, // 30 minutes
} as const;

// Error Messages
export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Network error. Please check your connection and try again.',
  UNAUTHORIZED: 'You are not authorized to perform this action.',
  FORBIDDEN: 'Access denied. You do not have permission to access this resource.',
  NOT_FOUND: 'The requested resource was not found.',
  VALIDATION_ERROR: 'Please check your input and try again.',
  SERVER_ERROR: 'An unexpected error occurred. Please try again later.',
  TIMEOUT_ERROR: 'Request timed out. Please try again.',
} as const;

// Success Messages
export const SUCCESS_MESSAGES = {
  PROJECT_CREATED: 'Project created successfully!',
  PROJECT_UPDATED: 'Project updated successfully!',
  PROJECT_DELETED: 'Project deleted successfully!',
  ISSUE_CREATED: 'Issue created successfully!',
  ISSUE_UPDATED: 'Issue updated successfully!',
  ISSUE_DELETED: 'Issue deleted successfully!',
  SPRINT_CREATED: 'Sprint created successfully!',
  SPRINT_UPDATED: 'Sprint updated successfully!',
  SPRINT_STARTED: 'Sprint started successfully!',
  SPRINT_COMPLETED: 'Sprint completed successfully!',
  LABEL_CREATED: 'Label created successfully!',
  LABEL_UPDATED: 'Label updated successfully!',
  LABEL_DELETED: 'Label deleted successfully!',
  COMMENT_CREATED: 'Comment added successfully!',
  COMMENT_UPDATED: 'Comment updated successfully!',
  COMMENT_DELETED: 'Comment deleted successfully!',
  LOGIN_SUCCESS: 'Welcome back!',
  REGISTER_SUCCESS: 'Account created successfully!',
  LOGOUT_SUCCESS: 'You have been logged out successfully.',
} as const;

// Local Storage Keys
export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'access_token',
  REFRESH_TOKEN: 'refresh_token',
  USER_PREFERENCES: 'user_preferences',
  THEME: 'theme',
  SIDEBAR_COLLAPSED: 'sidebar_collapsed',
  RECENT_PROJECTS: 'recent_projects',
  RECENT_ISSUES: 'recent_issues',
} as const;

// Route Paths
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  DASHBOARD: '/dashboard',
  PROJECTS: '/projects',
  PROJECT_DETAIL: '/projects/:id',
  ISSUES: '/issues',
  ISSUE_DETAIL: '/issues/:id',
  SPRINTS: '/sprints',
  SPRINT_DETAIL: '/sprints/:id',
  KANBAN: '/kanban',
  PROFILE: '/profile',
  SETTINGS: '/settings',
} as const;

// Theme Configuration
export const THEME_CONFIG = {
  LIGHT: 'light',
  DARK: 'dark',
  SYSTEM: 'system',
} as const;

// Animation Durations (in milliseconds)
export const ANIMATION_DURATIONS = {
  FAST: 150,
  NORMAL: 300,
  SLOW: 500,
} as const;

// Breakpoints (matching Tailwind CSS)
export const BREAKPOINTS = {
  SM: 640,
  MD: 768,
  LG: 1024,
  XL: 1280,
  '2XL': 1536,
} as const;

// Z-Index Layers
export const Z_INDEX = {
  DROPDOWN: 1000,
  STICKY: 1020,
  FIXED: 1030,
  MODAL_BACKDROP: 1040,
  MODAL: 1050,
  POPOVER: 1060,
  TOOLTIP: 1070,
  TOAST: 1080,
} as const;
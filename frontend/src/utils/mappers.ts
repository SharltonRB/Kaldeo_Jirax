import { IssueStatus, Priority, SprintStatus, Issue, Project, Sprint, Label, Comment } from '@/types';

/**
 * Maps frontend issue status to backend status
 * Frontend uses 'SELECTED' while backend uses 'SELECTED_FOR_DEVELOPMENT'
 */
export const mapFrontendStatusToBackend = (status: string): IssueStatus => {
  switch (status) {
    case 'SELECTED':
      return IssueStatus.SELECTED_FOR_DEVELOPMENT;
    case 'BACKLOG':
      return IssueStatus.BACKLOG;
    case 'IN_PROGRESS':
      return IssueStatus.IN_PROGRESS;
    case 'IN_REVIEW':
      return IssueStatus.IN_REVIEW;
    case 'DONE':
      return IssueStatus.DONE;
    default:
      return IssueStatus.BACKLOG;
  }
};

/**
 * Maps backend issue status to frontend status
 * Backend uses 'SELECTED_FOR_DEVELOPMENT' while frontend uses 'SELECTED'
 */
export const mapBackendStatusToFrontend = (status: IssueStatus): string => {
  switch (status) {
    case IssueStatus.SELECTED_FOR_DEVELOPMENT:
      return 'SELECTED';
    case IssueStatus.BACKLOG:
      return 'BACKLOG';
    case IssueStatus.IN_PROGRESS:
      return 'IN_PROGRESS';
    case IssueStatus.IN_REVIEW:
      return 'IN_REVIEW';
    case IssueStatus.DONE:
      return 'DONE';
    default:
      return 'BACKLOG';
  }
};

/**
 * Maps frontend issue type to backend issue type name
 * Frontend uses simple strings while backend uses IssueType entities
 */
export const mapFrontendTypeToBackend = (type: string): string => {
  switch (type) {
    case 'EPIC':
      return 'EPIC';
    case 'STORY':
      return 'STORY';
    case 'TASK':
      return 'TASK';
    case 'BUG':
      return 'BUG';
    default:
      return 'TASK';
  }
};

/**
 * Maps backend issue type name to frontend type
 */
export const mapBackendTypeToFrontend = (typeName: string): string => {
  return typeName; // Direct mapping for now
};

/**
 * Converts string ID to number (for backend compatibility)
 */
export const stringToNumberId = (id: string | undefined): number | undefined => {
  if (!id) return undefined;
  const numId = parseInt(id, 10);
  return isNaN(numId) ? undefined : numId;
};

/**
 * Converts number ID to string (for frontend compatibility)
 */
export const numberToStringId = (id: number | undefined): string | undefined => {
  return id?.toString();
};

/**
 * Converts frontend date string to backend LocalDate format (yyyy-MM-dd)
 */
export const formatDateForBackend = (date: string | Date): string => {
  const d = typeof date === 'string' ? new Date(date) : date;
  return d.toISOString().split('T')[0];
};

/**
 * Converts backend LocalDate string to frontend Date
 */
export const parseBackendLocalDate = (localDate: string): Date => {
  return new Date(localDate + 'T00:00:00.000Z');
};

/**
 * Converts backend Instant string to frontend Date
 */
export const parseBackendInstant = (instant: string): Date => {
  return new Date(instant);
};

/**
 * Converts frontend Date to backend Instant format
 */
export const formatDateForBackendInstant = (date: Date): string => {
  return date.toISOString();
};

/**
 * Formats date for display in the UI
 */
export const formatDisplayDate = (dateStr: string): string => {
  if (!dateStr) return 'N/A';
  try {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { 
      month: 'short', 
      day: 'numeric', 
      year: 'numeric' 
    });
  } catch (e) {
    return dateStr;
  }
};

/**
 * Formats time ago for display
 */
export const formatTimeAgo = (dateStr: string): string => {
  try {
    const date = new Date(dateStr);
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (diffInSeconds < 60) return 'just now';
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} min ago`;
    if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)} h ago`;
    return formatDisplayDate(dateStr);
  } catch (e) {
    return dateStr;
  }
};

/**
 * Generates a unique key for an issue (project key + issue number)
 */
export const generateIssueKey = (projectKey: string, issueNumber: number): string => {
  return `${projectKey}-${issueNumber}`;
};

/**
 * Extracts issue number from issue key
 */
export const extractIssueNumber = (issueKey: string): number => {
  const parts = issueKey.split('-');
  const lastPart = parts[parts.length - 1];
  return parseInt(lastPart, 10) || 0;
};

/**
 * Maps backend Issue DTO to frontend Issue with proper type conversions
 */
export const mapBackendIssueToFrontend = (backendIssue: any): Issue => {
  return {
    ...backendIssue,
    id: backendIssue.id,
    status: backendIssue.status as IssueStatus,
    priority: backendIssue.priority as Priority,
    createdAt: backendIssue.createdAt,
    updatedAt: backendIssue.updatedAt,
    labels: backendIssue.labels || [],
  };
};

/**
 * Maps frontend Issue to backend Issue DTO format
 */
export const mapFrontendIssueToBackend = (frontendIssue: Partial<Issue>): any => {
  return {
    ...frontendIssue,
    status: frontendIssue.status,
    priority: frontendIssue.priority,
    createdAt: frontendIssue.createdAt,
    updatedAt: frontendIssue.updatedAt,
  };
};

/**
 * Maps backend Project DTO to frontend Project
 */
export const mapBackendProjectToFrontend = (backendProject: any): Project => {
  return {
    ...backendProject,
    id: backendProject.id,
    createdAt: backendProject.createdAt,
    updatedAt: backendProject.updatedAt,
  };
};

/**
 * Maps backend Sprint DTO to frontend Sprint
 */
export const mapBackendSprintToFrontend = (backendSprint: any): Sprint => {
  return {
    ...backendSprint,
    id: backendSprint.id,
    status: backendSprint.status as SprintStatus,
    startDate: backendSprint.startDate, // LocalDate format
    endDate: backendSprint.endDate,     // LocalDate format
    createdAt: backendSprint.createdAt,
    updatedAt: backendSprint.updatedAt,
  };
};

/**
 * Maps backend Label DTO to frontend Label
 */
export const mapBackendLabelToFrontend = (backendLabel: any): Label => {
  return {
    ...backendLabel,
    id: backendLabel.id,
    createdAt: backendLabel.createdAt,
    updatedAt: backendLabel.updatedAt,
  };
};

/**
 * Maps backend Comment DTO to frontend Comment
 */
export const mapBackendCommentToFrontend = (backendComment: any): Comment => {
  return {
    ...backendComment,
    id: backendComment.id,
    createdAt: backendComment.createdAt,
    updatedAt: backendComment.updatedAt,
  };
};

/**
 * Validates and converts priority string to Priority enum
 */
export const validatePriority = (priority: string): Priority => {
  const validPriorities = Object.values(Priority);
  if (validPriorities.includes(priority as Priority)) {
    return priority as Priority;
  }
  return Priority.MEDIUM; // Default fallback
};

/**
 * Validates and converts status string to IssueStatus enum
 */
export const validateIssueStatus = (status: string): IssueStatus => {
  const validStatuses = Object.values(IssueStatus);
  if (validStatuses.includes(status as IssueStatus)) {
    return status as IssueStatus;
  }
  return IssueStatus.BACKLOG; // Default fallback
};

/**
 * Validates and converts status string to SprintStatus enum
 */
export const validateSprintStatus = (status: string): SprintStatus => {
  const validStatuses = Object.values(SprintStatus);
  if (validStatuses.includes(status as SprintStatus)) {
    return status as SprintStatus;
  }
  return SprintStatus.PLANNED; // Default fallback
};

/**
 * Safely parses JSON string, returns null if invalid
 */
export const safeJsonParse = <T>(jsonString: string | null | undefined): T | null => {
  if (!jsonString) return null;
  try {
    return JSON.parse(jsonString) as T;
  } catch {
    return null;
  }
};

/**
 * Safely stringifies object to JSON
 */
export const safeJsonStringify = (obj: any): string => {
  try {
    return JSON.stringify(obj);
  } catch {
    return '{}';
  }
};

/**
 * Converts backend pagination response to frontend format
 */
export const mapBackendPageToFrontend = <T>(backendPage: any, mapper?: (item: any) => T): {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
} => {
  return {
    content: mapper ? backendPage.content.map(mapper) : backendPage.content,
    totalElements: backendPage.totalElements || 0,
    totalPages: backendPage.totalPages || 0,
    size: backendPage.size || 0,
    number: backendPage.number || 0,
    first: backendPage.first || false,
    last: backendPage.last || false,
  };
};

/**
 * Debounce function for search inputs
 */
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let timeout: NodeJS.Timeout;
  return (...args: Parameters<T>) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  };
};

/**
 * Throttle function for frequent operations
 */
export const throttle = <T extends (...args: any[]) => any>(
  func: T,
  limit: number
): ((...args: Parameters<T>) => void) => {
  let inThrottle: boolean;
  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      func(...args);
      inThrottle = true;
      setTimeout(() => (inThrottle = false), limit);
    }
  };
};
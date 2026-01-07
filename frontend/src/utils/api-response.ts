/**
 * API Response mapping utilities
 * Maps between backend DTOs (number IDs) and frontend types (string IDs)
 */

import * as BackendTypes from '@/types';

// Frontend types (existing glass-design types with string IDs)
export interface FrontendUser {
  id: string;
  name: string;
  email: string;
  avatar?: string;
}

export interface FrontendProject {
  id: string;
  key: string;
  name: string;
  description: string;
  issueCount: number;
}

export interface FrontendSprint {
  id: string;
  name: string;
  startDate: string;
  endDate: string;
  status: 'PLANNED' | 'ACTIVE' | 'COMPLETED';
  goal?: string;
}

export interface FrontendIssue {
  id: string;
  key: string;
  title: string;
  description?: string;
  status: BackendTypes.IssueStatus;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  type: 'STORY' | 'TASK' | 'BUG' | 'EPIC';
  storyPoints?: number;
  sprintId?: string;
  lastCompletedSprintId?: number;
  projectId: string;
  assigneeId?: string;
  parentId?: string;
  updatedAt?: string;
  comments?: FrontendComment[];
}

export interface FrontendComment {
  id: string;
  issueId: string;
  userId: string;
  userName: string;
  content: string;
  createdAt: string;
}

export interface FrontendLabel {
  id: string;
  name: string;
  color: string;
}

// Mapping functions from Backend to Frontend
export const mapUserToFrontend = (user: BackendTypes.User): FrontendUser => ({
  id: user.id.toString(),
  name: user.name,
  email: user.email,
  avatar: undefined, // Not in backend yet
});

export const mapProjectToFrontend = (project: BackendTypes.Project): FrontendProject => ({
  id: project.id.toString(),
  key: project.key,
  name: project.name,
  description: project.description || '',
  issueCount: project.issueCount,
});

export const mapSprintToFrontend = (sprint: BackendTypes.Sprint): FrontendSprint => ({
  id: sprint.id.toString(),
  name: sprint.name,
  startDate: sprint.startDate,
  endDate: sprint.endDate,
  status: sprint.status as 'PLANNED' | 'ACTIVE' | 'COMPLETED',
  goal: sprint.goal,
});

export const mapIssueToFrontend = (issue: BackendTypes.Issue): FrontendIssue => {
  // Status is already the correct enum type from backend
  const status = issue.status;

  // Map backend priority to frontend priority
  const priorityMap: Record<BackendTypes.Priority, FrontendIssue['priority']> = {
    [BackendTypes.Priority.LOW]: 'LOW',
    [BackendTypes.Priority.MEDIUM]: 'MEDIUM',
    [BackendTypes.Priority.HIGH]: 'HIGH',
    [BackendTypes.Priority.CRITICAL]: 'CRITICAL',
  };

  // Determine issue type based on backend data
  let issueType: FrontendIssue['type'] = 'TASK';
  if (issue.isEpic) {
    issueType = 'EPIC';
  } else if (issue.issueTypeName) {
    const typeMap: Record<string, FrontendIssue['type']> = {
      'STORY': 'STORY',
      'Story': 'STORY',
      'TASK': 'TASK', 
      'Task': 'TASK',
      'BUG': 'BUG',
      'Bug': 'BUG',
      'EPIC': 'EPIC',
      'Epic': 'EPIC',
    };
    issueType = typeMap[issue.issueTypeName] || 'TASK';
  }

  // Generate issue key if not provided
  const issueKey = issue.projectKey ? `${issue.projectKey}-${issue.id}` : `ISS-${issue.id}`;

  return {
    id: issue.id.toString(),
    key: issueKey,
    title: issue.title,
    description: issue.description,
    status: status,
    priority: priorityMap[issue.priority],
    type: issueType,
    storyPoints: issue.storyPoints,
    sprintId: issue.sprintId?.toString(),
    lastCompletedSprintId: issue.lastCompletedSprintId,
    projectId: issue.projectId.toString(),
    assigneeId: undefined, // Not in backend Issue DTO yet
    parentId: issue.parentIssueId?.toString(),
    updatedAt: issue.updatedAt,
    comments: [], // Will be loaded separately
  };
};

export const mapCommentToFrontend = (comment: BackendTypes.Comment): FrontendComment => ({
  id: comment.id.toString(),
  issueId: comment.issueId.toString(),
  userId: comment.userId.toString(),
  userName: comment.userName,
  content: comment.content,
  createdAt: comment.createdAt,
});

export const mapLabelToFrontend = (label: BackendTypes.Label): FrontendLabel => ({
  id: label.id.toString(),
  name: label.name,
  color: label.color,
});

// Mapping functions from Frontend to Backend (for API requests)
export const mapProjectToBackend = (project: Partial<FrontendProject>): BackendTypes.CreateProjectRequest => ({
  name: project.name || '',
  key: project.key || '',
  description: project.description,
});

export const mapIssueToBackend = (issue: Partial<FrontendIssue>): Partial<BackendTypes.CreateIssueRequest> => {
  // Status is already the correct enum type, no mapping needed

  // Map frontend priority to backend priority
  const priorityMap: Record<FrontendIssue['priority'], BackendTypes.Priority> = {
    'LOW': BackendTypes.Priority.LOW,
    'MEDIUM': BackendTypes.Priority.MEDIUM,
    'HIGH': BackendTypes.Priority.HIGH,
    'CRITICAL': BackendTypes.Priority.CRITICAL,
  };

  // Determine issue type ID (we'll need to get this from backend or use defaults)
  const getIssueTypeId = (type?: FrontendIssue['type']): number => {
    const typeMap: Record<FrontendIssue['type'], number> = {
      'BUG': 1,
      'STORY': 2,
      'TASK': 3,
      'EPIC': 4,
    };
    return typeMap[type || 'TASK'];
  };

  return {
    title: issue.title,
    description: issue.description,
    priority: issue.priority ? priorityMap[issue.priority] : BackendTypes.Priority.MEDIUM,
    storyPoints: issue.storyPoints,
    projectId: issue.projectId ? parseInt(issue.projectId) : undefined,
    sprintId: issue.sprintId ? parseInt(issue.sprintId) : undefined,
    issueTypeId: getIssueTypeId(issue.type),
    parentIssueId: issue.parentId ? parseInt(issue.parentId) : undefined,
  };
};

export const mapSprintToBackend = (sprint: Partial<FrontendSprint>): BackendTypes.CreateSprintRequest => ({
  name: sprint.name || '',
  startDate: sprint.startDate || '',
  endDate: sprint.endDate || '',
  goal: sprint.goal,
});

export const mapCommentToBackend = (comment: Partial<FrontendComment>): BackendTypes.CreateCommentRequest => ({
  content: comment.content || '',
});

export const mapLabelToBackend = (label: Partial<FrontendLabel>): BackendTypes.CreateLabelRequest => ({
  name: label.name || '',
  color: label.color || '#000000',
});

// Status update mapping
export const mapStatusToBackend = (status: FrontendIssue['status']): BackendTypes.StatusUpdateRequest => {
  // Status is already the correct enum type, no mapping needed
  return {
    newStatus: status, // Backend expects "newStatus" field
  };
};

// Array mapping utilities
export const mapProjectsToFrontend = (projects: BackendTypes.Project[]): FrontendProject[] =>
  projects.map(mapProjectToFrontend);

export const mapIssuesToFrontend = (issues: BackendTypes.Issue[]): FrontendIssue[] =>
  issues.map(mapIssueToFrontend);

export const mapSprintsToFrontend = (sprints: BackendTypes.Sprint[]): FrontendSprint[] =>
  sprints.map(mapSprintToFrontend);

export const mapCommentsToFrontend = (comments: BackendTypes.Comment[]): FrontendComment[] =>
  comments.map(mapCommentToFrontend);

export const mapLabelsToFrontend = (labels: BackendTypes.Label[]): FrontendLabel[] =>
  labels.map(mapLabelToFrontend);

// Error handling utility
export const handleApiError = (error: any): string => {
  // Handle ApiError format from our client interceptor
  if (error?.message && error?.status) {
    const message = error.message.toLowerCase();
    const status = error.status;
    
    // Handle specific error cases with clear English messages
    switch (status) {
      case 400:
        if (message.includes('email') && message.includes('invalid')) {
          return 'Please enter a valid email address.';
        }
        if (message.includes('password')) {
          return 'Password must be at least 6 characters long.';
        }
        if (message.includes('name') || message.includes('required')) {
          return 'All required fields must be filled.';
        }
        return 'Invalid input. Please check your information and try again.';
        
      case 401:
        if (message.includes('invalid credentials') || 
            message.includes('bad credentials') ||
            message.includes('authentication failed') ||
            message.includes('wrong password') ||
            message.includes('incorrect')) {
          return 'Invalid email or password. Please check your credentials.';
        }
        return 'Authentication failed. Please check your credentials.';
        
      case 409:
        if (message.includes('email') || message.includes('already exists') || message.includes('duplicate')) {
          return 'An account with this email already exists. Please use a different email or try logging in.';
        }
        return 'This information is already in use. Please try with different details.';
        
      case 422:
        return 'Invalid data format. Please check your input and try again.';
        
      case 500:
        return 'Server error. Please try again later.';
        
      case 503:
        return 'Service temporarily unavailable. Please try again later.';
        
      default:
        // Return the original message if it's already in English and clear
        if (error.message && !message.includes('credenciales') && !message.includes('contraseña')) {
          return error.message;
        }
        return 'An unexpected error occurred. Please try again.';
    }
  }
  
  // Handle legacy error formats
  if (error?.response?.data?.message) {
    const message = error.response.data.message.toLowerCase();
    const status = error.response.status;
    
    if (status === 409 && (message.includes('email') || message.includes('already exists'))) {
      return 'An account with this email already exists. Please use a different email or try logging in.';
    }
    
    return error.response.data.message;
  }
  
  // Handle HTTP status codes without detailed messages
  if (error?.response?.status) {
    switch (error.response.status) {
      case 401:
        return 'Invalid email or password. Please check your credentials.';
      case 400:
        return 'Invalid input. Please check your information and try again.';
      case 409:
        return 'An account with this email already exists. Please use a different email or try logging in.';
      case 500:
        return 'Server error. Please try again later.';
      default:
        return 'An unexpected error occurred. Please try again.';
    }
  }
  
  return 'An unexpected error occurred. Please try again.';
};
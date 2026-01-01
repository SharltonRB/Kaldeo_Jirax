// Backend API Types - matching DTOs exactly

export interface User {
  id: number;
  name: string;
  email: string;
  createdAt: string;
  updatedAt: string;
}

export interface Project {
  id: number;
  name: string;
  key: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
  issueCount: number;
}

export interface Sprint {
  id: number;
  name: string;
  startDate: string; // LocalDate format: yyyy-MM-dd
  endDate: string;   // LocalDate format: yyyy-MM-dd
  status: SprintStatus;
  createdAt: string;
  updatedAt: string;
  issueCount?: number;
  completedIssueCount?: number;
}

export interface Issue {
  id: number;
  title: string;
  description?: string;
  status: IssueStatus;
  priority: Priority;
  storyPoints?: number;
  createdAt: string;
  updatedAt: string;
  // Related entity information
  projectId: number;
  projectName?: string;
  projectKey?: string;
  sprintId?: number;
  sprintName?: string;
  issueTypeId: number;
  issueTypeName: string;
  labels?: Label[];
  commentCount?: number;
  // Epic hierarchy information
  parentIssueId?: number;
  parentIssueTitle?: string;
  isEpic: boolean;
  childIssueCount?: number;
}

export interface Label {
  id: number;
  name: string;
  color: string;
  createdAt: string;
  updatedAt: string;
}

export interface Comment {
  id: number;
  content: string;
  createdAt: string;
  updatedAt: string;
  issueId: number;
  userId: number;
  userName: string;
}

export interface AuditLog {
  id: number;
  entityType: string;
  entityId: number;
  action: string;
  oldValues?: string;
  newValues?: string;
  createdAt: string;
  userId: number;
  userName: string;
}

// Enums - matching backend exactly
export enum IssueStatus {
  BACKLOG = 'BACKLOG',
  SELECTED_FOR_DEVELOPMENT = 'SELECTED_FOR_DEVELOPMENT',
  IN_PROGRESS = 'IN_PROGRESS',
  IN_REVIEW = 'IN_REVIEW',
  DONE = 'DONE'
}

export enum Priority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export enum SprintStatus {
  PLANNED = 'PLANNED',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED'
}

// Frontend-specific types for UI state
export type ViewType = 'auth' | 'dashboard' | 'projects' | 'sprints' | 'kanban' | 'profile';

export type Theme = 'light' | 'dark';

// API Request/Response types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface CreateProjectRequest {
  name: string;
  key: string;
  description?: string;
}

export interface UpdateProjectRequest {
  name?: string;
  description?: string;
}

export interface CreateIssueRequest {
  title: string;
  description?: string;
  priority: Priority;
  storyPoints?: number;
  projectId: number;
  sprintId?: number;
  issueTypeId: number;
  parentIssueId?: number;
}

export interface UpdateIssueRequest {
  title?: string;
  description?: string;
  priority?: Priority;
  storyPoints?: number;
  sprintId?: number;
  parentIssueId?: number;
}

export interface StatusUpdateRequest {
  status: IssueStatus;
}

export interface CreateSprintRequest {
  name: string;
  startDate: string;
  endDate: string;
}

export interface UpdateSprintRequest {
  name?: string;
  startDate?: string;
  endDate?: string;
  status?: SprintStatus;
}

export interface CreateCommentRequest {
  content: string;
}

export interface UpdateCommentRequest {
  content: string;
}

export interface CreateLabelRequest {
  name: string;
  color: string;
}

export interface UpdateLabelRequest {
  name?: string;
  color?: string;
}

// Dashboard metrics
export interface DashboardMetrics {
  totalProjects: number;
  totalIssues: number;
  totalSprints: number;
  activeSprintId?: number;
  issuesByStatus: Record<IssueStatus, number>;
  issuesByPriority: Record<Priority, number>;
  recentIssues: Issue[];
}

// Pagination
export interface PageRequest {
  page?: number;
  size?: number;
  sort?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Error handling
export interface ApiError {
  message: string;
  status: number;
  timestamp: string;
  path: string;
}

// Frontend UI state types
export interface AppState {
  user: User | null;
  theme: Theme;
  currentView: ViewType;
  isSidebarCollapsed: boolean;
  selectedIssueId: number | null;
  isCreateIssueModalOpen: boolean;
  createIssueInitialData: Partial<CreateIssueRequest> | null;
  searchQuery: string;
  issueHistory: number[];
}
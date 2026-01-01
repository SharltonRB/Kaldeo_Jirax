import { api } from './client';
import { PageRequest, PageResponse } from '@/types';

/**
 * Base API service class with common CRUD operations
 */
export abstract class BaseApiService<T, CreateRequest, UpdateRequest> {
  protected abstract baseUrl: string;

  /**
   * Get all entities with pagination
   */
  async getAll(params?: PageRequest & Record<string, any>): Promise<PageResponse<T>> {
    const queryParams = new URLSearchParams();
    
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          queryParams.append(key, String(value));
        }
      });
    }

    const url = queryParams.toString() 
      ? `${this.baseUrl}?${queryParams.toString()}`
      : this.baseUrl;

    return api.get<PageResponse<T>>(url);
  }

  /**
   * Get entity by ID
   */
  async getById(id: number): Promise<T> {
    return api.get<T>(`${this.baseUrl}/${id}`);
  }

  /**
   * Create new entity
   */
  async create(data: CreateRequest): Promise<T> {
    return api.post<T>(this.baseUrl, data);
  }

  /**
   * Update existing entity
   */
  async update(id: number, data: UpdateRequest): Promise<T> {
    return api.put<T>(`${this.baseUrl}/${id}`, data);
  }

  /**
   * Delete entity
   */
  async delete(id: number): Promise<void> {
    return api.delete<void>(`${this.baseUrl}/${id}`);
  }
}

/**
 * Query key factory for consistent cache keys
 */
export class QueryKeys {
  static readonly projects = {
    all: ['projects'] as const,
    lists: () => [...QueryKeys.projects.all, 'list'] as const,
    list: (filters: Record<string, any>) => [...QueryKeys.projects.lists(), { filters }] as const,
    details: () => [...QueryKeys.projects.all, 'detail'] as const,
    detail: (id: number) => [...QueryKeys.projects.details(), id] as const,
  };

  static readonly issues = {
    all: ['issues'] as const,
    lists: () => [...QueryKeys.issues.all, 'list'] as const,
    list: (filters: Record<string, any>) => [...QueryKeys.issues.lists(), { filters }] as const,
    details: () => [...QueryKeys.issues.all, 'detail'] as const,
    detail: (id: number) => [...QueryKeys.issues.details(), id] as const,
    history: (id: number) => [...QueryKeys.issues.detail(id), 'history'] as const,
  };

  static readonly sprints = {
    all: ['sprints'] as const,
    lists: () => [...QueryKeys.sprints.all, 'list'] as const,
    list: (filters: Record<string, any>) => [...QueryKeys.sprints.lists(), { filters }] as const,
    details: () => [...QueryKeys.sprints.all, 'detail'] as const,
    detail: (id: number) => [...QueryKeys.sprints.details(), id] as const,
    active: () => [...QueryKeys.sprints.all, 'active'] as const,
  };

  static readonly labels = {
    all: ['labels'] as const,
    lists: () => [...QueryKeys.labels.all, 'list'] as const,
    list: (filters: Record<string, any>) => [...QueryKeys.labels.lists(), { filters }] as const,
  };

  static readonly comments = {
    all: ['comments'] as const,
    byIssue: (issueId: number) => [...QueryKeys.comments.all, 'issue', issueId] as const,
  };

  static readonly dashboard = {
    all: ['dashboard'] as const,
    metrics: () => [...QueryKeys.dashboard.all, 'metrics'] as const,
  };

  static readonly auth = {
    all: ['auth'] as const,
    user: () => [...QueryKeys.auth.all, 'user'] as const,
  };
}

/**
 * Common mutation options for consistent error handling
 */
export const createMutationOptions = <TData, TError = Error, TVariables = void, TContext = unknown>(
  options?: {
    onSuccess?: (data: TData, variables: TVariables, context: TContext) => void;
    onError?: (error: TError, variables: TVariables, context: TContext) => void;
  }
) => ({
  retry: 1,
  ...options,
});

/**
 * Common query options for consistent caching
 */
export const createQueryOptions = (
  options?: {
    staleTime?: number;
    cacheTime?: number;
    enabled?: boolean;
  }
) => ({
  staleTime: 5 * 60 * 1000, // 5 minutes
  cacheTime: 10 * 60 * 1000, // 10 minutes
  retry: 1,
  ...options,
});
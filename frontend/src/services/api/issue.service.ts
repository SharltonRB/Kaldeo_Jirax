import { api } from './client';
import { BaseApiService } from './base.service';
import { 
  Issue, 
  CreateIssueRequest, 
  UpdateIssueRequest,
  StatusUpdateRequest,
  AuditLog,
  PageRequest,
  PageResponse,
  IssueStatus 
} from '@/types';

class IssueApiService extends BaseApiService<Issue, CreateIssueRequest, UpdateIssueRequest> {
  protected baseUrl = '/issues';

  /**
   * Get issues with filtering and pagination
   */
  async getIssues(params?: PageRequest & {
    projectId?: number;
    sprintId?: number;
    status?: IssueStatus;
    priority?: string;
    search?: string;
    assigneeId?: number;
  }): Promise<PageResponse<Issue>> {
    return this.getAll(params);
  }

  /**
   * Get issue by ID
   */
  async getIssue(id: number): Promise<Issue> {
    return this.getById(id);
  }

  /**
   * Create new issue
   */
  async createIssue(data: CreateIssueRequest): Promise<Issue> {
    return this.create(data);
  }

  /**
   * Update existing issue
   */
  async updateIssue(id: number, data: UpdateIssueRequest): Promise<Issue> {
    return this.update(id, data);
  }

  /**
   * Update issue status
   */
  async updateIssueStatus(id: number, data: StatusUpdateRequest): Promise<Issue> {
    return api.put<Issue>(`${this.baseUrl}/${id}/status`, data);
  }

  /**
   * Delete issue
   */
  async deleteIssue(id: number): Promise<void> {
    return this.delete(id);
  }

  /**
   * Get issue history/audit trail
   */
  async getIssueHistory(id: number): Promise<AuditLog[]> {
    return api.get<AuditLog[]>(`${this.baseUrl}/${id}/history`);
  }

  /**
   * Assign issue to sprint
   */
  async assignToSprint(id: number, sprintId: number | null): Promise<Issue> {
    return api.put<Issue>(`${this.baseUrl}/${id}`, { sprintId });
  }

  /**
   * Get issues by sprint
   */
  async getIssuesBySprint(sprintId: number, params?: PageRequest): Promise<PageResponse<Issue>> {
    return this.getIssues({ ...params, sprintId });
  }

  /**
   * Get issues by project
   */
  async getIssuesByProject(projectId: number, params?: PageRequest): Promise<PageResponse<Issue>> {
    return this.getIssues({ ...params, projectId });
  }

  /**
   * Search issues globally
   */
  async searchIssues(query: string, params?: PageRequest): Promise<PageResponse<Issue>> {
    return this.getIssues({ ...params, search: query });
  }

  /**
   * Get epic children (if issue is an epic)
   */
  async getEpicChildren(epicId: number, params?: PageRequest): Promise<PageResponse<Issue>> {
    return api.get<PageResponse<Issue>>(`${this.baseUrl}/${epicId}/children`, { params });
  }

  /**
   * Bulk update issues
   */
  async bulkUpdateIssues(issueIds: number[], updates: Partial<UpdateIssueRequest>): Promise<Issue[]> {
    return api.put<Issue[]>(`${this.baseUrl}/bulk`, {
      issueIds,
      updates,
    });
  }
}

export const issueService = new IssueApiService();
import { api } from './client';
import { BaseApiService } from './base.service';
import { 
  Sprint, 
  CreateSprintRequest, 
  UpdateSprintRequest,
  Issue,
  PageRequest,
  PageResponse,
  SprintStatus 
} from '@/types';

class SprintApiService extends BaseApiService<Sprint, CreateSprintRequest, UpdateSprintRequest> {
  protected baseUrl = '/sprints';

  /**
   * Get sprints with filtering and pagination
   */
  async getSprints(params?: PageRequest & {
    status?: SprintStatus;
    search?: string;
  }): Promise<PageResponse<Sprint>> {
    return this.getAll(params);
  }

  /**
   * Get sprint by ID
   */
  async getSprint(id: number): Promise<Sprint> {
    return this.getById(id);
  }

  /**
   * Create new sprint
   */
  async createSprint(data: CreateSprintRequest): Promise<Sprint> {
    return this.create(data);
  }

  /**
   * Update existing sprint
   */
  async updateSprint(id: number, data: UpdateSprintRequest): Promise<Sprint> {
    return this.update(id, data);
  }

  /**
   * Delete sprint
   */
  async deleteSprint(id: number): Promise<void> {
    return this.delete(id);
  }

  /**
   * Get active sprint
   */
  async getActiveSprint(): Promise<Sprint | null> {
    try {
      const sprints = await this.getSprints({ status: SprintStatus.ACTIVE, size: 1 });
      return sprints.content.length > 0 ? sprints.content[0] : null;
    } catch (error) {
      console.warn('Failed to get active sprint:', error);
      return null;
    }
  }

  /**
   * Start sprint (change status to ACTIVE)
   */
  async startSprint(id: number): Promise<Sprint> {
    return api.put<Sprint>(`${this.baseUrl}/${id}/start`);
  }

  /**
   * Complete sprint (change status to COMPLETED)
   */
  async completeSprint(id: number, moveIncompleteIssues?: {
    targetSprintId?: number;
    moveToBacklog?: boolean;
  }): Promise<Sprint> {
    return api.put<Sprint>(`${this.baseUrl}/${id}/complete`, moveIncompleteIssues);
  }

  /**
   * Get sprint issues
   */
  async getSprintIssues(id: number, params?: PageRequest): Promise<PageResponse<Issue>> {
    return api.get<PageResponse<Issue>>(`${this.baseUrl}/${id}/issues`, { params });
  }

  /**
   * Add issues to sprint
   */
  async addIssuesToSprint(sprintId: number, issueIds: number[]): Promise<void> {
    return api.post<void>(`${this.baseUrl}/${sprintId}/issues`, { issueIds });
  }

  /**
   * Remove issues from sprint
   */
  async removeIssuesFromSprint(sprintId: number, issueIds: number[]): Promise<void> {
    return api.delete<void>(`${this.baseUrl}/${sprintId}/issues`, { data: { issueIds } });
  }

  /**
   * Get sprint progress/metrics
   */
  async getSprintProgress(id: number): Promise<{
    totalIssues: number;
    completedIssues: number;
    inProgressIssues: number;
    todoIssues: number;
    totalStoryPoints: number;
    completedStoryPoints: number;
    burndownData: Array<{
      date: string;
      remainingStoryPoints: number;
      idealRemaining: number;
    }>;
  }> {
    return api.get(`${this.baseUrl}/${id}/progress`);
  }
}

export const sprintService = new SprintApiService();
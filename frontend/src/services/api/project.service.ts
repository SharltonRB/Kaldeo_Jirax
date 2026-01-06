import { BaseApiService } from './base.service';
import { api } from './client';
import { 
  Project, 
  CreateProjectRequest, 
  UpdateProjectRequest,
  PageRequest,
  PageResponse 
} from '@/types';

class ProjectApiService extends BaseApiService<Project, CreateProjectRequest, UpdateProjectRequest> {
  protected baseUrl = '/projects';

  /**
   * Get projects with optional filtering and search
   */
  async getProjects(params?: PageRequest & { search?: string }): Promise<PageResponse<Project>> {
    const searchParams = new URLSearchParams();
    
    if (params?.page !== undefined) searchParams.append('page', params.page.toString());
    if (params?.size !== undefined) searchParams.append('size', params.size.toString());
    if (params?.sort) searchParams.append('sort', params.sort);
    if (params?.search && params.search.trim()) {
      searchParams.append('search', params.search.trim());
    }
    
    const queryString = searchParams.toString();
    const url = queryString ? `${this.baseUrl}?${queryString}` : this.baseUrl;
    
    return api.get<PageResponse<Project>>(url);
  }

  /**
   * Get project by ID
   */
  async getProject(id: number): Promise<Project> {
    return this.getById(id);
  }

  /**
   * Create new project
   */
  async createProject(data: CreateProjectRequest): Promise<Project> {
    return this.create(data);
  }

  /**
   * Update existing project
   */
  async updateProject(id: number, data: UpdateProjectRequest): Promise<Project> {
    return this.update(id, data);
  }

  /**
   * Delete project
   */
  async deleteProject(id: number): Promise<void> {
    return this.delete(id);
  }

  /**
   * Check if project key is available
   */
  async checkProjectKeyAvailability(key: string): Promise<{ available: boolean }> {
    try {
      const available = await api.get<boolean>(`${this.baseUrl}/key-available/${encodeURIComponent(key)}`);
      return { available };
    } catch (error) {
      console.warn('Project key availability check failed:', error);
      return { available: false }; // Assume not available if check fails for safety
    }
  }
}

export const projectService = new ProjectApiService();
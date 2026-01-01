import { BaseApiService } from './base.service';
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
   * Get projects with optional filtering
   */
  async getProjects(params?: PageRequest & { search?: string }): Promise<PageResponse<Project>> {
    return this.getAll(params);
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
    // This endpoint might not exist yet, so we'll implement a basic check
    try {
      const projects = await this.getProjects({ size: 1000 }); // Get all projects
      const keyExists = projects.content.some(project => project.key === key);
      return { available: !keyExists };
    } catch (error) {
      console.warn('Project key availability check failed:', error);
      return { available: true }; // Assume available if check fails
    }
  }
}

export const projectService = new ProjectApiService();
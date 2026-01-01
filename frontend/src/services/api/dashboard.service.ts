import { api } from './client';
import { DashboardMetrics, Issue, Sprint } from '@/types';

class DashboardApiService {
  private baseUrl = '/dashboard';

  /**
   * Get dashboard metrics
   */
  async getDashboardMetrics(): Promise<DashboardMetrics> {
    return api.get<DashboardMetrics>(`${this.baseUrl}/metrics`);
  }

  /**
   * Get recent issues for dashboard
   */
  async getRecentIssues(limit: number = 10): Promise<Issue[]> {
    return api.get<Issue[]>(`${this.baseUrl}/recent-issues?limit=${limit}`);
  }

  /**
   * Get active sprint summary
   */
  async getActiveSprintSummary(): Promise<{
    sprint: Sprint | null;
    totalIssues: number;
    completedIssues: number;
    inProgressIssues: number;
    remainingDays: number;
  } | null> {
    try {
      return api.get(`${this.baseUrl}/active-sprint`);
    } catch (error) {
      console.warn('Failed to get active sprint summary:', error);
      return null;
    }
  }

  /**
   * Get project statistics
   */
  async getProjectStatistics(): Promise<Array<{
    projectId: number;
    projectName: string;
    projectKey: string;
    totalIssues: number;
    completedIssues: number;
    inProgressIssues: number;
    lastActivity: string;
  }>> {
    return api.get(`${this.baseUrl}/project-stats`);
  }

  /**
   * Get issue distribution data for charts
   */
  async getIssueDistribution(): Promise<{
    byStatus: Record<string, number>;
    byPriority: Record<string, number>;
    byProject: Array<{
      projectName: string;
      issueCount: number;
    }>;
    byType: Record<string, number>;
  }> {
    return api.get(`${this.baseUrl}/issue-distribution`);
  }

  /**
   * Get sprint progress data for burndown charts
   */
  async getSprintBurndown(sprintId: number): Promise<Array<{
    date: string;
    remainingStoryPoints: number;
    idealRemaining: number;
    completedStoryPoints: number;
  }>> {
    return api.get(`${this.baseUrl}/sprint/${sprintId}/burndown`);
  }

  /**
   * Get velocity data for the last few sprints
   */
  async getVelocityData(sprintCount: number = 6): Promise<Array<{
    sprintName: string;
    plannedStoryPoints: number;
    completedStoryPoints: number;
    sprintDuration: number;
  }>> {
    return api.get(`${this.baseUrl}/velocity?count=${sprintCount}`);
  }
}

export const dashboardService = new DashboardApiService();
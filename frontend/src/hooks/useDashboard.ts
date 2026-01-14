import { useQuery } from '@tanstack/react-query';
import { dashboardService } from '@/services/api/dashboard.service';
import { DashboardMetrics, Issue, Sprint } from '@/types';

/**
 * Hook to get dashboard metrics
 */
export const useDashboardMetrics = (enabled: boolean = true) => {
  return useQuery({
    queryKey: ['dashboard', 'metrics'],
    queryFn: () => dashboardService.getDashboardMetrics(),
    enabled,
    staleTime: 5 * 60 * 1000, // 5 minutes
    refetchInterval: 5 * 60 * 1000, // Auto-refresh every 5 minutes
    retry: (failureCount, error: any) => {
      // Don't retry on 4xx errors
      if (error?.response?.status >= 400 && error?.response?.status < 500) {
        return false;
      }
      return failureCount < 2;
    },
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 10000),
  });
};

/**
 * Hook to get recent issues for dashboard
 */
export const useRecentIssues = (limit: number = 10, enabled: boolean = true) => {
  return useQuery({
    queryKey: ['dashboard', 'recent-issues', limit],
    queryFn: () => dashboardService.getRecentIssues(limit),
    enabled,
    staleTime: 2 * 60 * 1000, // 2 minutes
    retry: (failureCount, error: any) => {
      if (error?.response?.status >= 400 && error?.response?.status < 500) {
        return false;
      }
      return failureCount < 2;
    },
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 10000),
  });
};

/**
 * Hook to get active sprint summary
 */
export const useActiveSprintSummary = (enabled: boolean = true) => {
  return useQuery({
    queryKey: ['dashboard', 'active-sprint'],
    queryFn: () => dashboardService.getActiveSprintSummary(),
    enabled,
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: (failureCount, error: any) => {
      if (error?.response?.status >= 400 && error?.response?.status < 500) {
        return false;
      }
      return failureCount < 2;
    },
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 10000),
  });
};

/**
 * Hook to get project statistics
 */
export const useProjectStatistics = (enabled: boolean = true) => {
  return useQuery({
    queryKey: ['dashboard', 'project-stats'],
    queryFn: () => dashboardService.getProjectStatistics(),
    enabled,
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: (failureCount, error: any) => {
      if (error?.response?.status >= 400 && error?.response?.status < 500) {
        return false;
      }
      return failureCount < 2;
    },
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 10000),
  });
};

/**
 * Hook to get issue distribution data
 */
export const useIssueDistribution = (enabled: boolean = true) => {
  return useQuery({
    queryKey: ['dashboard', 'issue-distribution'],
    queryFn: () => dashboardService.getIssueDistribution(),
    enabled,
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: (failureCount, error: any) => {
      if (error?.response?.status >= 400 && error?.response?.status < 500) {
        return false;
      }
      return failureCount < 2;
    },
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 10000),
  });
};

/**
 * Hook to get sprint burndown data
 */
export const useSprintBurndown = (sprintId: number | null, enabled: boolean = true) => {
  return useQuery({
    queryKey: ['dashboard', 'sprint-burndown', sprintId],
    queryFn: () => sprintId ? dashboardService.getSprintBurndown(sprintId) : Promise.resolve([]),
    enabled: enabled && !!sprintId,
    staleTime: 10 * 60 * 1000, // 10 minutes
    retry: (failureCount, error: any) => {
      if (error?.response?.status >= 400 && error?.response?.status < 500) {
        return false;
      }
      return failureCount < 2;
    },
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 10000),
  });
};

/**
 * Hook to get velocity data
 */
export const useVelocityData = (sprintCount: number = 6, enabled: boolean = true) => {
  return useQuery({
    queryKey: ['dashboard', 'velocity', sprintCount],
    queryFn: () => dashboardService.getVelocityData(sprintCount),
    enabled,
    staleTime: 30 * 60 * 1000, // 30 minutes
    retry: (failureCount, error: any) => {
      if (error?.response?.status >= 400 && error?.response?.status < 500) {
        return false;
      }
      return failureCount < 2;
    },
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 10000),
  });
};
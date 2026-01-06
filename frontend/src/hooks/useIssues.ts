import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { issueService } from '@/services/api/issue.service';
import { 
  mapIssuesToFrontend, 
  mapIssueToFrontend, 
  mapIssueToBackend,
  mapStatusToBackend,
  handleApiError,
  type FrontendIssue 
} from '@/utils/api-response';

// Query keys for React Query
export const issueKeys = {
  all: ['issues'] as const,
  lists: () => [...issueKeys.all, 'list'] as const,
  list: (filters: Record<string, any>) => [...issueKeys.lists(), { filters }] as const,
  details: () => [...issueKeys.all, 'detail'] as const,
  detail: (id: number) => [...issueKeys.details(), id] as const,
};

/**
 * Hook for fetching all issues with optional filters
 */
export const useIssues = (filters?: {
  projectId?: number;
  sprintId?: number;
  status?: string;
  priority?: string;
  search?: string;
} | undefined) => {
  const enabled = filters !== undefined;
  
  return useQuery({
    queryKey: issueKeys.list(filters || {}),
    queryFn: async () => {
      const response = await issueService.getIssues({
        size: 1000, // Get all issues for now
        ...(filters || {})
      } as any);
      return mapIssuesToFrontend(response.content);
    },
    enabled,
    staleTime: 2 * 60 * 1000, // 2 minutes
    gcTime: 5 * 60 * 1000, // 5 minutes
  });
};

/**
 * Hook for fetching a single issue by ID
 */
export const useIssue = (id: number) => {
  return useQuery({
    queryKey: issueKeys.detail(id),
    queryFn: async () => {
      const issue = await issueService.getIssue(id);
      return mapIssueToFrontend(issue);
    },
    enabled: !!id,
    staleTime: 2 * 60 * 1000,
    gcTime: 5 * 60 * 1000,
  });
};

/**
 * Hook for creating a new issue
 */
export const useCreateIssue = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (issueData: Partial<FrontendIssue>) => {
      const backendData = mapIssueToBackend(issueData);
      
      // Ensure required fields are present
      if (!backendData.title || !backendData.projectId || !backendData.issueTypeId) {
        throw new Error('Missing required fields for issue creation');
      }
      
      const newIssue = await issueService.createIssue(backendData as any);
      return mapIssueToFrontend(newIssue);
    },
    onSuccess: (newIssue) => {
      // Invalidate and refetch issues list
      queryClient.invalidateQueries({ queryKey: issueKeys.lists() });
      
      // Optimistically add to cache
      queryClient.setQueryData(issueKeys.detail(parseInt(newIssue.id)), newIssue);
      
      // Also invalidate projects to update issue counts
      queryClient.invalidateQueries({ queryKey: ['projects'] });
    },
    onError: (error) => {
      console.error('Failed to create issue:', error);
      throw new Error(handleApiError(error));
    },
  });
};

/**
 * Hook for updating an existing issue
 */
export const useUpdateIssue = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, data }: { id: number; data: Partial<FrontendIssue> }) => {
      const backendData = mapIssueToBackend(data);
      const updatedIssue = await issueService.updateIssue(id, backendData);
      return mapIssueToFrontend(updatedIssue);
    },
    onSuccess: (updatedIssue, { id }) => {
      // Update the specific issue in cache
      queryClient.setQueryData(issueKeys.detail(id), updatedIssue);
      
      // Invalidate issues list to ensure consistency
      queryClient.invalidateQueries({ queryKey: issueKeys.lists() });
    },
    onError: (error) => {
      console.error('Failed to update issue:', error);
      throw new Error(handleApiError(error));
    },
  });
};

/**
 * Hook for updating issue status
 */
export const useUpdateIssueStatus = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, status }: { id: number; status: string }) => {
      const statusUpdateRequest = mapStatusToBackend(status as any);
      const updatedIssue = await issueService.updateIssueStatus(id, statusUpdateRequest);
      return mapIssueToFrontend(updatedIssue);
    },
    onSuccess: (updatedIssue, { id }) => {
      // Update the specific issue in cache
      queryClient.setQueryData(issueKeys.detail(id), updatedIssue);
      
      // Invalidate issues list to ensure consistency
      queryClient.invalidateQueries({ queryKey: issueKeys.lists() });
    },
    onError: (error) => {
      console.error('Failed to update issue status:', error);
      throw new Error(handleApiError(error));
    },
  });
};

/**
 * Hook for fetching issues with filtering by project
 */
export const useIssuesByProject = (projectId?: number, additionalFilters?: {
  status?: string;
  priority?: string;
  search?: string;
}) => {
  const filters = projectId ? { projectId, ...additionalFilters } : undefined;
  return useIssues(filters);
};

/**
 * Hook for fetching issues with filtering by sprint
 */
export const useIssuesBySprint = (sprintId?: number, additionalFilters?: {
  status?: string;
  priority?: string;
  search?: string;
}) => {
  const filters = sprintId ? { sprintId, ...additionalFilters } : undefined;
  return useIssues(filters);
};
export const useDeleteIssue = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => {
      await issueService.deleteIssue(id);
      return id;
    },
    onSuccess: (deletedId) => {
      // Remove from cache
      queryClient.removeQueries({ queryKey: issueKeys.detail(deletedId) });
      
      // Invalidate issues list
      queryClient.invalidateQueries({ queryKey: issueKeys.lists() });
      
      // Also invalidate projects to update issue counts
      queryClient.invalidateQueries({ queryKey: ['projects'] });
    },
    onError: (error) => {
      console.error('Failed to delete issue:', error);
      throw new Error(handleApiError(error));
    },
  });
};
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { sprintService } from '@/services/api/sprint.service';
import { 
  mapSprintsToFrontend, 
  mapSprintToFrontend, 
  mapSprintToBackend,
  handleApiError,
  type FrontendSprint 
} from '@/utils/api-response';

// Query keys for React Query
export const sprintKeys = {
  all: ['sprints'] as const,
  lists: () => [...sprintKeys.all, 'list'] as const,
  list: (filters: Record<string, any>) => [...sprintKeys.lists(), { filters }] as const,
  details: () => [...sprintKeys.all, 'detail'] as const,
  detail: (id: number) => [...sprintKeys.details(), id] as const,
};

/**
 * Hook for fetching all sprints
 */
export const useSprints = (enabled: boolean = true) => {
  return useQuery({
    queryKey: sprintKeys.list({}),
    queryFn: async () => {
      const response = await sprintService.getSprints({
        size: 1000 // Get all sprints for now
      });
      return mapSprintsToFrontend(response.content);
    },
    enabled,
    staleTime: 2 * 60 * 1000, // 2 minutes
    gcTime: 5 * 60 * 1000, // 5 minutes
  });
};

/**
 * Hook for fetching a single sprint by ID
 */
export const useSprint = (id: number) => {
  return useQuery({
    queryKey: sprintKeys.detail(id),
    queryFn: async () => {
      const sprint = await sprintService.getSprint(id);
      return mapSprintToFrontend(sprint);
    },
    enabled: !!id,
    staleTime: 2 * 60 * 1000,
    gcTime: 5 * 60 * 1000,
  });
};

/**
 * Hook for creating a new sprint
 */
export const useCreateSprint = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (sprintData: Partial<FrontendSprint>) => {
      const backendData = mapSprintToBackend(sprintData);
      const newSprint = await sprintService.createSprint(backendData);
      return mapSprintToFrontend(newSprint);
    },
    onSuccess: (newSprint) => {
      // Invalidate and refetch sprints list
      queryClient.invalidateQueries({ queryKey: sprintKeys.lists() });
      
      // Optimistically add to cache
      queryClient.setQueryData(sprintKeys.detail(parseInt(newSprint.id)), newSprint);
    },
    onError: (error) => {
      console.error('Failed to create sprint:', error);
      throw new Error(handleApiError(error));
    },
  });
};

/**
 * Hook for updating an existing sprint
 */
export const useUpdateSprint = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, data }: { id: number; data: Partial<FrontendSprint> }) => {
      const backendData = {
        name: data.name,
        startDate: data.startDate,
        endDate: data.endDate,
        status: data.status as any,
      };
      const updatedSprint = await sprintService.updateSprint(id, backendData);
      return mapSprintToFrontend(updatedSprint);
    },
    onSuccess: (updatedSprint, { id }) => {
      // Update the specific sprint in cache
      queryClient.setQueryData(sprintKeys.detail(id), updatedSprint);
      
      // Invalidate sprints list to ensure consistency
      queryClient.invalidateQueries({ queryKey: sprintKeys.lists() });
    },
    onError: (error) => {
      console.error('Failed to update sprint:', error);
      throw new Error(handleApiError(error));
    },
  });
};

/**
 * Hook for deleting a sprint
 */
export const useDeleteSprint = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => {
      await sprintService.deleteSprint(id);
      return id;
    },
    onSuccess: (deletedId) => {
      // Remove from cache
      queryClient.removeQueries({ queryKey: sprintKeys.detail(deletedId) });
      
      // Invalidate sprints list
      queryClient.invalidateQueries({ queryKey: sprintKeys.lists() });
    },
    onError: (error) => {
      console.error('Failed to delete sprint:', error);
      throw new Error(handleApiError(error));
    },
  });
};

/**
 * Hook for starting a sprint
 */
export const useStartSprint = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => {
      const updatedSprint = await sprintService.startSprint(id);
      return mapSprintToFrontend(updatedSprint);
    },
    onSuccess: (updatedSprint, id) => {
      // Update the specific sprint in cache
      queryClient.setQueryData(sprintKeys.detail(id), updatedSprint);
      
      // Invalidate sprints list to ensure consistency
      queryClient.invalidateQueries({ queryKey: sprintKeys.lists() });
    },
    onError: (error) => {
      console.error('Failed to start sprint:', error);
      throw new Error(handleApiError(error));
    },
  });
};

/**
 * Hook for completing a sprint
 */
export const useCompleteSprint = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => {
      const updatedSprint = await sprintService.completeSprint(id);
      return mapSprintToFrontend(updatedSprint);
    },
    onSuccess: (updatedSprint, id) => {
      // Update the specific sprint in cache
      queryClient.setQueryData(sprintKeys.detail(id), updatedSprint);
      
      // Invalidate sprints list to ensure consistency
      queryClient.invalidateQueries({ queryKey: sprintKeys.lists() });
    },
    onError: (error) => {
      console.error('Failed to complete sprint:', error);
      throw new Error(handleApiError(error));
    },
  });
};
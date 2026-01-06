import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { labelService } from '@/services/api/label.service';
import { 
  mapLabelsToFrontend, 
  mapLabelToFrontend, 
  mapLabelToBackend,
  handleApiError,
  type FrontendLabel 
} from '@/utils/api-response';

// Query keys for React Query
export const labelKeys = {
  all: ['labels'] as const,
  lists: () => [...labelKeys.all, 'list'] as const,
  list: (filters: Record<string, any>) => [...labelKeys.lists(), { filters }] as const,
  details: () => [...labelKeys.all, 'detail'] as const,
  detail: (id: number) => [...labelKeys.details(), id] as const,
};

/**
 * Hook for fetching all labels
 */
export const useLabels = (enabled: boolean = true) => {
  return useQuery({
    queryKey: labelKeys.list({}),
    queryFn: async () => {
      const response = await labelService.getLabels({
        size: 1000 // Get all labels for now
      });
      return mapLabelsToFrontend(response.content);
    },
    enabled,
    staleTime: 5 * 60 * 1000, // 5 minutes (labels change less frequently)
    gcTime: 10 * 60 * 1000, // 10 minutes
  });
};

/**
 * Hook for fetching a single label by ID
 */
export const useLabel = (id: number) => {
  return useQuery({
    queryKey: labelKeys.detail(id),
    queryFn: async () => {
      const label = await labelService.getLabel(id);
      return mapLabelToFrontend(label);
    },
    enabled: !!id,
    staleTime: 5 * 60 * 1000,
    gcTime: 10 * 60 * 1000,
  });
};

/**
 * Hook for creating a new label
 */
export const useCreateLabel = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (labelData: Partial<FrontendLabel>) => {
      const backendData = mapLabelToBackend(labelData);
      const newLabel = await labelService.createLabel(backendData);
      return mapLabelToFrontend(newLabel);
    },
    onSuccess: (newLabel) => {
      // Invalidate and refetch labels list
      queryClient.invalidateQueries({ queryKey: labelKeys.lists() });
      
      // Optimistically add to cache
      queryClient.setQueryData(labelKeys.detail(parseInt(newLabel.id)), newLabel);
    },
    onError: (error) => {
      console.error('Failed to create label:', error);
      throw new Error(handleApiError(error));
    },
  });
};

/**
 * Hook for updating an existing label
 */
export const useUpdateLabel = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, data }: { id: number; data: Partial<FrontendLabel> }) => {
      const backendData = mapLabelToBackend(data);
      const updatedLabel = await labelService.updateLabel(id, backendData);
      return mapLabelToFrontend(updatedLabel);
    },
    onSuccess: (updatedLabel, { id }) => {
      // Update the specific label in cache
      queryClient.setQueryData(labelKeys.detail(id), updatedLabel);
      
      // Invalidate labels list to ensure consistency
      queryClient.invalidateQueries({ queryKey: labelKeys.lists() });
    },
    onError: (error) => {
      console.error('Failed to update label:', error);
      throw new Error(handleApiError(error));
    },
  });
};

/**
 * Hook for deleting a label
 */
export const useDeleteLabel = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => {
      await labelService.deleteLabel(id);
      return id;
    },
    onSuccess: (deletedId) => {
      // Remove from cache
      queryClient.removeQueries({ queryKey: labelKeys.detail(deletedId) });
      
      // Invalidate labels list
      queryClient.invalidateQueries({ queryKey: labelKeys.lists() });
    },
    onError: (error) => {
      console.error('Failed to delete label:', error);
      throw new Error(handleApiError(error));
    },
  });
};
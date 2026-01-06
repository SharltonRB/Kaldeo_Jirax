import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useMemo } from 'react';
import { projectService } from '@/services/api/project.service';
import { 
  mapProjectsToFrontend, 
  mapProjectToFrontend, 
  mapProjectToBackend,
  handleApiError,
  type FrontendProject 
} from '@/utils/api-response';

// Debounce utility for search
const useDebounce = (value: string, delay: number) => {
  return useMemo(() => {
    const handler = setTimeout(() => value, delay);
    return () => clearTimeout(handler);
  }, [value, delay]);
};

// Query keys for React Query
export const projectKeys = {
  all: ['projects'] as const,
  lists: () => [...projectKeys.all, 'list'] as const,
  list: (filters: Record<string, any>) => [...projectKeys.lists(), { filters }] as const,
  details: () => [...projectKeys.all, 'detail'] as const,
  detail: (id: number) => [...projectKeys.details(), id] as const,
};

/**
 * Hook for fetching all projects with optional search (debounced)
 */
export const useProjects = (search?: string) => {
  // Only run query if search is defined (user is authenticated)
  const isEnabled = search !== undefined;
  
  // Debounce search to avoid too many API calls
  const debouncedSearch = useMemo(() => {
    if (!isEnabled) return undefined; // Don't search if not authenticated
    if (!search || search.trim().length === 0) return '';
    return search.trim();
  }, [search, isEnabled]);

  return useQuery({
    queryKey: projectKeys.list({ search: debouncedSearch }),
    queryFn: async () => {
      const response = await projectService.getProjects({ 
        size: 1000, // Get all projects for now
        ...(debouncedSearch ? { search: debouncedSearch } : {})
      });
      return mapProjectsToFrontend(response.content);
    },
    enabled: isEnabled, // Only run query if user is authenticated
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 10 * 60 * 1000, // 10 minutes
  });
};

/**
 * Hook for fetching a single project by ID
 */
export const useProject = (id: number) => {
  return useQuery({
    queryKey: projectKeys.detail(id),
    queryFn: async () => {
      const project = await projectService.getProject(id);
      return mapProjectToFrontend(project);
    },
    enabled: !!id,
    staleTime: 5 * 60 * 1000,
    gcTime: 10 * 60 * 1000,
  });
};

/**
 * Hook for creating a new project
 */
export const useCreateProject = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (projectData: Partial<FrontendProject>) => {
      const backendData = mapProjectToBackend(projectData);
      const newProject = await projectService.createProject(backendData);
      return mapProjectToFrontend(newProject);
    },
    onSuccess: (newProject) => {
      // Invalidate and refetch projects list
      queryClient.invalidateQueries({ queryKey: projectKeys.lists() });
      
      // Optimistically add to cache
      queryClient.setQueryData(projectKeys.detail(parseInt(newProject.id)), newProject);
    },
    onError: (error) => {
      console.error('Failed to create project:', error);
      throw new Error(handleApiError(error));
    },
  });
};

/**
 * Hook for updating an existing project
 */
export const useUpdateProject = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, data }: { id: number; data: Partial<FrontendProject> }) => {
      const backendData = {
        name: data.name,
        description: data.description,
      };
      const updatedProject = await projectService.updateProject(id, backendData);
      return mapProjectToFrontend(updatedProject);
    },
    onSuccess: (updatedProject, { id }) => {
      // Update the specific project in cache
      queryClient.setQueryData(projectKeys.detail(id), updatedProject);
      
      // Invalidate projects list to ensure consistency
      queryClient.invalidateQueries({ queryKey: projectKeys.lists() });
    },
    onError: (error) => {
      console.error('Failed to update project:', error);
      throw new Error(handleApiError(error));
    },
  });
};

/**
 * Hook for deleting a project
 */
export const useDeleteProject = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => {
      await projectService.deleteProject(id);
      return id;
    },
    onSuccess: (deletedId) => {
      // Remove from cache
      queryClient.removeQueries({ queryKey: projectKeys.detail(deletedId) });
      
      // Invalidate projects list
      queryClient.invalidateQueries({ queryKey: projectKeys.lists() });
    },
    onError: (error) => {
      console.error('Failed to delete project:', error);
      throw new Error(handleApiError(error));
    },
  });
};

/**
 * Hook for checking project key availability
 */
export const useCheckProjectKey = () => {
  return useMutation({
    mutationFn: async (key: string) => {
      const result = await projectService.checkProjectKeyAvailability(key);
      return result.available;
    },
    onError: (error) => {
      console.error('Failed to check project key:', error);
      // Don't throw error for key checking, just return false
      return false;
    },
  });
};

/**
 * Utility hook to get project by string ID (for compatibility with existing frontend)
 */
export const useProjectByStringId = (stringId: string | undefined) => {
  const numericId = stringId ? parseInt(stringId) : undefined;
  return useProject(numericId!);
};
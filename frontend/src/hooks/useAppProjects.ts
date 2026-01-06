import { useCallback, useRef, useEffect } from 'react';
import { 
  useProjects, 
  useCreateProject, 
  useDeleteProject, 
  useCheckProjectKey 
} from './useProjects';
import type { FrontendProject } from '@/utils/api-response';

/**
 * Hook that provides project management functions compatible with existing AppContext
 * This replaces the mock project functions with real API calls
 */
export const useAppProjects = (searchQuery?: string) => {
  // React Query hooks - only execute if searchQuery is defined (user is authenticated)
  const { data: projects = [], isLoading, error, refetch } = useProjects(searchQuery);
  const createProjectMutation = useCreateProject();
  const deleteProjectMutation = useDeleteProject();
  const checkKeyMutation = useCheckProjectKey();

  // Use useRef for cache to avoid re-renders
  const keyValidationCacheRef = useRef<Record<string, boolean>>({});

  // Compatible functions for existing AppContext
  const createProject = useCallback(async (data: Partial<FrontendProject>) => {
    try {
      await createProjectMutation.mutateAsync(data);
      // Clear validation cache for the created key
      if (data.key) {
        delete keyValidationCacheRef.current[data.key];
      }
    } catch (error) {
      console.error('Failed to create project:', error);
      throw error;
    }
  }, [createProjectMutation]);

  const deleteProject = useCallback(async (projectId: string) => {
    try {
      const numericId = parseInt(projectId);
      await deleteProjectMutation.mutateAsync(numericId);
    } catch (error) {
      console.error('Failed to delete project:', error);
      throw error;
    }
  }, [deleteProjectMutation]);

  const checkProjectKeyAvailability = useCallback(async (key: string): Promise<boolean> => {
    if (!key || key.trim().length === 0) return false;
    
    const trimmedKey = key.trim().toUpperCase();
    
    // Check cache first
    if (keyValidationCacheRef.current[trimmedKey] !== undefined) {
      return keyValidationCacheRef.current[trimmedKey];
    }

    try {
      const available = await checkKeyMutation.mutateAsync(trimmedKey);
      
      // Cache the result
      keyValidationCacheRef.current[trimmedKey] = available;
      
      return available;
    } catch (error) {
      console.error('Failed to check project key:', error);
      return false; // Assume not available on error
    }
  }, [checkKeyMutation]);

  // Clear validation cache when projects change
  useEffect(() => {
    keyValidationCacheRef.current = {};
  }, [projects]);

  // Loading states
  const isCreating = createProjectMutation.isPending;
  const isDeleting = deleteProjectMutation.isPending;
  const isCheckingKey = checkKeyMutation.isPending;

  return {
    // Data
    projects,
    isLoading: searchQuery !== undefined ? isLoading : false, // Don't show loading if not authenticated
    error,
    
    // Actions
    createProject,
    deleteProject,
    checkProjectKeyAvailability,
    refetch,
    
    // Loading states
    isCreating,
    isDeleting,
    isCheckingKey,
    
    // For compatibility with existing code
    isProjectsLoading: searchQuery !== undefined ? isLoading : false,
  };
};
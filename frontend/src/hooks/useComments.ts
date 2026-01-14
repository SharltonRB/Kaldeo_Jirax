import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { commentService } from '@/services/api/comment.service';
import { Comment, CreateCommentRequest, UpdateCommentRequest, PageRequest } from '@/types';
import { useToast } from '@/context/ToastContext';

/**
 * Hook to get comments for an issue
 */
export const useIssueComments = (issueId: number | null, params?: PageRequest) => {
  return useQuery({
    queryKey: ['comments', 'issue', issueId, params],
    queryFn: () => {
      if (!issueId) {
        return Promise.resolve({ 
          content: [], 
          totalElements: 0, 
          totalPages: 0, 
          size: 0, 
          number: 0,
          first: true,
          last: true
        });
      }
      return commentService.getIssueComments(issueId, params);
    },
    enabled: !!issueId,
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
};

/**
 * Hook to get a single comment
 */
export const useComment = (id: number | null) => {
  return useQuery({
    queryKey: ['comments', id],
    queryFn: () => id ? commentService.getComment(id) : Promise.resolve(null),
    enabled: !!id,
  });
};

/**
 * Hook to create a new comment
 */
export const useCreateComment = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: ({ issueId, data }: { issueId: number; data: CreateCommentRequest }) =>
      commentService.createComment(issueId, data),
    onSuccess: (newComment, { issueId }) => {
      // Invalidate and refetch issue comments
      queryClient.invalidateQueries({ queryKey: ['comments', 'issue', issueId] });
      // Also invalidate recent comments
      queryClient.invalidateQueries({ queryKey: ['comments', 'recent'] });
      // Invalidate issues to update comment count
      queryClient.invalidateQueries({ queryKey: ['issues'] });
      
      showSuccess('Comment Added', 'Your comment has been added successfully.');
    },
    onError: (error: any) => {
      console.error('Failed to create comment:', error);
      showError('Comment Failed', error.message || 'Failed to add comment. Please try again.');
    },
  });
};

/**
 * Hook to update a comment
 */
export const useUpdateComment = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateCommentRequest }) =>
      commentService.updateComment(id, data),
    onSuccess: (updatedComment) => {
      // Invalidate and refetch the specific comment
      queryClient.invalidateQueries({ queryKey: ['comments', updatedComment.id] });
      // Invalidate issue comments
      queryClient.invalidateQueries({ queryKey: ['comments', 'issue'] });
      // Invalidate recent comments
      queryClient.invalidateQueries({ queryKey: ['comments', 'recent'] });
      
      showSuccess('Comment Updated', 'Your comment has been updated successfully.');
    },
    onError: (error: any) => {
      console.error('Failed to update comment:', error);
      showError('Update Failed', error.message || 'Failed to update comment. Please try again.');
    },
  });
};

/**
 * Hook to delete a comment
 */
export const useDeleteComment = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: (id: number) => commentService.deleteComment(id),
    onSuccess: (_, deletedId) => {
      // Invalidate all comment-related queries
      queryClient.invalidateQueries({ queryKey: ['comments'] });
      // Invalidate issues to update comment count
      queryClient.invalidateQueries({ queryKey: ['issues'] });
      
      showSuccess('Comment Deleted', 'Comment has been deleted successfully.');
    },
    onError: (error: any) => {
      console.error('Failed to delete comment:', error);
      showError('Delete Failed', error.message || 'Failed to delete comment. Please try again.');
    },
  });
};

/**
 * Hook to get recent comments across all issues
 */
export const useRecentComments = (params?: PageRequest) => {
  return useQuery({
    queryKey: ['comments', 'recent', params],
    queryFn: () => commentService.getRecentComments(params),
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
};
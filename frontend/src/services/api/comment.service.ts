import { api } from './client';
import { 
  Comment, 
  CreateCommentRequest, 
  UpdateCommentRequest,
  PageRequest,
  PageResponse 
} from '@/types';

class CommentApiService {
  private baseUrl = '/comments';

  /**
   * Get comments for an issue
   */
  async getIssueComments(issueId: number, params?: PageRequest): Promise<PageResponse<Comment>> {
    const queryParams = new URLSearchParams();
    
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          queryParams.append(key, String(value));
        }
      });
    }

    const url = queryParams.toString() 
      ? `/issues/${issueId}/comments?${queryParams.toString()}`
      : `/issues/${issueId}/comments`;

    return api.get<PageResponse<Comment>>(url);
  }

  /**
   * Get comment by ID
   */
  async getComment(id: number): Promise<Comment> {
    return api.get<Comment>(`${this.baseUrl}/${id}`);
  }

  /**
   * Create new comment
   */
  async createComment(issueId: number, data: CreateCommentRequest): Promise<Comment> {
    return api.post<Comment>(`/issues/${issueId}/comments`, data);
  }

  /**
   * Update existing comment
   */
  async updateComment(id: number, data: UpdateCommentRequest): Promise<Comment> {
    return api.put<Comment>(`${this.baseUrl}/${id}`, data);
  }

  /**
   * Delete comment
   */
  async deleteComment(id: number): Promise<void> {
    return api.delete<void>(`${this.baseUrl}/${id}`);
  }

  /**
   * Get recent comments across all issues
   */
  async getRecentComments(params?: PageRequest): Promise<PageResponse<Comment>> {
    const queryParams = new URLSearchParams();
    
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          queryParams.append(key, String(value));
        }
      });
    }

    const url = queryParams.toString() 
      ? `${this.baseUrl}/recent?${queryParams.toString()}`
      : `${this.baseUrl}/recent`;

    return api.get<PageResponse<Comment>>(url);
  }
}

export const commentService = new CommentApiService();
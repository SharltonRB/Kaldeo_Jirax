import { api, setTokens, clearTokens } from './client';
import { 
  AuthResponse, 
  LoginRequest, 
  RegisterRequest, 
  User 
} from '@/types';

export const authService = {
  /**
   * Login user with email and password
   */
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/login', credentials);
    
    // Store tokens in localStorage
    setTokens(response.access_token, response.refresh_token);
    
    return response;
  },

  /**
   * Register new user
   */
  async register(userData: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/register', userData);
    
    // Store tokens in localStorage
    setTokens(response.access_token, response.refresh_token);
    
    return response;
  },

  /**
   * Refresh access token
   */
  async refreshToken(refreshToken: string): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/refresh', {
      refreshToken,
    });
    
    // Update stored tokens
    setTokens(response.access_token, response.refresh_token);
    
    return response;
  },

  /**
   * Logout user (clear tokens)
   */
  logout(): void {
    clearTokens();
  },

  /**
   * Get current user profile
   */
  async getCurrentUser(): Promise<User> {
    return await api.get<User>('/auth/me');
  },

  /**
   * Check if user is authenticated (has valid token)
   */
  isAuthenticated(): boolean {
    const token = localStorage.getItem('access_token');
    if (!token) return false;

    try {
      // Basic JWT token validation (check if not expired)
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Date.now() / 1000;
      return payload.exp > currentTime;
    } catch {
      return false;
    }
  },
};
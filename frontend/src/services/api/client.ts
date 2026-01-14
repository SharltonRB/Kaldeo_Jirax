import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { ApiError } from '@/types';
import { tokenStorage } from '@/utils/secure-storage';

// Create axios instance with base configuration
const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.MODE === 'development' ? '/api' : import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: parseInt(import.meta.env.VITE_API_TIMEOUT || '10000'),
  headers: {
    'Content-Type': 'application/json',
  },
});

// Token management using secure storage
export const getAccessToken = async (): Promise<string | null> => {
  return await tokenStorage.getAccessToken();
};

export const getRefreshToken = async (): Promise<string | null> => {
  return await tokenStorage.getRefreshToken();
};

export const setTokens = async (accessToken: string, refreshToken: string): Promise<void> => {
  await tokenStorage.setAccessToken(accessToken);
  await tokenStorage.setRefreshToken(refreshToken);
};

export const clearTokens = (): void => {
  tokenStorage.clearTokens();
};

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  async (config) => {
    const token = await getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // Add CSRF protection for state-changing requests
    if (['post', 'put', 'patch', 'delete'].includes(config.method?.toLowerCase() || '')) {
      // Add CSRF token if available
      const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
      if (csrfToken) {
        config.headers['X-CSRF-Token'] = csrfToken;
      }
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling and token refresh
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    // If the error is 401 and we haven't already tried to refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      const refreshToken = await getRefreshToken();
      if (refreshToken) {
        try {
          // Try to refresh the token
          const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL || '/api'}/auth/refresh`, {
            refreshToken,
          });

          const { access_token, refresh_token: newRefreshToken } = response.data;
          await setTokens(access_token, newRefreshToken);

          // Retry the original request with new token
          originalRequest.headers.Authorization = `Bearer ${access_token}`;
          return apiClient(originalRequest);
        } catch (refreshError) {
          // Refresh failed, clear tokens and redirect to login
          clearTokens();
          // Don't redirect automatically, let the auth context handle it
          return Promise.reject(refreshError);
        }
      } else {
        // No refresh token, clear tokens
        clearTokens();
      }
    }

    // Handle rate limiting
    if (error.response?.status === 429) {
      const retryAfter = error.response.headers['retry-after'];
      const rateLimitReset = error.response.headers['x-ratelimit-reset'];
      
      console.warn('Rate limit exceeded:', {
        retryAfter,
        rateLimitReset,
        endpoint: error.config?.url,
      });
    }

    // Transform error to our ApiError format
    const apiError: ApiError = {
      message: error.response?.data?.message || error.message || 'An error occurred',
      status: error.response?.status || 500,
      timestamp: new Date().toISOString(),
      path: error.config?.url || '',
    };

    return Promise.reject(apiError);
  }
);

// Generic API methods with enhanced security
export const api = {
  get: <T>(url: string, config?: AxiosRequestConfig): Promise<T> =>
    apiClient.get(url, config).then((response) => response.data),

  post: <T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> =>
    apiClient.post(url, data, config).then((response) => response.data),

  put: <T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> =>
    apiClient.put(url, data, config).then((response) => response.data),

  patch: <T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> =>
    apiClient.patch(url, data, config).then((response) => response.data),

  delete: <T>(url: string, config?: AxiosRequestConfig): Promise<T> =>
    apiClient.delete(url, config).then((response) => response.data),
};

export default apiClient;
import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { authService } from '@/services/api';
import { FrontendUser, mapUserToFrontend, handleApiError } from '@/utils/api-response';
import { tokenStorage, secureStorage } from '@/utils/secure-storage';
import type { LoginRequest, RegisterRequest } from '@/types';

interface AuthContextType {
  user: FrontendUser | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (userData: RegisterRequest) => Promise<void>;
  logout: () => void;
  error: string | null;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<FrontendUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const queryClient = useQueryClient();

  const isAuthenticated = !!user;

  // Initialize auth state on app load
  useEffect(() => {
    const initializeAuth = async () => {
      try {
        // Check if session is still valid
        if (!secureStorage.isSessionValid()) {
          // Session expired, clear everything
          tokenStorage.clearTokens();
          tokenStorage.clearUserData();
          setIsLoading(false);
          return;
        }

        // Try to get stored user data first
        const storedUserData = await tokenStorage.getUserData();
        if (storedUserData) {
          setUser(storedUserData);
        }

        // Verify authentication with backend (with error handling)
        if (authService.isAuthenticated()) {
          try {
            const currentUser = await authService.getCurrentUser();
            const frontendUser = mapUserToFrontend(currentUser);
            setUser(frontendUser);
            
            // Update stored user data
            await tokenStorage.setUserData(frontendUser);
          } catch (error) {
            console.error('Failed to verify authentication:', error);
            // Don't fail completely, just clear invalid tokens
            authService.logout();
            tokenStorage.clearUserData();
            setUser(null);
          }
        }
      } catch (error) {
        console.error('Failed to initialize auth:', error);
        // Clear invalid tokens and user data
        authService.logout();
        tokenStorage.clearUserData();
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };

    initializeAuth();
  }, []);

  const login = async (credentials: LoginRequest): Promise<void> => {
    try {
      setIsLoading(true);
      setError(null);
      
      // Clear all cached data before login
      queryClient.clear();
      
      const response = await authService.login(credentials);
      const frontendUser = mapUserToFrontend(response.user);
      
      setUser(frontendUser);
      
      // Store user data securely
      await tokenStorage.setUserData(frontendUser);
      
      // Update session timestamp
      secureStorage.updateSessionTimestamp();
    } catch (error) {
      const errorMessage = handleApiError(error);
      setError(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (userData: RegisterRequest): Promise<void> => {
    try {
      setIsLoading(true);
      setError(null);
      
      // Clear all cached data before register
      queryClient.clear();
      
      const response = await authService.register(userData);
      const frontendUser = mapUserToFrontend(response.user);
      
      setUser(frontendUser);
      
      // Store user data securely
      await tokenStorage.setUserData(frontendUser);
      
      // Update session timestamp
      secureStorage.updateSessionTimestamp();
    } catch (error) {
      const errorMessage = handleApiError(error);
      setError(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const logout = (): void => {
    authService.logout();
    tokenStorage.clearTokens();
    tokenStorage.clearUserData();
    secureStorage.clear();
    setUser(null);
    setError(null);
    
    // Clear all cached data on logout
    queryClient.clear();
  };

  const clearError = (): void => {
    setError(null);
  };

  const value: AuthContextType = {
    user,
    isLoading,
    isAuthenticated,
    login,
    register,
    logout,
    error,
    clearError,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
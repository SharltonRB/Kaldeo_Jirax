import { useEffect, useCallback, useRef, useState } from 'react';
import { secureStorage, SESSION_CONFIG } from '@/utils/secure-storage';
import { useAuth } from '@/context/AuthContext';

interface UseSessionTimeoutOptions {
  onWarning?: () => void;
  onTimeout?: () => void;
  warningTime?: number;
  maxIdleTime?: number;
}

interface SessionTimeoutState {
  remainingTime: number;
  showWarning: boolean;
  isActive: boolean;
}

/**
 * Hook for managing session timeout and automatic logout.
 * Monitors user activity and provides warnings before session expiration.
 */
export const useSessionTimeout = (options: UseSessionTimeoutOptions = {}) => {
  const {
    onWarning,
    onTimeout,
    warningTime = SESSION_CONFIG.WARNING_TIME,
    maxIdleTime = SESSION_CONFIG.MAX_IDLE_TIME,
  } = options;

  const { logout, isAuthenticated } = useAuth();
  const [sessionState, setSessionState] = useState<SessionTimeoutState>({
    remainingTime: maxIdleTime,
    showWarning: false,
    isActive: true,
  });

  const warningShownRef = useRef(false);
  const timeoutIdRef = useRef<NodeJS.Timeout | null>(null);
  const intervalIdRef = useRef<NodeJS.Timeout | null>(null);
  const warningTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const lastActivityRef = useRef(Date.now());

  /**
   * Clear all timers
   */
  const clearAllTimers = useCallback(() => {
    if (timeoutIdRef.current) {
      clearTimeout(timeoutIdRef.current);
      timeoutIdRef.current = null;
    }
    if (intervalIdRef.current) {
      clearInterval(intervalIdRef.current);
      intervalIdRef.current = null;
    }
    if (warningTimeoutRef.current) {
      clearTimeout(warningTimeoutRef.current);
      warningTimeoutRef.current = null;
    }
  }, []);

  /**
   * Update session activity timestamp and reset timers.
   */
  const updateActivity = useCallback(() => {
    if (!isAuthenticated) return;

    const now = Date.now();
    lastActivityRef.current = now;
    secureStorage.updateSessionTimestamp();
    warningShownRef.current = false;
    
    // Clear existing timers
    clearAllTimers();
    
    // Reset state
    setSessionState({
      remainingTime: maxIdleTime,
      showWarning: false,
      isActive: true,
    });

    // Set warning timer
    warningTimeoutRef.current = setTimeout(() => {
      if (!warningShownRef.current && isAuthenticated) {
        warningShownRef.current = true;
        setSessionState(prev => ({
          ...prev,
          showWarning: true,
        }));
        onWarning?.();
      }
    }, maxIdleTime - warningTime);

    // Set logout timer
    timeoutIdRef.current = setTimeout(() => {
      if (isAuthenticated) {
        onTimeout?.();
        logout();
      }
    }, maxIdleTime);

  }, [isAuthenticated, maxIdleTime, warningTime, onWarning, onTimeout, logout, clearAllTimers]);

  /**
   * Extend the current session.
   */
  const extendSession = useCallback(() => {
    updateActivity();
  }, [updateActivity]);

  /**
   * Manually trigger session timeout.
   */
  const forceTimeout = useCallback(() => {
    clearAllTimers();
    onTimeout?.();
    logout();
  }, [onTimeout, logout, clearAllTimers]);

  // Set up activity listeners
  useEffect(() => {
    if (!isAuthenticated) {
      clearAllTimers();
      return;
    }

    // Initialize session
    if (secureStorage.isSessionValid()) {
      updateActivity();
    } else {
      // Session already expired
      logout();
      return;
    }

    // Activity events to monitor
    const activityEvents = [
      'mousedown',
      'mousemove',
      'keypress',
      'scroll',
      'touchstart',
      'click',
    ];

    // Throttle activity updates to avoid excessive calls
    const throttleDelay = 5000; // 5 seconds

    const handleActivity = () => {
      const now = Date.now();
      if (now - lastActivityRef.current > throttleDelay) {
        updateActivity();
      }
    };

    // Add event listeners with passive option for better performance
    activityEvents.forEach(event => {
      document.addEventListener(event, handleActivity, { passive: true, capture: true });
    });

    // Handle visibility change (tab switching)
    const handleVisibilityChange = () => {
      if (!document.hidden && isAuthenticated) {
        // Tab became visible, check session validity
        if (!secureStorage.isSessionValid()) {
          logout();
        } else {
          updateActivity();
        }
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);

    // Cleanup function
    return () => {
      activityEvents.forEach(event => {
        document.removeEventListener(event, handleActivity, true);
      });
      document.removeEventListener('visibilitychange', handleVisibilityChange);
      clearAllTimers();
    };
  }, [isAuthenticated, updateActivity, logout, clearAllTimers]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      clearAllTimers();
    };
  }, [clearAllTimers]);

  return {
    ...sessionState,
    extendSession,
    forceTimeout,
    formatRemainingTime: (time: number) => {
      const minutes = Math.floor(time / 60000);
      const seconds = Math.floor((time % 60000) / 1000);
      return `${minutes}:${seconds.toString().padStart(2, '0')}`;
    },
  };
};
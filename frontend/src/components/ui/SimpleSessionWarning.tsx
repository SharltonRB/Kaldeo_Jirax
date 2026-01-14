import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '@/context/AuthContext';

/**
 * Simplified session warning component that doesn't cause infinite re-renders.
 * Only shows when session is actually about to expire.
 */
export const SimpleSessionWarning: React.FC = () => {
  const { isAuthenticated, logout } = useAuth();
  const [showWarning, setShowWarning] = useState(false);
  const [remainingTime, setRemainingTime] = useState(0);

  const handleExtend = useCallback(() => {
    // Simply hide the warning and update session timestamp
    setShowWarning(false);
    localStorage.setItem('lastActivity', Date.now().toString());
  }, []);

  const handleLogout = useCallback(() => {
    logout();
  }, [logout]);

  useEffect(() => {
    if (!isAuthenticated) {
      setShowWarning(false);
      return;
    }

    // Check session every 30 seconds instead of every second
    const interval = setInterval(() => {
      const lastActivity = parseInt(localStorage.getItem('lastActivity') || '0');
      const now = Date.now();
      const timeSinceActivity = now - lastActivity;
      const sessionTimeout = 30 * 60 * 1000; // 30 minutes
      const warningTime = 5 * 60 * 1000; // 5 minutes before timeout

      if (timeSinceActivity > sessionTimeout) {
        // Session expired
        logout();
      } else if (timeSinceActivity > sessionTimeout - warningTime) {
        // Show warning
        const remaining = sessionTimeout - timeSinceActivity;
        setRemainingTime(remaining);
        setShowWarning(true);
      } else {
        setShowWarning(false);
      }
    }, 30000); // Check every 30 seconds

    // Update activity on mount
    localStorage.setItem('lastActivity', Date.now().toString());

    return () => clearInterval(interval);
  }, [isAuthenticated, logout]);

  if (!showWarning) {
    return null;
  }

  const formatTime = (ms: number) => {
    const minutes = Math.floor(ms / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="bg-white/10 backdrop-blur-md border border-white/20 rounded-2xl p-6 max-w-md mx-4 shadow-2xl">
        <div className="text-center">
          <div className="mx-auto w-16 h-16 bg-yellow-500/20 rounded-full flex items-center justify-center mb-4">
            <svg
              className="w-8 h-8 text-yellow-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z"
              />
            </svg>
          </div>

          <h3 className="text-xl font-semibold text-white mb-2">
            Session Expiring Soon
          </h3>
          <p className="text-gray-300 mb-4">
            Your session will expire in{' '}
            <span className="font-mono font-bold text-yellow-400">
              {formatTime(remainingTime)}
            </span>
          </p>

          <div className="flex gap-3">
            <button
              onClick={handleLogout}
              className="flex-1 px-4 py-2 bg-red-500/20 hover:bg-red-500/30 border border-red-500/30 rounded-lg text-red-300 hover:text-red-200 transition-all duration-200"
            >
              Logout Now
            </button>
            <button
              onClick={handleExtend}
              className="flex-1 px-4 py-2 bg-blue-500/20 hover:bg-blue-500/30 border border-blue-500/30 rounded-lg text-blue-300 hover:text-blue-200 transition-all duration-200"
            >
              Stay Logged In
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SimpleSessionWarning;
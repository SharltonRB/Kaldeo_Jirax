import React from 'react';
import { useSessionTimeout } from '@/hooks/useSessionTimeout';

interface SessionWarningProps {
  onExtend?: () => void;
  onLogout?: () => void;
}

/**
 * Session warning component that displays when the user's session is about to expire.
 * Provides options to extend the session or logout immediately.
 */
export const SessionWarning: React.FC<SessionWarningProps> = ({
  onExtend,
  onLogout,
}) => {
  const {
    remainingTime,
    showWarning,
    extendSession,
    forceTimeout,
    formatRemainingTime,
  } = useSessionTimeout({
    onWarning: () => {
      // Could add sound notification or other alerts here
      console.warn('Session expiring soon');
    },
    onTimeout: () => {
      console.info('Session expired due to inactivity');
    },
  });

  const handleExtend = () => {
    extendSession();
    onExtend?.();
  };

  const handleLogout = () => {
    forceTimeout();
    onLogout?.();
  };

  if (!showWarning) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="bg-white/10 backdrop-blur-md border border-white/20 rounded-2xl p-6 max-w-md mx-4 shadow-2xl">
        <div className="text-center">
          {/* Warning Icon */}
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

          {/* Warning Message */}
          <h3 className="text-xl font-semibold text-white mb-2">
            Session Expiring Soon
          </h3>
          <p className="text-gray-300 mb-4">
            Your session will expire in{' '}
            <span className="font-mono font-bold text-yellow-400">
              {formatRemainingTime(remainingTime)}
            </span>
          </p>
          <p className="text-sm text-gray-400 mb-6">
            You will be automatically logged out due to inactivity.
          </p>

          {/* Action Buttons */}
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

          {/* Security Notice */}
          <p className="text-xs text-gray-500 mt-4">
            This is a security feature to protect your account from unauthorized access.
          </p>
        </div>
      </div>
    </div>
  );
};

export default SessionWarning;
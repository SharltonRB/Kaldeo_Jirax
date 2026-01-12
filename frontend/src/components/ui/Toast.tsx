import React, { useEffect, useState } from 'react';
import { CheckCircle2, AlertTriangle, X, AlertCircle, Info } from 'lucide-react';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface ToastProps {
  id: string;
  type: ToastType;
  title: string;
  message?: string;
  duration?: number;
  onClose: (id: string) => void;
}

const Toast: React.FC<ToastProps> = ({ 
  id, 
  type, 
  title, 
  message, 
  duration = 5000, 
  onClose 
}) => {
  const [isVisible, setIsVisible] = useState(false);
  const [isLeaving, setIsLeaving] = useState(false);

  useEffect(() => {
    // Animate in
    const timer = setTimeout(() => setIsVisible(true), 10);
    
    // Auto dismiss
    const dismissTimer = setTimeout(() => {
      handleClose();
    }, duration);

    return () => {
      clearTimeout(timer);
      clearTimeout(dismissTimer);
    };
  }, [duration]);

  const handleClose = () => {
    setIsLeaving(true);
    setTimeout(() => {
      onClose(id);
    }, 300);
  };

  const getIcon = () => {
    switch (type) {
      case 'success':
        return <CheckCircle2 className="w-5 h-5 text-green-500" />;
      case 'error':
        return <AlertCircle className="w-5 h-5 text-red-500" />;
      case 'warning':
        return <AlertTriangle className="w-5 h-5 text-yellow-500" />;
      case 'info':
        return <Info className="w-5 h-5 text-blue-500" />;
      default:
        return <Info className="w-5 h-5 text-blue-500" />;
    }
  };

  const getColorClasses = () => {
    switch (type) {
      case 'success':
        return 'border-green-200 dark:border-green-900/30 bg-green-50/80 dark:bg-green-900/20';
      case 'error':
        return 'border-red-200 dark:border-red-900/30 bg-red-50/80 dark:bg-red-900/20';
      case 'warning':
        return 'border-yellow-200 dark:border-yellow-900/30 bg-yellow-50/80 dark:bg-yellow-900/20';
      case 'info':
        return 'border-blue-200 dark:border-blue-900/30 bg-blue-50/80 dark:bg-blue-900/20';
      default:
        return 'border-gray-200 dark:border-gray-700 bg-white/80 dark:bg-gray-800/80';
    }
  };

  return (
    <div
      className={`
        fixed top-4 right-4 z-[100] max-w-sm w-full sm:w-96
        transform transition-all duration-300 ease-out
        ${isVisible && !isLeaving 
          ? 'translate-x-0 opacity-100 scale-100' 
          : 'translate-x-full opacity-0 scale-95'
        }
      `}
    >
      <div
        className={`
          backdrop-blur-xl border rounded-xl p-4 shadow-2xl min-w-[320px]
          ${getColorClasses()}
        `}
      >
        <div className="flex items-start gap-3">
          <div className="flex-shrink-0 mt-0.5">
            {getIcon()}
          </div>
          
          <div className="flex-1 min-w-0">
            <h4 className="text-sm font-semibold text-gray-900 dark:text-white">
              {title}
            </h4>
            {message && (
              <p className="mt-1 text-xs text-gray-600 dark:text-gray-300">
                {message}
              </p>
            )}
          </div>
          
          <button
            onClick={handleClose}
            className="flex-shrink-0 p-1 rounded-lg hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          >
            <X className="w-4 h-4 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200" />
          </button>
        </div>
      </div>
    </div>
  );
};

export default Toast;
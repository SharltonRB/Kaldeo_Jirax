/**
 * Lazy-loaded components for better performance and code splitting.
 * These components are loaded on-demand to reduce initial bundle size.
 * 
 * Note: This file contains placeholder lazy loading setup.
 * In a real implementation, these would point to actual component files.
 */

import React, { Suspense } from 'react';
import { DashboardSkeleton, ProjectsListSkeleton, IssuesListSkeleton, SprintsListSkeleton, KanbanBoardSkeleton, FormSkeleton } from './ui/SkeletonLoaders';

// Error boundary for lazy components
class LazyComponentErrorBoundary extends React.Component<
  { children: React.ReactNode; fallback?: React.ReactNode },
  { hasError: boolean; error?: Error }
> {
  constructor(props: { children: React.ReactNode; fallback?: React.ReactNode }) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error) {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Lazy component error:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return this.props.fallback || (
        <div className="flex items-center justify-center p-8">
          <div className="text-center">
            <div className="text-red-500 mb-2">⚠️ Component failed to load</div>
            <button
              onClick={() => this.setState({ hasError: false })}
              className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            >
              Retry
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

// Higher-order component for lazy loading with error boundary and suspense
const withLazyLoading = <P extends object>(
  LazyComponent: React.LazyExoticComponent<React.ComponentType<P>>,
  LoadingComponent: React.ComponentType,
  errorFallback?: React.ReactNode
) => {
  return React.forwardRef<any, P>((props, ref) => (
    <LazyComponentErrorBoundary fallback={errorFallback}>
      <Suspense fallback={<LoadingComponent />}>
        <LazyComponent {...props} ref={ref} />
      </Suspense>
    </LazyComponentErrorBoundary>
  ));
};

// Placeholder components for lazy loading
// In a real implementation, these would be actual separate component files

const PlaceholderDashboardMetrics: React.FC = () => (
  <div className="p-4 text-center">
    <h3 className="text-lg font-semibold mb-2">Dashboard Metrics</h3>
    <p className="text-gray-600">Dashboard metrics component would be loaded here</p>
  </div>
);

const PlaceholderProjectStatistics: React.FC = () => (
  <div className="p-4 text-center">
    <h3 className="text-lg font-semibold mb-2">Project Statistics</h3>
    <p className="text-gray-600">Project statistics component would be loaded here</p>
  </div>
);

const PlaceholderIssueForm: React.FC = () => (
  <div className="p-4 text-center">
    <h3 className="text-lg font-semibold mb-2">Issue Form</h3>
    <p className="text-gray-600">Issue form component would be loaded here</p>
  </div>
);

const PlaceholderProjectForm: React.FC = () => (
  <div className="p-4 text-center">
    <h3 className="text-lg font-semibold mb-2">Project Form</h3>
    <p className="text-gray-600">Project form component would be loaded here</p>
  </div>
);

const PlaceholderSprintForm: React.FC = () => (
  <div className="p-4 text-center">
    <h3 className="text-lg font-semibold mb-2">Sprint Form</h3>
    <p className="text-gray-600">Sprint form component would be loaded here</p>
  </div>
);

const PlaceholderKanbanBoard: React.FC = () => (
  <div className="p-4 text-center">
    <h3 className="text-lg font-semibold mb-2">Kanban Board</h3>
    <p className="text-gray-600">Kanban board component would be loaded here</p>
  </div>
);

const PlaceholderSprintPlanning: React.FC = () => (
  <div className="p-4 text-center">
    <h3 className="text-lg font-semibold mb-2">Sprint Planning</h3>
    <p className="text-gray-600">Sprint planning component would be loaded here</p>
  </div>
);

const PlaceholderIssueDetails: React.FC = () => (
  <div className="p-4 text-center">
    <h3 className="text-lg font-semibold mb-2">Issue Details</h3>
    <p className="text-gray-600">Issue details component would be loaded here</p>
  </div>
);

const PlaceholderUserSettings: React.FC = () => (
  <div className="p-4 text-center">
    <h3 className="text-lg font-semibold mb-2">User Settings</h3>
    <p className="text-gray-600">User settings component would be loaded here</p>
  </div>
);

const PlaceholderExportData: React.FC = () => (
  <div className="p-4 text-center">
    <h3 className="text-lg font-semibold mb-2">Export Data</h3>
    <p className="text-gray-600">Export data component would be loaded here</p>
  </div>
);

// Create lazy components using React.lazy with placeholder components
const LazyDashboardMetrics = React.lazy(() => 
  Promise.resolve({ default: PlaceholderDashboardMetrics })
);

const LazyProjectStatistics = React.lazy(() => 
  Promise.resolve({ default: PlaceholderProjectStatistics })
);

const LazyIssueForm = React.lazy(() => 
  Promise.resolve({ default: PlaceholderIssueForm })
);

const LazyProjectForm = React.lazy(() => 
  Promise.resolve({ default: PlaceholderProjectForm })
);

const LazySprintForm = React.lazy(() => 
  Promise.resolve({ default: PlaceholderSprintForm })
);

const LazyKanbanBoard = React.lazy(() => 
  Promise.resolve({ default: PlaceholderKanbanBoard })
);

const LazySprintPlanning = React.lazy(() => 
  Promise.resolve({ default: PlaceholderSprintPlanning })
);

const LazyIssueDetails = React.lazy(() => 
  Promise.resolve({ default: PlaceholderIssueDetails })
);

const LazyUserSettings = React.lazy(() => 
  Promise.resolve({ default: PlaceholderUserSettings })
);

const LazyExportData = React.lazy(() => 
  Promise.resolve({ default: PlaceholderExportData })
);

// Export wrapped components with proper loading states
export const DashboardMetrics = withLazyLoading(
  LazyDashboardMetrics,
  DashboardSkeleton
);

export const ProjectStatistics = withLazyLoading(
  LazyProjectStatistics,
  DashboardSkeleton
);

export const IssueForm = withLazyLoading(
  LazyIssueForm,
  FormSkeleton
);

export const ProjectForm = withLazyLoading(
  LazyProjectForm,
  FormSkeleton
);

export const SprintForm = withLazyLoading(
  LazySprintForm,
  FormSkeleton
);

export const KanbanBoard = withLazyLoading(
  LazyKanbanBoard,
  KanbanBoardSkeleton
);

export const SprintPlanning = withLazyLoading(
  LazySprintPlanning,
  SprintsListSkeleton
);

export const IssueDetails = withLazyLoading(
  LazyIssueDetails,
  FormSkeleton
);

export const UserSettings = withLazyLoading(
  LazyUserSettings,
  FormSkeleton
);

export const ExportData = withLazyLoading(
  LazyExportData,
  FormSkeleton
);

// Utility for preloading components
export const preloadComponent = (componentImport: () => Promise<any>) => {
  // Preload on idle or after a delay
  if ('requestIdleCallback' in window) {
    requestIdleCallback(() => componentImport());
  } else {
    setTimeout(() => componentImport(), 100);
  }
};

// Preload critical components after initial load
export const preloadCriticalComponents = () => {
  // Preload components that are likely to be used soon
  preloadComponent(() => Promise.resolve({ default: PlaceholderDashboardMetrics }));
  preloadComponent(() => Promise.resolve({ default: PlaceholderIssueForm }));
  preloadComponent(() => Promise.resolve({ default: PlaceholderKanbanBoard }));
};

// Hook for conditional component loading based on user behavior
export const useConditionalLoading = () => {
  const [loadedComponents, setLoadedComponents] = React.useState<Set<string>>(new Set());

  const markAsLoaded = (componentName: string) => {
    setLoadedComponents(prev => new Set(prev).add(componentName));
  };

  const isLoaded = (componentName: string) => {
    return loadedComponents.has(componentName);
  };

  return { markAsLoaded, isLoaded };
};

// Performance-aware component loader
export const PerformanceAwareLoader: React.FC<{
  children: React.ReactNode;
  priority: 'high' | 'medium' | 'low';
  condition?: boolean;
}> = ({ children, priority, condition = true }) => {
  const [shouldLoad, setShouldLoad] = React.useState(priority === 'high');

  React.useEffect(() => {
    if (!condition || shouldLoad) return;

    const loadComponent = () => setShouldLoad(true);

    switch (priority) {
      case 'medium':
        // Load after a short delay
        setTimeout(loadComponent, 100);
        break;
      case 'low':
        // Load on idle or after longer delay
        if ('requestIdleCallback' in window) {
          requestIdleCallback(loadComponent);
        } else {
          setTimeout(loadComponent, 1000);
        }
        break;
    }
  }, [priority, condition, shouldLoad]);

  if (!shouldLoad || !condition) {
    return null;
  }

  return <>{children}</>;
};

export default {
  DashboardMetrics,
  ProjectStatistics,
  IssueForm,
  ProjectForm,
  SprintForm,
  KanbanBoard,
  SprintPlanning,
  IssueDetails,
  UserSettings,
  ExportData,
  preloadCriticalComponents,
  PerformanceAwareLoader,
};
/**
 * Performance monitoring utilities for the frontend application.
 * Provides metrics collection, performance tracking, and optimization helpers.
 */

// Performance metrics interface
export interface PerformanceMetrics {
  name: string;
  startTime: number;
  endTime?: number;
  duration?: number;
  metadata?: Record<string, any>;
}

// Performance observer for monitoring
class PerformanceMonitor {
  private metrics: Map<string, PerformanceMetrics> = new Map();
  private observers: PerformanceObserver[] = [];

  constructor() {
    this.initializeObservers();
  }

  /**
   * Initialize performance observers for various metrics
   */
  private initializeObservers() {
    if (typeof window === 'undefined' || !('PerformanceObserver' in window)) {
      return;
    }

    try {
      // Observe navigation timing
      const navObserver = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          if (entry.entryType === 'navigation') {
            const navEntry = entry as PerformanceNavigationTiming;
            this.recordMetric('page-load', {
              name: 'page-load',
              startTime: navEntry.fetchStart,
              endTime: navEntry.loadEventEnd,
              duration: navEntry.loadEventEnd - navEntry.fetchStart,
              metadata: {
                domContentLoaded: navEntry.domContentLoadedEventEnd - navEntry.fetchStart,
                firstPaint: navEntry.responseEnd - navEntry.fetchStart,
                type: navEntry.type,
              },
            });
          }
        }
      });
      navObserver.observe({ entryTypes: ['navigation'] });
      this.observers.push(navObserver);

      // Observe resource timing
      const resourceObserver = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          if (entry.entryType === 'resource') {
            const resourceEntry = entry as PerformanceResourceTiming;
            // Only track significant resources (JS, CSS, API calls)
            if (this.isSignificantResource(resourceEntry.name)) {
              this.recordMetric(`resource-${resourceEntry.name}`, {
                name: `resource-${resourceEntry.name}`,
                startTime: resourceEntry.fetchStart,
                endTime: resourceEntry.responseEnd,
                duration: resourceEntry.duration,
                metadata: {
                  size: resourceEntry.transferSize,
                  type: this.getResourceType(resourceEntry.name),
                  cached: resourceEntry.transferSize === 0,
                },
              });
            }
          }
        }
      });
      resourceObserver.observe({ entryTypes: ['resource'] });
      this.observers.push(resourceObserver);

      // Observe largest contentful paint
      const lcpObserver = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          this.recordMetric('largest-contentful-paint', {
            name: 'largest-contentful-paint',
            startTime: 0,
            endTime: entry.startTime,
            duration: entry.startTime,
            metadata: {
              element: (entry as any).element?.tagName,
              url: (entry as any).url,
            },
          });
        }
      });
      lcpObserver.observe({ entryTypes: ['largest-contentful-paint'] });
      this.observers.push(lcpObserver);

      // Observe first input delay
      const fidObserver = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          this.recordMetric('first-input-delay', {
            name: 'first-input-delay',
            startTime: entry.startTime,
            endTime: entry.startTime + (entry as any).processingStart,
            duration: (entry as any).processingStart,
            metadata: {
              inputType: (entry as any).name,
            },
          });
        }
      });
      fidObserver.observe({ entryTypes: ['first-input'] });
      this.observers.push(fidObserver);

    } catch (error) {
      console.warn('Performance monitoring initialization failed:', error);
    }
  }

  /**
   * Check if a resource is significant enough to track
   */
  private isSignificantResource(url: string): boolean {
    return (
      url.includes('.js') ||
      url.includes('.css') ||
      url.includes('/api/') ||
      url.includes('chunk') ||
      url.includes('vendor')
    );
  }

  /**
   * Get resource type from URL
   */
  private getResourceType(url: string): string {
    if (url.includes('.js')) return 'javascript';
    if (url.includes('.css')) return 'stylesheet';
    if (url.includes('/api/')) return 'api';
    if (url.includes('chunk')) return 'chunk';
    if (url.includes('vendor')) return 'vendor';
    return 'other';
  }

  /**
   * Start timing a custom operation
   */
  startTiming(name: string, metadata?: Record<string, any>): void {
    this.metrics.set(name, {
      name,
      startTime: performance.now(),
      metadata,
    });
  }

  /**
   * End timing a custom operation
   */
  endTiming(name: string): PerformanceMetrics | null {
    const metric = this.metrics.get(name);
    if (!metric) {
      console.warn(`No timing started for: ${name}`);
      return null;
    }

    const endTime = performance.now();
    const completedMetric: PerformanceMetrics = {
      ...metric,
      endTime,
      duration: endTime - metric.startTime,
    };

    this.metrics.set(name, completedMetric);
    return completedMetric;
  }

  /**
   * Record a metric directly
   */
  recordMetric(name: string, metric: PerformanceMetrics): void {
    this.metrics.set(name, metric);
  }

  /**
   * Get all recorded metrics
   */
  getMetrics(): Map<string, PerformanceMetrics> {
    return new Map(this.metrics);
  }

  /**
   * Get a specific metric
   */
  getMetric(name: string): PerformanceMetrics | undefined {
    return this.metrics.get(name);
  }

  /**
   * Clear all metrics
   */
  clearMetrics(): void {
    this.metrics.clear();
  }

  /**
   * Get performance summary
   */
  getSummary(): Record<string, any> {
    const metrics = Array.from(this.metrics.values());
    
    return {
      totalMetrics: metrics.length,
      pageLoad: this.getMetric('page-load')?.duration || 0,
      largestContentfulPaint: this.getMetric('largest-contentful-paint')?.duration || 0,
      firstInputDelay: this.getMetric('first-input-delay')?.duration || 0,
      resourceCount: metrics.filter(m => m.name.startsWith('resource-')).length,
      averageResourceLoadTime: this.calculateAverageResourceLoadTime(),
      slowestResources: this.getSlowestResources(5),
    };
  }

  /**
   * Calculate average resource load time
   */
  private calculateAverageResourceLoadTime(): number {
    const resourceMetrics = Array.from(this.metrics.values())
      .filter(m => m.name.startsWith('resource-') && m.duration);
    
    if (resourceMetrics.length === 0) return 0;
    
    const totalTime = resourceMetrics.reduce((sum, m) => sum + (m.duration || 0), 0);
    return totalTime / resourceMetrics.length;
  }

  /**
   * Get slowest resources
   */
  private getSlowestResources(count: number): PerformanceMetrics[] {
    return Array.from(this.metrics.values())
      .filter(m => m.name.startsWith('resource-') && m.duration)
      .sort((a, b) => (b.duration || 0) - (a.duration || 0))
      .slice(0, count);
  }

  /**
   * Cleanup observers
   */
  cleanup(): void {
    this.observers.forEach(observer => observer.disconnect());
    this.observers = [];
    this.metrics.clear();
  }
}

// Global performance monitor instance
export const performanceMonitor = new PerformanceMonitor();

// React hook for performance monitoring
export const usePerformanceMonitoring = () => {
  const startTiming = (name: string, metadata?: Record<string, any>) => {
    performanceMonitor.startTiming(name, metadata);
  };

  const endTiming = (name: string) => {
    return performanceMonitor.endTiming(name);
  };

  const getMetrics = () => {
    return performanceMonitor.getMetrics();
  };

  const getSummary = () => {
    return performanceMonitor.getSummary();
  };

  return {
    startTiming,
    endTiming,
    getMetrics,
    getSummary,
  };
};

// Utility functions for performance optimization
export const performanceUtils = {
  /**
   * Debounce function for performance optimization
   */
  debounce: <T extends (...args: any[]) => any>(
    func: T,
    wait: number
  ): ((...args: Parameters<T>) => void) => {
    let timeout: NodeJS.Timeout;
    return (...args: Parameters<T>) => {
      clearTimeout(timeout);
      timeout = setTimeout(() => func(...args), wait);
    };
  },

  /**
   * Throttle function for performance optimization
   */
  throttle: <T extends (...args: any[]) => any>(
    func: T,
    limit: number
  ): ((...args: Parameters<T>) => void) => {
    let inThrottle: boolean;
    return (...args: Parameters<T>) => {
      if (!inThrottle) {
        func(...args);
        inThrottle = true;
        setTimeout(() => (inThrottle = false), limit);
      }
    };
  },

  /**
   * Measure component render time
   */
  measureRender: (componentName: string) => {
    return {
      start: () => performanceMonitor.startTiming(`render-${componentName}`),
      end: () => performanceMonitor.endTiming(`render-${componentName}`),
    };
  },

  /**
   * Lazy load images with intersection observer
   */
  lazyLoadImage: (img: HTMLImageElement, src: string) => {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          img.src = src;
          observer.unobserve(img);
        }
      });
    });
    observer.observe(img);
    return observer;
  },

  /**
   * Preload critical resources
   */
  preloadResource: (href: string, as: string) => {
    const link = document.createElement('link');
    link.rel = 'preload';
    link.href = href;
    link.as = as;
    document.head.appendChild(link);
  },

  /**
   * Check if user prefers reduced motion
   */
  prefersReducedMotion: (): boolean => {
    return window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  },

  /**
   * Get connection information
   */
  getConnectionInfo: () => {
    const connection = (navigator as any).connection || (navigator as any).mozConnection || (navigator as any).webkitConnection;
    if (!connection) return null;
    
    return {
      effectiveType: connection.effectiveType,
      downlink: connection.downlink,
      rtt: connection.rtt,
      saveData: connection.saveData,
    };
  },
};

// Development-only performance logging
if (import.meta.env.DEV) {
  // Log performance summary every 30 seconds in development
  setInterval(() => {
    const summary = performanceMonitor.getSummary();
    if (summary.totalMetrics > 0) {
      console.group('🚀 Performance Summary');
      console.log('Page Load Time:', `${summary.pageLoad.toFixed(2)}ms`);
      console.log('LCP:', `${summary.largestContentfulPaint.toFixed(2)}ms`);
      console.log('FID:', `${summary.firstInputDelay.toFixed(2)}ms`);
      console.log('Resources Loaded:', summary.resourceCount);
      console.log('Avg Resource Time:', `${summary.averageResourceLoadTime.toFixed(2)}ms`);
      if (summary.slowestResources.length > 0) {
        console.log('Slowest Resources:', summary.slowestResources.map((r: PerformanceMetrics) => ({
          name: r.name.replace('resource-', ''),
          duration: `${r.duration?.toFixed(2)}ms`
        })));
      }
      console.groupEnd();
    }
  }, 30000);
}
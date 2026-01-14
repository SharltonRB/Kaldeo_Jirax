/**
 * Performance monitoring service for tracking frontend performance metrics
 * Collects and reports performance data for optimization insights
 */

interface PerformanceMetric {
  name: string
  value: number
  timestamp: number
  url?: string
  userAgent?: string
}

class PerformanceService {
  private metrics: PerformanceMetric[] = []
  private isEnabled: boolean = import.meta.env.MODE === 'production'

  constructor() {
    this.initializePerformanceObserver()
  }

  /**
   * Initialize Performance Observer for tracking various performance metrics
   */
  private initializePerformanceObserver() {
    if (!this.isEnabled || typeof window === 'undefined' || !('PerformanceObserver' in window)) {
      return
    }

    try {
      // Track navigation timing
      const navObserver = new PerformanceObserver((list) => {
        list.getEntries().forEach((entry) => {
          if (entry.entryType === 'navigation') {
            const navEntry = entry as PerformanceNavigationTiming
            this.recordMetric('page-load-time', navEntry.loadEventEnd - navEntry.fetchStart)
            this.recordMetric('dom-content-loaded', navEntry.domContentLoadedEventEnd - navEntry.fetchStart)
            this.recordMetric('first-byte', navEntry.responseStart - navEntry.fetchStart)
          }
        })
      })
      navObserver.observe({ entryTypes: ['navigation'] })

      // Track resource timing
      const resourceObserver = new PerformanceObserver((list) => {
        list.getEntries().forEach((entry) => {
          if (entry.entryType === 'resource') {
            const resourceEntry = entry as PerformanceResourceTiming
            // Track slow resources (> 1 second)
            if (resourceEntry.duration > 1000) {
              this.recordMetric('slow-resource', resourceEntry.duration, resourceEntry.name)
            }
          }
        })
      })
      resourceObserver.observe({ entryTypes: ['resource'] })

      // Track long tasks (> 50ms)
      const longTaskObserver = new PerformanceObserver((list) => {
        list.getEntries().forEach((entry) => {
          if (entry.entryType === 'longtask') {
            this.recordMetric('long-task', entry.duration)
            console.warn(`Long task detected: ${entry.duration}ms`)
          }
        })
      })
      longTaskObserver.observe({ entryTypes: ['longtask'] })

      // Track layout shifts
      const layoutShiftObserver = new PerformanceObserver((list) => {
        list.getEntries().forEach((entry) => {
          if (entry.entryType === 'layout-shift' && !(entry as any).hadRecentInput) {
            this.recordMetric('layout-shift', (entry as any).value)
          }
        })
      })
      layoutShiftObserver.observe({ entryTypes: ['layout-shift'] })

    } catch (error) {
      console.warn('Performance Observer not supported:', error)
    }
  }

  /**
   * Record a performance metric
   */
  private recordMetric(name: string, value: number, url?: string) {
    const metric: PerformanceMetric = {
      name,
      value,
      timestamp: Date.now(),
      url: url || window.location.href,
      userAgent: navigator.userAgent,
    }

    this.metrics.push(metric)

    // Log in development
    if (import.meta.env.MODE === 'development') {
      console.log(`Performance metric: ${name} = ${value}ms`)
    }

    // Send to analytics in production (implement your analytics service)
    if (this.isEnabled) {
      this.sendToAnalytics(metric)
    }
  }

  /**
   * Send metric to analytics service
   */
  private sendToAnalytics(metric: PerformanceMetric) {
    // Implement your analytics service here
    // Example: Google Analytics, DataDog, New Relic, etc.
    
    // For now, just batch and log
    if (this.metrics.length >= 10) {
      this.flushMetrics()
    }
  }

  /**
   * Flush accumulated metrics
   */
  private flushMetrics() {
    if (this.metrics.length === 0) return

    // Send batch to analytics service
    console.log('Flushing performance metrics:', this.metrics.length)
    
    // Clear metrics after sending
    this.metrics = []
  }

  /**
   * Track custom performance metric
   */
  public trackCustomMetric(name: string, value: number, url?: string) {
    this.recordMetric(`custom-${name}`, value, url)
  }

  /**
   * Track component render time
   */
  public trackComponentRender(componentName: string, renderTime: number) {
    this.recordMetric(`component-render-${componentName}`, renderTime)
  }

  /**
   * Track API call performance
   */
  public trackApiCall(endpoint: string, duration: number, status: number) {
    this.recordMetric(`api-call-${endpoint}`, duration)
    
    if (status >= 400) {
      this.recordMetric(`api-error-${endpoint}`, status)
    }
  }

  /**
   * Track user interaction performance
   */
  public trackUserInteraction(action: string, duration: number) {
    this.recordMetric(`user-interaction-${action}`, duration)
  }

  /**
   * Get current performance metrics
   */
  public getMetrics(): PerformanceMetric[] {
    return [...this.metrics]
  }

  /**
   * Clear all metrics
   */
  public clearMetrics() {
    this.metrics = []
  }

  /**
   * Enable/disable performance tracking
   */
  public setEnabled(enabled: boolean) {
    this.isEnabled = enabled
  }
}

// Export singleton instance
export const performanceService = new PerformanceService()

// Export hook for React components
export const usePerformanceTracking = () => {
  return {
    trackCustomMetric: performanceService.trackCustomMetric.bind(performanceService),
    trackComponentRender: performanceService.trackComponentRender.bind(performanceService),
    trackApiCall: performanceService.trackApiCall.bind(performanceService),
    trackUserInteraction: performanceService.trackUserInteraction.bind(performanceService),
  }
}

export default performanceService
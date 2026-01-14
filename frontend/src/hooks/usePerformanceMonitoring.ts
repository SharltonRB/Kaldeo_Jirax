import React, { useCallback } from 'react'

interface PerformanceMetrics {
  loadTime: number
  renderTime: number
  memoryUsage?: number
}

/**
 * Hook for monitoring frontend performance metrics
 * Tracks page load times, render performance, and memory usage
 */
export const usePerformanceMonitoring = (componentName?: string) => {
  // Track component render time
  const trackRenderTime = useCallback((startTime: number) => {
    const endTime = performance.now()
    const renderTime = endTime - startTime
    
    if (import.meta.env.MODE === 'development') {
      console.log(`${componentName || 'Component'} render time: ${renderTime.toFixed(2)}ms`)
    }
    
    // In production, send to analytics service
    if (import.meta.env.MODE === 'production' && renderTime > 100) {
      // Log slow renders for optimization
      console.warn(`Slow render detected: ${componentName} took ${renderTime.toFixed(2)}ms`)
    }
    
    return renderTime
  }, [componentName])

  // Track memory usage
  const trackMemoryUsage = useCallback(() => {
    if ('memory' in performance) {
      const memory = (performance as any).memory
      const memoryUsage = {
        used: Math.round(memory.usedJSHeapSize / 1048576), // MB
        total: Math.round(memory.totalJSHeapSize / 1048576), // MB
        limit: Math.round(memory.jsHeapSizeLimit / 1048576), // MB
      }
      
      if (import.meta.env.MODE === 'development') {
        console.log(`Memory usage: ${memoryUsage.used}MB / ${memoryUsage.total}MB (limit: ${memoryUsage.limit}MB)`)
      }
      
      // Warn if memory usage is high
      if (memoryUsage.used > memoryUsage.limit * 0.8) {
        console.warn('High memory usage detected:', memoryUsage)
      }
      
      return memoryUsage
    }
    return null
  }, [])

  // Track page load performance
  React.useEffect(() => {
    const measurePageLoad = () => {
      if (typeof window !== 'undefined' && 'performance' in window) {
        const navigation = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming
        
        if (navigation) {
          const metrics = {
            dns: navigation.domainLookupEnd - navigation.domainLookupStart,
            tcp: navigation.connectEnd - navigation.connectStart,
            request: navigation.responseStart - navigation.requestStart,
            response: navigation.responseEnd - navigation.responseStart,
            dom: navigation.domContentLoadedEventEnd - navigation.domContentLoadedEventStart,
            load: navigation.loadEventEnd - navigation.loadEventStart,
            total: navigation.loadEventEnd - navigation.fetchStart,
          }
          
          if (import.meta.env.MODE === 'development') {
            console.log('Page load metrics:', metrics)
          }
          
          // Track slow page loads
          if (metrics.total > 3000) { // 3 seconds
            console.warn('Slow page load detected:', metrics.total + 'ms')
          }
        }
      }
    }

    // Measure after page is fully loaded
    if (document.readyState === 'complete') {
      measurePageLoad()
    } else {
      window.addEventListener('load', measurePageLoad)
      return () => window.removeEventListener('load', measurePageLoad)
    }
  }, [])

  return {
    trackRenderTime,
    trackMemoryUsage,
  }
}
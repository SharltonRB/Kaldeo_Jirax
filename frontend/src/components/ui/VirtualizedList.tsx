import React, { useState, useEffect, useRef, useMemo } from 'react'

interface VirtualizedListProps<T> {
  items: T[]
  itemHeight: number
  containerHeight: number
  renderItem: (item: T, index: number) => React.ReactNode
  overscan?: number
  className?: string
  onScroll?: (scrollTop: number) => void
}

/**
 * Virtualized list component for rendering large datasets efficiently
 * Only renders visible items plus a small buffer to improve performance
 */
export function VirtualizedList<T>({
  items,
  itemHeight,
  containerHeight,
  renderItem,
  overscan = 5,
  className = '',
  onScroll,
}: VirtualizedListProps<T>) {
  const [scrollTop, setScrollTop] = useState(0)
  const scrollElementRef = useRef<HTMLDivElement>(null)

  // Calculate visible range
  const { startIndex, endIndex, totalHeight } = useMemo(() => {
    const visibleStart = Math.floor(scrollTop / itemHeight)
    const visibleEnd = Math.min(
      visibleStart + Math.ceil(containerHeight / itemHeight),
      items.length - 1
    )

    return {
      startIndex: Math.max(0, visibleStart - overscan),
      endIndex: Math.min(items.length - 1, visibleEnd + overscan),
      totalHeight: items.length * itemHeight,
    }
  }, [scrollTop, itemHeight, containerHeight, items.length, overscan])

  // Get visible items
  const visibleItems = useMemo(() => {
    return items.slice(startIndex, endIndex + 1).map((item, index) => ({
      item,
      index: startIndex + index,
    }))
  }, [items, startIndex, endIndex])

  // Handle scroll
  const handleScroll = (e: React.UIEvent<HTMLDivElement>) => {
    const newScrollTop = e.currentTarget.scrollTop
    setScrollTop(newScrollTop)
    onScroll?.(newScrollTop)
  }

  // Scroll to specific index
  const scrollToIndex = (index: number) => {
    if (scrollElementRef.current) {
      const scrollTop = index * itemHeight
      scrollElementRef.current.scrollTop = scrollTop
      setScrollTop(scrollTop)
    }
  }

  return (
    <div
      ref={scrollElementRef}
      className={`overflow-auto ${className}`}
      style={{ height: containerHeight }}
      onScroll={handleScroll}
    >
      <div style={{ height: totalHeight, position: 'relative' }}>
        <div
          style={{
            transform: `translateY(${startIndex * itemHeight}px)`,
          }}
        >
          {visibleItems.map(({ item, index }) => (
            <div
              key={index}
              style={{
                height: itemHeight,
                overflow: 'hidden',
              }}
            >
              {renderItem(item, index)}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

/**
 * Hook for managing virtualized list state
 */
export const useVirtualizedList = <T,>(
  items: T[],
  itemHeight: number,
  containerHeight: number
) => {
  const [scrollTop, setScrollTop] = useState(0)

  const visibleRange = useMemo(() => {
    const startIndex = Math.floor(scrollTop / itemHeight)
    const endIndex = Math.min(
      startIndex + Math.ceil(containerHeight / itemHeight),
      items.length - 1
    )

    return { startIndex, endIndex }
  }, [scrollTop, itemHeight, containerHeight, items.length])

  return {
    scrollTop,
    setScrollTop,
    visibleRange,
    totalHeight: items.length * itemHeight,
  }
}

/**
 * Infinite scroll component for loading data as user scrolls
 */
interface InfiniteScrollProps<T> {
  items: T[]
  renderItem: (item: T, index: number) => React.ReactNode
  loadMore: () => void
  hasMore: boolean
  isLoading: boolean
  threshold?: number
  className?: string
}

export function InfiniteScroll<T>({
  items,
  renderItem,
  loadMore,
  hasMore,
  isLoading,
  threshold = 100,
  className = '',
}: InfiniteScrollProps<T>) {
  const containerRef = useRef<HTMLDivElement>(null)
  const loadingRef = useRef<HTMLDivElement>(null)

  // Intersection observer for infinite scroll
  useEffect(() => {
    if (!loadingRef.current || !hasMore || isLoading) return

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          loadMore()
        }
      },
      { threshold: 0.1 }
    )

    observer.observe(loadingRef.current)

    return () => observer.disconnect()
  }, [loadMore, hasMore, isLoading])

  // Handle scroll for threshold-based loading
  const handleScroll = (e: React.UIEvent<HTMLDivElement>) => {
    const { scrollTop, scrollHeight, clientHeight } = e.currentTarget

    if (
      scrollHeight - scrollTop - clientHeight < threshold &&
      hasMore &&
      !isLoading
    ) {
      loadMore()
    }
  }

  return (
    <div
      ref={containerRef}
      className={`overflow-auto ${className}`}
      onScroll={handleScroll}
    >
      {items.map((item, index) => (
        <div key={index}>{renderItem(item, index)}</div>
      ))}

      {/* Loading indicator */}
      {hasMore && (
        <div
          ref={loadingRef}
          className="flex items-center justify-center p-4"
        >
          {isLoading ? (
            <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-500"></div>
          ) : (
            <div className="text-gray-500">Load more...</div>
          )}
        </div>
      )}

      {/* End of list indicator */}
      {!hasMore && items.length > 0 && (
        <div className="text-center text-gray-500 p-4">
          No more items to load
        </div>
      )}
    </div>
  )
}

export default VirtualizedList
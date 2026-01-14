import React from 'react'
import ReactDOM from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import App from './App.tsx'
import './index.css'

// Enhanced React Query configuration for better performance
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // Caching strategy
      staleTime: 5 * 60 * 1000, // 5 minutes - data is fresh for 5 minutes
      gcTime: 10 * 60 * 1000, // 10 minutes - keep in cache for 10 minutes after last use
      
      // Retry configuration
      retry: (failureCount, error: any) => {
        // Don't retry on 4xx errors (client errors)
        if (error?.response?.status >= 400 && error?.response?.status < 500) {
          return false;
        }
        // Only retry up to 2 times for server errors
        return failureCount < 2;
      },
      retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
      
      // Performance optimizations
      refetchOnWindowFocus: false, // Disable refetch on window focus to reduce noise
      refetchOnReconnect: true, // Refetch when network reconnects
      refetchOnMount: true, // Refetch when component mounts
      
      // Network mode configuration
      networkMode: 'online', // Only run queries when online
      
      // Suspense configuration (for future use)
      // suspense: false, // Removed as it's not supported in current version
      
      // Error handling
      throwOnError: false, // Handle errors in components instead of throwing
    },
    mutations: {
      retry: false, // Don't retry mutations by default
      networkMode: 'online', // Only run mutations when online
      throwOnError: false, // Handle errors in components
      
      // Optimistic updates configuration
      onMutate: async () => {
        // Cancel any outgoing refetches (so they don't overwrite our optimistic update)
        // This will be implemented per mutation as needed
      },
    },
  },
  
  // Query cache configuration
  queryCache: undefined, // Use default query cache
  mutationCache: undefined, // Use default mutation cache
})

// Add global error handling for React Query
queryClient.setMutationDefaults(['createIssue'], {
  mutationFn: async (variables: any) => {
    // This will be overridden by individual mutations
    throw new Error('Mutation function not implemented')
  },
  onError: (error: any) => {
    console.error('Mutation error:', error)
    // Global error handling can be added here
  },
})

// Performance monitoring (development only)
if (import.meta.env.DEV) {
  // Add query client devtools in development
  // Note: Devtools import is commented out to avoid build errors
  // import('@tanstack/react-query-devtools').then(({ ReactQueryDevtools }) => {
  //   // Devtools will be available in development
  // })
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </React.StrictMode>,
)
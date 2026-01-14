import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig(({ command, mode }) => {
  // Load env file based on `mode` in the current working directory.
  const env = loadEnv(mode, process.cwd(), '')
  
  return {
    plugins: [
      react({
        // Enable React Fast Refresh for better development experience
        fastRefresh: true,
        // Optimize JSX runtime
        jsxRuntime: 'automatic',
      })
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    server: {
      port: 3000,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
          ws: true,
        },
      },
      // Add security headers for development
      headers: {
        'X-Content-Type-Options': 'nosniff',
        'X-Frame-Options': 'DENY',
        'X-XSS-Protection': '1; mode=block',
        'Referrer-Policy': 'strict-origin-when-cross-origin',
        'Permissions-Policy': 'camera=(), microphone=(), geolocation=(), payment=()',
      },
    },
    build: {
      outDir: 'dist',
      sourcemap: mode === 'development',
      minify: mode === 'production' ? 'esbuild' : false,
      target: 'es2020',
      rollupOptions: {
        output: {
          manualChunks: (id) => {
            // Vendor chunk for React and core libraries
            if (id.includes('node_modules')) {
              if (id.includes('react') || id.includes('react-dom') || id.includes('react-router')) {
                return 'vendor-react'
              }
              if (id.includes('@tanstack/react-query')) {
                return 'vendor-query'
              }
              if (id.includes('axios')) {
                return 'vendor-http'
              }
              if (id.includes('lucide-react')) {
                return 'vendor-icons'
              }
              if (id.includes('date-fns')) {
                return 'vendor-date'
              }
              if (id.includes('react-hook-form') || id.includes('@hookform') || id.includes('zod')) {
                return 'vendor-forms'
              }
              if (id.includes('tailwind') || id.includes('clsx')) {
                return 'vendor-styles'
              }
              // Other vendor libraries
              return 'vendor-misc'
            }
            
            // Application chunks
            if (id.includes('/hooks/')) {
              return 'app-hooks'
            }
            if (id.includes('/services/')) {
              return 'app-services'
            }
            if (id.includes('/components/ui/')) {
              return 'app-ui'
            }
            if (id.includes('/utils/')) {
              return 'app-utils'
            }
            if (id.includes('/context/')) {
              return 'app-context'
            }
          },
          // Optimize chunk file names with better caching
          chunkFileNames: (chunkInfo) => {
            const name = chunkInfo.name || 'chunk'
            return `js/${name}-[hash].js`
          },
          entryFileNames: 'js/[name]-[hash].js',
          assetFileNames: (assetInfo) => {
            const info = assetInfo.name.split('.')
            const ext = info[info.length - 1]
            if (/\.(png|jpe?g|svg|gif|tiff|bmp|ico)$/i.test(assetInfo.name)) {
              return `images/[name]-[hash].${ext}`
            }
            if (/\.(css)$/i.test(assetInfo.name)) {
              return `css/[name]-[hash].${ext}`
            }
            if (/\.(woff2?|eot|ttf|otf)$/i.test(assetInfo.name)) {
              return `fonts/[name]-[hash].${ext}`
            }
            return `assets/[name]-[hash].${ext}`
          },
        },
        // Optimize external dependencies
        external: [],
      },
      // Optimize build performance and output
      chunkSizeWarningLimit: 800, // Warn for chunks larger than 800kb
      reportCompressedSize: true,
      // Enable CSS code splitting
      cssCodeSplit: true,
      // Optimize asset inlining
      assetsInlineLimit: 4096, // Inline assets smaller than 4kb
    },
    define: {
      global: 'globalThis',
      // Inject build-time variables
      __BUILD_TIME__: JSON.stringify(new Date().toISOString()),
      __VERSION__: JSON.stringify(process.env.npm_package_version || '1.0.0'),
      __DEV__: JSON.stringify(mode === 'development'),
    },
    // Optimize dependencies pre-bundling
    optimizeDeps: {
      include: [
        'react',
        'react-dom',
        'react-router-dom',
        '@tanstack/react-query',
        'axios',
        'lucide-react',
        'clsx',
        'tailwind-merge',
        'react-hook-form',
        '@hookform/resolvers',
        'zod',
        'date-fns',
        'dompurify',
      ],
      // Force pre-bundling of these dependencies
      force: mode === 'development',
    },
    // Preview configuration for production testing
    preview: {
      port: 4173,
      host: true,
      // Add compression for preview
      headers: {
        'Cache-Control': 'public, max-age=31536000, immutable',
        'Content-Encoding': 'gzip',
      },
    },
    // Environment variables configuration
    envPrefix: 'VITE_',
    // CSS optimization
    css: {
      devSourcemap: mode === 'development',
    },
    // Performance optimizations
    esbuild: {
      // Drop console and debugger in production
      drop: mode === 'production' ? ['console', 'debugger'] : [],
      // Optimize for modern browsers in production
      target: mode === 'production' ? 'es2020' : 'es2017',
    },
  }
})
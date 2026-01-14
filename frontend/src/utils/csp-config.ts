/**
 * Content Security Policy configuration for the frontend application.
 * Provides comprehensive protection against XSS and other injection attacks.
 */

// CSP directives for different environments
export const CSP_DIRECTIVES = {
  development: {
    'default-src': ["'self'"],
    'script-src': [
      "'self'",
      "'unsafe-inline'", // Required for Vite HMR in development
      "'unsafe-eval'", // Required for Vite HMR in development
      'localhost:*',
      '127.0.0.1:*',
    ],
    'style-src': [
      "'self'",
      "'unsafe-inline'", // Required for styled-components and CSS-in-JS
      'fonts.googleapis.com',
    ],
    'img-src': [
      "'self'",
      'data:',
      'blob:',
      'https:',
    ],
    'font-src': [
      "'self'",
      'data:',
      'fonts.gstatic.com',
    ],
    'connect-src': [
      "'self'",
      'localhost:*',
      '127.0.0.1:*',
      'ws://localhost:*', // WebSocket for HMR
      'ws://127.0.0.1:*',
    ],
    'media-src': ["'self'"],
    'object-src': ["'none'"],
    'base-uri': ["'self'"],
    'form-action': ["'self'"],
    // Remove frame-ancestors from meta tag CSP (only works in HTTP headers)
    'upgrade-insecure-requests': [],
  },
  production: {
    'default-src': ["'self'"],
    'script-src': [
      "'self'",
      // Add specific domains if needed for analytics, etc.
    ],
    'style-src': [
      "'self'",
      "'unsafe-inline'", // May be needed for some CSS frameworks
    ],
    'img-src': [
      "'self'",
      'data:',
      'https:',
    ],
    'font-src': [
      "'self'",
      'data:',
      'fonts.gstatic.com',
    ],
    'connect-src': [
      "'self'",
      // Add your API domain here
      import.meta.env.VITE_API_BASE_URL || '',
    ].filter(Boolean),
    'media-src': ["'self'"],
    'object-src': ["'none'"],
    'base-uri': ["'self'"],
    'form-action': ["'self'"],
    // Remove frame-ancestors from meta tag CSP (only works in HTTP headers)
    'upgrade-insecure-requests': [],
    'block-all-mixed-content': [],
  },
} as const;

/**
 * Generate CSP header value from directives.
 */
export const generateCSPHeader = (environment: 'development' | 'production' = 'production'): string => {
  const directives = CSP_DIRECTIVES[environment];
  
  return Object.entries(directives)
    .map(([directive, values]) => {
      if (values.length === 0) {
        return directive;
      }
      return `${directive} ${values.join(' ')}`;
    })
    .join('; ');
};

/**
 * Set CSP meta tag in document head.
 */
export const setCSPMetaTag = (environment: 'development' | 'production' = 'production'): void => {
  // Remove existing CSP meta tag
  const existingTag = document.querySelector('meta[http-equiv="Content-Security-Policy"]');
  if (existingTag) {
    existingTag.remove();
  }

  // Create new CSP meta tag
  const metaTag = document.createElement('meta');
  metaTag.setAttribute('http-equiv', 'Content-Security-Policy');
  metaTag.setAttribute('content', generateCSPHeader(environment));
  
  document.head.appendChild(metaTag);
};

/**
 * Validate that inline scripts have proper nonce or are allowed.
 */
export const validateInlineScript = (scriptContent: string): boolean => {
  // In production, inline scripts should be avoided
  if (import.meta.env.PROD) {
    console.warn('Inline script detected in production:', scriptContent.substring(0, 100));
    return false;
  }
  
  return true;
};

/**
 * Create a secure script element with proper attributes.
 */
export const createSecureScript = (src: string, nonce?: string): HTMLScriptElement => {
  const script = document.createElement('script');
  script.src = src;
  script.type = 'text/javascript';
  
  if (nonce) {
    script.setAttribute('nonce', nonce);
  }
  
  // Add integrity check for external scripts
  if (src.startsWith('https://')) {
    script.crossOrigin = 'anonymous';
    // Note: In a real application, you would add the integrity hash
    // script.integrity = 'sha384-...';
  }
  
  return script;
};

/**
 * Sanitize and validate URLs for CSP compliance.
 */
export const validateCSPUrl = (url: string): boolean => {
  try {
    const urlObj = new URL(url);
    
    // Block dangerous protocols
    const dangerousProtocols = ['javascript:', 'data:', 'vbscript:', 'file:'];
    if (dangerousProtocols.some(protocol => url.toLowerCase().startsWith(protocol))) {
      return false;
    }
    
    // In production, only allow HTTPS (except for localhost)
    if (import.meta.env.PROD) {
      if (urlObj.protocol !== 'https:' && !urlObj.hostname.includes('localhost')) {
        return false;
      }
    }
    
    return true;
  } catch {
    return false;
  }
};

/**
 * Report CSP violations (in a real app, this would send to a logging service).
 */
export const handleCSPViolation = (event: SecurityPolicyViolationEvent): void => {
  const violation = {
    blockedURI: event.blockedURI,
    violatedDirective: event.violatedDirective,
    originalPolicy: event.originalPolicy,
    sourceFile: event.sourceFile,
    lineNumber: event.lineNumber,
    columnNumber: event.columnNumber,
    timestamp: new Date().toISOString(),
  };
  
  console.error('CSP Violation:', violation);
  
  // In production, send to logging service
  if (import.meta.env.PROD) {
    // Example: send to logging service
    // fetch('/api/csp-violations', {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify(violation),
    // }).catch(console.error);
  }
};

/**
 * Initialize CSP configuration and violation reporting.
 */
export const initializeCSP = (): void => {
  const environment = import.meta.env.PROD ? 'production' : 'development';
  
  // Set CSP meta tag
  setCSPMetaTag(environment);
  
  // Set up CSP violation reporting
  document.addEventListener('securitypolicyviolation', handleCSPViolation);
  
  console.info(`CSP initialized for ${environment} environment`);
};

// Security headers that should be set by the server
export const SECURITY_HEADERS = {
  'Strict-Transport-Security': 'max-age=31536000; includeSubDomains; preload',
  'X-Content-Type-Options': 'nosniff',
  'X-Frame-Options': 'DENY',
  'X-XSS-Protection': '1; mode=block',
  'Referrer-Policy': 'strict-origin-when-cross-origin',
  'Permissions-Policy': 'camera=(), microphone=(), geolocation=(), payment=()',
} as const;
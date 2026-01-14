import DOMPurify from 'dompurify';

/**
 * Security utilities for input sanitization and validation.
 * Provides comprehensive protection against XSS and injection attacks.
 */

// Configure DOMPurify for strict sanitization
const purifyConfig = {
  ALLOWED_TAGS: ['b', 'i', 'em', 'strong', 'u', 'br', 'p', 'span'],
  ALLOWED_ATTR: ['class'],
  KEEP_CONTENT: true,
  RETURN_DOM: false,
  RETURN_DOM_FRAGMENT: false,
  RETURN_DOM_IMPORT: false,
  SANITIZE_DOM: true,
  WHOLE_DOCUMENT: false,
  FORCE_BODY: false,
};

/**
 * Sanitizes HTML content to prevent XSS attacks.
 * Removes dangerous tags and attributes while preserving safe formatting.
 *
 * @param html - Raw HTML content
 * @returns Sanitized HTML string
 */
export const sanitizeHtml = (html: string): string => {
  if (!html || typeof html !== 'string') {
    return '';
  }
  
  return DOMPurify.sanitize(html, purifyConfig);
};

/**
 * Sanitizes plain text input by removing HTML tags and dangerous characters.
 * Use for user inputs that should not contain any HTML.
 *
 * @param text - Raw text input
 * @returns Sanitized plain text
 */
export const sanitizeText = (text: string): string => {
  if (!text || typeof text !== 'string') {
    return '';
  }
  
  // Remove all HTML tags and decode HTML entities
  const withoutHtml = DOMPurify.sanitize(text, { 
    ALLOWED_TAGS: [], 
    ALLOWED_ATTR: [],
    KEEP_CONTENT: true 
  });
  
  // Additional sanitization for special characters
  return withoutHtml
    .replace(/[<>]/g, '') // Remove any remaining angle brackets
    .trim();
};

/**
 * Validates and sanitizes email addresses.
 *
 * @param email - Email address to validate
 * @returns Sanitized email or empty string if invalid
 */
export const sanitizeEmail = (email: string): string => {
  if (!email || typeof email !== 'string') {
    return '';
  }
  
  const sanitized = sanitizeText(email).toLowerCase();
  
  // Basic email validation regex
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  
  return emailRegex.test(sanitized) ? sanitized : '';
};

/**
 * Sanitizes URLs to prevent javascript: and data: URI attacks.
 *
 * @param url - URL to sanitize
 * @returns Sanitized URL or empty string if dangerous
 */
export const sanitizeUrl = (url: string): string => {
  if (!url || typeof url !== 'string') {
    return '';
  }
  
  const sanitized = sanitizeText(url);
  
  // Allow only http, https, and relative URLs
  const urlRegex = /^(https?:\/\/|\/|\.\/|#)/i;
  
  // Block dangerous protocols
  const dangerousProtocols = /^(javascript|data|vbscript|file|ftp):/i;
  
  if (dangerousProtocols.test(sanitized)) {
    return '';
  }
  
  return urlRegex.test(sanitized) ? sanitized : '';
};

/**
 * Sanitizes file names to prevent path traversal attacks.
 *
 * @param filename - File name to sanitize
 * @returns Sanitized file name
 */
export const sanitizeFilename = (filename: string): string => {
  if (!filename || typeof filename !== 'string') {
    return '';
  }
  
  return sanitizeText(filename)
    .replace(/[<>:"/\\|?*]/g, '') // Remove dangerous file name characters
    .replace(/\.\./g, '') // Remove path traversal attempts
    .replace(/^\.+/, '') // Remove leading dots
    .trim();
};

/**
 * Validates and sanitizes numeric inputs.
 *
 * @param value - Value to sanitize as number
 * @param min - Minimum allowed value
 * @param max - Maximum allowed value
 * @returns Sanitized number or null if invalid
 */
export const sanitizeNumber = (value: any, min?: number, max?: number): number | null => {
  const num = parseFloat(value);
  
  if (isNaN(num) || !isFinite(num)) {
    return null;
  }
  
  if (min !== undefined && num < min) {
    return null;
  }
  
  if (max !== undefined && num > max) {
    return null;
  }
  
  return num;
};

/**
 * Sanitizes object properties recursively.
 * Useful for sanitizing form data or API responses.
 *
 * @param obj - Object to sanitize
 * @returns Sanitized object
 */
export const sanitizeObject = (obj: any): any => {
  if (obj === null || obj === undefined) {
    return obj;
  }
  
  if (typeof obj === 'string') {
    return sanitizeText(obj);
  }
  
  if (typeof obj === 'number' || typeof obj === 'boolean') {
    return obj;
  }
  
  if (Array.isArray(obj)) {
    return obj.map(sanitizeObject);
  }
  
  if (typeof obj === 'object') {
    const sanitized: any = {};
    for (const [key, value] of Object.entries(obj)) {
      const sanitizedKey = sanitizeText(key);
      if (sanitizedKey) {
        sanitized[sanitizedKey] = sanitizeObject(value);
      }
    }
    return sanitized;
  }
  
  return obj;
};

/**
 * Content Security Policy utilities
 */
export const CSP_NONCE_ATTRIBUTE = 'data-csp-nonce';

/**
 * Generates a random nonce for CSP.
 *
 * @returns Random nonce string
 */
export const generateNonce = (): string => {
  const array = new Uint8Array(16);
  crypto.getRandomValues(array);
  return Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('');
};

/**
 * Validates that a script element has the correct CSP nonce.
 *
 * @param element - Script element to validate
 * @param expectedNonce - Expected nonce value
 * @returns True if nonce is valid
 */
export const validateScriptNonce = (element: HTMLScriptElement, expectedNonce: string): boolean => {
  const nonce = element.getAttribute('nonce') || element.getAttribute(CSP_NONCE_ATTRIBUTE);
  return nonce === expectedNonce;
};
/**
 * Secure storage utilities for sensitive data like authentication tokens.
 * Provides enhanced security features including encryption and secure storage options.
 */

// Storage keys
const STORAGE_KEYS = {
  ACCESS_TOKEN: 'auth_access_token',
  REFRESH_TOKEN: 'auth_refresh_token',
  USER_DATA: 'auth_user_data',
  SESSION_TIMESTAMP: 'auth_session_timestamp',
  CSRF_TOKEN: 'csrf_token',
} as const;

// Session configuration
const SESSION_CONFIG = {
  MAX_IDLE_TIME: 30 * 60 * 1000, // 30 minutes in milliseconds
  WARNING_TIME: 5 * 60 * 1000,   // 5 minutes warning before timeout
  STORAGE_PREFIX: 'issuetracker_',
} as const;

/**
 * Simple encryption/decryption using Web Crypto API for additional security.
 * Note: This is not a replacement for HTTPS but adds an extra layer of protection.
 */
class SecureStorage {
  private static instance: SecureStorage;
  private encryptionKey: CryptoKey | null = null;

  private constructor() {
    this.initializeEncryption();
  }

  public static getInstance(): SecureStorage {
    if (!SecureStorage.instance) {
      SecureStorage.instance = new SecureStorage();
    }
    return SecureStorage.instance;
  }

  /**
   * Initialize encryption key for secure storage.
   */
  private async initializeEncryption(): Promise<void> {
    try {
      // Generate or retrieve encryption key
      const keyData = localStorage.getItem(SESSION_CONFIG.STORAGE_PREFIX + 'key');
      
      if (keyData) {
        // Import existing key
        const keyBuffer = this.base64ToArrayBuffer(keyData);
        this.encryptionKey = await crypto.subtle.importKey(
          'raw',
          keyBuffer,
          { name: 'AES-GCM' },
          false,
          ['encrypt', 'decrypt']
        );
      } else {
        // Generate new key
        this.encryptionKey = await crypto.subtle.generateKey(
          { name: 'AES-GCM', length: 256 },
          true,
          ['encrypt', 'decrypt']
        );
        
        // Export and store key
        const keyBuffer = await crypto.subtle.exportKey('raw', this.encryptionKey);
        const keyBase64 = this.arrayBufferToBase64(keyBuffer);
        localStorage.setItem(SESSION_CONFIG.STORAGE_PREFIX + 'key', keyBase64);
      }
    } catch (error) {
      console.warn('Failed to initialize encryption, falling back to plain storage:', error);
      this.encryptionKey = null;
    }
  }

  /**
   * Encrypt data using AES-GCM.
   */
  private async encrypt(data: string): Promise<string> {
    if (!this.encryptionKey) {
      return data; // Fallback to plain text if encryption fails
    }

    try {
      const encoder = new TextEncoder();
      const dataBuffer = encoder.encode(data);
      
      // Generate random IV
      const iv = crypto.getRandomValues(new Uint8Array(12));
      
      // Encrypt data
      const encryptedBuffer = await crypto.subtle.encrypt(
        { name: 'AES-GCM', iv },
        this.encryptionKey,
        dataBuffer
      );
      
      // Combine IV and encrypted data
      const combined = new Uint8Array(iv.length + encryptedBuffer.byteLength);
      combined.set(iv);
      combined.set(new Uint8Array(encryptedBuffer), iv.length);
      
      return this.arrayBufferToBase64(combined.buffer);
    } catch (error) {
      console.warn('Encryption failed, storing as plain text:', error);
      return data;
    }
  }

  /**
   * Decrypt data using AES-GCM.
   */
  private async decrypt(encryptedData: string): Promise<string> {
    if (!this.encryptionKey) {
      return encryptedData; // Fallback to plain text if encryption not available
    }

    try {
      const combined = this.base64ToArrayBuffer(encryptedData);
      
      // Extract IV and encrypted data
      const iv = combined.slice(0, 12);
      const encryptedBuffer = combined.slice(12);
      
      // Decrypt data
      const decryptedBuffer = await crypto.subtle.decrypt(
        { name: 'AES-GCM', iv },
        this.encryptionKey,
        encryptedBuffer
      );
      
      const decoder = new TextDecoder();
      return decoder.decode(decryptedBuffer);
    } catch (error) {
      console.warn('Decryption failed, returning as plain text:', error);
      return encryptedData;
    }
  }

  /**
   * Store data securely with optional encryption.
   */
  public async setItem(key: string, value: string, encrypt: boolean = true): Promise<void> {
    try {
      const storageKey = SESSION_CONFIG.STORAGE_PREFIX + key;
      const dataToStore = encrypt ? await this.encrypt(value) : value;
      
      // Use sessionStorage for sensitive data, localStorage for less sensitive
      const storage = this.isSensitiveKey(key) ? sessionStorage : localStorage;
      storage.setItem(storageKey, dataToStore);
      
      // Update session timestamp
      this.updateSessionTimestamp();
    } catch (error) {
      console.error('Failed to store data securely:', error);
      throw new Error('Storage operation failed');
    }
  }

  /**
   * Retrieve data securely with optional decryption.
   */
  public async getItem(key: string, decrypt: boolean = true): Promise<string | null> {
    try {
      const storageKey = SESSION_CONFIG.STORAGE_PREFIX + key;
      
      // Check both sessionStorage and localStorage
      const storage = this.isSensitiveKey(key) ? sessionStorage : localStorage;
      const storedData = storage.getItem(storageKey);
      
      if (!storedData) {
        return null;
      }
      
      return decrypt ? await this.decrypt(storedData) : storedData;
    } catch (error) {
      console.error('Failed to retrieve data securely:', error);
      return null;
    }
  }

  /**
   * Remove item from secure storage.
   */
  public removeItem(key: string): void {
    const storageKey = SESSION_CONFIG.STORAGE_PREFIX + key;
    sessionStorage.removeItem(storageKey);
    localStorage.removeItem(storageKey);
  }

  /**
   * Clear all secure storage data.
   */
  public clear(): void {
    // Clear all items with our prefix
    const keysToRemove: string[] = [];
    
    // Check sessionStorage
    for (let i = 0; i < sessionStorage.length; i++) {
      const key = sessionStorage.key(i);
      if (key && key.startsWith(SESSION_CONFIG.STORAGE_PREFIX)) {
        keysToRemove.push(key);
      }
    }
    
    // Check localStorage
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key && key.startsWith(SESSION_CONFIG.STORAGE_PREFIX)) {
        keysToRemove.push(key);
      }
    }
    
    // Remove all found keys
    keysToRemove.forEach(key => {
      sessionStorage.removeItem(key);
      localStorage.removeItem(key);
    });
  }

  /**
   * Check if session is still valid based on idle time.
   */
  public isSessionValid(): boolean {
    const timestamp = localStorage.getItem(SESSION_CONFIG.STORAGE_PREFIX + STORAGE_KEYS.SESSION_TIMESTAMP);
    
    if (!timestamp) {
      return false;
    }
    
    const lastActivity = parseInt(timestamp, 10);
    const now = Date.now();
    const idleTime = now - lastActivity;
    
    return idleTime < SESSION_CONFIG.MAX_IDLE_TIME;
  }

  /**
   * Get remaining session time in milliseconds.
   */
  public getRemainingSessionTime(): number {
    const timestamp = localStorage.getItem(SESSION_CONFIG.STORAGE_PREFIX + STORAGE_KEYS.SESSION_TIMESTAMP);
    
    if (!timestamp) {
      return 0;
    }
    
    const lastActivity = parseInt(timestamp, 10);
    const now = Date.now();
    const idleTime = now - lastActivity;
    
    return Math.max(0, SESSION_CONFIG.MAX_IDLE_TIME - idleTime);
  }

  /**
   * Check if session warning should be shown.
   */
  public shouldShowSessionWarning(): boolean {
    const remainingTime = this.getRemainingSessionTime();
    return remainingTime > 0 && remainingTime <= SESSION_CONFIG.WARNING_TIME;
  }

  /**
   * Update session timestamp to current time.
   */
  public updateSessionTimestamp(): void {
    localStorage.setItem(
      SESSION_CONFIG.STORAGE_PREFIX + STORAGE_KEYS.SESSION_TIMESTAMP,
      Date.now().toString()
    );
  }

  /**
   * Determine if a key contains sensitive data.
   */
  private isSensitiveKey(key: string): boolean {
    const sensitiveKeys = [
      STORAGE_KEYS.ACCESS_TOKEN,
      STORAGE_KEYS.REFRESH_TOKEN,
      STORAGE_KEYS.CSRF_TOKEN,
    ];
    return sensitiveKeys.includes(key as any);
  }

  /**
   * Convert ArrayBuffer to base64 string.
   */
  private arrayBufferToBase64(buffer: ArrayBuffer): string {
    const bytes = new Uint8Array(buffer);
    let binary = '';
    for (let i = 0; i < bytes.byteLength; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary);
  }

  /**
   * Convert base64 string to ArrayBuffer.
   */
  private base64ToArrayBuffer(base64: string): ArrayBuffer {
    const binary = atob(base64);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) {
      bytes[i] = binary.charCodeAt(i);
    }
    return bytes.buffer;
  }
}

// Export singleton instance and storage keys
export const secureStorage = SecureStorage.getInstance();
export { STORAGE_KEYS, SESSION_CONFIG };

// Convenience functions for token management
export const tokenStorage = {
  async setAccessToken(token: string): Promise<void> {
    await secureStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, token, true);
  },

  async getAccessToken(): Promise<string | null> {
    return await secureStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN, true);
  },

  async setRefreshToken(token: string): Promise<void> {
    await secureStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, token, true);
  },

  async getRefreshToken(): Promise<string | null> {
    return await secureStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN, true);
  },

  clearTokens(): void {
    secureStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
    secureStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
  },

  async setUserData(userData: any): Promise<void> {
    await secureStorage.setItem(STORAGE_KEYS.USER_DATA, JSON.stringify(userData), false);
  },

  async getUserData(): Promise<any | null> {
    const data = await secureStorage.getItem(STORAGE_KEYS.USER_DATA, false);
    return data ? JSON.parse(data) : null;
  },

  clearUserData(): void {
    secureStorage.removeItem(STORAGE_KEYS.USER_DATA);
  },
};
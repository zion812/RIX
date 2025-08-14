/**
 * Firebase Production Configuration for RIO Platform
 * Optimized for rural India deployment with 600K+ users
 */

const admin = require('firebase-admin');

// Production Firebase configuration
const productionConfig = {
  // Project configuration
  projectId: 'rio-platform-prod',
  region: 'asia-south1', // Mumbai region for optimal India performance

  // Firestore configuration
  firestore: {
    // Multi-region configuration for better performance
    databaseId: '(default)',
    settings: {
      ignoreUndefinedProperties: true,
      timestampsInSnapshots: true,
      // Optimized for rural connectivity
      cacheSizeBytes: 100 * 1024 * 1024, // 100MB cache
      experimentalForceLongPolling: false, // Use WebSocket when available
      experimentalAutoDetectLongPolling: true, // Auto-detect best connection
    }
  },

  // Authentication configuration
  auth: {
    // Custom claims configuration
    customClaims: {
      maxSize: 1000, // 1KB limit for custom claims
      tierLevels: ['general', 'farmer', 'enthusiast'],
      permissions: [
        'canCreateListings',
        'canEditListings',
        'canDeleteListings',
        'canAccessMarketplace',
        'canManageBreedingRecords',
        'canAccessAnalytics',
        'canAccessPremiumFeatures',
        'canVerifyTransfers',
        'canAccessPrioritySupport',
        'canModerateContent'
      ]
    },

    // Session management
    sessionCookieMaxAge: 60 * 60 * 24 * 5 * 1000, // 5 days

    // Multi-factor authentication
    mfa: {
      enabled: true,
      providers: ['phone', 'totp']
    }
  },

  // Storage configuration
  storage: {
    bucket: 'rio-platform-prod.appspot.com',
    // Regional buckets for better performance
    regionalBuckets: {
      'andhra-pradesh': 'rio-ap-storage',
      'telangana': 'rio-ts-storage',
      'default': 'rio-platform-prod.appspot.com'
    },

    // Image optimization settings
    imageOptimization: {
      maxWidth: 1920,
      maxHeight: 1080,
      quality: 85,
      format: 'webp', // Better compression for rural networks
      thumbnailSizes: [150, 300, 600]
    },

    // Upload limits
    maxFileSize: 10 * 1024 * 1024, // 10MB max file size
    allowedMimeTypes: [
      'image/jpeg',
      'image/png',
      'image/webp',
      'video/mp4',
      'application/pdf'
    ]
  },

  // Functions configuration
  functions: {
    region: 'asia-south1',
    runtime: 'nodejs18',
    memory: '512MB',
    timeout: 60,

    // Environment variables
    env: {
      NODE_ENV: 'production',
      LOG_LEVEL: 'info',
      ENABLE_ANALYTICS: 'true',
      ENABLE_MONITORING: 'true'
    },

    // Scaling configuration
    scaling: {
      minInstances: 2, // Always keep 2 instances warm
      maxInstances: 100, // Scale up to 100 instances
      concurrency: 80 // 80 concurrent requests per instance
    }
  },

  // Analytics configuration
  analytics: {
    enabled: true,
    dataRetentionDays: 365,

    // Custom events for rural metrics
    customEvents: [
      'fowl_registration',
      'marketplace_listing_created',
      'payment_completed',
      'offline_sync_completed',
      'tier_upgrade_requested',
      'rural_connectivity_issue'
    ],

    // User properties
    userProperties: [
      'user_tier',
      'region',
      'district',
      'connectivity_type',
      'device_type',
      'language_preference'
    ]
  },

  // Performance monitoring
  performance: {
    enabled: true,

    // Custom traces for rural optimization
    customTraces: [
      'app_startup_time',
      'offline_sync_duration',
      'image_upload_time',
      'payment_processing_time',
      'search_response_time'
    ],

    // Network request monitoring
    networkRequestMonitoring: {
      enabled: true,
      urlPatterns: [
        'https://firestore.googleapis.com/*',
        'https://firebase.googleapis.com/*',
        'https://storage.googleapis.com/*'
      ]
    }
  },

  // Crashlytics configuration
  crashlytics: {
    enabled: true,

    // Custom keys for rural debugging
    customKeys: [
      'user_tier',
      'connectivity_type',
      'sync_status',
      'offline_actions_pending',
      'battery_level',
      'storage_available'
    ],

    // Crash reporting settings
    reportingSettings: {
      enableAutomaticDataCollection: true,
      enableCrashlyticsCollection: true,
      enablePerformanceCollection: true
    }
  }
};

// Security rules for production
const securityRules = {
  firestore: `
    rules_version = '2';
    service cloud.firestore {
      match /databases/{database}/documents {
        // User documents - users can only access their own data
        match /users/{userId} {
          allow read, write: if request.auth != null && request.auth.uid == userId;
          allow read: if request.auth != null &&
                     request.auth.token.tier in ['farmer', 'enthusiast'] &&
                     resource.data.region == request.auth.token.region;
        }

        // Fowl documents - tier-based access
        match /fowls/{fowlId} {
          allow read: if request.auth != null;
          allow create: if request.auth != null &&
                       request.auth.token.canCreateListings == true;
          allow update, delete: if request.auth != null &&
                               (resource.data.ownerId == request.auth.uid ||
                                request.auth.token.canModerateContent == true);
        }

        // Marketplace listings - public read, tier-based write
        match /marketplace/{listingId} {
          allow read: if request.auth != null;
          allow create: if request.auth != null &&
                       request.auth.token.canCreateListings == true;
          allow update, delete: if request.auth != null &&
                               (resource.data.sellerId == request.auth.uid ||
                                request.auth.token.canModerateContent == true);
        }

        // Messages - conversation participants only
        match /messages/{messageId} {
          allow read, write: if request.auth != null &&
                            (request.auth.uid in resource.data.participantIds ||
                             request.auth.uid == resource.data.senderId);
        }

        // Transfers - involved parties and moderators only
        match /transfers/{transferId} {
          allow read: if request.auth != null &&
                     (request.auth.uid == resource.data.fromUserId ||
                      request.auth.uid == resource.data.toUserId ||
                      request.auth.token.canVerifyTransfers == true);
          allow create: if request.auth != null;
          allow update: if request.auth != null &&
                       request.auth.token.canVerifyTransfers == true;
        }
      }
    }
  `,

  storage: `
    rules_version = '2';
    service firebase.storage {
      match /b/{bucket}/o {
        // User profile images
        match /users/{userId}/profile/{imageId} {
          allow read: if request.auth != null;
          allow write: if request.auth != null && request.auth.uid == userId;
        }

        // Fowl images - owner and public read access
        match /fowls/{fowlId}/images/{imageId} {
          allow read: if request.auth != null;
          allow write: if request.auth != null &&
                      request.auth.token.canCreateListings == true;
        }

        // Verification documents - user and admin access only
        match /verification/{userId}/documents/{documentId} {
          allow read, write: if request.auth != null &&
                            (request.auth.uid == userId ||
                             request.auth.token.canModerateContent == true);
        }
      }
    }
  `
};

module.exports = {
  productionConfig,
  securityRules
};
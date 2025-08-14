/**
 * RIO Analytics Dashboard Configuration
 * Comprehensive monitoring for rural farmer platform with connectivity optimization
 */

const analyticsConfig = {
  // Firebase Analytics Configuration
  firebase: {
    // Core events for rural farmer behavior
    customEvents: {
      // User Journey Events
      'farmer_onboarding_started': {
        parameters: ['user_tier', 'region', 'language', 'device_type'],
        description: 'Farmer begins onboarding process'
      },
      'farmer_onboarding_completed': {
        parameters: ['completion_time', 'steps_completed', 'tier_selected'],
        description: 'Farmer completes onboarding successfully'
      },
      'tier_upgrade_requested': {
        parameters: ['from_tier', 'to_tier', 'reason', 'documents_uploaded'],
        description: 'User requests tier upgrade'
      },

      // Fowl Management Events
      'fowl_registered': {
        parameters: ['breed', 'age_category', 'gender', 'photo_count'],
        description: 'New fowl registered in system'
      },
      'fowl_updated': {
        parameters: ['update_type', 'fields_changed', 'photo_added'],
        description: 'Fowl information updated'
      },
      'family_tree_viewed': {
        parameters: ['fowl_id', 'generation_depth', 'view_duration'],
        description: 'User views fowl family tree'
      },

      // Marketplace Events
      'listing_created': {
        parameters: ['fowl_breed', 'price_range', 'listing_type', 'photo_count'],
        description: 'New marketplace listing created'
      },
      'listing_viewed': {
        parameters: ['listing_id', 'viewer_tier', 'view_duration', 'region_match'],
        description: 'Marketplace listing viewed'
      },
      'purchase_initiated': {
        parameters: ['listing_id', 'payment_method', 'buyer_tier'],
        description: 'Purchase process started'
      },
      'purchase_completed': {
        parameters: ['transaction_amount', 'payment_method', 'completion_time'],
        description: 'Purchase successfully completed'
      },

      // Communication Events
      'message_sent': {
        parameters: ['conversation_type', 'message_type', 'recipient_tier'],
        description: 'Message sent between users'
      },
      'chat_initiated': {
        parameters: ['initiator_tier', 'recipient_tier', 'context_type'],
        description: 'New chat conversation started'
      },

      // Rural Connectivity Events
      'offline_mode_activated': {
        parameters: ['trigger_reason', 'pending_actions', 'data_cached'],
        description: 'App switches to offline mode'
      },
      'sync_completed': {
        parameters: ['sync_duration', 'items_synced', 'conflicts_resolved'],
        description: 'Data synchronization completed'
      },
      'connectivity_issue': {
        parameters: ['network_type', 'error_type', 'retry_count'],
        description: 'Network connectivity problem encountered'
      },

      // Performance Events
      'app_startup': {
        parameters: ['startup_time', 'cold_start', 'cached_data_size'],
        description: 'App startup performance tracking'
      },
      'feature_load_time': {
        parameters: ['feature_name', 'load_time', 'network_type'],
        description: 'Feature loading performance'
      },
      'image_upload': {
        parameters: ['file_size', 'compression_ratio', 'upload_time'],
        description: 'Image upload performance'
      }
    },

    // User Properties for Segmentation
    userProperties: {
      'user_tier': ['general', 'farmer', 'enthusiast'],
      'region': ['andhra_pradesh', 'telangana', 'other'],
      'district': 'string',
      'language_preference': ['te', 'hi', 'en'],
      'connectivity_type': ['2g', '3g', '4g', 'wifi'],
      'device_category': ['low_end', 'mid_range', 'high_end'],
      'farming_experience': ['beginner', 'intermediate', 'expert'],
      'flock_size_category': ['small', 'medium', 'large'],
      'verification_status': ['unverified', 'basic', 'enhanced', 'premium']
    },

    // Conversion Funnels
    conversionFunnels: {
      'farmer_onboarding': [
        'app_install',
        'farmer_onboarding_started',
        'phone_verification_completed',
        'farmer_onboarding_completed',
        'first_fowl_registered'
      ],
      'marketplace_purchase': [
        'listing_viewed',
        'contact_seller',
        'purchase_initiated',
        'payment_completed',
        'transfer_verified'
      ],
      'tier_upgrade': [
        'tier_upgrade_requested',
        'documents_uploaded',
        'verification_submitted',
        'tier_upgrade_approved',
        'premium_feature_used'
      ]
    }
  },

  // Rural-Specific Metrics Dashboard
  ruralMetrics: {
    // Connectivity Optimization Metrics
    connectivity: {
      networkQualityDistribution: {
        query: 'SELECT network_type, COUNT(*) as users FROM user_sessions GROUP BY network_type',
        visualization: 'pie_chart',
        refreshInterval: '5m'
      },
      offlineUsagePatterns: {
        query: 'SELECT hour, AVG(offline_duration) FROM offline_sessions GROUP BY hour',
        visualization: 'line_chart',
        refreshInterval: '1h'
      },
      syncPerformance: {
        query: 'SELECT region, AVG(sync_duration), AVG(items_synced) FROM sync_events GROUP BY region',
        visualization: 'bar_chart',
        refreshInterval: '15m'
      },
      dataUsageOptimization: {
        query: 'SELECT feature, SUM(data_consumed) FROM feature_usage GROUP BY feature',
        visualization: 'horizontal_bar',
        refreshInterval: '1h'
      }
    },

    // User Behavior Analytics
    userBehavior: {
      tierDistribution: {
        query: 'SELECT user_tier, COUNT(*) FROM users WHERE active=true GROUP BY user_tier',
        visualization: 'donut_chart',
        refreshInterval: '1h'
      },
      regionalAdoption: {
        query: 'SELECT district, COUNT(*) as farmers FROM users WHERE user_tier IN ("farmer", "enthusiast") GROUP BY district',
        visualization: 'map_chart',
        refreshInterval: '6h'
      },
      featureAdoption: {
        query: 'SELECT feature_name, COUNT(DISTINCT user_id) as users FROM feature_usage GROUP BY feature_name',
        visualization: 'bar_chart',
        refreshInterval: '30m'
      },
      retentionCohorts: {
        query: 'SELECT install_week, retention_day, retention_rate FROM user_retention',
        visualization: 'cohort_table',
        refreshInterval: '24h'
      }
    },

    // Business Performance Metrics
    business: {
      transactionVolume: {
        query: 'SELECT DATE(created_at) as date, SUM(amount) as volume FROM transactions GROUP BY date',
        visualization: 'line_chart',
        refreshInterval: '1h'
      },
      marketplaceHealth: {
        query: 'SELECT listing_status, COUNT(*) FROM marketplace_listings GROUP BY listing_status',
        visualization: 'stacked_bar',
        refreshInterval: '30m'
      },
      fowlRegistrations: {
        query: 'SELECT breed, COUNT(*) as registrations FROM fowls GROUP BY breed ORDER BY registrations DESC LIMIT 10',
        visualization: 'horizontal_bar',
        refreshInterval: '6h'
      },
      revenueMetrics: {
        query: 'SELECT revenue_source, SUM(amount) FROM revenue_events GROUP BY revenue_source',
        visualization: 'pie_chart',
        refreshInterval: '1h'
      }
    }
  }
};
package com.rio.rostry.core.config

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Configuration manager for field testing deployment
 * Handles feature flags, monitoring settings, and rural optimizations
 */
@Singleton
class FieldTestingConfig @Inject constructor(
    private val context: Context
) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "field_testing_config", Context.MODE_PRIVATE
    )
    
    companion object {
        // Feature flags for controlled rollout
        const val FEATURE_ORDER_PLACEMENT = "feature_order_placement"
        const val FEATURE_COMMUNITY_CHAT = "feature_community_chat"
        const val FEATURE_LISTING_CREATION = "feature_listing_creation"
        const val FEATURE_PAYMENT_INTEGRATION = "feature_payment_integration"
        const val FEATURE_OFFLINE_MODE = "feature_offline_mode"
        
        // Performance settings
        const val MAX_IMAGE_SIZE_KB = "max_image_size_kb"
        const val CACHE_EXPIRY_HOURS = "cache_expiry_hours"
        const val SYNC_INTERVAL_MINUTES = "sync_interval_minutes"
        const val MAX_RETRY_ATTEMPTS = "max_retry_attempts"
        
        // Rural optimization settings
        const val ENABLE_DATA_SAVER = "enable_data_saver"
        const val COMPRESS_IMAGES = "compress_images"
        const val PRELOAD_CONTENT = "preload_content"
        const val OFFLINE_FIRST_MODE = "offline_first_mode"
        
        // Monitoring settings
        const val ENABLE_ANALYTICS = "enable_analytics"
        const val ENABLE_CRASH_REPORTING = "enable_crash_reporting"
        const val ENABLE_PERFORMANCE_MONITORING = "enable_performance_monitoring"
        const val ANALYTICS_UPLOAD_INTERVAL = "analytics_upload_interval"
    }
    
    init {
        // Set default values for field testing
        setDefaultValues()
    }
    
    private fun setDefaultValues() {
        val editor = prefs.edit()
        
        // Feature flags - conservative approach for field testing
        if (!prefs.contains(FEATURE_ORDER_PLACEMENT)) {
            editor.putBoolean(FEATURE_ORDER_PLACEMENT, true) // Enable for testing
        }
        if (!prefs.contains(FEATURE_COMMUNITY_CHAT)) {
            editor.putBoolean(FEATURE_COMMUNITY_CHAT, true)
        }
        if (!prefs.contains(FEATURE_LISTING_CREATION)) {
            editor.putBoolean(FEATURE_LISTING_CREATION, true)
        }
        if (!prefs.contains(FEATURE_PAYMENT_INTEGRATION)) {
            editor.putBoolean(FEATURE_PAYMENT_INTEGRATION, false) // Disable until backend ready
        }
        if (!prefs.contains(FEATURE_OFFLINE_MODE)) {
            editor.putBoolean(FEATURE_OFFLINE_MODE, true)
        }
        
        // Performance settings optimized for rural networks
        if (!prefs.contains(MAX_IMAGE_SIZE_KB)) {
            editor.putInt(MAX_IMAGE_SIZE_KB, 500) // 500KB max for rural networks
        }
        if (!prefs.contains(CACHE_EXPIRY_HOURS)) {
            editor.putInt(CACHE_EXPIRY_HOURS, 24)
        }
        if (!prefs.contains(SYNC_INTERVAL_MINUTES)) {
            editor.putInt(SYNC_INTERVAL_MINUTES, 30)
        }
        if (!prefs.contains(MAX_RETRY_ATTEMPTS)) {
            editor.putInt(MAX_RETRY_ATTEMPTS, 3)
        }
        
        // Rural optimization settings
        if (!prefs.contains(ENABLE_DATA_SAVER)) {
            editor.putBoolean(ENABLE_DATA_SAVER, true)
        }
        if (!prefs.contains(COMPRESS_IMAGES)) {
            editor.putBoolean(COMPRESS_IMAGES, true)
        }
        if (!prefs.contains(PRELOAD_CONTENT)) {
            editor.putBoolean(PRELOAD_CONTENT, false) // Disable to save data
        }
        if (!prefs.contains(OFFLINE_FIRST_MODE)) {
            editor.putBoolean(OFFLINE_FIRST_MODE, true)
        }
        
        // Monitoring settings
        if (!prefs.contains(ENABLE_ANALYTICS)) {
            editor.putBoolean(ENABLE_ANALYTICS, true)
        }
        if (!prefs.contains(ENABLE_CRASH_REPORTING)) {
            editor.putBoolean(ENABLE_CRASH_REPORTING, true)
        }
        if (!prefs.contains(ENABLE_PERFORMANCE_MONITORING)) {
            editor.putBoolean(ENABLE_PERFORMANCE_MONITORING, true)
        }
        if (!prefs.contains(ANALYTICS_UPLOAD_INTERVAL)) {
            editor.putInt(ANALYTICS_UPLOAD_INTERVAL, 60) // 1 hour
        }
        
        editor.apply()
    }
    
    // Feature flag methods
    fun isFeatureEnabled(feature: String): Boolean {
        return prefs.getBoolean(feature, false)
    }
    
    fun enableFeature(feature: String) {
        prefs.edit().putBoolean(feature, true).apply()
    }
    
    fun disableFeature(feature: String) {
        prefs.edit().putBoolean(feature, false).apply()
    }
    
    // Performance settings
    fun getMaxImageSizeKB(): Int {
        return prefs.getInt(MAX_IMAGE_SIZE_KB, 500)
    }
    
    fun getCacheExpiryHours(): Int {
        return prefs.getInt(CACHE_EXPIRY_HOURS, 24)
    }
    
    fun getSyncIntervalMinutes(): Int {
        return prefs.getInt(SYNC_INTERVAL_MINUTES, 30)
    }
    
    fun getMaxRetryAttempts(): Int {
        return prefs.getInt(MAX_RETRY_ATTEMPTS, 3)
    }
    
    // Rural optimization settings
    fun isDataSaverEnabled(): Boolean {
        return prefs.getBoolean(ENABLE_DATA_SAVER, true)
    }
    
    fun shouldCompressImages(): Boolean {
        return prefs.getBoolean(COMPRESS_IMAGES, true)
    }
    
    fun shouldPreloadContent(): Boolean {
        return prefs.getBoolean(PRELOAD_CONTENT, false)
    }
    
    fun isOfflineFirstMode(): Boolean {
        return prefs.getBoolean(OFFLINE_FIRST_MODE, true)
    }
    
    // Monitoring settings
    fun isAnalyticsEnabled(): Boolean {
        return prefs.getBoolean(ENABLE_ANALYTICS, true)
    }
    
    fun isCrashReportingEnabled(): Boolean {
        return prefs.getBoolean(ENABLE_CRASH_REPORTING, true)
    }
    
    fun isPerformanceMonitoringEnabled(): Boolean {
        return prefs.getBoolean(ENABLE_PERFORMANCE_MONITORING, true)
    }
    
    fun getAnalyticsUploadInterval(): Int {
        return prefs.getInt(ANALYTICS_UPLOAD_INTERVAL, 60)
    }
    
    // Field testing specific methods
    fun getFieldTestingMode(): FieldTestingMode {
        val mode = prefs.getString("field_testing_mode", FieldTestingMode.PILOT.name)
        return try {
            FieldTestingMode.valueOf(mode ?: FieldTestingMode.PILOT.name)
        } catch (e: Exception) {
            FieldTestingMode.PILOT
        }
    }
    
    fun setFieldTestingMode(mode: FieldTestingMode) {
        prefs.edit().putString("field_testing_mode", mode.name).apply()
        
        // Adjust settings based on mode
        when (mode) {
            FieldTestingMode.DEVELOPMENT -> {
                enableFeature(FEATURE_ORDER_PLACEMENT)
                enableFeature(FEATURE_COMMUNITY_CHAT)
                enableFeature(FEATURE_LISTING_CREATION)
                prefs.edit().putInt(ANALYTICS_UPLOAD_INTERVAL, 5).apply() // 5 minutes for dev
            }
            FieldTestingMode.PILOT -> {
                enableFeature(FEATURE_ORDER_PLACEMENT)
                enableFeature(FEATURE_COMMUNITY_CHAT)
                enableFeature(FEATURE_LISTING_CREATION)
                disableFeature(FEATURE_PAYMENT_INTEGRATION)
            }
            FieldTestingMode.PRODUCTION -> {
                // Enable all features for production
                enableFeature(FEATURE_ORDER_PLACEMENT)
                enableFeature(FEATURE_COMMUNITY_CHAT)
                enableFeature(FEATURE_LISTING_CREATION)
                enableFeature(FEATURE_PAYMENT_INTEGRATION)
            }
        }
    }
    
    fun getTestingPhase(): TestingPhase {
        val phase = prefs.getString("testing_phase", TestingPhase.TECHNICAL_VALIDATION.name)
        return try {
            TestingPhase.valueOf(phase ?: TestingPhase.TECHNICAL_VALIDATION.name)
        } catch (e: Exception) {
            TestingPhase.TECHNICAL_VALIDATION
        }
    }
    
    fun setTestingPhase(phase: TestingPhase) {
        prefs.edit().putString("testing_phase", phase.name).apply()
    }
    
    fun getParticipantId(): String {
        var participantId = prefs.getString("participant_id", null)
        if (participantId == null) {
            participantId = "PILOT_${System.currentTimeMillis()}"
            prefs.edit().putString("participant_id", participantId).apply()
        }
        return participantId
    }
    
    fun isDebugMode(): Boolean {
        return prefs.getBoolean("debug_mode", false)
    }
    
    fun setDebugMode(enabled: Boolean) {
        prefs.edit().putBoolean("debug_mode", enabled).apply()
    }
    
    // Get configuration summary for monitoring
    fun getConfigSummary(): Map<String, Any> {
        return mapOf(
            "field_testing_mode" to getFieldTestingMode().name,
            "testing_phase" to getTestingPhase().name,
            "participant_id" to getParticipantId(),
            "features_enabled" to mapOf(
                "order_placement" to isFeatureEnabled(FEATURE_ORDER_PLACEMENT),
                "community_chat" to isFeatureEnabled(FEATURE_COMMUNITY_CHAT),
                "listing_creation" to isFeatureEnabled(FEATURE_LISTING_CREATION),
                "payment_integration" to isFeatureEnabled(FEATURE_PAYMENT_INTEGRATION),
                "offline_mode" to isFeatureEnabled(FEATURE_OFFLINE_MODE)
            ),
            "performance_settings" to mapOf(
                "max_image_size_kb" to getMaxImageSizeKB(),
                "cache_expiry_hours" to getCacheExpiryHours(),
                "sync_interval_minutes" to getSyncIntervalMinutes()
            ),
            "rural_optimizations" to mapOf(
                "data_saver" to isDataSaverEnabled(),
                "compress_images" to shouldCompressImages(),
                "offline_first" to isOfflineFirstMode()
            )
        )
    }
}

enum class FieldTestingMode {
    DEVELOPMENT,    // Internal testing with all features enabled
    PILOT,          // Limited pilot with selected features
    PRODUCTION      // Full production deployment
}

enum class TestingPhase {
    TECHNICAL_VALIDATION,   // Week 1-2: Technical functionality
    UX_VALIDATION,          // Week 3-4: User experience testing
    MARKET_VALIDATION       // Week 5-8: Real marketplace activity
}

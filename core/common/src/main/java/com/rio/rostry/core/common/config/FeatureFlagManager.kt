package com.rio.rostry.core.common.config

import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await

/**
 * Manager for handling feature flags with Firebase Remote Config
 * Allows enabling/disabling features remotely for gradual rollouts and A/B testing
 */
class FeatureFlagManager private constructor(private val context: Context) {
    
    private val remoteConfig = FirebaseRemoteConfig.getInstance()
    
    companion object {
        @Volatile
        private var INSTANCE: FeatureFlagManager? = null
        
        fun getInstance(context: Context): FeatureFlagManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FeatureFlagManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // 1 hour in production
            .build()
        
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(FeatureFlags.DEFAULT_VALUES)
    }
    
    /**
     * Fetch and activate the latest remote config values
     */
    suspend fun fetchAndActivate(): Boolean {
        return try {
            val updated = remoteConfig.fetchAndActivate().await()
            updated
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get the value of a boolean feature flag
     */
    fun isFeatureEnabled(flagKey: String): Boolean {
        return remoteConfig.getBoolean(flagKey)
    }
    
    /**
     * Get the value of a string feature flag
     */
    fun getStringValue(flagKey: String): String {
        return remoteConfig.getString(flagKey)
    }
    
    /**
     * Get the value of a long feature flag
     */
    fun getLongValue(flagKey: String): Long {
        return remoteConfig.getLong(flagKey)
    }
    
    /**
     * Get the value of a double feature flag
     */
    fun getDoubleValue(flagKey: String): Double {
        return remoteConfig.getDouble(flagKey)
    }
    
    /**
     * Force refresh all feature flags
     */
    suspend fun forceRefresh(): Boolean {
        return try {
            remoteConfig.fetch(0).await()
            val activated = remoteConfig.activate().await()
            activated
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if Fowl Records feature is enabled
     */
    fun isFowlRecordsEnabled(): Boolean {
        return isFeatureEnabled(FeatureFlags.FOWL_RECORDS_ENABLED)
    }
    
    /**
     * Check if UploadProofWorker is enabled
     */
    fun isUploadProofWorkerEnabled(): Boolean {
        return isFeatureEnabled(FeatureFlags.UPLOAD_PROOF_WORKER_ENABLED)
    }
    
    /**
     * Check if export sharing is enabled
     */
    fun isExportSharingEnabled(): Boolean {
        return isFeatureEnabled(FeatureFlags.EXPORT_SHARING_ENABLED)
    }
    
    /**
     * Check if thumbnail caching is enabled
     */
    fun isThumbnailCachingEnabled(): Boolean {
        return isFeatureEnabled(FeatureFlags.THUMBNAIL_CACHING_ENABLED)
    }
    
    /**
     * Check if cover thumbnails are enabled
     */
    fun isCoverThumbnailsEnabled(): Boolean {
        return isFeatureEnabled(FeatureFlags.COVER_THUMBNAILS_ENABLED)
    }
    
    /**
     * Check if smart suggestions are enabled
     */
    fun isSmartSuggestionsEnabled(): Boolean {
        return isFeatureEnabled(FeatureFlags.SMART_SUGGESTIONS_ENABLED)
    }
}
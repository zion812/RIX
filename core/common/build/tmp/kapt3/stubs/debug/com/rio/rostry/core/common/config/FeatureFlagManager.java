package com.rio.rostry.core.common.config;

/**
 * Manager for handling feature flags with Firebase Remote Config
 * Allows enabling/disabling features remotely for gradual rollouts and A/B testing
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\n\u0018\u0000 \u00192\u00020\u0001:\u0001\u0019B\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0007\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\tJ\u000e\u0010\n\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\tJ\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u000f\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u0011\u001a\u00020\u000e2\u0006\u0010\r\u001a\u00020\u000eJ\u0006\u0010\u0012\u001a\u00020\bJ\u0006\u0010\u0013\u001a\u00020\bJ\u000e\u0010\u0014\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\u000eJ\u0006\u0010\u0015\u001a\u00020\bJ\u0006\u0010\u0016\u001a\u00020\bJ\u0006\u0010\u0017\u001a\u00020\bJ\u0006\u0010\u0018\u001a\u00020\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001a"}, d2 = {"Lcom/rio/rostry/core/common/config/FeatureFlagManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "remoteConfig", "Lcom/google/firebase/remoteconfig/FirebaseRemoteConfig;", "fetchAndActivate", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "forceRefresh", "getDoubleValue", "", "flagKey", "", "getLongValue", "", "getStringValue", "isCoverThumbnailsEnabled", "isExportSharingEnabled", "isFeatureEnabled", "isFowlRecordsEnabled", "isSmartSuggestionsEnabled", "isThumbnailCachingEnabled", "isUploadProofWorkerEnabled", "Companion", "common_debug"})
public final class FeatureFlagManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.remoteconfig.FirebaseRemoteConfig remoteConfig = null;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.rio.rostry.core.common.config.FeatureFlagManager INSTANCE;
    @org.jetbrains.annotations.NotNull()
    public static final com.rio.rostry.core.common.config.FeatureFlagManager.Companion Companion = null;
    
    private FeatureFlagManager(android.content.Context context) {
        super();
    }
    
    /**
     * Fetch and activate the latest remote config values
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object fetchAndActivate(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Get the value of a boolean feature flag
     */
    public final boolean isFeatureEnabled(@org.jetbrains.annotations.NotNull()
    java.lang.String flagKey) {
        return false;
    }
    
    /**
     * Get the value of a string feature flag
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getStringValue(@org.jetbrains.annotations.NotNull()
    java.lang.String flagKey) {
        return null;
    }
    
    /**
     * Get the value of a long feature flag
     */
    public final long getLongValue(@org.jetbrains.annotations.NotNull()
    java.lang.String flagKey) {
        return 0L;
    }
    
    /**
     * Get the value of a double feature flag
     */
    public final double getDoubleValue(@org.jetbrains.annotations.NotNull()
    java.lang.String flagKey) {
        return 0.0;
    }
    
    /**
     * Force refresh all feature flags
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object forceRefresh(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Check if Fowl Records feature is enabled
     */
    public final boolean isFowlRecordsEnabled() {
        return false;
    }
    
    /**
     * Check if UploadProofWorker is enabled
     */
    public final boolean isUploadProofWorkerEnabled() {
        return false;
    }
    
    /**
     * Check if export sharing is enabled
     */
    public final boolean isExportSharingEnabled() {
        return false;
    }
    
    /**
     * Check if thumbnail caching is enabled
     */
    public final boolean isThumbnailCachingEnabled() {
        return false;
    }
    
    /**
     * Check if cover thumbnails are enabled
     */
    public final boolean isCoverThumbnailsEnabled() {
        return false;
    }
    
    /**
     * Check if smart suggestions are enabled
     */
    public final boolean isSmartSuggestionsEnabled() {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0007R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/rio/rostry/core/common/config/FeatureFlagManager$Companion;", "", "()V", "INSTANCE", "Lcom/rio/rostry/core/common/config/FeatureFlagManager;", "getInstance", "context", "Landroid/content/Context;", "common_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.rio.rostry.core.common.config.FeatureFlagManager getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}
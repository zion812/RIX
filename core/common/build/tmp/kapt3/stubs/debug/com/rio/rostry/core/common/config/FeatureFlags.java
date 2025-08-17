package com.rio.rostry.core.common.config;

/**
 * Feature flags for the ROSTRY platform
 * Allows enabling/disabling features remotely for gradual rollouts and A/B testing
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000b\n\u0002\b\b\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0005\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/rio/rostry/core/common/config/FeatureFlags;", "", "()V", "COVER_THUMBNAILS_ENABLED", "", "DEFAULT_VALUES", "", "", "getDEFAULT_VALUES", "()Ljava/util/Map;", "EXPORT_SHARING_ENABLED", "FOWL_RECORDS_ENABLED", "SMART_SUGGESTIONS_ENABLED", "THUMBNAIL_CACHING_ENABLED", "UPLOAD_PROOF_WORKER_ENABLED", "common_debug"})
public final class FeatureFlags {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String FOWL_RECORDS_ENABLED = "fowl_records_enabled";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String UPLOAD_PROOF_WORKER_ENABLED = "upload_proof_worker_enabled";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXPORT_SHARING_ENABLED = "export_sharing_enabled";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String THUMBNAIL_CACHING_ENABLED = "thumbnail_caching_enabled";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String COVER_THUMBNAILS_ENABLED = "cover_thumbnails_enabled";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SMART_SUGGESTIONS_ENABLED = "smart_suggestions_enabled";
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Map<java.lang.String, java.lang.Boolean> DEFAULT_VALUES = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.rio.rostry.core.common.config.FeatureFlags INSTANCE = null;
    
    private FeatureFlags() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.lang.Boolean> getDEFAULT_VALUES() {
        return null;
    }
}
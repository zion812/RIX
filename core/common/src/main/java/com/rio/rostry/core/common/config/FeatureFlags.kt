package com.rio.rostry.core.common.config

/**
 * Feature flags for the ROSTRY platform
 * Allows enabling/disabling features remotely for gradual rollouts and A/B testing
 */
object FeatureFlags {
    // Fowl Records feature flag
    const val FOWL_RECORDS_ENABLED = "fowl_records_enabled"
    
    // UploadProofWorker kill switch
    const val UPLOAD_PROOF_WORKER_ENABLED = "upload_proof_worker_enabled"
    
    // Export sharing kill switch
    const val EXPORT_SHARING_ENABLED = "export_sharing_enabled"
    
    // Thumbnail caching feature flag
    const val THUMBNAIL_CACHING_ENABLED = "thumbnail_caching_enabled"
    
    // Cover thumbnails feature flag
    const val COVER_THUMBNAILS_ENABLED = "cover_thumbnails_enabled"
    
    // Smart suggestions feature flag
    const val SMART_SUGGESTIONS_ENABLED = "smart_suggestions_enabled"
    
    // Default values for feature flags
    val DEFAULT_VALUES = mapOf(
        FOWL_RECORDS_ENABLED to true,
        UPLOAD_PROOF_WORKER_ENABLED to true,
        EXPORT_SHARING_ENABLED to false, // Disabled by default for MVP
        THUMBNAIL_CACHING_ENABLED to true,
        COVER_THUMBNAILS_ENABLED to true,
        SMART_SUGGESTIONS_ENABLED to true
    )
}
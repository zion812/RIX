package com.rio.rostry.core.database.entities

import androidx.room.Embedded
import java.util.*

/**
 * Base interface for all entities that support offline synchronization
 */
interface SyncableEntity {
    val id: String
    val syncMetadata: SyncMetadata
    val isDeleted: Boolean
        get() = syncMetadata.isDeleted
    val syncStatus: SyncStatus
        get() = syncMetadata.syncStatus
}

/**
 * Synchronization metadata for offline-first entities
 */
data class SyncMetadata(
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD,
    val lastSyncTime: Date? = null,
    val conflictVersion: Int = 0,
    val retryCount: Int = 0,
    val isDeleted: Boolean = false,
    val priority: SyncPriority = SyncPriority.NORMAL,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * Synchronization status for entities
 */
enum class SyncStatus {
    SYNCED,              // Entity is synchronized with server
    PENDING_UPLOAD,      // Entity needs to be uploaded to server
    PENDING_DOWNLOAD,    // Entity needs to be downloaded from server
    CONFLICT,            // Entity has conflicts that need resolution
    ERROR,               // Entity sync failed with error
    UPLOADING,           // Entity is currently being uploaded
    DOWNLOADING          // Entity is currently being downloaded
}

/**
 * Priority levels for synchronization
 */
enum class SyncPriority {
    HIGH,     // Critical data (payments, transfers)
    NORMAL,   // Regular data (fowl records, marketplace)
    LOW       // Non-critical data (analytics, logs)
}

/**
 * Sync operation result
 */
data class SyncResult(
    val entityType: String,
    val totalItems: Int,
    val successCount: Int,
    val failureCount: Int,
    val conflictCount: Int,
    val bytesTransferred: Long,
    val duration: Long,
    val errors: List<SyncError> = emptyList()
)

/**
 * Sync error information
 */
data class SyncError(
    val entityId: String,
    val errorType: SyncErrorType,
    val message: String,
    val timestamp: Date = Date()
)

/**
 * Types of sync errors
 */
enum class SyncErrorType {
    NETWORK_ERROR,
    AUTHENTICATION_ERROR,
    PERMISSION_ERROR,
    VALIDATION_ERROR,
    CONFLICT_ERROR,
    UNKNOWN_ERROR
}
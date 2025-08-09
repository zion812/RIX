package com.rio.rostry.core.database.entities

import androidx.room.*
import java.util.*

/**
 * Base interface for all syncable entities with offline-first capabilities
 */
interface SyncableEntity {
    val id: String
    val lastSyncTime: Date?
    val syncStatus: SyncStatus
    val conflictVersion: Long
    val isDeleted: Boolean
    val createdAt: Date
    val updatedAt: Date
}

/**
 * Sync status enumeration
 */
enum class SyncStatus {
    SYNCED,           // Successfully synced with server
    PENDING_UPLOAD,   // Local changes waiting to be uploaded
    PENDING_DOWNLOAD, // Server changes waiting to be downloaded
    CONFLICT,         // Sync conflict detected
    FAILED,           // Sync failed, needs retry
    OFFLINE_ONLY      // Local-only data, not synced
}

/**
 * Sync priority levels for queue management
 */
enum class SyncPriority(val value: Int) {
    CRITICAL(1),    // Transfers, payments, ownership changes
    HIGH(2),        // User fowls, health records
    MEDIUM(3),      // Marketplace listings, breeding records
    LOW(4)          // General browsing data, cached content
}

/**
 * Base sync metadata embedded in all entities
 */
@Embeddable
data class SyncMetadata(
    @ColumnInfo(name = "last_sync_time")
    val lastSyncTime: Date? = null,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD,
    
    @ColumnInfo(name = "conflict_version")
    val conflictVersion: Long = 1L,
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
    
    @ColumnInfo(name = "sync_priority")
    val syncPriority: SyncPriority = SyncPriority.MEDIUM,
    
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,
    
    @ColumnInfo(name = "last_error")
    val lastError: String? = null,
    
    @ColumnInfo(name = "compressed_data")
    val compressedData: Boolean = false,
    
    @ColumnInfo(name = "data_size")
    val dataSize: Long = 0L
)

/**
 * Regional metadata for efficient offline queries
 */
@Embeddable
data class RegionalMetadata(
    @ColumnInfo(name = "region")
    val region: String,
    
    @ColumnInfo(name = "district")
    val district: String,
    
    @ColumnInfo(name = "mandal")
    val mandal: String? = null,
    
    @ColumnInfo(name = "village")
    val village: String? = null,
    
    @ColumnInfo(name = "latitude")
    val latitude: Double? = null,
    
    @ColumnInfo(name = "longitude")
    val longitude: Double? = null
)

/**
 * Conflict resolution metadata
 */
@Embeddable
data class ConflictMetadata(
    @ColumnInfo(name = "has_conflict")
    val hasConflict: Boolean = false,
    
    @ColumnInfo(name = "conflict_detected_at")
    val conflictDetectedAt: Date? = null,
    
    @ColumnInfo(name = "server_version")
    val serverVersion: Long? = null,
    
    @ColumnInfo(name = "local_version")
    val localVersion: Long? = null,
    
    @ColumnInfo(name = "conflict_resolution_strategy")
    val conflictResolutionStrategy: ConflictResolutionStrategy? = null,
    
    @ColumnInfo(name = "conflict_resolved_at")
    val conflictResolvedAt: Date? = null
)

/**
 * Conflict resolution strategies
 */
enum class ConflictResolutionStrategy {
    LAST_WRITER_WINS,     // Use most recent timestamp
    SERVER_AUTHORITATIVE, // Always use server version
    CLIENT_AUTHORITATIVE, // Always use client version
    MERGE_STRATEGY,       // Merge non-conflicting fields
    USER_RESOLUTION       // Require user intervention
}

/**
 * Type converters for Room database with proper JSON serialization
 */
class SyncTypeConverters {

    private val gson = com.google.gson.Gson()
    private val stringListType = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
    private val stringMapType = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
    private val stringBooleanMapType = object : com.google.gson.reflect.TypeToken<Map<String, Boolean>>() {}.type
    private val stringListMapType = object : com.google.gson.reflect.TypeToken<Map<String, List<String>>>() {}.type

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return status.name
    }

    @TypeConverter
    fun toSyncStatus(status: String): SyncStatus {
        return try {
            SyncStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            SyncStatus.PENDING_UPLOAD
        }
    }

    @TypeConverter
    fun fromSyncPriority(priority: SyncPriority): Int {
        return priority.value
    }

    @TypeConverter
    fun toSyncPriority(value: Int): SyncPriority {
        return SyncPriority.values().find { it.value == value } ?: SyncPriority.MEDIUM
    }

    @TypeConverter
    fun fromConflictResolutionStrategy(strategy: ConflictResolutionStrategy?): String? {
        return strategy?.name
    }

    @TypeConverter
    fun toConflictResolutionStrategy(strategy: String?): ConflictResolutionStrategy? {
        return strategy?.let {
            try {
                ConflictResolutionStrategy.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return try {
            gson.toJson(value)
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            if (value.isEmpty()) return emptyList()
            gson.fromJson(value, stringListType) ?: emptyList()
        } catch (e: Exception) {
            // Fallback for old comma-separated format
            if (value.contains(",") && !value.startsWith("[")) {
                value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                emptyList()
            }
        }
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String {
        return try {
            gson.toJson(value)
        } catch (e: Exception) {
            "{}"
        }
    }

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        return try {
            if (value.isEmpty()) return emptyMap()
            gson.fromJson(value, stringMapType) ?: emptyMap()
        } catch (e: Exception) {
            // Fallback for old semicolon-separated format
            if (value.contains(";") && !value.startsWith("{")) {
                value.split(";").associate {
                    val parts = it.split(":")
                    parts[0] to (parts.getOrNull(1) ?: "")
                }
            } else {
                emptyMap()
            }
        }
    }

    @TypeConverter
    fun fromStringBooleanMap(value: Map<String, Boolean>): String {
        return try {
            gson.toJson(value)
        } catch (e: Exception) {
            "{}"
        }
    }

    @TypeConverter
    fun toStringBooleanMap(value: String): Map<String, Boolean> {
        return try {
            if (value.isEmpty()) return emptyMap()
            gson.fromJson(value, stringBooleanMapType) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @TypeConverter
    fun fromStringListMap(value: Map<String, List<String>>): String {
        return try {
            gson.toJson(value)
        } catch (e: Exception) {
            "{}"
        }
    }

    @TypeConverter
    fun toStringListMap(value: String): Map<String, List<String>> {
        return try {
            if (value.isEmpty()) return emptyMap()
            gson.fromJson(value, stringListMapType) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

/**
 * Base DAO interface for syncable entities
 */
interface BaseSyncableDao<T : SyncableEntity> {
    
    suspend fun insert(entity: T): Long
    suspend fun insertAll(entities: List<T>): List<Long>
    suspend fun update(entity: T): Int
    suspend fun delete(entity: T): Int
    suspend fun deleteById(id: String): Int
    
    // Sync-specific queries
    suspend fun getById(id: String): T?
    suspend fun getAllPendingSync(): List<T>
    suspend fun getAllByStatus(status: SyncStatus): List<T>
    suspend fun getAllByPriority(priority: SyncPriority): List<T>
    suspend fun getAllConflicted(): List<T>
    suspend fun getAllInRegion(region: String, district: String): List<T>
    
    // Sync metadata updates
    suspend fun updateSyncStatus(id: String, status: SyncStatus, lastSyncTime: Date = Date())
    suspend fun updateConflictVersion(id: String, version: Long)
    suspend fun markAsDeleted(id: String)
    suspend fun incrementRetryCount(id: String)
    suspend fun clearRetryCount(id: String)
    
    // Cleanup operations
    suspend fun deleteOldSyncedItems(olderThan: Date): Int
    suspend fun deleteLowPriorityItems(limit: Int): Int
    suspend fun getStorageSize(): Long
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
    val errors: List<SyncError>
)

/**
 * Sync error details
 */
data class SyncError(
    val entityId: String,
    val errorType: SyncErrorType,
    val message: String,
    val timestamp: Date = Date(),
    val retryable: Boolean = true
)

/**
 * Sync error types
 */
enum class SyncErrorType {
    NETWORK_ERROR,
    AUTHENTICATION_ERROR,
    PERMISSION_ERROR,
    DATA_VALIDATION_ERROR,
    CONFLICT_ERROR,
    STORAGE_ERROR,
    UNKNOWN_ERROR
}

/**
 * Sync configuration for different entity types
 */
data class SyncConfiguration(
    val entityType: String,
    val syncPriority: SyncPriority,
    val batchSize: Int,
    val maxRetries: Int,
    val retryDelayMs: Long,
    val conflictResolutionStrategy: ConflictResolutionStrategy,
    val compressionEnabled: Boolean,
    val encryptionEnabled: Boolean,
    val ttlHours: Int? = null
) {
    companion object {
        fun getDefaultConfig(entityType: String): SyncConfiguration {
            return when (entityType) {
                "transfers", "payments" -> SyncConfiguration(
                    entityType = entityType,
                    syncPriority = SyncPriority.CRITICAL,
                    batchSize = 10,
                    maxRetries = 5,
                    retryDelayMs = 1000L,
                    conflictResolutionStrategy = ConflictResolutionStrategy.SERVER_AUTHORITATIVE,
                    compressionEnabled = true,
                    encryptionEnabled = true
                )
                "fowls", "health_records" -> SyncConfiguration(
                    entityType = entityType,
                    syncPriority = SyncPriority.HIGH,
                    batchSize = 25,
                    maxRetries = 3,
                    retryDelayMs = 2000L,
                    conflictResolutionStrategy = ConflictResolutionStrategy.LAST_WRITER_WINS,
                    compressionEnabled = true,
                    encryptionEnabled = false
                )
                "marketplace_listings", "breeding_records" -> SyncConfiguration(
                    entityType = entityType,
                    syncPriority = SyncPriority.MEDIUM,
                    batchSize = 50,
                    maxRetries = 2,
                    retryDelayMs = 5000L,
                    conflictResolutionStrategy = ConflictResolutionStrategy.LAST_WRITER_WINS,
                    compressionEnabled = true,
                    encryptionEnabled = false,
                    ttlHours = 24
                )
                else -> SyncConfiguration(
                    entityType = entityType,
                    syncPriority = SyncPriority.LOW,
                    batchSize = 100,
                    maxRetries = 1,
                    retryDelayMs = 10000L,
                    conflictResolutionStrategy = ConflictResolutionStrategy.SERVER_AUTHORITATIVE,
                    compressionEnabled = true,
                    encryptionEnabled = false,
                    ttlHours = 6
                )
            }
        }
    }
}

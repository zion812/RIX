package com.rio.rostry.core.database.entities

import androidx.room.*
import java.util.*

/**
 * Missing entities that were referenced but not implemented
 */

/**
 * Breeding record entity for tracking fowl breeding activities
 */
@Entity(
    tableName = "breeding_records",
    indices = [
        Index(value = ["sire_id"]),
        Index(value = ["dam_id"]),
        Index(value = ["breeder_id"]),
        Index(value = ["breeding_date"]),
        Index(value = ["sync_status"]),
        Index(value = ["is_deleted"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = FowlEntity::class,
            parentColumns = ["id"],
            childColumns = ["sire_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FowlEntity::class,
            parentColumns = ["id"],
            childColumns = ["dam_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["breeder_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BreedingRecordEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    override val id: String,
    
    @ColumnInfo(name = "sire_id")
    val sireId: String,
    
    @ColumnInfo(name = "dam_id")
    val damId: String,
    
    @ColumnInfo(name = "breeder_id")
    val breederId: String,
    
    @ColumnInfo(name = "breeding_date")
    val breedingDate: Date,
    
    @ColumnInfo(name = "expected_hatch_date")
    val expectedHatchDate: Date? = null,
    
    @ColumnInfo(name = "actual_hatch_date")
    val actualHatchDate: Date? = null,
    
    @ColumnInfo(name = "eggs_laid")
    val eggsLaid: Int = 0,
    
    @ColumnInfo(name = "eggs_fertile")
    val eggsFertile: Int = 0,
    
    @ColumnInfo(name = "chicks_hatched")
    val chicksHatched: Int = 0,
    
    @ColumnInfo(name = "chicks_survived")
    val chicksSurvived: Int = 0,
    
    @ColumnInfo(name = "breeding_method")
    val breedingMethod: String, // NATURAL, ARTIFICIAL_INSEMINATION
    
    @ColumnInfo(name = "breeding_purpose")
    val breedingPurpose: String, // IMPROVEMENT, COMMERCIAL, EXHIBITION
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "offspring_ids")
    val offspringIds: List<String> = emptyList(),
    
    @Embedded
    val regionalMetadata: RegionalMetadata,
    
    @Embedded
    val syncMetadata: SyncMetadata,
    
    @Embedded
    val conflictMetadata: ConflictMetadata = ConflictMetadata()
) : SyncableEntity {
    
    override val lastSyncTime: Date?
        get() = syncMetadata.lastSyncTime
    
    override val syncStatus: SyncStatus
        get() = syncMetadata.syncStatus
    
    override val conflictVersion: Long
        get() = syncMetadata.conflictVersion
    
    override val isDeleted: Boolean
        get() = syncMetadata.isDeleted
    
    override val createdAt: Date
        get() = syncMetadata.createdAt
    
    override val updatedAt: Date
        get() = syncMetadata.updatedAt
}

/**
 * Sync queue entity for managing sync operations
 */
@Entity(
    tableName = "sync_queue",
    indices = [
        Index(value = ["entity_type"]),
        Index(value = ["sync_priority"]),
        Index(value = ["sync_status"]),
        Index(value = ["scheduled_at"]),
        Index(value = ["retry_count"])
    ]
)
data class SyncQueueEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "entity_type")
    val entityType: String,
    
    @ColumnInfo(name = "entity_id")
    val entityId: String,
    
    @ColumnInfo(name = "operation_type")
    val operationType: String, // CREATE, UPDATE, DELETE
    
    @ColumnInfo(name = "sync_priority")
    val syncPriority: Int,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String, // QUEUED, PROCESSING, COMPLETED, FAILED
    
    @ColumnInfo(name = "scheduled_at")
    val scheduledAt: Date,
    
    @ColumnInfo(name = "last_attempt_at")
    val lastAttemptAt: Date? = null,
    
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,
    
    @ColumnInfo(name = "max_retries")
    val maxRetries: Int = 3,
    
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,
    
    @ColumnInfo(name = "payload")
    val payload: String? = null
)

/**
 * Offline action entity for queuing user actions
 */
@Entity(
    tableName = "offline_actions",
    indices = [
        Index(value = ["entity_type"]),
        Index(value = ["action_type"]),
        Index(value = ["priority"]),
        Index(value = ["status"]),
        Index(value = ["queued_at"]),
        Index(value = ["depends_on"])
    ]
)
data class OfflineActionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "entity_type")
    val entityType: String,
    
    @ColumnInfo(name = "entity_id")
    val entityId: String,
    
    @ColumnInfo(name = "action_type")
    val actionType: String,
    
    @ColumnInfo(name = "action_data")
    val actionData: String,
    
    @ColumnInfo(name = "priority")
    val priority: Int,
    
    @ColumnInfo(name = "depends_on")
    val dependsOn: List<String> = emptyList(),
    
    @ColumnInfo(name = "validation_rules")
    val validationRules: List<String> = emptyList(),
    
    @ColumnInfo(name = "status")
    val status: String,
    
    @ColumnInfo(name = "queued_at")
    val queuedAt: Date,
    
    @ColumnInfo(name = "processed_at")
    val processedAt: Date? = null,
    
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,
    
    @ColumnInfo(name = "max_retries")
    val maxRetries: Int = 3,
    
    @ColumnInfo(name = "next_retry_at")
    val nextRetryAt: Date? = null,
    
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,
    
    @Embedded
    val syncMetadata: SyncMetadata
)

/**
 * Media entity for tracking uploaded files
 */
@Entity(
    tableName = "media",
    indices = [
        Index(value = ["entity_type", "entity_id"]),
        Index(value = ["media_type"]),
        Index(value = ["upload_status"]),
        Index(value = ["created_at"]),
        Index(value = ["is_deleted"])
    ]
)
data class MediaEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "entity_type")
    val entityType: String, // fowl, user, listing, message
    
    @ColumnInfo(name = "entity_id")
    val entityId: String,
    
    @ColumnInfo(name = "media_type")
    val mediaType: String, // IMAGE, VIDEO, AUDIO, DOCUMENT
    
    @ColumnInfo(name = "file_name")
    val fileName: String,
    
    @ColumnInfo(name = "file_size")
    val fileSize: Long,
    
    @ColumnInfo(name = "mime_type")
    val mimeType: String,
    
    @ColumnInfo(name = "local_path")
    val localPath: String,
    
    @ColumnInfo(name = "remote_url")
    val remoteUrl: String? = null,
    
    @ColumnInfo(name = "thumbnail_path")
    val thumbnailPath: String? = null,
    
    @ColumnInfo(name = "upload_status")
    val uploadStatus: String, // PENDING, UPLOADING, COMPLETED, FAILED
    
    @ColumnInfo(name = "upload_progress")
    val uploadProgress: Int = 0,
    
    @ColumnInfo(name = "compression_applied")
    val compressionApplied: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "uploaded_at")
    val uploadedAt: Date? = null,
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)

/**
 * Notification entity for offline notification management
 */
@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["notification_type"]),
        Index(value = ["is_read"]),
        Index(value = ["created_at"]),
        Index(value = ["is_deleted"])
    ]
)
data class NotificationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "notification_type")
    val notificationType: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "message")
    val message: String,
    
    @ColumnInfo(name = "data")
    val data: String? = null, // JSON data
    
    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,
    
    @ColumnInfo(name = "read_at")
    val readAt: Date? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "expires_at")
    val expiresAt: Date? = null,
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)

/**
 * DAOs for missing entities
 */
@Dao
interface BreedingRecordDao : BaseSyncableDao<BreedingRecordEntity> {
    
    @Query("SELECT * FROM breeding_records WHERE id = :id AND is_deleted = 0")
    override suspend fun getById(id: String): BreedingRecordEntity?
    
    @Query("SELECT * FROM breeding_records WHERE sync_status = :status AND is_deleted = 0")
    override suspend fun getAllByStatus(status: SyncStatus): List<BreedingRecordEntity>
    
    @Query("SELECT * FROM breeding_records WHERE sync_priority = :priority AND is_deleted = 0")
    override suspend fun getAllByPriority(priority: SyncPriority): List<BreedingRecordEntity>
    
    @Query("SELECT * FROM breeding_records WHERE sync_status IN ('PENDING_UPLOAD', 'FAILED') AND is_deleted = 0 ORDER BY sync_priority ASC")
    override suspend fun getAllPendingSync(): List<BreedingRecordEntity>
    
    @Query("SELECT * FROM breeding_records WHERE has_conflict = 1 AND is_deleted = 0")
    override suspend fun getAllConflicted(): List<BreedingRecordEntity>
    
    @Query("SELECT * FROM breeding_records WHERE region = :region AND district = :district AND is_deleted = 0")
    override suspend fun getAllInRegion(region: String, district: String): List<BreedingRecordEntity>
    
    @Query("SELECT * FROM breeding_records WHERE breeder_id = :breederId AND is_deleted = 0 ORDER BY breeding_date DESC")
    suspend fun getRecordsByBreeder(breederId: String): List<BreedingRecordEntity>
    
    @Query("SELECT * FROM breeding_records WHERE sire_id = :fowlId OR dam_id = :fowlId AND is_deleted = 0")
    suspend fun getRecordsByParent(fowlId: String): List<BreedingRecordEntity>
    
    // Implement required abstract methods
    @Query("UPDATE breeding_records SET sync_status = :status, last_sync_time = :lastSyncTime WHERE id = :id")
    override suspend fun updateSyncStatus(id: String, status: SyncStatus, lastSyncTime: Date)
    
    @Query("UPDATE breeding_records SET conflict_version = :version WHERE id = :id")
    override suspend fun updateConflictVersion(id: String, version: Long)
    
    @Query("UPDATE breeding_records SET is_deleted = 1, updated_at = :deletedAt WHERE id = :id")
    override suspend fun markAsDeleted(id: String)
    
    @Query("UPDATE breeding_records SET retry_count = retry_count + 1 WHERE id = :id")
    override suspend fun incrementRetryCount(id: String)
    
    @Query("UPDATE breeding_records SET retry_count = 0 WHERE id = :id")
    override suspend fun clearRetryCount(id: String)
    
    @Query("DELETE FROM breeding_records WHERE sync_status = 'SYNCED' AND updated_at < :olderThan AND sync_priority = 'LOW'")
    override suspend fun deleteOldSyncedItems(olderThan: Date): Int
    
    @Query("DELETE FROM breeding_records WHERE sync_priority = 'LOW' AND sync_status = 'SYNCED' ORDER BY last_sync_time ASC LIMIT :limit")
    override suspend fun deleteLowPriorityItems(limit: Int): Int
    
    @Query("SELECT SUM(data_size) FROM breeding_records")
    override suspend fun getStorageSize(): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(entity: BreedingRecordEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertAll(entities: List<BreedingRecordEntity>): List<Long>
    
    @Update
    override suspend fun update(entity: BreedingRecordEntity): Int
    
    @Delete
    override suspend fun delete(entity: BreedingRecordEntity): Int
    
    @Query("DELETE FROM breeding_records WHERE id = :id")
    override suspend fun deleteById(id: String): Int
}

@Dao
interface SyncQueueDao {
    
    @Query("SELECT * FROM sync_queue WHERE sync_status = 'QUEUED' ORDER BY sync_priority ASC, scheduled_at ASC")
    suspend fun getAllQueued(): List<SyncQueueEntity>
    
    @Query("SELECT * FROM sync_queue WHERE sync_status = 'PROCESSING'")
    suspend fun getAllProcessing(): List<SyncQueueEntity>
    
    @Query("SELECT COUNT(*) FROM sync_queue WHERE sync_status = 'QUEUED'")
    suspend fun getQueuedCount(): Int
    
    @Query("SELECT COUNT(*) FROM sync_queue WHERE sync_status = 'PROCESSING'")
    suspend fun getProcessingCount(): Int
    
    @Query("SELECT COUNT(*) FROM sync_queue WHERE sync_status = 'FAILED'")
    suspend fun getFailedCount(): Int
    
    @Query("SELECT COUNT(*) FROM sync_queue WHERE sync_status = 'COMPLETED'")
    suspend fun getCompletedCount(): Int
    
    @Query("SELECT * FROM sync_queue WHERE sync_status = 'QUEUED' ORDER BY scheduled_at ASC LIMIT 1")
    suspend fun getOldestQueuedAction(): SyncQueueEntity?
    
    @Query("SELECT AVG(last_attempt_at - scheduled_at) FROM sync_queue WHERE sync_status = 'COMPLETED' AND last_attempt_at IS NOT NULL")
    suspend fun getAverageProcessingTime(): Long?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SyncQueueEntity): Long
    
    @Update
    suspend fun update(entity: SyncQueueEntity): Int
    
    @Query("UPDATE sync_queue SET sync_status = :status, last_attempt_at = :attemptTime WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, attemptTime: Date)
    
    @Query("UPDATE sync_queue SET retry_count = :retryCount, next_retry_at = :nextRetryAt WHERE id = :id")
    suspend fun updateRetryCount(id: String, retryCount: Int, nextRetryAt: Date)
    
    @Query("DELETE FROM sync_queue WHERE sync_status = 'COMPLETED' AND last_attempt_at < :olderThan")
    suspend fun deleteCompletedOlderThan(olderThan: Date): Int
    
    @Query("UPDATE sync_queue SET sync_status = 'QUEUED' WHERE sync_status = 'FAILED' AND retry_count < max_retries")
    suspend fun resetFailedToQueued(): Int
}

@Dao
interface OfflineActionDao {
    
    @Query("SELECT * FROM offline_actions WHERE status = 'QUEUED' ORDER BY priority ASC, queued_at ASC")
    suspend fun getAllQueued(): List<OfflineActionEntity>
    
    @Query("SELECT COUNT(*) FROM offline_actions WHERE status = 'QUEUED'")
    suspend fun getQueuedCount(): Int
    
    @Query("SELECT COUNT(*) FROM offline_actions WHERE status = 'PROCESSING'")
    suspend fun getProcessingCount(): Int
    
    @Query("SELECT COUNT(*) FROM offline_actions WHERE status = 'FAILED'")
    suspend fun getFailedCount(): Int
    
    @Query("SELECT COUNT(*) FROM offline_actions WHERE status = 'COMPLETED'")
    suspend fun getCompletedCount(): Int
    
    @Query("SELECT * FROM offline_actions WHERE status = 'QUEUED' ORDER BY queued_at ASC LIMIT 1")
    suspend fun getOldestQueuedAction(): OfflineActionEntity?
    
    @Query("SELECT AVG(processed_at - queued_at) FROM offline_actions WHERE status = 'COMPLETED' AND processed_at IS NOT NULL")
    suspend fun getAverageProcessingTime(): Long?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OfflineActionEntity): Long
    
    @Update
    suspend fun update(entity: OfflineActionEntity): Int
    
    @Query("UPDATE offline_actions SET status = :status, processed_at = :processedAt, error_message = :errorMessage WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, processedAt: Date? = null, errorMessage: String? = null)
    
    @Query("UPDATE offline_actions SET retry_count = :retryCount, next_retry_at = :nextRetryAt WHERE id = :id")
    suspend fun updateRetryCount(id: String, retryCount: Int, nextRetryAt: Date)
    
    @Query("DELETE FROM offline_actions WHERE status = 'COMPLETED' AND processed_at < :olderThan")
    suspend fun deleteCompletedOlderThan(olderThan: Date): Int
    
    @Query("UPDATE offline_actions SET status = 'QUEUED' WHERE status = 'FAILED' AND retry_count < max_retries")
    suspend fun resetFailedToQueued(): Int
}

@Dao
interface MediaDao {
    
    @Query("SELECT * FROM media WHERE id = :id AND is_deleted = 0")
    suspend fun getById(id: String): MediaEntity?
    
    @Query("SELECT * FROM media WHERE entity_type = :entityType AND entity_id = :entityId AND is_deleted = 0")
    suspend fun getByEntity(entityType: String, entityId: String): List<MediaEntity>
    
    @Query("SELECT * FROM media WHERE upload_status = 'PENDING' AND is_deleted = 0")
    suspend fun getPendingUploads(): List<MediaEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MediaEntity): Long
    
    @Update
    suspend fun update(entity: MediaEntity): Int
    
    @Query("UPDATE media SET upload_status = :status, upload_progress = :progress WHERE id = :id")
    suspend fun updateUploadStatus(id: String, status: String, progress: Int = 100)
    
    @Query("UPDATE media SET remote_url = :remoteUrl, uploaded_at = :uploadedAt WHERE id = :id")
    suspend fun updateRemoteUrl(id: String, remoteUrl: String, uploadedAt: Date)
    
    @Query("DELETE FROM media WHERE id = :id")
    suspend fun deleteById(id: String): Int
}

@Dao
interface NotificationDao {
    
    @Query("SELECT * FROM notifications WHERE user_id = :userId AND is_deleted = 0 ORDER BY created_at DESC")
    suspend fun getByUser(userId: String): List<NotificationEntity>
    
    @Query("SELECT * FROM notifications WHERE user_id = :userId AND is_read = 0 AND is_deleted = 0")
    suspend fun getUnreadByUser(userId: String): List<NotificationEntity>
    
    @Query("SELECT COUNT(*) FROM notifications WHERE user_id = :userId AND is_read = 0 AND is_deleted = 0")
    suspend fun getUnreadCount(userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotificationEntity): Long
    
    @Query("UPDATE notifications SET is_read = 1, read_at = :readAt WHERE id = :id")
    suspend fun markAsRead(id: String, readAt: Date = Date())
    
    @Query("UPDATE notifications SET is_read = 1, read_at = :readAt WHERE user_id = :userId AND is_read = 0")
    suspend fun markAllAsRead(userId: String, readAt: Date = Date())
    
    @Query("DELETE FROM notifications WHERE expires_at < :currentTime AND is_deleted = 0")
    suspend fun deleteExpired(currentTime: Date = Date()): Int
}

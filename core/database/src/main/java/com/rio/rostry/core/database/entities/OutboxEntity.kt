package com.rio.rostry.core.database.entities

import androidx.room.*
import java.util.*

/**
 * Room entity for outbox operations to support offline-first sync with Firestore
 * This entity tracks pending sync operations for all entity types
 */
@Entity(
    tableName = "outbox",
    indices = [
        Index(value = ["entity_type"]),
        Index(value = ["operation_type"]),
        Index(value = ["created_at"]),
        Index(value = ["sync_status"]),
        Index(value = ["priority"])
    ]
)
data class OutboxEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "entity_type")
    val entityType: String, // FOWL, TRANSFER_LOG, MARKETPLACE, etc.
    
    @ColumnInfo(name = "entity_id")
    val entityId: String,
    
    @ColumnInfo(name = "operation_type")
    val operationType: String, // CREATE, UPDATE, DELETE
    
    @ColumnInfo(name = "entity_data")
    val entityData: String?, // JSON representation of the entity
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String, // PENDING, IN_PROGRESS, SUCCESS, FAILED
    
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,
    
    @ColumnInfo(name = "priority")
    val priority: Int = 1, // 1=low, 5=high
    
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,
    
    @ColumnInfo(name = "last_attempt_at")
    val lastAttemptAt: Date? = null,
    
    @ColumnInfo(name = "synced_at")
    val syncedAt: Date? = null
)

/**
 * DAO for outbox operations
 */
@Dao
interface OutboxDaoV2 {
    
    @Query("SELECT * FROM outbox WHERE id = :id")
    suspend fun getById(id: String): OutboxEntity?
    
    @Query("SELECT * FROM outbox WHERE sync_status = 'PENDING' ORDER BY priority DESC, created_at ASC LIMIT :limit")
    suspend fun getPendingOperations(limit: Int = 50): List<OutboxEntity>
    
    @Query("SELECT * FROM outbox WHERE sync_status = 'FAILED' AND retry_count < 3 ORDER BY priority DESC, created_at ASC LIMIT :limit")
    suspend fun getFailedRetriableOperations(limit: Int = 50): List<OutboxEntity>
    
    @Query("SELECT COUNT(*) FROM outbox WHERE sync_status = 'PENDING'")
    suspend fun getPendingCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(outboxEntity: OutboxEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(outboxEntities: List<OutboxEntity>): List<Long>
    
    @Update
    suspend fun update(outboxEntity: OutboxEntity): Int
    
    @Query("UPDATE outbox SET sync_status = :status, synced_at = :syncedAt, retry_count = retry_count + 1, last_attempt_at = :attemptAt, error_message = :errorMessage WHERE id = :id")
    suspend fun updateSyncStatus(
        id: String,
        status: String,
        syncedAt: Date?,
        attemptAt: Date,
        errorMessage: String?
    ): Int
    
    @Delete
    suspend fun delete(outboxEntity: OutboxEntity): Int
    
    @Query("DELETE FROM outbox WHERE id = :id")
    suspend fun deleteById(id: String): Int
}

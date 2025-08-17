package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.OutboxEntity

/**
 * Data Access Object for outbox operations
 */
@Dao
interface OutboxDao {
    
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
        syncedAt: java.util.Date?,
        attemptAt: java.util.Date,
        errorMessage: String?
    ): Int
    
    @Delete
    suspend fun delete(outboxEntity: OutboxEntity): Int
    
    @Query("DELETE FROM outbox WHERE id = :id")
    suspend fun deleteById(id: String): Int
}
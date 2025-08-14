package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.SyncQueueEntity
import com.rio.rostry.core.database.entities.SyncPriority
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Data Access Object for sync queue operations
 */
@Dao
interface SyncDao {
    
    @Query("SELECT * FROM sync_queue WHERE id = :queueId")
    suspend fun getSyncQueueItemById(queueId: String): SyncQueueEntity?
    
    @Query("SELECT * FROM sync_queue ORDER BY priority DESC, createdAt ASC")
    suspend fun getAllSyncQueueItems(): List<SyncQueueEntity>
    
    @Query("SELECT * FROM sync_queue WHERE priority = :priority ORDER BY createdAt ASC")
    suspend fun getSyncQueueItemsByPriority(priority: SyncPriority): List<SyncQueueEntity>
    
    @Query("SELECT * FROM sync_queue WHERE entityType = :entityType ORDER BY priority DESC, createdAt ASC")
    suspend fun getSyncQueueItemsByEntityType(entityType: String): List<SyncQueueEntity>
    
    @Query("SELECT * FROM sync_queue WHERE entityId = :entityId ORDER BY createdAt DESC")
    suspend fun getSyncQueueItemsByEntityId(entityId: String): List<SyncQueueEntity>
    
    @Query("SELECT * FROM sync_queue WHERE retryCount < maxRetries AND (nextAttempt IS NULL OR nextAttempt <= :currentTime) ORDER BY priority DESC, createdAt ASC LIMIT :limit")
    suspend fun getReadyForSyncItems(currentTime: Date = Date(), limit: Int = 50): List<SyncQueueEntity>
    
    @Query("SELECT * FROM sync_queue WHERE retryCount >= maxRetries ORDER BY createdAt ASC")
    suspend fun getFailedSyncItems(): List<SyncQueueEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncQueueItem(item: SyncQueueEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncQueueItems(items: List<SyncQueueEntity>)
    
    @Update
    suspend fun updateSyncQueueItem(item: SyncQueueEntity)
    
    @Query("UPDATE sync_queue SET retryCount = retryCount + 1, lastAttempt = :lastAttempt, nextAttempt = :nextAttempt, errorMessage = :errorMessage, updatedAt = :updatedAt WHERE id = :queueId")
    suspend fun incrementRetryCount(
        queueId: String, 
        lastAttempt: Date = Date(), 
        nextAttempt: Date, 
        errorMessage: String?,
        updatedAt: Date = Date()
    )
    
    @Query("UPDATE sync_queue SET nextAttempt = :nextAttempt, updatedAt = :updatedAt WHERE id = :queueId")
    suspend fun scheduleNextAttempt(queueId: String, nextAttempt: Date, updatedAt: Date = Date())
    
    @Delete
    suspend fun deleteSyncQueueItem(item: SyncQueueEntity)
    
    @Query("DELETE FROM sync_queue WHERE id = :queueId")
    suspend fun deleteSyncQueueItemById(queueId: String)
    
    @Query("DELETE FROM sync_queue WHERE entityId = :entityId")
    suspend fun deleteSyncQueueItemsByEntityId(entityId: String)
    
    @Query("DELETE FROM sync_queue WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun deleteSyncQueueItemsByEntity(entityType: String, entityId: String)
    
    @Query("DELETE FROM sync_queue WHERE retryCount >= maxRetries AND createdAt < :cutoffDate")
    suspend fun deleteOldFailedItems(cutoffDate: Date)
    
    // Analytics and monitoring
    @Query("SELECT COUNT(*) FROM sync_queue")
    suspend fun getTotalSyncQueueCount(): Int
    
    @Query("SELECT COUNT(*) FROM sync_queue WHERE priority = :priority")
    suspend fun getSyncQueueCountByPriority(priority: SyncPriority): Int
    
    @Query("SELECT COUNT(*) FROM sync_queue WHERE retryCount >= maxRetries")
    suspend fun getFailedSyncCount(): Int
    
    @Query("SELECT COUNT(*) FROM sync_queue WHERE retryCount < maxRetries")
    suspend fun getPendingSyncCount(): Int
    
    @Query("SELECT entityType, COUNT(*) as count FROM sync_queue GROUP BY entityType ORDER BY count DESC")
    suspend fun getSyncQueueDistributionByEntityType(): List<EntityTypeCount>
    
    @Query("SELECT operation, COUNT(*) as count FROM sync_queue GROUP BY operation ORDER BY count DESC")
    suspend fun getSyncQueueDistributionByOperation(): List<OperationCount>
    
    @Query("SELECT priority, COUNT(*) as count FROM sync_queue GROUP BY priority ORDER BY count DESC")
    suspend fun getSyncQueueDistributionByPriority(): List<PriorityCount>
    
    // Real-time monitoring
    @Query("SELECT COUNT(*) FROM sync_queue")
    fun observeTotalSyncQueueCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM sync_queue WHERE retryCount >= maxRetries")
    fun observeFailedSyncCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM sync_queue WHERE priority = 'HIGH'")
    fun observeHighPrioritySyncCount(): Flow<Int>
    
    // Batch operations
    @Query("SELECT * FROM sync_queue WHERE entityType = :entityType AND operation = :operation ORDER BY createdAt ASC LIMIT :batchSize")
    suspend fun getBatchForSync(entityType: String, operation: String, batchSize: Int = 10): List<SyncQueueEntity>
    
    @Query("DELETE FROM sync_queue WHERE id IN (:queueIds)")
    suspend fun deleteSyncQueueItemsByIds(queueIds: List<String>)
    
    @Query("UPDATE sync_queue SET retryCount = retryCount + 1, lastAttempt = :lastAttempt, nextAttempt = :nextAttempt, errorMessage = :errorMessage, updatedAt = :updatedAt WHERE id IN (:queueIds)")
    suspend fun batchIncrementRetryCount(
        queueIds: List<String>,
        lastAttempt: Date = Date(),
        nextAttempt: Date,
        errorMessage: String?,
        updatedAt: Date = Date()
    )
    
    // Cleanup operations
    @Query("DELETE FROM sync_queue WHERE createdAt < :cutoffDate")
    suspend fun deleteOldSyncQueueItems(cutoffDate: Date)
    
    @Query("SELECT * FROM sync_queue WHERE createdAt < :cutoffDate ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getOldSyncQueueItems(cutoffDate: Date, limit: Int = 100): List<SyncQueueEntity>
}

data class EntityTypeCount(
    val entityType: String,
    val count: Int
)

data class OperationCount(
    val operation: String,
    val count: Int
)

data class PriorityCount(
    val priority: String,
    val count: Int
)
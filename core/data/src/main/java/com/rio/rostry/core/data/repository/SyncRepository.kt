package com.rio.rostry.core.data.repository

import com.rio.rostry.core.database.entities.OutboxDaoV2
import com.rio.rostry.core.database.entities.OutboxEntity
import com.rio.rostry.core.common.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling synchronization operations with outbox pattern
 */
@Singleton
class SyncRepository @Inject constructor(
    private val outboxDao: OutboxDaoV2
) {
    /**
     * Sync pending operations from the outbox
     *
     * @param limit Maximum number of operations to sync
     * @return SyncResult containing counts of synced and failed operations
     */
    suspend fun syncPendingOperations(limit: Int = 50): Result<SyncResult> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Get pending operations
            val pendingOperations = outboxDao.getPendingOperations(limit)
            val failedOperations = outboxDao.getFailedRetriableOperations(limit)
            
            val allOperations = (pendingOperations + failedOperations).distinctBy { it.id }
                .sortedByDescending { it.priority }
                .take(limit)
            
            var syncedCount = 0
            var failedCount = 0
            
            // Process each operation
            for (operation in allOperations) {
                val result = processOperation(operation)
                when (result) {
                    is SyncOperationResult.Success -> {
                        syncedCount++
                    }
                    is SyncOperationResult.Failure -> {
                        failedCount++
                        // Update operation with failure details
                        outboxDao.updateSyncStatus(
                            id = operation.id,
                            status = "FAILED",
                            syncedAt = null,
                            attemptAt = Date(),
                            errorMessage = result.error.message
                        )
                    }
                }
            }
            
            Result.Success(SyncResult(syncedCount, failedCount))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Process a single outbox operation
     */
    private suspend fun processOperation(operation: OutboxEntity): SyncOperationResult {
        return try {
            // In a real implementation, this would:
            // 1. Deserialize the entity data
            // 2. Perform the appropriate API call based on entity type and operation type
            // 3. Handle the response
            // 4. Update the outbox entry with the result
            
            // For now, we'll simulate a successful sync
            outboxDao.updateSyncStatus(
                id = operation.id,
                status = "SUCCESS",
                syncedAt = Date(),
                attemptAt = Date(),
                errorMessage = null
            )
            
            SyncOperationResult.Success
        } catch (e: Exception) {
            SyncOperationResult.Failure(e)
        }
    }
    
    /**
     * Add an operation to the outbox
     */
    suspend fun addToOutbox(operation: OutboxEntity): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val id = outboxDao.insert(operation)
            Result.Success(operation.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Get the count of pending operations
     */
    suspend fun getPendingOperationCount(): Result<Int> = withContext(Dispatchers.IO) {
        return@withContext try {
            val count = outboxDao.getPendingCount()
            Result.Success(count)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

/**
 * Result of a sync operation
 */
data class SyncResult(
    val syncedCount: Int,
    val failedCount: Int
)

/**
 * Result of processing a single sync operation
 */
sealed class SyncOperationResult {
    object Success : SyncOperationResult()
    data class Failure(val error: Exception) : SyncOperationResult()
}
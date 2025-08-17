package com.rio.rostry.core.sync.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.rio.rostry.core.data.repository.SyncRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * WorkManager worker for periodic sync operations
 * This worker handles syncing pending operations from the outbox to Firestore
 */
class PeriodicSyncWorker @Inject constructor(
    @ApplicationContext appContext: Context,
    workerParams: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Perform sync operation
            val result = syncRepository.syncPendingOperations(50) // Sync up to 50 operations
            
            // Return success with output data
            Result.success(
                workDataOf(
                    "synced_count" to result.syncedCount,
                    "failed_count" to result.failedCount
                )
            )
        } catch (e: Exception) {
            // Log error and return retry
            Result.retry()
        }
    }
}
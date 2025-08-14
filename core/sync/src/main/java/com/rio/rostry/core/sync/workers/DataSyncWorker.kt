package com.rio.rostry.core.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.rio.rostry.core.database.RIOLocalDatabase
import com.rio.rostry.core.network.NetworkStateManager
import com.rio.rostry.core.sync.SyncConflictResolver
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.TimeUnit

@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val db: RIOLocalDatabase,
    private val networkManager: NetworkStateManager,
    private val conflictResolver: SyncConflictResolver
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "DataSyncWorker"
        private const val SYNC_WORK_NAME = "rio_data_sync"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        try {
            // Check network quality for sync strategy
            val networkQuality = networkManager.connectionQuality.value
            val isMetered = networkManager.isMetered.value

            // Sync entities based on priority and network conditions
            syncEntities(networkQuality, isMetered)

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun syncEntities(networkQuality: ConnectionQuality, isMetered: Boolean) {
        // Start with high priority items regardless of network
        syncHighPriorityItems()

        // On good network, sync everything
        if (networkQuality == ConnectionQuality.EXCELLENT && !isMetered) {
            syncAllItems()
            return
        }

        // On medium network or metered connection, sync selectively
        if (networkQuality == ConnectionQuality.GOOD || isMetered) {
            syncMediumPriorityItems()
            return
        }

        // On poor network, sync only critical updates
        if (networkQuality == ConnectionQuality.POOR) {
            syncCriticalUpdatesOnly()
        }
    }

    private suspend fun syncHighPriorityItems() {
        // Sync payments, transfers, and critical user data
        syncPaymentTransactions()
        syncOwnershipTransfers()
        syncUserProfiles()
    }

    private suspend fun syncMediumPriorityItems() {
        // Sync listing updates and messages
        syncMarketplaceListings()
        syncMessages()
    }

    private suspend fun syncAllItems() {
        // Full sync including images and non-critical data
        syncHighPriorityItems()
        syncMediumPriorityItems()
        syncMediaItems()
        syncAnalytics()
    }

    private suspend fun syncCriticalUpdatesOnly() {
        // Sync only validation-required and payment-related items
        syncPaymentTransactions()
        syncOwnershipTransfers()
    }

    private suspend fun syncPaymentTransactions() {
        val transactions = db.coinTransactionDao().getPendingSync()
        transactions.forEach { transaction ->
            try {
                // Attempt sync with conflict resolution
                conflictResolver.resolveAndSync(transaction)
            } catch (e: Exception) {
                // Log failure and continue
                if (transaction.retryCount < 3) {
                    db.coinTransactionDao().incrementRetryCount(transaction.id)
                }
            }
        }
    }

    // Similar implementations for other sync methods...
}

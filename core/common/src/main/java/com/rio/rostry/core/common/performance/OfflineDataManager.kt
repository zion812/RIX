package com.rio.rostry.core.common.performance

import android.content.Context
import androidx.room.*
import androidx.work.*
import com.rio.rostry.core.common.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Comprehensive offline data management for rural connectivity
 */
@Singleton
class OfflineDataManager @Inject constructor(
    private val context: Context,
    private val localDatabase: RIOLocalDatabase,
    private val networkAwareManager: NetworkAwareManager,
    private val workManager: WorkManager
) {

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.SYNCED)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _offlineActionsCount = MutableStateFlow(0)
    val offlineActionsCount: StateFlow<Int> = _offlineActionsCount.asStateFlow()

    /**
     * Initialize offline data management
     */
    suspend fun initialize() {
        setupPeriodicSync()
        loadOfflineActionsCount()
        startNetworkObserver()
    }

    /**
     * Cache critical user data for offline access
     */
    suspend fun cacheCriticalData(userId: String) {
        val cacheOperations = listOf(
            { cacheUserProfile(userId) },
            { cacheUserFowls(userId) },
            { cacheRecentConversations(userId) },
            { cacheRegionalMarketplace(userId) },
            { cacheBreedingRecords(userId) }
        )

        cacheOperations.forEach { operation ->
            try {
                operation()
            } catch (e: Exception) {
                // Log error but continue with other operations
            }
        }
    }

    /**
     * Queue action for offline execution
     */
    suspend fun queueOfflineAction(action: OfflineAction) {
        localDatabase.offlineActionDao().insert(action.toEntity())
        updateOfflineActionsCount()
        
        // Try immediate sync if online
        if (networkAwareManager.isConnected.value) {
            scheduleImmediateSync()
        }
    }

    /**
     * Process all queued offline actions
     */
    suspend fun processSyncQueue(): SyncResult {
        _syncStatus.value = SyncStatus.PENDING
        
        val queuedActions = localDatabase.offlineActionDao().getAllPending()
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<String>()

        for (actionEntity in queuedActions) {
            try {
                val action = actionEntity.toOfflineAction()
                executeOfflineAction(action)
                
                // Mark as synced
                localDatabase.offlineActionDao().markAsSynced(actionEntity.id)
                successCount++
                
            } catch (e: Exception) {
                failureCount++
                errors.add("${actionEntity.action}: ${e.message}")
                
                // Update retry count
                val updatedEntity = actionEntity.copy(
                    retryCount = actionEntity.retryCount + 1,
                    lastRetryAt = Date()
                )
                
                if (updatedEntity.retryCount >= actionEntity.maxRetries) {
                    localDatabase.offlineActionDao().markAsFailed(actionEntity.id)
                } else {
                    localDatabase.offlineActionDao().update(updatedEntity)
                }
            }
        }

        updateOfflineActionsCount()
        _syncStatus.value = if (failureCount == 0) SyncStatus.SYNCED else SyncStatus.FAILED

        return SyncResult(
            totalActions = queuedActions.size,
            successCount = successCount,
            failureCount = failureCount,
            errors = errors
        )
    }

    /**
     * Get cached data with fallback to server
     */
    suspend fun <T> getCachedDataWithFallback(
        cacheKey: String,
        serverFetch: suspend () -> T,
        cacheExpiry: Long = 3600000 // 1 hour default
    ): T? {
        // Try cache first
        val cachedData = getCachedData<T>(cacheKey, cacheExpiry)
        if (cachedData != null) {
            return cachedData
        }

        // Fallback to server if online
        return if (networkAwareManager.isConnected.value) {
            try {
                val serverData = serverFetch()
                cacheData(cacheKey, serverData)
                serverData
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    /**
     * Cache data with compression for rural networks
     */
    suspend fun <T> cacheData(key: String, data: T, priority: CachePriority = CachePriority.MEDIUM) {
        val compressedData = if (networkAwareManager.shouldUseCompression()) {
            compressData(data)
        } else {
            data.toString()
        }

        val cacheEntity = CacheEntity(
            key = key,
            data = compressedData,
            priority = priority.name,
            createdAt = Date(),
            expiresAt = Date(System.currentTimeMillis() + getCacheExpiry(priority)),
            compressed = networkAwareManager.shouldUseCompression()
        )

        localDatabase.cacheDao().insert(cacheEntity)
        
        // Clean up old cache if needed
        cleanupCache()
    }

    /**
     * Get cached data
     */
    suspend fun <T> getCachedData(key: String, maxAge: Long = 3600000): T? {
        val cacheEntity = localDatabase.cacheDao().get(key)
        
        return if (cacheEntity != null && !isExpired(cacheEntity, maxAge)) {
            try {
                if (cacheEntity.compressed) {
                    decompressData<T>(cacheEntity.data)
                } else {
                    parseData<T>(cacheEntity.data)
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    /**
     * Smart cache cleanup based on storage and priority
     */
    private suspend fun cleanupCache() {
        val cacheSize = localDatabase.cacheDao().getTotalSize()
        val maxCacheSize = networkAwareManager.getOptimalCacheSize()

        if (cacheSize > maxCacheSize) {
            // Remove expired items first
            localDatabase.cacheDao().deleteExpired()
            
            // If still over limit, remove low priority items
            val remainingSize = localDatabase.cacheDao().getTotalSize()
            if (remainingSize > maxCacheSize) {
                localDatabase.cacheDao().deleteLowPriorityItems(
                    limit = ((remainingSize - maxCacheSize) / 1024).toInt() // Convert to KB
                )
            }
        }
    }

    /**
     * Setup periodic background sync
     */
    private fun setupPeriodicSync() {
        val syncConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<OfflineSyncWorker>(15, java.util.concurrent.TimeUnit.MINUTES)
            .setConstraints(syncConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "offline_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    /**
     * Schedule immediate sync when network becomes available
     */
    private fun scheduleImmediateSync() {
        val syncRequest = OneTimeWorkRequestBuilder<OfflineSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "immediate_sync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    /**
     * Start observing network changes
     */
    private fun startNetworkObserver() {
        // Implementation would observe network changes and trigger sync
    }

    /**
     * Execute offline action
     */
    private suspend fun executeOfflineAction(action: OfflineAction) {
        when (action.type) {
            OfflineActionType.CREATE -> executeCreateAction(action)
            OfflineActionType.UPDATE -> executeUpdateAction(action)
            OfflineActionType.DELETE -> executeDeleteAction(action)
        }
    }

    private suspend fun executeCreateAction(action: OfflineAction) {
        // Implementation for creating documents on server
    }

    private suspend fun executeUpdateAction(action: OfflineAction) {
        // Implementation for updating documents on server
    }

    private suspend fun executeDeleteAction(action: OfflineAction) {
        // Implementation for deleting documents on server
    }

    /**
     * Cache user profile
     */
    private suspend fun cacheUserProfile(userId: String) {
        // Implementation for caching user profile
    }

    /**
     * Cache user fowls
     */
    private suspend fun cacheUserFowls(userId: String) {
        // Implementation for caching user's fowls
    }

    /**
     * Cache recent conversations
     */
    private suspend fun cacheRecentConversations(userId: String) {
        // Implementation for caching conversations
    }

    /**
     * Cache regional marketplace
     */
    private suspend fun cacheRegionalMarketplace(userId: String) {
        // Implementation for caching marketplace data
    }

    /**
     * Cache breeding records
     */
    private suspend fun cacheBreedingRecords(userId: String) {
        // Implementation for caching breeding records
    }

    /**
     * Update offline actions count
     */
    private suspend fun updateOfflineActionsCount() {
        val count = localDatabase.offlineActionDao().getPendingCount()
        _offlineActionsCount.value = count
    }

    /**
     * Compress data for rural networks
     */
    private fun <T> compressData(data: T): String {
        // Implementation for data compression
        return data.toString() // Simplified
    }

    /**
     * Decompress data
     */
    private fun <T> decompressData(data: String): T? {
        // Implementation for data decompression
        return null // Simplified
    }

    /**
     * Parse data from string
     */
    private fun <T> parseData(data: String): T? {
        // Implementation for data parsing
        return null // Simplified
    }

    /**
     * Check if cache entry is expired
     */
    private fun isExpired(cacheEntity: CacheEntity, maxAge: Long): Boolean {
        return System.currentTimeMillis() - cacheEntity.createdAt.time > maxAge
    }

    /**
     * Get cache expiry based on priority
     */
    private fun getCacheExpiry(priority: CachePriority): Long {
        return when (priority) {
            CachePriority.HIGH -> 24 * 60 * 60 * 1000L // 24 hours
            CachePriority.MEDIUM -> 6 * 60 * 60 * 1000L // 6 hours
            CachePriority.LOW -> 1 * 60 * 60 * 1000L // 1 hour
        }
    }
}

/**
 * Offline action types
 */
enum class OfflineActionType {
    CREATE,
    UPDATE,
    DELETE
}

/**
 * Cache priority levels
 */
enum class CachePriority {
    HIGH,
    MEDIUM,
    LOW
}

/**
 * Offline action data class
 */
data class OfflineAction(
    val id: String = UUID.randomUUID().toString(),
    val type: OfflineActionType,
    val collection: String,
    val documentId: String,
    val data: Map<String, Any>,
    val timestamp: Date = Date(),
    val retryCount: Int = 0,
    val maxRetries: Int = 3
)

/**
 * Sync result data class
 */
data class SyncResult(
    val totalActions: Int,
    val successCount: Int,
    val failureCount: Int,
    val errors: List<String>
)

/**
 * Extension function to convert OfflineAction to Entity
 */
private fun OfflineAction.toEntity(): OfflineActionEntity {
    return OfflineActionEntity(
        id = id,
        action = type.name,
        collection = collection,
        documentId = documentId,
        data = data.toString(), // Simplified serialization
        timestamp = timestamp,
        retryCount = retryCount,
        maxRetries = maxRetries,
        status = "pending"
    )
}

/**
 * Extension function to convert Entity to OfflineAction
 */
private fun OfflineActionEntity.toOfflineAction(): OfflineAction {
    return OfflineAction(
        id = id,
        type = OfflineActionType.valueOf(action),
        collection = collection,
        documentId = documentId,
        data = emptyMap(), // Simplified deserialization
        timestamp = timestamp,
        retryCount = retryCount,
        maxRetries = maxRetries
    )
}

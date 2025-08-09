package com.rio.rostry.core.sync

import android.content.Context
import androidx.work.*
import com.rio.rostry.core.common.network.NetworkStateManager
import com.rio.rostry.core.database.entities.*
import com.rio.rostry.core.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central synchronization manager coordinating all sync operations
 * Implements priority-based sync with conflict resolution
 */
@Singleton
class SyncManager @Inject constructor(
    private val context: Context,
    private val networkStateManager: NetworkStateManager,
    private val conflictResolver: ConflictResolver,
    private val syncMetrics: SyncMetrics,
    private val workManager: WorkManager,
    
    // Repository dependencies
    private val userRepository: UserRepository,
    private val fowlRepository: FowlRepository,
    private val marketplaceRepository: MarketplaceRepository,
    private val messageRepository: MessageRepository,
    private val transferRepository: TransferRepository
) {
    
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    private val _syncProgress = MutableStateFlow<SyncProgress>(SyncProgress())
    val syncProgress: StateFlow<SyncProgress> = _syncProgress.asStateFlow()
    
    private val repositories = listOf(
        userRepository,
        fowlRepository,
        marketplaceRepository,
        messageRepository,
        transferRepository
    )
    
    init {
        setupPeriodicSync()
        observeNetworkChanges()
    }
    
    /**
     * Trigger immediate sync of all pending data
     */
    suspend fun syncAll(force: Boolean = false): SyncResult {
        if (!networkStateManager.isConnected.value && !force) {
            return SyncResult.failure("No network connection")
        }
        
        _syncStatus.value = SyncStatus.SYNCING
        _syncProgress.value = SyncProgress(isActive = true)
        
        return try {
            val results = mutableListOf<SyncResult>()
            var totalItems = 0
            var totalSuccess = 0
            var totalFailures = 0
            var totalConflicts = 0
            
            // Sync in priority order: Critical -> High -> Medium -> Low
            val priorityOrder = listOf(
                SyncPriority.CRITICAL,
                SyncPriority.HIGH,
                SyncPriority.MEDIUM,
                SyncPriority.LOW
            )
            
            for (priority in priorityOrder) {
                val priorityResults = syncByPriority(priority)
                results.addAll(priorityResults)
                
                priorityResults.forEach { result ->
                    totalItems += result.totalItems
                    totalSuccess += result.successCount
                    totalFailures += result.failureCount
                    totalConflicts += result.conflictCount
                }
                
                // Update progress
                val progress = (priorityOrder.indexOf(priority) + 1) * 100 / priorityOrder.size
                _syncProgress.value = _syncProgress.value.copy(
                    progress = progress,
                    currentOperation = "Syncing ${priority.name.lowercase()} priority items"
                )
            }
            
            val overallResult = SyncResult(
                entityType = "ALL",
                totalItems = totalItems,
                successCount = totalSuccess,
                failureCount = totalFailures,
                conflictCount = totalConflicts,
                bytesTransferred = results.sumOf { it.bytesTransferred },
                duration = results.sumOf { it.duration },
                errors = results.flatMap { it.errors }
            )
            
            _syncStatus.value = if (totalFailures == 0) SyncStatus.COMPLETED else SyncStatus.FAILED
            _syncProgress.value = SyncProgress(isActive = false, progress = 100)
            
            // Record metrics
            syncMetrics.recordSyncResult(overallResult)
            
            overallResult
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.FAILED
            _syncProgress.value = SyncProgress(isActive = false, error = e.message)
            
            SyncResult.failure(e.message ?: "Sync failed")
        }
    }
    
    /**
     * Sync entities by priority level
     */
    private suspend fun syncByPriority(priority: SyncPriority): List<SyncResult> = supervisorScope {
        val results = mutableListOf<SyncResult>()
        
        repositories.forEach { repository ->
            launch {
                try {
                    val result = repository.syncPendingByPriority(priority)
                    results.add(result)
                } catch (e: Exception) {
                    results.add(SyncResult.failure("${repository.javaClass.simpleName}: ${e.message}"))
                }
            }
        }
        
        results
    }
    
    /**
     * Sync specific entity type
     */
    suspend fun syncEntityType(entityType: String): SyncResult {
        if (!networkStateManager.isConnected.value) {
            return SyncResult.failure("No network connection")
        }
        
        val repository = when (entityType.lowercase()) {
            "users" -> userRepository
            "fowls" -> fowlRepository
            "marketplace" -> marketplaceRepository
            "messages" -> messageRepository
            "transfers" -> transferRepository
            else -> return SyncResult.failure("Unknown entity type: $entityType")
        }
        
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            val result = repository.syncPendingToServer()
            _syncStatus.value = if (result.failureCount == 0) SyncStatus.COMPLETED else SyncStatus.FAILED
            
            syncMetrics.recordSyncResult(result)
            result
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.FAILED
            SyncResult.failure(e.message ?: "Sync failed for $entityType")
        }
    }
    
    /**
     * Sync critical data immediately (transfers, payments)
     */
    suspend fun syncCriticalData(): SyncResult {
        return syncByPriority(SyncPriority.CRITICAL).let { results ->
            SyncResult(
                entityType = "CRITICAL",
                totalItems = results.sumOf { it.totalItems },
                successCount = results.sumOf { it.successCount },
                failureCount = results.sumOf { it.failureCount },
                conflictCount = results.sumOf { it.conflictCount },
                bytesTransferred = results.sumOf { it.bytesTransferred },
                duration = results.sumOf { it.duration },
                errors = results.flatMap { it.errors }
            )
        }
    }
    
    /**
     * Download fresh data from server
     */
    suspend fun downloadFreshData(
        entityTypes: List<String> = listOf("users", "fowls", "marketplace", "messages", "transfers"),
        region: String? = null,
        district: String? = null
    ): SyncResult {
        if (!networkStateManager.isConnected.value) {
            return SyncResult.failure("No network connection")
        }
        
        _syncStatus.value = SyncStatus.DOWNLOADING
        
        return try {
            val results = mutableListOf<SyncResult>()
            
            entityTypes.forEach { entityType ->
                val repository = getRepositoryForEntityType(entityType)
                if (repository != null) {
                    val result = repository.downloadFreshData(region, district)
                    results.add(result)
                }
            }
            
            val overallResult = SyncResult(
                entityType = "DOWNLOAD",
                totalItems = results.sumOf { it.totalItems },
                successCount = results.sumOf { it.successCount },
                failureCount = results.sumOf { it.failureCount },
                conflictCount = results.sumOf { it.conflictCount },
                bytesTransferred = results.sumOf { it.bytesTransferred },
                duration = results.sumOf { it.duration },
                errors = results.flatMap { it.errors }
            )
            
            _syncStatus.value = if (overallResult.failureCount == 0) SyncStatus.COMPLETED else SyncStatus.FAILED
            overallResult
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.FAILED
            SyncResult.failure(e.message ?: "Download failed")
        }
    }
    
    /**
     * Resolve sync conflicts
     */
    suspend fun resolveConflicts(): SyncResult {
        val conflictedEntities = mutableListOf<SyncableEntity>()
        
        // Collect all conflicted entities
        repositories.forEach { repository ->
            conflictedEntities.addAll(repository.getConflictedEntities())
        }
        
        var resolvedCount = 0
        var failedCount = 0
        val errors = mutableListOf<SyncError>()
        
        conflictedEntities.forEach { entity ->
            try {
                val resolution = conflictResolver.resolveConflict(entity)
                if (resolution.resolved) {
                    resolvedCount++
                } else {
                    failedCount++
                    errors.add(SyncError(
                        entityId = entity.id,
                        errorType = SyncErrorType.CONFLICT_ERROR,
                        message = "Could not resolve conflict automatically"
                    ))
                }
            } catch (e: Exception) {
                failedCount++
                errors.add(SyncError(
                    entityId = entity.id,
                    errorType = SyncErrorType.CONFLICT_ERROR,
                    message = e.message ?: "Conflict resolution failed"
                ))
            }
        }
        
        return SyncResult(
            entityType = "CONFLICTS",
            totalItems = conflictedEntities.size,
            successCount = resolvedCount,
            failureCount = failedCount,
            conflictCount = 0,
            bytesTransferred = 0L,
            duration = 0L,
            errors = errors
        )
    }
    
    /**
     * Get sync statistics
     */
    suspend fun getSyncStatistics(): SyncStatistics {
        val pendingCounts = mutableMapOf<String, Int>()
        val conflictCounts = mutableMapOf<String, Int>()
        
        repositories.forEach { repository ->
            val entityType = repository.getEntityType()
            pendingCounts[entityType] = repository.getPendingSyncCount()
            conflictCounts[entityType] = repository.getConflictCount()
        }
        
        return SyncStatistics(
            pendingSync = pendingCounts,
            conflicts = conflictCounts,
            lastSyncTime = syncMetrics.getLastSyncTime(),
            syncSuccess = syncMetrics.getSyncSuccessRate(),
            averageSyncDuration = syncMetrics.getAverageSyncDuration()
        )
    }
    
    /**
     * Setup periodic background sync
     */
    private fun setupPeriodicSync() {
        // Critical data sync every 5 minutes when connected
        val criticalSyncRequest = PeriodicWorkRequestBuilder<CriticalSyncWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        ).build()
        
        // Full sync every hour on WiFi
        val fullSyncRequest = PeriodicWorkRequestBuilder<FullSyncWorker>(
            1, TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .setRequiresDeviceIdle(true)
                .build()
        ).build()
        
        workManager.enqueueUniquePeriodicWork(
            "critical_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            criticalSyncRequest
        )
        
        workManager.enqueueUniquePeriodicWork(
            "full_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            fullSyncRequest
        )
    }
    
    /**
     * Observe network changes and trigger sync when connected
     */
    private fun observeNetworkChanges() {
        networkStateManager.isConnected
            .distinctUntilChanged()
            .onEach { isConnected ->
                if (isConnected) {
                    // Schedule immediate critical sync when network becomes available
                    val immediateSyncRequest = OneTimeWorkRequestBuilder<CriticalSyncWorker>()
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .build()
                    
                    workManager.enqueueUniqueWork(
                        "immediate_sync",
                        ExistingWorkPolicy.REPLACE,
                        immediateSyncRequest
                    )
                }
            }
    }
    
    /**
     * Get repository for entity type
     */
    private fun getRepositoryForEntityType(entityType: String): BaseOfflineRepository<*, *>? {
        return when (entityType.lowercase()) {
            "users" -> userRepository
            "fowls" -> fowlRepository
            "marketplace" -> marketplaceRepository
            "messages" -> messageRepository
            "transfers" -> transferRepository
            else -> null
        }
    }
    
    /**
     * Cancel all sync operations
     */
    fun cancelSync() {
        workManager.cancelUniqueWork("immediate_sync")
        _syncStatus.value = SyncStatus.CANCELLED
        _syncProgress.value = SyncProgress(isActive = false)
    }
}

/**
 * Sync status enumeration
 */
enum class SyncStatus {
    IDLE,
    SYNCING,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Sync progress data
 */
data class SyncProgress(
    val isActive: Boolean = false,
    val progress: Int = 0, // 0-100
    val currentOperation: String = "",
    val error: String? = null
)

/**
 * Sync statistics
 */
data class SyncStatistics(
    val pendingSync: Map<String, Int>,
    val conflicts: Map<String, Int>,
    val lastSyncTime: Date?,
    val syncSuccess: Double, // 0.0 - 1.0
    val averageSyncDuration: Long // milliseconds
)

/**
 * Extension function for SyncResult
 */
fun SyncResult.Companion.failure(message: String): SyncResult {
    return SyncResult(
        entityType = "ERROR",
        totalItems = 0,
        successCount = 0,
        failureCount = 1,
        conflictCount = 0,
        bytesTransferred = 0L,
        duration = 0L,
        errors = listOf(SyncError(
            entityId = "",
            errorType = SyncErrorType.UNKNOWN_ERROR,
            message = message
        ))
    )
}

package com.rio.rostry.core.data.sync

import com.rio.rostry.core.network.NetworkStateManager
import com.rio.rostry.core.database.dao.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityQueue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ Centralized sync manager for offline-first functionality
 * Handles intelligent sync prioritization and conflict resolution
 */
@Singleton
class SyncManager @Inject constructor(
    private val networkStateManager: NetworkStateManager,
    private val fowlDao: FowlDao,
    private val transferDao: TransferDao,
    private val marketplaceDao: MarketplaceDao,
    private val chatDao: ChatDao
) {
    
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // ✅ Priority queue for sync operations
    private val syncQueue = PriorityQueue<SyncItem>(compareBy { it.priority.ordinal })
    private val activeSyncs = ConcurrentHashMap<String, Job>()
    
    // ✅ Sync statistics
    private val _syncStats = MutableStateFlow(SyncStats())
    val syncStats: StateFlow<SyncStats> = _syncStats.asStateFlow()
    
    init {
        observeNetworkChanges()
    }
    
    /**
     * ✅ Observe network changes and trigger sync when online
     */
    private fun observeNetworkChanges() {
        syncScope.launch {
            networkStateManager.isConnected
                .distinctUntilChanged()
                .collect { isConnected ->
                    if (isConnected) {
                        startSyncProcess()
                    } else {
                        pauseAllSyncs()
                    }
                }
        }
    }
    
    /**
     * ✅ Queue item for sync with priority
     */
    fun queueForSync(item: SyncItem) {
        synchronized(syncQueue) {
            // Remove existing item with same ID if present
            syncQueue.removeAll { it.id == item.id }
            syncQueue.offer(item)
        }
        
        // Start sync if online
        if (networkStateManager.isConnected.value) {
            syncScope.launch {
                processSyncQueue()
            }
        }
    }
    
    /**
     * ✅ Start comprehensive sync process
     */
    private suspend fun startSyncProcess() {
        try {
            _syncStats.update { it.copy(isActive = true, lastSyncStarted = Date()) }
            
            // Process queued items first
            processSyncQueue()
            
            // Then sync all pending changes
            syncAllPendingChanges()
            
            _syncStats.update { 
                it.copy(
                    isActive = false, 
                    lastSyncCompleted = Date(),
                    successfulSyncs = it.successfulSyncs + 1
                )
            }
            
        } catch (e: Exception) {
            _syncStats.update { 
                it.copy(
                    isActive = false,
                    lastError = e.message,
                    failedSyncs = it.failedSyncs + 1
                )
            }
            android.util.Log.e("SyncManager", "Sync process failed", e)
        }
    }
    
    /**
     * ✅ Process sync queue with priority ordering
     */
    private suspend fun processSyncQueue() {
        while (syncQueue.isNotEmpty() && networkStateManager.isConnected.value) {
            val item = synchronized(syncQueue) { syncQueue.poll() }
            if (item != null) {
                processSyncItem(item)
            }
        }
    }
    
    /**
     * ✅ Process individual sync item
     */
    private suspend fun processSyncItem(item: SyncItem) {
        val syncJob = syncScope.launch {
            try {
                when (item.type) {
                    SyncType.FOWL_CREATE -> syncFowlCreation(item)
                    SyncType.FOWL_UPDATE -> syncFowlUpdate(item)
                    SyncType.TRANSFER_CREATE -> syncTransferCreation(item)
                    SyncType.TRANSFER_UPDATE -> syncTransferUpdate(item)
                    SyncType.MARKETPLACE_CREATE -> syncMarketplaceCreation(item)
                    SyncType.MARKETPLACE_UPDATE -> syncMarketplaceUpdate(item)
                    SyncType.CHAT_MESSAGE -> syncChatMessage(item)
                }
                
                // Mark as synced
                markItemAsSynced(item)
                
            } catch (e: Exception) {
                handleSyncError(item, e)
            }
        }
        
        activeSyncs[item.id] = syncJob
        syncJob.join()
        activeSyncs.remove(item.id)
    }
    
    /**
     * ✅ Sync all pending changes from local database
     */
    private suspend fun syncAllPendingChanges() {
        coroutineScope {
            // Launch parallel sync operations
            val jobs = listOf(
                async { syncPendingFowls() },
                async { syncPendingTransfers() },
                async { syncPendingMarketplaceListings() },
                async { syncPendingChatMessages() }
            )
            
            jobs.awaitAll()
        }
    }
    
    /**
     * ✅ Sync pending fowl changes
     */
    private suspend fun syncPendingFowls() {
        try {
            val unsyncedFowls = fowlDao.getUnsyncedFowls()
            
            unsyncedFowls.forEach { fowl ->
                val syncItem = SyncItem(
                    id = fowl.id,
                    type = if (fowl.createdAt == fowl.updatedAt) SyncType.FOWL_CREATE else SyncType.FOWL_UPDATE,
                    data = fowl,
                    priority = SyncPriority.HIGH,
                    retryCount = 0
                )
                
                processSyncItem(syncItem)
            }
            
        } catch (e: Exception) {
            android.util.Log.e("SyncManager", "Failed to sync fowls", e)
        }
    }
    
    /**
     * ✅ Sync pending transfers
     */
    private suspend fun syncPendingTransfers() {
        try {
            val unsyncedTransfers = transferDao.getUnsyncedTransfers()
            
            unsyncedTransfers.forEach { transfer ->
                val syncItem = SyncItem(
                    id = transfer.id,
                    type = SyncType.TRANSFER_CREATE,
                    data = transfer,
                    priority = SyncPriority.CRITICAL, // Transfers are critical
                    retryCount = 0
                )
                
                processSyncItem(syncItem)
            }
            
        } catch (e: Exception) {
            android.util.Log.e("SyncManager", "Failed to sync transfers", e)
        }
    }
    
    /**
     * ✅ Sync pending marketplace listings
     */
    private suspend fun syncPendingMarketplaceListings() {
        try {
            val unsyncedListings = marketplaceDao.getUnsyncedListings()
            
            unsyncedListings.forEach { listing ->
                val syncItem = SyncItem(
                    id = listing.id,
                    type = SyncType.MARKETPLACE_CREATE,
                    data = listing,
                    priority = SyncPriority.MEDIUM,
                    retryCount = 0
                )
                
                processSyncItem(syncItem)
            }
            
        } catch (e: Exception) {
            android.util.Log.e("SyncManager", "Failed to sync marketplace listings", e)
        }
    }
    
    /**
     * ✅ Sync pending chat messages
     */
    private suspend fun syncPendingChatMessages() {
        try {
            val unsyncedMessages = chatDao.getUnsyncedMessages()
            
            unsyncedMessages.forEach { message ->
                val syncItem = SyncItem(
                    id = message.id,
                    type = SyncType.CHAT_MESSAGE,
                    data = message,
                    priority = SyncPriority.LOW,
                    retryCount = 0
                )
                
                processSyncItem(syncItem)
            }
            
        } catch (e: Exception) {
            android.util.Log.e("SyncManager", "Failed to sync chat messages", e)
        }
    }
    
    /**
     * ✅ Handle sync errors with retry logic
     */
    private suspend fun handleSyncError(item: SyncItem, error: Exception) {
        val maxRetries = when (item.priority) {
            SyncPriority.CRITICAL -> 5
            SyncPriority.HIGH -> 3
            SyncPriority.MEDIUM -> 2
            SyncPriority.LOW -> 1
        }
        
        if (item.retryCount < maxRetries) {
            // Exponential backoff
            val delayMs = (1000 * Math.pow(2.0, item.retryCount.toDouble())).toLong()
            delay(delayMs)
            
            val retryItem = item.copy(retryCount = item.retryCount + 1)
            queueForSync(retryItem)
        } else {
            // Mark as failed
            markItemAsFailed(item, error)
        }
    }
    
    /**
     * ✅ Pause all active syncs
     */
    private fun pauseAllSyncs() {
        activeSyncs.values.forEach { job ->
            job.cancel()
        }
        activeSyncs.clear()
        
        _syncStats.update { it.copy(isActive = false) }
    }
    
    /**
     * ✅ Force sync now (user-triggered)
     */
    suspend fun forceSyncNow(): Result<Unit> {
        return try {
            if (!networkStateManager.isConnected.value) {
                return Result.failure(Exception("No network connection"))
            }
            
            startSyncProcess()
            Result.success(Unit)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * ✅ Get sync status for UI
     */
    fun getSyncStatus(): Flow<SyncStatus> {
        return combine(
            syncStats,
            networkStateManager.isConnected
        ) { stats, isConnected ->
            when {
                !isConnected -> SyncStatus.OFFLINE
                stats.isActive -> SyncStatus.SYNCING
                syncQueue.isNotEmpty() -> SyncStatus.PENDING
                stats.lastError != null -> SyncStatus.ERROR
                else -> SyncStatus.SYNCED
            }
        }
    }
    
    // Placeholder implementations for specific sync operations
    private suspend fun syncFowlCreation(item: SyncItem) { /* Implementation */ }
    private suspend fun syncFowlUpdate(item: SyncItem) { /* Implementation */ }
    private suspend fun syncTransferCreation(item: SyncItem) { /* Implementation */ }
    private suspend fun syncTransferUpdate(item: SyncItem) { /* Implementation */ }
    private suspend fun syncMarketplaceCreation(item: SyncItem) { /* Implementation */ }
    private suspend fun syncMarketplaceUpdate(item: SyncItem) { /* Implementation */ }
    private suspend fun syncChatMessage(item: SyncItem) { /* Implementation */ }
    private suspend fun markItemAsSynced(item: SyncItem) { /* Implementation */ }
    private suspend fun markItemAsFailed(item: SyncItem, error: Exception) { /* Implementation */ }
    
    /**
     * ✅ Cleanup resources
     */
    fun cleanup() {
        syncScope.cancel()
        activeSyncs.clear()
        syncQueue.clear()
    }
}

/**
 * Sync item data class
 */
data class SyncItem(
    val id: String,
    val type: SyncType,
    val data: Any,
    val priority: SyncPriority,
    val retryCount: Int = 0,
    val createdAt: Date = Date()
)

/**
 * Sync operation types
 */
enum class SyncType {
    FOWL_CREATE,
    FOWL_UPDATE,
    TRANSFER_CREATE,
    TRANSFER_UPDATE,
    MARKETPLACE_CREATE,
    MARKETPLACE_UPDATE,
    CHAT_MESSAGE
}

/**
 * Sync priority levels
 */
enum class SyncPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Sync status for UI
 */
enum class SyncStatus {
    OFFLINE,
    PENDING,
    SYNCING,
    SYNCED,
    ERROR
}

/**
 * Sync statistics
 */
data class SyncStats(
    val isActive: Boolean = false,
    val lastSyncStarted: Date? = null,
    val lastSyncCompleted: Date? = null,
    val successfulSyncs: Int = 0,
    val failedSyncs: Int = 0,
    val lastError: String? = null
)

package com.rio.rostry.core.sync

import android.content.SharedPreferences
import com.rio.rostry.core.database.entities.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sync metrics for monitoring sync performance and success rates
 * Tracks sync operations and provides analytics for optimization
 */
@Singleton
class SyncMetrics @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    
    private val syncHistory = ConcurrentLinkedQueue<SyncRecord>()
    private val maxHistorySize = 1000
    
    private val _currentMetrics = MutableStateFlow(SyncMetricsData())
    val currentMetrics: StateFlow<SyncMetricsData> = _currentMetrics.asStateFlow()
    
    init {
        loadStoredMetrics()
    }
    
    /**
     * Record sync result
     */
    fun recordSyncResult(result: SyncResult) {
        val record = SyncRecord(
            timestamp = Date(),
            entityType = result.entityType,
            totalItems = result.totalItems,
            successCount = result.successCount,
            failureCount = result.failureCount,
            conflictCount = result.conflictCount,
            duration = result.duration,
            bytesTransferred = result.bytesTransferred,
            networkType = getCurrentNetworkType(),
            connectionQuality = getCurrentConnectionQuality()
        )
        
        addSyncRecord(record)
        updateMetrics()
        persistMetrics()
    }
    
    /**
     * Record sync start
     */
    fun recordSyncStart(entityType: String) {
        val metrics = _currentMetrics.value
        _currentMetrics.value = metrics.copy(
            activeSyncs = metrics.activeSyncs + 1,
            lastSyncStartTime = Date()
        )
    }
    
    /**
     * Record sync completion
     */
    fun recordSyncCompletion(entityType: String, success: Boolean) {
        val metrics = _currentMetrics.value
        _currentMetrics.value = metrics.copy(
            activeSyncs = maxOf(0, metrics.activeSyncs - 1),
            totalSyncs = metrics.totalSyncs + 1,
            successfulSyncs = if (success) metrics.successfulSyncs + 1 else metrics.successfulSyncs,
            lastSyncTime = Date()
        )
    }
    
    /**
     * Get sync success rate
     */
    fun getSyncSuccessRate(): Double {
        val recentRecords = getRecentRecords(24 * 60 * 60 * 1000) // Last 24 hours
        if (recentRecords.isEmpty()) return 1.0
        
        val totalOperations = recentRecords.sumOf { it.totalItems }
        val successfulOperations = recentRecords.sumOf { it.successCount }
        
        return if (totalOperations > 0) {
            successfulOperations.toDouble() / totalOperations.toDouble()
        } else {
            1.0
        }
    }
    
    /**
     * Get average sync duration
     */
    fun getAverageSyncDuration(): Long {
        val recentRecords = getRecentRecords(24 * 60 * 60 * 1000) // Last 24 hours
        if (recentRecords.isEmpty()) return 0L
        
        return recentRecords.map { it.duration }.average().toLong()
    }
    
    /**
     * Get last sync time
     */
    fun getLastSyncTime(): Date? {
        return syncHistory.maxByOrNull { it.timestamp }?.timestamp
    }
    
    /**
     * Get sync performance by entity type
     */
    fun getSyncPerformanceByEntityType(): Map<String, EntitySyncMetrics> {
        val recentRecords = getRecentRecords(7 * 24 * 60 * 60 * 1000) // Last 7 days
        
        return recentRecords.groupBy { it.entityType }.mapValues { (_, records) ->
            EntitySyncMetrics(
                totalSyncs = records.size,
                totalItems = records.sumOf { it.totalItems },
                successfulItems = records.sumOf { it.successCount },
                failedItems = records.sumOf { it.failureCount },
                conflictedItems = records.sumOf { it.conflictCount },
                averageDuration = records.map { it.duration }.average().toLong(),
                totalBytesTransferred = records.sumOf { it.bytesTransferred },
                successRate = if (records.sumOf { it.totalItems } > 0) {
                    records.sumOf { it.successCount }.toDouble() / records.sumOf { it.totalItems }.toDouble()
                } else 1.0
            )
        }
    }
    
    /**
     * Get sync performance by network type
     */
    fun getSyncPerformanceByNetworkType(): Map<String, NetworkSyncMetrics> {
        val recentRecords = getRecentRecords(7 * 24 * 60 * 60 * 1000) // Last 7 days
        
        return recentRecords.groupBy { it.networkType }.mapValues { (_, records) ->
            NetworkSyncMetrics(
                totalSyncs = records.size,
                averageDuration = records.map { it.duration }.average().toLong(),
                averageBytesPerSecond = calculateAverageBytesPerSecond(records),
                successRate = if (records.sumOf { it.totalItems } > 0) {
                    records.sumOf { it.successCount }.toDouble() / records.sumOf { it.totalItems }.toDouble()
                } else 1.0,
                averageItemsPerSync = if (records.isNotEmpty()) {
                    records.sumOf { it.totalItems }.toDouble() / records.size
                } else 0.0
            )
        }
    }
    
    /**
     * Get sync trends over time
     */
    fun getSyncTrends(periodHours: Int = 24): SyncTrends {
        val cutoffTime = Date(System.currentTimeMillis() - periodHours * 60 * 60 * 1000)
        val recentRecords = syncHistory.filter { it.timestamp.after(cutoffTime) }
        
        // Group by hour
        val hourlyData = recentRecords.groupBy { 
            it.timestamp.time / (60 * 60 * 1000) // Hour bucket
        }.mapValues { (_, records) ->
            HourlySyncData(
                syncCount = records.size,
                totalItems = records.sumOf { it.totalItems },
                successfulItems = records.sumOf { it.successCount },
                failedItems = records.sumOf { it.failureCount },
                averageDuration = if (records.isNotEmpty()) {
                    records.map { it.duration }.average().toLong()
                } else 0L
            )
        }
        
        return SyncTrends(
            periodHours = periodHours,
            hourlyData = hourlyData,
            totalSyncs = recentRecords.size,
            peakSyncHour = hourlyData.maxByOrNull { it.value.syncCount }?.key,
            averageSuccessRate = getSyncSuccessRate()
        )
    }
    
    /**
     * Get conflict analysis
     */
    fun getConflictAnalysis(): ConflictAnalysis {
        val recentRecords = getRecentRecords(7 * 24 * 60 * 60 * 1000) // Last 7 days
        val totalConflicts = recentRecords.sumOf { it.conflictCount }
        val totalItems = recentRecords.sumOf { it.totalItems }
        
        val conflictsByEntityType = recentRecords
            .filter { it.conflictCount > 0 }
            .groupBy { it.entityType }
            .mapValues { (_, records) -> records.sumOf { it.conflictCount } }
        
        return ConflictAnalysis(
            totalConflicts = totalConflicts,
            conflictRate = if (totalItems > 0) {
                totalConflicts.toDouble() / totalItems.toDouble()
            } else 0.0,
            conflictsByEntityType = conflictsByEntityType,
            mostConflictedEntityType = conflictsByEntityType.maxByOrNull { it.value }?.key
        )
    }
    
    /**
     * Get bandwidth usage statistics
     */
    fun getBandwidthUsage(): BandwidthUsage {
        val recentRecords = getRecentRecords(24 * 60 * 60 * 1000) // Last 24 hours
        val totalBytes = recentRecords.sumOf { it.bytesTransferred }
        val totalDuration = recentRecords.sumOf { it.duration }
        
        val usageByNetworkType = recentRecords.groupBy { it.networkType }
            .mapValues { (_, records) -> records.sumOf { it.bytesTransferred } }
        
        return BandwidthUsage(
            totalBytesTransferred = totalBytes,
            averageBytesPerSync = if (recentRecords.isNotEmpty()) {
                totalBytes / recentRecords.size
            } else 0L,
            averageBytesPerSecond = if (totalDuration > 0) {
                (totalBytes * 1000) / totalDuration
            } else 0L,
            usageByNetworkType = usageByNetworkType
        )
    }
    
    /**
     * Reset metrics
     */
    fun resetMetrics() {
        syncHistory.clear()
        _currentMetrics.value = SyncMetricsData()
        clearStoredMetrics()
    }
    
    /**
     * Add sync record to history
     */
    private fun addSyncRecord(record: SyncRecord) {
        syncHistory.offer(record)
        
        // Maintain max history size
        while (syncHistory.size > maxHistorySize) {
            syncHistory.poll()
        }
    }
    
    /**
     * Update current metrics
     */
    private fun updateMetrics() {
        val recentRecords = getRecentRecords(24 * 60 * 60 * 1000) // Last 24 hours
        
        _currentMetrics.value = _currentMetrics.value.copy(
            totalItemsSynced = recentRecords.sumOf { it.totalItems },
            totalBytesTransferred = recentRecords.sumOf { it.bytesTransferred },
            averageSyncDuration = getAverageSyncDuration(),
            successRate = getSyncSuccessRate()
        )
    }
    
    /**
     * Get recent records within time period
     */
    private fun getRecentRecords(periodMs: Long): List<SyncRecord> {
        val cutoffTime = Date(System.currentTimeMillis() - periodMs)
        return syncHistory.filter { it.timestamp.after(cutoffTime) }
    }
    
    /**
     * Calculate average bytes per second
     */
    private fun calculateAverageBytesPerSecond(records: List<SyncRecord>): Long {
        val totalBytes = records.sumOf { it.bytesTransferred }
        val totalDuration = records.sumOf { it.duration }
        
        return if (totalDuration > 0) {
            (totalBytes * 1000) / totalDuration
        } else 0L
    }
    
    /**
     * Persist metrics to storage
     */
    private fun persistMetrics() {
        val editor = sharedPreferences.edit()
        val metrics = _currentMetrics.value
        
        editor.putInt("total_syncs", metrics.totalSyncs)
        editor.putInt("successful_syncs", metrics.successfulSyncs)
        editor.putLong("total_items_synced", metrics.totalItemsSynced)
        editor.putLong("total_bytes_transferred", metrics.totalBytesTransferred)
        editor.putLong("last_sync_time", metrics.lastSyncTime?.time ?: 0L)
        editor.putFloat("success_rate", metrics.successRate.toFloat())
        
        editor.apply()
    }
    
    /**
     * Load stored metrics
     */
    private fun loadStoredMetrics() {
        val totalSyncs = sharedPreferences.getInt("total_syncs", 0)
        val successfulSyncs = sharedPreferences.getInt("successful_syncs", 0)
        val totalItemsSynced = sharedPreferences.getLong("total_items_synced", 0L)
        val totalBytesTransferred = sharedPreferences.getLong("total_bytes_transferred", 0L)
        val lastSyncTime = sharedPreferences.getLong("last_sync_time", 0L)
        val successRate = sharedPreferences.getFloat("success_rate", 1.0f).toDouble()
        
        _currentMetrics.value = SyncMetricsData(
            totalSyncs = totalSyncs,
            successfulSyncs = successfulSyncs,
            totalItemsSynced = totalItemsSynced,
            totalBytesTransferred = totalBytesTransferred,
            lastSyncTime = if (lastSyncTime > 0) Date(lastSyncTime) else null,
            successRate = successRate
        )
    }
    
    /**
     * Clear stored metrics
     */
    private fun clearStoredMetrics() {
        sharedPreferences.edit().clear().apply()
    }
    
    // Placeholder methods for network info
    private fun getCurrentNetworkType(): String = "UNKNOWN"
    private fun getCurrentConnectionQuality(): String = "UNKNOWN"
}

/**
 * Sync record for tracking individual sync operations
 */
data class SyncRecord(
    val timestamp: Date,
    val entityType: String,
    val totalItems: Int,
    val successCount: Int,
    val failureCount: Int,
    val conflictCount: Int,
    val duration: Long,
    val bytesTransferred: Long,
    val networkType: String,
    val connectionQuality: String
)

/**
 * Current sync metrics data
 */
data class SyncMetricsData(
    val activeSyncs: Int = 0,
    val totalSyncs: Int = 0,
    val successfulSyncs: Int = 0,
    val totalItemsSynced: Long = 0L,
    val totalBytesTransferred: Long = 0L,
    val averageSyncDuration: Long = 0L,
    val successRate: Double = 1.0,
    val lastSyncTime: Date? = null,
    val lastSyncStartTime: Date? = null
)

/**
 * Entity-specific sync metrics
 */
data class EntitySyncMetrics(
    val totalSyncs: Int,
    val totalItems: Int,
    val successfulItems: Int,
    val failedItems: Int,
    val conflictedItems: Int,
    val averageDuration: Long,
    val totalBytesTransferred: Long,
    val successRate: Double
)

/**
 * Network-specific sync metrics
 */
data class NetworkSyncMetrics(
    val totalSyncs: Int,
    val averageDuration: Long,
    val averageBytesPerSecond: Long,
    val successRate: Double,
    val averageItemsPerSync: Double
)

/**
 * Sync trends over time
 */
data class SyncTrends(
    val periodHours: Int,
    val hourlyData: Map<Long, HourlySyncData>,
    val totalSyncs: Int,
    val peakSyncHour: Long?,
    val averageSuccessRate: Double
)

/**
 * Hourly sync data
 */
data class HourlySyncData(
    val syncCount: Int,
    val totalItems: Int,
    val successfulItems: Int,
    val failedItems: Int,
    val averageDuration: Long
)

/**
 * Conflict analysis
 */
data class ConflictAnalysis(
    val totalConflicts: Int,
    val conflictRate: Double,
    val conflictsByEntityType: Map<String, Int>,
    val mostConflictedEntityType: String?
)

/**
 * Bandwidth usage statistics
 */
data class BandwidthUsage(
    val totalBytesTransferred: Long,
    val averageBytesPerSync: Long,
    val averageBytesPerSecond: Long,
    val usageByNetworkType: Map<String, Long>
)

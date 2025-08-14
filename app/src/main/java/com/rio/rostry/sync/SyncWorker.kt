package com.rio.rostry.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.core.database.di.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background sync worker for periodic data synchronization
 * Handles offline-first sync with conflict resolution
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        const val KEY_SYNC_TYPE = "sync_type"
        const val KEY_USERS_SYNCED = "users_synced"
        const val KEY_FOWLS_SYNCED = "fowls_synced"
        const val KEY_ERROR_MESSAGE = "error_message"
        
        const val SYNC_TYPE_FULL = "full"
        const val SYNC_TYPE_INCREMENTAL = "incremental"
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Initialize dependencies
            val database = DatabaseProvider.getDatabase(applicationContext)
            val firestore = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()
            
            val syncManager = SimpleSyncManager(
                context = applicationContext,
                database = database,
                firestore = firestore,
                auth = auth
            )
            
            // Perform sync
            val syncResult = syncManager.performManualSync()
            
            when (syncResult) {
                is SyncResult.Success -> {
                    val outputData = workDataOf(
                        KEY_USERS_SYNCED to syncResult.usersSynced,
                        KEY_FOWLS_SYNCED to syncResult.fowlsSynced
                    )
                    Result.success(outputData)
                }
                is SyncResult.Error -> {
                    val outputData = workDataOf(
                        KEY_ERROR_MESSAGE to syncResult.message
                    )
                    Result.failure(outputData)
                }
            }
        } catch (e: Exception) {
            val outputData = workDataOf(
                KEY_ERROR_MESSAGE to (e.message ?: "Unknown sync error")
            )
            Result.failure(outputData)
        }
    }
}

/**
 * Offline action queue for storing actions when offline
 */
data class OfflineAction(
    val id: String,
    val type: ActionType,
    val entityId: String,
    val data: Map<String, Any>,
    val timestamp: Long,
    val retryCount: Int = 0
)

enum class ActionType {
    CREATE_FOWL,
    UPDATE_FOWL,
    DELETE_FOWL,
    CREATE_LISTING,
    UPDATE_LISTING,
    DELETE_LISTING,
    UPDATE_USER
}

/**
 * Simple conflict resolver for sync conflicts
 */
class SimpleConflictResolver {
    
    /**
     * Resolve conflicts using last-write-wins strategy
     * In production, this could be more sophisticated
     */
    fun resolveConflict(
        localTimestamp: Long,
        remoteTimestamp: Long,
        localData: Map<String, Any>,
        remoteData: Map<String, Any>
    ): Map<String, Any> {
        return if (localTimestamp > remoteTimestamp) {
            localData
        } else {
            remoteData
        }
    }
    
    /**
     * Merge non-conflicting fields
     */
    fun mergeData(
        localData: Map<String, Any>,
        remoteData: Map<String, Any>
    ): Map<String, Any> {
        val merged = mutableMapOf<String, Any>()
        
        // Add all remote data first
        merged.putAll(remoteData)
        
        // Override with local data for specific fields that should be preserved
        val preserveLocalFields = setOf("lastModifiedLocally", "syncStatus")
        for (field in preserveLocalFields) {
            localData[field]?.let { merged[field] = it }
        }
        
        return merged
    }
}

/**
 * Sync metrics for monitoring sync performance
 */
data class SyncMetrics(
    val totalSyncAttempts: Int = 0,
    val successfulSyncs: Int = 0,
    val failedSyncs: Int = 0,
    val averageSyncDuration: Long = 0,
    val lastSyncTimestamp: Long? = null,
    val conflictsResolved: Int = 0,
    val dataTransferred: Long = 0 // in bytes
) {
    val successRate: Float
        get() = if (totalSyncAttempts > 0) {
            successfulSyncs.toFloat() / totalSyncAttempts.toFloat()
        } else 0f
}

/**
 * Network state manager for checking connectivity
 */
class SimpleNetworkStateManager(private val context: Context) {
    
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
            as android.net.ConnectivityManager
        
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        
        return networkCapabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    fun isWifiConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
            as android.net.ConnectivityManager
        
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        
        return networkCapabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
    }
}

/**
 * Sync preferences for user-configurable sync settings
 */
data class SyncPreferences(
    val autoSyncEnabled: Boolean = true,
    val syncOnWifiOnly: Boolean = false,
    val syncFrequencyHours: Int = 6,
    val syncOnlyWhenCharging: Boolean = false,
    val backgroundSyncEnabled: Boolean = true
)

/**
 * Priority levels for sync operations
 */
enum class SyncPriority {
    LOW,      // Non-critical data, can wait
    NORMAL,   // Regular data updates
    HIGH,     // Important user data
    CRITICAL  // Essential data that must sync immediately
}

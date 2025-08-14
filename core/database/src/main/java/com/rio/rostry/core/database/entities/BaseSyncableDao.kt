package com.rio.rostry.core.database.entities

import java.util.Date

/**
 * Base DAO interface for syncable entities (Room DAOs should extend this for offline sync)
 */
interface BaseSyncableDao<T : SyncableEntity> {
    suspend fun insert(entity: T): Long
    suspend fun insertAll(entities: List<T>): List<Long>
    suspend fun update(entity: T): Int
    suspend fun delete(entity: T): Int
    suspend fun deleteById(id: String): Int

    // Sync-specific queries
    suspend fun getById(id: String): T?
    suspend fun getAllPendingSync(): List<T>
    suspend fun getAllByStatus(status: SyncStatus): List<T>
    suspend fun getAllByPriority(priority: SyncPriority): List<T>
    suspend fun getAllConflicted(): List<T>
    suspend fun getAllInRegion(region: String, district: String): List<T>

    // Sync metadata updates
    suspend fun updateSyncStatus(id: String, status: SyncStatus, lastSyncTime: Date = Date())
    suspend fun updateConflictVersion(id: String, version: Int)
    suspend fun markAsDeleted(id: String, deletedAt: Date = Date())
    suspend fun incrementRetryCount(id: String)
    suspend fun clearRetryCount(id: String)

    // Cleanup operations
    suspend fun deleteOldSyncedItems(olderThan: Date): Int
    suspend fun deleteLowPriorityItems(limit: Int): Int
    suspend fun getStorageSize(): Long
}

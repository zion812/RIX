package com.rio.rostry.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.rio.rostry.core.database.entities.*
import com.rio.rostry.core.common.network.NetworkStateManager
import com.rio.rostry.core.common.utils.DataValidator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

/**
 * Base repository implementing offline-first data access pattern
 * Provides common functionality for all domain repositories
 */
abstract class BaseOfflineRepository<T : SyncableEntity, R : Any> @Inject constructor(
    protected val firestore: FirebaseFirestore,
    protected val networkStateManager: NetworkStateManager,
    protected val dataValidator: DataValidator
) {
    
    /**
     * Get entity by ID with offline-first approach
     * 1. Check local cache first
     * 2. If not found and online, fetch from server
     * 3. Cache server response for offline access
     */
    suspend fun getById(id: String): Flow<Result<R?>> = flow {
        try {
            // First, try to get from local cache
            val localEntity = getLocalById(id)
            if (localEntity != null) {
                emit(Result.success(mapEntityToDomain(localEntity)))
                
                // If online, check for updates in background
                if (networkStateManager.isConnected.value) {
                    try {
                        val serverEntity = getServerById(id)
                        if (serverEntity != null && shouldUpdateLocal(localEntity, serverEntity)) {
                            saveLocal(serverEntity)
                            emit(Result.success(mapEntityToDomain(serverEntity)))
                        }
                    } catch (e: Exception) {
                        // Server fetch failed, but we have local data
                        // Don't emit error, just log it
                    }
                }
            } else if (networkStateManager.isConnected.value) {
                // Not in cache and online - fetch from server
                val serverEntity = getServerById(id)
                if (serverEntity != null) {
                    saveLocal(serverEntity)
                    emit(Result.success(mapEntityToDomain(serverEntity)))
                } else {
                    emit(Result.success(null))
                }
            } else {
                // Not in cache and offline
                emit(Result.success(null))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get multiple entities with offline-first approach
     */
    suspend fun getAll(
        limit: Int = 50,
        offset: Int = 0,
        forceRefresh: Boolean = false
    ): Flow<Result<List<R>>> = flow {
        try {
            // Always emit cached data first (if available)
            if (!forceRefresh) {
                val localEntities = getLocalAll(limit, offset)
                if (localEntities.isNotEmpty()) {
                    emit(Result.success(localEntities.map { mapEntityToDomain(it) }))
                }
            }
            
            // If online, fetch fresh data
            if (networkStateManager.isConnected.value) {
                try {
                    val serverEntities = getServerAll(limit, offset)
                    
                    // Save to local cache
                    saveLocalBatch(serverEntities)
                    
                    // Emit fresh data
                    emit(Result.success(serverEntities.map { mapEntityToDomain(it) }))
                } catch (e: Exception) {
                    // If we already emitted cached data, don't emit error
                    if (forceRefresh) {
                        emit(Result.failure(e))
                    }
                }
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Create new entity with offline support
     */
    suspend fun create(domainObject: R): Flow<Result<R>> = flow {
        try {
            val entity = mapDomainToEntity(domainObject)
            
            // Validate data
            if (!dataValidator.validate(entity)) {
                emit(Result.failure(IllegalArgumentException("Invalid data")))
                return@flow
            }
            
            if (networkStateManager.isConnected.value) {
                try {
                    // Save to server first
                    val savedEntity = saveServer(entity)
                    
                    // Then save to local with synced status
                    val syncedEntity = savedEntity.copy(
                        syncMetadata = savedEntity.syncMetadata.copy(
                            syncStatus = SyncStatus.SYNCED,
                            lastSyncTime = Date()
                        )
                    ) as T
                    
                    saveLocal(syncedEntity)
                    emit(Result.success(mapEntityToDomain(syncedEntity)))
                } catch (e: Exception) {
                    // Server save failed - save locally with pending status
                    val pendingEntity = entity.copy(
                        syncMetadata = entity.syncMetadata.copy(
                            syncStatus = SyncStatus.PENDING_UPLOAD,
                            retryCount = 0
                        )
                    ) as T
                    
                    saveLocal(pendingEntity)
                    queueForSync(pendingEntity)
                    
                    emit(Result.success(mapEntityToDomain(pendingEntity)))
                }
            } else {
                // Offline - save locally with pending status
                val pendingEntity = entity.copy(
                    syncMetadata = entity.syncMetadata.copy(
                        syncStatus = SyncStatus.PENDING_UPLOAD,
                        retryCount = 0
                    )
                ) as T
                
                saveLocal(pendingEntity)
                queueForSync(pendingEntity)
                
                emit(Result.success(mapEntityToDomain(pendingEntity)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Update entity with offline support
     */
    suspend fun update(domainObject: R): Flow<Result<R>> = flow {
        try {
            val entity = mapDomainToEntity(domainObject)
            
            // Validate data
            if (!dataValidator.validate(entity)) {
                emit(Result.failure(IllegalArgumentException("Invalid data")))
                return@flow
            }
            
            if (networkStateManager.isConnected.value) {
                try {
                    // Update on server first
                    val updatedEntity = updateServer(entity)
                    
                    // Then update local with synced status
                    val syncedEntity = updatedEntity.copy(
                        syncMetadata = updatedEntity.syncMetadata.copy(
                            syncStatus = SyncStatus.SYNCED,
                            lastSyncTime = Date(),
                            conflictVersion = updatedEntity.syncMetadata.conflictVersion + 1
                        )
                    ) as T
                    
                    saveLocal(syncedEntity)
                    emit(Result.success(mapEntityToDomain(syncedEntity)))
                } catch (e: Exception) {
                    // Server update failed - save locally with pending status
                    val pendingEntity = entity.copy(
                        syncMetadata = entity.syncMetadata.copy(
                            syncStatus = SyncStatus.PENDING_UPLOAD,
                            retryCount = 0,
                            conflictVersion = entity.syncMetadata.conflictVersion + 1
                        )
                    ) as T
                    
                    saveLocal(pendingEntity)
                    queueForSync(pendingEntity)
                    
                    emit(Result.success(mapEntityToDomain(pendingEntity)))
                }
            } else {
                // Offline - save locally with pending status
                val pendingEntity = entity.copy(
                    syncMetadata = entity.syncMetadata.copy(
                        syncStatus = SyncStatus.PENDING_UPLOAD,
                        retryCount = 0,
                        conflictVersion = entity.syncMetadata.conflictVersion + 1
                    )
                ) as T
                
                saveLocal(pendingEntity)
                queueForSync(pendingEntity)
                
                emit(Result.success(mapEntityToDomain(pendingEntity)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Delete entity with offline support
     */
    suspend fun delete(id: String): Flow<Result<Boolean>> = flow {
        try {
            if (networkStateManager.isConnected.value) {
                try {
                    // Delete from server first
                    deleteServer(id)
                    
                    // Then mark as deleted locally
                    markLocalAsDeleted(id)
                    emit(Result.success(true))
                } catch (e: Exception) {
                    // Server delete failed - mark locally with pending status
                    val entity = getLocalById(id)
                    if (entity != null) {
                        val pendingEntity = entity.copy(
                            syncMetadata = entity.syncMetadata.copy(
                                syncStatus = SyncStatus.PENDING_UPLOAD,
                                isDeleted = true,
                                retryCount = 0
                            )
                        ) as T
                        
                        saveLocal(pendingEntity)
                        queueForSync(pendingEntity)
                    }
                    
                    emit(Result.success(true))
                }
            } else {
                // Offline - mark as deleted locally
                val entity = getLocalById(id)
                if (entity != null) {
                    val pendingEntity = entity.copy(
                        syncMetadata = entity.syncMetadata.copy(
                            syncStatus = SyncStatus.PENDING_UPLOAD,
                            isDeleted = true,
                            retryCount = 0
                        )
                    ) as T
                    
                    saveLocal(pendingEntity)
                    queueForSync(pendingEntity)
                }
                
                emit(Result.success(true))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get entities pending sync
     */
    suspend fun getPendingSync(): List<T> {
        return getLocalPendingSync()
    }
    
    /**
     * Sync pending entities to server
     */
    suspend fun syncPendingToServer(): SyncResult {
        val pendingEntities = getPendingSync()
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<SyncError>()
        
        for (entity in pendingEntities) {
            try {
                when {
                    entity.isDeleted -> {
                        deleteServer(entity.id)
                        markLocalAsDeleted(entity.id)
                    }
                    entity.syncStatus == SyncStatus.PENDING_UPLOAD -> {
                        if (getServerById(entity.id) != null) {
                            updateServer(entity)
                        } else {
                            saveServer(entity)
                        }
                        
                        val syncedEntity = entity.copy(
                            syncMetadata = entity.syncMetadata.copy(
                                syncStatus = SyncStatus.SYNCED,
                                lastSyncTime = Date(),
                                retryCount = 0
                            )
                        ) as T
                        
                        saveLocal(syncedEntity)
                    }
                }
                successCount++
            } catch (e: Exception) {
                failureCount++
                errors.add(SyncError(
                    entityId = entity.id,
                    errorType = SyncErrorType.NETWORK_ERROR,
                    message = e.message ?: "Unknown error"
                ))
                
                // Increment retry count
                incrementRetryCount(entity.id)
            }
        }
        
        return SyncResult(
            entityType = getEntityType(),
            totalItems = pendingEntities.size,
            successCount = successCount,
            failureCount = failureCount,
            conflictCount = 0,
            bytesTransferred = 0L,
            duration = 0L,
            errors = errors
        )
    }
    
    /**
     * Sync pending entities by priority
     */
    suspend fun syncPendingByPriority(priority: SyncPriority): SyncResult {
        val pendingEntities = getLocalPendingByPriority(priority)
        return syncEntities(pendingEntities)
    }

    /**
     * Sync a list of entities
     */
    private suspend fun syncEntities(entities: List<T>): SyncResult {
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<SyncError>()

        for (entity in entities) {
            try {
                when {
                    entity.isDeleted -> {
                        deleteServer(entity.id)
                        markLocalAsDeleted(entity.id)
                    }
                    entity.syncStatus == SyncStatus.PENDING_UPLOAD -> {
                        if (getServerById(entity.id) != null) {
                            updateServer(entity)
                        } else {
                            saveServer(entity)
                        }

                        val syncedEntity = entity.copy(
                            syncMetadata = entity.syncMetadata.copy(
                                syncStatus = SyncStatus.SYNCED,
                                lastSyncTime = Date(),
                                retryCount = 0
                            )
                        ) as T

                        saveLocal(syncedEntity)
                    }
                }
                successCount++
            } catch (e: Exception) {
                failureCount++
                errors.add(SyncError(
                    entityId = entity.id,
                    errorType = SyncErrorType.NETWORK_ERROR,
                    message = e.message ?: "Unknown error"
                ))

                incrementRetryCount(entity.id)
            }
        }

        return SyncResult(
            entityType = getEntityType(),
            totalItems = entities.size,
            successCount = successCount,
            failureCount = failureCount,
            conflictCount = 0,
            bytesTransferred = 0L,
            duration = 0L,
            errors = errors
        )
    }

    // Abstract methods to be implemented by concrete repositories
    protected abstract suspend fun getLocalById(id: String): T?
    protected abstract suspend fun getLocalAll(limit: Int, offset: Int): List<T>
    protected abstract suspend fun getLocalPendingSync(): List<T>
    protected abstract suspend fun getLocalPendingByPriority(priority: SyncPriority): List<T>
    protected abstract suspend fun saveLocal(entity: T)
    protected abstract suspend fun saveLocalBatch(entities: List<T>)
    protected abstract suspend fun markLocalAsDeleted(id: String)
    protected abstract suspend fun incrementRetryCount(id: String)

    protected abstract suspend fun getServerById(id: String): T?
    protected abstract suspend fun getServerAll(limit: Int, offset: Int): List<T>
    protected abstract suspend fun saveServer(entity: T): T
    protected abstract suspend fun updateServer(entity: T): T
    protected abstract suspend fun deleteServer(id: String)

    protected abstract fun mapEntityToDomain(entity: T): R
    protected abstract fun mapDomainToEntity(domain: R): T
    protected abstract fun getEntityType(): String
    protected abstract fun getCollectionName(): String

    protected abstract suspend fun queueForSync(entity: T)
    protected abstract fun shouldUpdateLocal(local: T, server: T): Boolean

    // Additional abstract methods for repository functionality
    abstract suspend fun getConflictedEntities(): List<T>
    abstract suspend fun getPendingSyncCount(): Int
    abstract suspend fun getConflictCount(): Int
    abstract suspend fun downloadFreshData(region: String?, district: String?): SyncResult
}

package com.rio.rostry.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.core.data.service.UserValidationService
import com.rio.rostry.core.data.service.ValidationException
import com.rio.rostry.core.database.RIOLocalDatabase
import com.rio.rostry.core.database.entities.*
import com.rio.rostry.core.database.mappers.toDomain
import com.rio.rostry.core.database.mappers.toEntity
import com.rio.rostry.core.network.NetworkStateManager
import com.rio.rostry.core.common.utils.DataValidator
import com.rio.rostry.core.common.exceptions.SyncException
import com.rio.rostry.shared.domain.model.*
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of fowl repository with offline-first capabilities
 */
@Singleton
class FowlRepositoryImpl @Inject constructor(
    private val database: RIOLocalDatabase,
    private val userValidationService: UserValidationService,
    firestore: FirebaseFirestore,
    networkStateManager: NetworkStateManager,
    dataValidator: DataValidator
) : BaseOfflineRepository<FowlEntity, Fowl>(firestore, networkStateManager, dataValidator) {

    private val fowlDao = database.fowlDao()
    
    // Local database operations
    override suspend fun getLocalById(id: String): FowlEntity? {
        return try {
            fowlDao.getById(id)
        } catch (e: Exception) {
            throw SyncException.DatabaseError.QueryFailed("Failed to get fowl by ID: $id", e)
        }
    }
    
    override suspend fun getLocalAll(limit: Int, offset: Int): List<FowlEntity> {
        return try {
            // For now, get all fowls - would implement proper pagination
            fowlDao.getAllByStatus(SyncStatus.SYNCED).take(limit)
        } catch (e: Exception) {
            throw SyncException.DatabaseError.QueryFailed("Failed to get local fowls", e)
        }
    }
    
    override suspend fun getLocalPendingSync(): List<FowlEntity> {
        return try {
            fowlDao.getAllPendingSync()
        } catch (e: Exception) {
            throw SyncException.DatabaseError.QueryFailed("Failed to get pending sync fowls", e)
        }
    }
    
    override suspend fun getLocalPendingByPriority(priority: SyncPriority): List<FowlEntity> {
        return try {
            fowlDao.getAllByPriority(priority)
        } catch (e: Exception) {
            throw SyncException.DatabaseError.QueryFailed("Failed to get fowls by priority", e)
        }
    }
    
    override suspend fun saveLocal(entity: FowlEntity) {
        try {
            fowlDao.insert(entity)
        } catch (e: Exception) {
            throw SyncException.DatabaseError.QueryFailed("Failed to save fowl locally", e)
        }
    }
    
    override suspend fun saveLocalBatch(entities: List<FowlEntity>) {
        try {
            fowlDao.insertAll(entities)
        } catch (e: Exception) {
            throw SyncException.DatabaseError.QueryFailed("Failed to save fowls batch locally", e)
        }
    }
    
    override suspend fun markLocalAsDeleted(id: String) {
        try {
            fowlDao.markAsDeleted(id)
        } catch (e: Exception) {
            throw SyncException.DatabaseError.QueryFailed("Failed to mark fowl as deleted", e)
        }
    }
    
    override suspend fun incrementRetryCount(id: String) {
        try {
            fowlDao.incrementRetryCount(id)
        } catch (e: Exception) {
            throw SyncException.DatabaseError.QueryFailed("Failed to increment retry count", e)
        }
    }
    
    // Server operations
    override suspend fun getServerById(id: String): FowlEntity? {
        return try {
            val doc = firestore.collection("fowls").document(id).get().await()
            if (doc.exists()) {
                doc.toFowlEntity()
            } else {
                null
            }
        } catch (e: Exception) {
            throw SyncException.NetworkError.ServerError("Failed to get fowl from server: $id", e)
        }
    }
    
    override suspend fun getServerAll(limit: Int, offset: Int): List<FowlEntity> {
        return try {
            val query = firestore.collection("fowls")
                .orderBy("createdAt")
                .limit(limit.toLong())
            
            val snapshot = query.get().await()
            snapshot.documents.mapNotNull { it.toFowlEntity() }
        } catch (e: Exception) {
            throw SyncException.NetworkError.ServerError("Failed to get fowls from server", e)
        }
    }
    
    override suspend fun saveServer(entity: FowlEntity): FowlEntity {
        return try {
            // ✅ Validate user can own fowls before saving
            val validationResult = userValidationService.validateCanOwnFowls(entity.ownerId)
            if (!validationResult.isValid) {
                throw ValidationException(validationResult)
            }

            val fowlData = entity.toFirestoreMap()
            firestore.collection("fowls")
                .document(entity.id)
                .set(fowlData)
                .await()
            entity
        } catch (e: Exception) {
            when (e) {
                is ValidationException -> throw e
                else -> throw SyncException.NetworkError.ServerError("Failed to save fowl to server", e)
            }
        }
    }
    
    override suspend fun updateServer(entity: FowlEntity): FowlEntity {
        return try {
            val fowlData = entity.toFirestoreMap()
            firestore.collection("fowls")
                .document(entity.id)
                .set(fowlData)
                .await()
            entity
        } catch (e: Exception) {
            throw SyncException.NetworkError.ServerError("Failed to update fowl on server", e)
        }
    }
    
    override suspend fun deleteServer(id: String) {
        try {
            firestore.collection("fowls")
                .document(id)
                .delete()
                .await()
        } catch (e: Exception) {
            throw SyncException.NetworkError.ServerError("Failed to delete fowl from server", e)
        }
    }
    
    // Mapping functions
    override fun mapEntityToDomain(entity: FowlEntity): Fowl {
        return entity.toDomain()
    }
    
    override fun mapDomainToEntity(domain: Fowl): FowlEntity {
        return domain.toEntity()
    }
    
    override fun getEntityType(): String = "fowls"
    override fun getCollectionName(): String = "fowls"
    
    override suspend fun queueForSync(entity: FowlEntity) {
        try {
            val syncQueueEntity = SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                entityType = getEntityType(),
                entityId = entity.id,
                operationType = if (entity.syncStatus == SyncStatus.PENDING_UPLOAD) "UPDATE" else "CREATE",
                syncPriority = entity.syncMetadata.syncPriority.value,
                syncStatus = "QUEUED",
                scheduledAt = Date()
            )
            database.syncQueueDao().insert(syncQueueEntity)
        } catch (e: Exception) {
            throw SyncException.DatabaseError.QueryFailed("Failed to queue fowl for sync", e)
        }
    }
    
    override fun shouldUpdateLocal(local: FowlEntity, server: FowlEntity): Boolean {
        return server.updatedAt.after(local.updatedAt) || 
               server.conflictVersion > local.conflictVersion
    }
    
    // Additional fowl-specific methods
    suspend fun getFowlsByOwner(ownerId: String): List<Fowl> {
        return try {
            val entities = fowlDao.getFowlsByOwner(ownerId)
            entities.map { it.toDomain() }
        } catch (e: Exception) {
            throw SyncException.DatabaseError.QueryFailed("Failed to get fowls by owner", e)
        }
    }
    
    suspend fun getFowlsByBreedInRegion(breed: String, region: String, limit: Int = 50): List<Fowl> {
        return try {
            val entities = fowlDao.getFowlsByBreedInRegion(breed, region, limit)
            entities.map { it.toDomain() }
        } catch (e: Exception) {
            throw SyncException.DatabaseError.QueryFailed("Failed to get fowls by breed in region", e)
        }
    }
    
    suspend fun searchFowls(criteria: FowlSearchCriteria, pageRequest: PageRequest): PageResult<Fowl> {
        return try {
            // Simplified search implementation
            val query = criteria.searchQuery ?: ""
            val region = criteria.region ?: ""
            
            val entities = if (query.isNotEmpty() && region.isNotEmpty()) {
                fowlDao.searchFowlsInRegion(query, region, pageRequest.limit)
            } else {
                fowlDao.getLocalAll(pageRequest.limit, pageRequest.offset)
            }
            
            val fowls = entities.map { it.toDomain() }
            
            PageResult(
                items = fowls,
                hasMore = fowls.size == pageRequest.limit,
                totalCount = fowls.size
            )
        } catch (e: Exception) {
            throw SyncException.DatabaseError.QueryFailed("Failed to search fowls", e)
        }
    }
    
    suspend fun getConflictedEntities(): List<FowlEntity> {
        return try {
            fowlDao.getAllConflicted()
        } catch (e: Exception) {
            throw SyncException.DatabaseError.QueryFailed("Failed to get conflicted fowls", e)
        }
    }
    
    suspend fun getPendingSyncCount(): Int {
        return try {
            fowlDao.getPendingSyncCount()
        } catch (e: Exception) {
            throw SyncException.DatabaseError.QueryFailed("Failed to get pending sync count", e)
        }
    }
    
    suspend fun getConflictCount(): Int {
        return try {
            fowlDao.getConflictCount()
        } catch (e: Exception) {
            throw SyncException.DatabaseError.QueryFailed("Failed to get conflict count", e)
        }
    }
    
    suspend fun downloadFreshData(region: String?, district: String?): SyncResult {
        return try {
            val query = if (region != null && district != null) {
                firestore.collection("fowls")
                    .whereEqualTo("region", region)
                    .whereEqualTo("district", district)
                    .orderBy("updatedAt")
                    .limit(100)
            } else {
                firestore.collection("fowls")
                    .orderBy("updatedAt")
                    .limit(100)
            }
            
            val snapshot = query.get().await()
            val entities = snapshot.documents.mapNotNull { it.toFowlEntity() }
            
            // Save to local database
            saveLocalBatch(entities)
            
            SyncResult(
                entityType = getEntityType(),
                totalItems = entities.size,
                successCount = entities.size,
                failureCount = 0,
                conflictCount = 0,
                bytesTransferred = 0L, // Would calculate actual bytes
                duration = 0L, // Would measure actual duration
                errors = emptyList()
            )
        } catch (e: Exception) {
            SyncResult(
                entityType = getEntityType(),
                totalItems = 0,
                successCount = 0,
                failureCount = 1,
                conflictCount = 0,
                bytesTransferred = 0L,
                duration = 0L,
                errors = listOf(SyncError(
                    entityId = "",
                    errorType = SyncErrorType.NETWORK_ERROR,
                    message = e.message ?: "Download failed"
                ))
            )
        }
    }
}

/**
 * Extension functions for Firestore document conversion
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toFowlEntity(): FowlEntity? {
    return try {
        val data = this.data ?: return null
        
        FowlEntity(
            id = this.id,
            ownerId = data["ownerId"] as? String ?: return null,
            name = data["name"] as? String,
            breedPrimary = data["breedPrimary"] as? String ?: return null,
            breedSecondary = data["breedSecondary"] as? String,
            gender = data["gender"] as? String ?: "UNKNOWN",
            ageCategory = data["ageCategory"] as? String ?: "ADULT",
            color = data["color"] as? String ?: "UNKNOWN",
            weight = (data["weight"] as? Number)?.toDouble() ?: 0.0,
            height = (data["height"] as? Number)?.toDouble() ?: 0.0,
            combType = data["combType"] as? String ?: "SINGLE",
            legColor = data["legColor"] as? String ?: "YELLOW",
            eyeColor = data["eyeColor"] as? String ?: "RED",
            healthStatus = data["healthStatus"] as? String ?: "GOOD",
            availabilityStatus = data["availabilityStatus"] as? String ?: "AVAILABLE",
            fatherId = data["fatherId"] as? String,
            motherId = data["motherId"] as? String,
            generation = (data["generation"] as? Number)?.toInt() ?: 1,
            primaryPhoto = data["primaryPhoto"] as? String,
            photos = (data["photos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            registrationNumber = data["registrationNumber"] as? String,
            qrCode = data["qrCode"] as? String,
            notes = data["notes"] as? String,
            tags = (data["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            regionalMetadata = RegionalMetadata(
                region = data["region"] as? String ?: "",
                district = data["district"] as? String ?: ""
            ),
            syncMetadata = SyncMetadata(
                syncStatus = SyncStatus.SYNCED,
                lastSyncTime = Date(),
                createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                updatedAt = (data["updatedAt"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
            )
        )
    } catch (e: Exception) {
        null
    }
}

private fun FowlEntity.toFirestoreMap(): Map<String, Any> {
    return mapOf(
        "ownerId" to ownerId,
        "name" to (name ?: ""),
        "breedPrimary" to breedPrimary,
        "breedSecondary" to (breedSecondary ?: ""),
        "gender" to gender,
        "ageCategory" to ageCategory,
        "color" to color,
        "weight" to weight,
        "height" to height,
        "healthStatus" to healthStatus,
        "availabilityStatus" to availabilityStatus,
        "fatherId" to (fatherId ?: ""),
        "motherId" to (motherId ?: ""),
        "generation" to generation,
        "primaryPhoto" to (primaryPhoto ?: ""),
        "photos" to photos,
        "registrationNumber" to (registrationNumber ?: ""),
        "qrCode" to (qrCode ?: ""),
        "notes" to (notes ?: ""),
        "tags" to tags,
        "region" to regionalMetadata.region,
        "district" to regionalMetadata.district,
        "createdAt" to com.google.firebase.Timestamp(createdAt),
        "updatedAt" to com.google.firebase.Timestamp(updatedAt)
    )

    // ✅ Additional methods to break circular dependencies

    /**
     * Get fowl count by owner - used by UserRepository without circular dependency
     */
    suspend fun getFowlCountByOwner(ownerId: String): Int {
        return try {
            fowlDao.getCountByOwner(ownerId)
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Validate fowl ownership
     */
    suspend fun validateFowlOwnership(fowlId: String, userId: String): Boolean {
        return try {
            val fowl = fowlDao.getById(fowlId)
            fowl?.ownerId == userId
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get fowls by owner with validation
     */
    suspend fun getFowlsByOwnerValidated(ownerId: String): List<FowlEntity> {
        // ✅ Validate user can own fowls
        val validationResult = userValidationService.validateCanOwnFowls(ownerId)
        if (!validationResult.isValid) {
            throw ValidationException(validationResult)
        }

        return fowlDao.getByOwner(ownerId)
    }
}

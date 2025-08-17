package com.rio.rostry.core.data.repository

import com.rio.rostry.core.database.dao.FowlDaoV2
import com.rio.rostry.core.database.dao.OutboxDao
import com.rio.rostry.core.database.entities.FowlEntity
import com.rio.rostry.core.database.entities.OutboxEntity
import com.rio.rostry.core.common.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for fowl-related operations with offline-first support
 */
@Singleton
class FowlRepository @Inject constructor(
    private val fowlDao: FowlDaoV2,
    private val outboxDao: OutboxDao
) {
    /**
     * Get a fowl by its ID
     */
    suspend fun getFowlById(fowlId: String): Result<FowlEntity?> = withContext(Dispatchers.IO) {
        return@withContext try {
            val fowl = fowlDao.getFowlById(fowlId)
            Result.Success(fowl)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Observe a fowl by its ID
     */
    fun observeFowl(fowlId: String): Flow<FowlEntity?> = fowlDao.observeFowl(fowlId)

    /**
     * Get all fowls owned by a user
     */
    fun getFowlsByOwner(ownerId: String): Flow<List<FowlEntity>> = fowlDao.getFowlsByOwner(ownerId)

    /**
     * Get fowls that need attention (5 weeks or 20 weeks old)
     */
    suspend fun getFowlsNeedingAttention(): Result<List<FowlEntity>> = withContext(Dispatchers.IO) {
        return@withContext try {
            // This is a simplified implementation
            // In a real implementation, we would check the fowl's age and determine if it needs attention
            val fowls = emptyList<FowlEntity>() // Placeholder
            Result.Success(fowls)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Get lineage information for a fowl (2-generation pedigree)
     */
    suspend fun getLineageInfo(fowlId: String): Result<LineageInfo> = withContext(Dispatchers.IO) {
        return@withContext try {
            val fowl = fowlDao.getFowlById(fowlId)
            if (fowl == null || fowl.isDeleted) {
                return@withContext Result.Error(Exception("Fowl not found"))
            }
            
            // Get parents
            val father = fowl.fatherId?.let { fowlDao.getFowlById(it) }
            val mother = fowl.motherId?.let { fowlDao.getFowlById(it) }
            
            // Get children
            val children = fowlDao.getOffspringByParent(fowlId)
            
            // Build lineage info
            val subjectFowl = LineageFowl(
                id = fowl.id,
                name = fowl.name,
                breedPrimary = fowl.breedPrimary,
                gender = fowl.gender,
                photoReference = fowl.primaryPhoto,
                isDeceased = fowl.healthStatus == "DECEASED"
            )
            
            val parents = listOfNotNull(
                father?.let {
                    LineageFowl(
                        id = it.id,
                        name = it.name,
                        breedPrimary = it.breedPrimary,
                        gender = it.gender,
                        photoReference = it.primaryPhoto,
                        isDeceased = it.healthStatus == "DECEASED"
                    )
                },
                mother?.let {
                    LineageFowl(
                        id = it.id,
                        name = it.name,
                        breedPrimary = it.breedPrimary,
                        gender = it.gender,
                        photoReference = it.primaryPhoto,
                        isDeceased = it.healthStatus == "DECEASED"
                    )
                }
            )
            
            val lineageChildren = children.map {
                LineageFowl(
                    id = it.id,
                    name = it.name,
                    breedPrimary = it.breedPrimary,
                    gender = it.gender,
                    photoReference = it.primaryPhoto,
                    isDeceased = it.healthStatus == "DECEASED"
                )
            }
            
            val relationships = buildList {
                father?.let {
                    add(LineageRelationship(it.id, fowlId, "FATHER"))
                }
                mother?.let {
                    add(LineageRelationship(it.id, fowlId, "MOTHER"))
                }
                children.forEach { child ->
                    add(LineageRelationship(fowlId, child.id, if (fowl.gender == "MALE") "FATHER" else "MOTHER"))
                }
            }
            
            val lineageInfo = LineageInfo(
                subjectFowl = subjectFowl,
                parents = parents,
                children = lineageChildren,
                relationships = relationships
            )
            
            Result.Success(lineageInfo)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Insert a new fowl
     */
    suspend fun insertFowl(fowl: FowlEntity): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            fowlDao.insertFowl(fowl)
            
            // Add to outbox for sync
            val outboxEntry = OutboxEntity(
                id = UUID.randomUUID().toString(),
                entityType = "FOWL",
                entityId = fowl.id,
                operationType = "CREATE",
                entityData = null, // Will be serialized by sync service
                createdAt = Date(),
                updatedAt = Date(),
                syncStatus = "PENDING",
                priority = 3 // Medium priority
            )
            
            outboxDao.insert(outboxEntry)
            
            Result.Success(fowl.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Update an existing fowl
     */
    suspend fun updateFowl(fowl: FowlEntity): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            fowlDao.updateFowl(fowl)
            
            // Add to outbox for sync
            val outboxEntry = OutboxEntity(
                id = UUID.randomUUID().toString(),
                entityType = "FOWL",
                entityId = fowl.id,
                operationType = "UPDATE",
                entityData = null, // Will be serialized by sync service
                createdAt = Date(),
                updatedAt = Date(),
                syncStatus = "PENDING",
                priority = 3 // Medium priority
            )
            
            outboxDao.insert(outboxEntry)
            
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Delete a fowl
     */
    suspend fun deleteFowl(fowlId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            fowlDao.deleteFowlById(fowlId)
            
            // Add to outbox for sync
            val outboxEntry = OutboxEntity(
                id = UUID.randomUUID().toString(),
                entityType = "FOWL",
                entityId = fowlId,
                operationType = "DELETE",
                entityData = null, // Will be serialized by sync service
                createdAt = Date(),
                updatedAt = Date(),
                syncStatus = "PENDING",
                priority = 3 // Medium priority
            )
            
            outboxDao.insert(outboxEntry)
            
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Transfer ownership of a fowl
     */
    suspend fun transferOwnership(
        fowlId: String,
        newOwnerId: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            fowlDao.transferOwnership(fowlId, newOwnerId)
            
            // Add to outbox for sync
            val outboxEntry = OutboxEntity(
                id = UUID.randomUUID().toString(),
                entityType = "FOWL",
                entityId = fowlId,
                operationType = "UPDATE",
                entityData = null, // Will be serialized by sync service
                createdAt = Date(),
                updatedAt = Date(),
                syncStatus = "PENDING",
                priority = 5 // High priority
            )
            
            outboxDao.insert(outboxEntry)
            
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Get fowl summaries for marketplace integration
     */
    suspend fun getFowlSummaries(ownerId: String): Result<List<FowlSummary>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val summaries = fowlDao.getFowlSummariesByOwner(ownerId)
            Result.Success(summaries)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Update the sale status of a fowl
     */
    suspend fun updateSaleStatus(
        fowlId: String,
        isForSale: Boolean,
        priceInCoins: Int?
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            fowlDao.updateSaleStatus(fowlId, isForSale, priceInCoins)
            
            // Add to outbox for sync
            val outboxEntry = OutboxEntity(
                id = UUID.randomUUID().toString(),
                entityType = "FOWL",
                entityId = fowlId,
                operationType = "UPDATE",
                entityData = null, // Will be serialized by sync service
                createdAt = Date(),
                updatedAt = Date(),
                syncStatus = "PENDING",
                priority = 3 // Medium priority
            )
            
            outboxDao.insert(outboxEntry)
            
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
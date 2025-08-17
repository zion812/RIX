package com.rio.rostry.core.data.repository

import com.rio.rostry.core.common.model.Result
import com.rio.rostry.core.data.model.Fowl
import com.rio.rostry.core.data.model.FowlSummary
import com.rio.rostry.core.database.dao.FowlDao
import com.rio.rostry.core.database.dao.OutboxDaoV2
import com.rio.rostry.core.database.entities.FowlEntity
import com.rio.rostry.core.database.entities.OutboxEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for fowl operations with offline-first support
 */
@Singleton
class FowlRepositoryImpl @Inject constructor(
    private val fowlDao: FowlDao,
    private val outboxDao: OutboxDaoV2
) : FowlRepository {
    
    /**
     * Get all fowls owned by a specific user
     */
    override fun getFowlsByOwner(ownerId: String): Flow<List<Fowl>> =
        fowlDao.getFowlsByOwnerId(ownerId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    
    /**
     * Get a specific fowl by ID
     */
    override suspend fun getFowlById(id: String): Fowl? = withContext(Dispatchers.IO) {
        fowlDao.getFowlById(id)?.toDomainModel()
    }
    
    /**
     * Save a new fowl
     */
    override suspend fun saveFowl(fowl: Fowl): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val entity = fowl.toEntity()
            fowlDao.insert(entity)
            
            // Add to outbox for sync
            val outboxEntry = OutboxEntity(
                id = UUID.randomUUID().toString(),
                entityType = "fowl",
                entityId = entity.id,
                operation = "CREATE",
                payload = entity.toJson(),
                timestamp = System.currentTimeMillis(),
                status = "PENDING",
                retryCount = 0
            )
            outboxDao.insert(outboxEntry)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Update an existing fowl
     */
    override suspend fun updateFowl(fowl: Fowl): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val entity = fowl.toEntity()
            fowlDao.update(entity)
            
            // Add to outbox for sync
            val outboxEntry = OutboxEntity(
                id = UUID.randomUUID().toString(),
                entityType = "fowl",
                entityId = entity.id,
                operation = "UPDATE",
                payload = entity.toJson(),
                timestamp = System.currentTimeMillis(),
                status = "PENDING",
                retryCount = 0
            )
            outboxDao.insert(outboxEntry)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Delete a fowl
     */
    override suspend fun deleteFowl(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            fowlDao.deleteById(id)
            
            // Add to outbox for sync
            val outboxEntry = OutboxEntity(
                id = UUID.randomUUID().toString(),
                entityType = "fowl",
                entityId = id,
                operation = "DELETE",
                payload = "",
                timestamp = System.currentTimeMillis(),
                status = "PENDING",
                retryCount = 0
            )
            outboxDao.insert(outboxEntry)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Get fowl summaries for marketplace integration
     */
    override suspend fun getFowlSummaries(ownerId: String): Result<List<FowlSummary>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val entities = fowlDao.getFowlsByOwnerIdSuspend(ownerId)
            Result.Success(entities.map { it.toFowlSummary() })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

/**
 * Extension function to convert FowlEntity to Fowl domain model
 */
fun FowlEntity.toDomainModel(): Fowl {
    return Fowl(
        id = id,
        ownerId = ownerId,
        name = name,
        motherId = motherId,
        fatherId = fatherId,
        dob = dob,
        breederStatus = breederStatus,
        status = status,
        gender = gender,
        color = color,
        breedPrimary = breedPrimary,
        breedSecondary = breedSecondary,
        generation = generation,
        healthStatus = healthStatus,
        availabilityStatus = availabilityStatus,
        region = region,
        district = district,
        inbreedingCoefficient = inbreedingCoefficient,
        totalOffspring = totalOffspring,
        fightingWins = fightingWins,
        fightingLosses = fightingLosses,
        showWins = showWins,
        traits = traits,
        siblings = siblings,
        notes = notes,
        coverThumbnailUrl = coverThumbnailUrl,
        createdAt = createdAt,
        updatedAt = updatedAt,
        createdBy = createdBy,
        updatedBy = updatedBy,
        version = version
    )
}

/**
 * Extension function to convert Fowl domain model to FowlEntity
 */
fun Fowl.toEntity(): FowlEntity {
    return FowlEntity(
        id = id,
        ownerId = ownerId,
        name = name,
        motherId = motherId,
        fatherId = fatherId,
        dob = dob,
        breederStatus = breederStatus,
        status = status,
        gender = gender,
        color = color,
        breedPrimary = breedPrimary,
        breedSecondary = breedSecondary,
        generation = generation,
        healthStatus = healthStatus,
        availabilityStatus = availabilityStatus,
        region = region,
        district = district,
        inbreedingCoefficient = inbreedingCoefficient,
        totalOffspring = totalOffspring,
        fightingWins = fightingWins,
        fightingLosses = fightingLosses,
        showWins = showWins,
        traits = traits,
        siblings = siblings,
        notes = notes,
        coverThumbnailUrl = coverThumbnailUrl,
        createdAt = createdAt,
        updatedAt = updatedAt,
        createdBy = createdBy,
        updatedBy = updatedBy,
        version = version
    )
}

/**
 * Extension function to convert FowlEntity to FowlSummary
 */
fun FowlEntity.toFowlSummary(): FowlSummary {
    return FowlSummary(
        id = id,
        name = name,
        breedPrimary = breedPrimary,
        breedSecondary = breedSecondary,
        gender = gender,
        color = color,
        generation = generation,
        dob = dob,
        coverThumbnailUrl = coverThumbnailUrl,
        healthStatus = healthStatus,
        availabilityStatus = availabilityStatus,
        region = region,
        district = district,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Extension function to convert FowlEntity to JSON string for outbox
 */
fun FowlEntity.toJson(): String {
    // In a real implementation, we would serialize the entity to JSON
    // For now, we'll return a placeholder
    return "{}"
}
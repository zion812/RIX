package com.rio.rostry.core.data.repository

import com.rio.rostry.core.common.model.Result
import com.rio.rostry.core.data.model.FowlRecord
import com.rio.rostry.core.data.model.FowlRecordListItem
import com.rio.rostry.core.data.model.TimelineSummary
import com.rio.rostry.core.database.dao.FowlRecordDao
import com.rio.rostry.core.database.dao.OutboxDaoV2
import com.rio.rostry.core.database.entities.FowlRecordEntity
import com.rio.rostry.core.database.entities.OutboxEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for fowl record operations with offline-first support
 */
@Singleton
class FowlRecordRepositoryImpl @Inject constructor(
    private val fowlRecordDao: FowlRecordDao,
    private val outboxDao: OutboxDaoV2
) : FowlRecordRepository {
    
    /**
     * Get all records for a specific fowl ordered by date (newest first)
     */
    override fun getRecordsByFowlId(fowlId: String): Flow<List<FowlRecord>> =
        fowlRecordDao.getRecordsByFowlId(fowlId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    
    /**
     * Get records by type for a specific fowl
     */
    override fun getRecordsByType(fowlId: String, type: String): Flow<List<FowlRecord>> =
        fowlRecordDao.getRecordsByType(fowlId, type).map { entities ->
            entities.map { it.toDomainModel() }
        }
    
    /**
     * Add a new fowl record
     */
    override suspend fun addRecord(record: FowlRecord): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val entity = record.toEntity()
            fowlRecordDao.insert(entity)
            
            // Add to outbox for sync
            val outboxEntry = OutboxEntity(
                id = UUID.randomUUID().toString(),
                entityType = "fowl_record",
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
     * Update an existing fowl record
     */
    override suspend fun updateRecord(record: FowlRecord): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val entity = record.toEntity()
            fowlRecordDao.update(entity)
            
            // Add to outbox for sync
            val outboxEntry = OutboxEntity(
                id = UUID.randomUUID().toString(),
                entityType = "fowl_record",
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
     * Delete a fowl record
     */
    override suspend fun deleteRecord(recordId: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            fowlRecordDao.deleteById(recordId)
            
            // Add to outbox for sync
            val outboxEntry = OutboxEntity(
                id = UUID.randomUUID().toString(),
                entityType = "fowl_record",
                entityId = recordId,
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
     * Get a specific record by ID
     */
    override suspend fun getRecordById(recordId: String): Result<FowlRecord?> = withContext(Dispatchers.IO) {
        return@withContext try {
            val entity = fowlRecordDao.getRecordById(recordId)
            Result.Success(entity?.toDomainModel())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Get records for a specific fowl with pagination support
     */
    override suspend fun getRecordsByFowlIdPaged(fowlId: String, limit: Int, offset: Int): Result<List<FowlRecord>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val entities = fowlRecordDao.getRecordsByFowlIdPaged(fowlId, limit, offset)
            Result.Success(entities.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Get lightweight projection of records for a specific fowl with pagination support
     * Minimizes Map/List deserialization for better performance on low-end devices
     */
    override suspend fun getRecordListItemsByFowlIdPaged(fowlId: String, limit: Int, offset: Int): Result<List<FowlRecordListItem>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val entities = fowlRecordDao.getRecordListItemsByFowlIdPaged(fowlId, limit, offset)
            Result.Success(entities.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Get compact timeline summary for a specific fowl
     * Used for quick display during transfer flow
     */
    override suspend fun getTimelineSummary(fowlId: String, limit: Int): Result<List<TimelineSummary>> = withContext(Dispatchers.IO) {
        return@withContext try {
            // In a real implementation, we would have a DAO method that returns TimelineSummary entities
            // For now, we'll fetch the full records and convert them
            val entities = fowlRecordDao.getRecordsByFowlIdPaged(fowlId, limit, 0)
            Result.Success(entities.map { it.toTimelineSummary() })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

/**
 * Extension function to convert FowlRecordEntity to FowlRecord domain model
 */
fun FowlRecordEntity.toDomainModel(): FowlRecord {
    return FowlRecord(
        id = id,
        fowlId = fowlId,
        recordType = recordType,
        recordDate = recordDate,
        description = description,
        metrics = metrics,
        proofUrls = proofUrls,
        proofCount = proofCount,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        version = version
    )
}

/**
 * Extension function to convert FowlRecordListItem entity to FowlRecordListItem domain model
 */
fun com.rio.rostry.core.database.entities.FowlRecordListItem.toDomainModel(): FowlRecordListItem {
    return FowlRecordListItem(
        id = id,
        fowlId = fowlId,
        recordType = recordType,
        recordDate = recordDate,
        description = description,
        proofCount = proofCount,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        version = version
    )
}

/**
 * Extension function to convert FowlRecordEntity to TimelineSummary
 */
fun FowlRecordEntity.toTimelineSummary(): TimelineSummary {
    return TimelineSummary(
        recordType = recordType,
        recordDate = recordDate,
        description = description,
        proofCount = proofCount,
        isVerified = false // In a real implementation, we would check verification status
    )
}

/**
 * Extension function to convert FowlRecord domain model to FowlRecordEntity
 */
fun FowlRecord.toEntity(): FowlRecordEntity {
    return FowlRecordEntity(
        id = id,
        fowlId = fowlId,
        recordType = recordType,
        recordDate = recordDate,
        description = description,
        metrics = metrics,
        proofUrls = proofUrls,
        proofCount = proofCount,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        version = version,
        isDeleted = false
    )
}

/**
 * Extension function to convert FowlRecordEntity to JSON string for outbox
 */
fun FowlRecordEntity.toJson(): String {
    // In a real implementation, we would serialize the entity to JSON
    // For now, we'll return a placeholder
    return "{}"
}
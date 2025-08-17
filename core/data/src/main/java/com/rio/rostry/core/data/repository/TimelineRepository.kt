package com.rio.rostry.core.data.repository

import com.rio.rostry.core.database.dao.TimelineDao
import com.rio.rostry.core.database.dao.OutboxDao
import com.rio.rostry.core.database.entities.TimelineEntity
import com.rio.rostry.core.database.entities.OutboxEntity
import com.rio.rostry.core.common.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for timeline events with offline-first capabilities
 * Manages the complete lifecycle of timeline events with outbox pattern for synchronization
 */
@Singleton
class TimelineRepository @Inject constructor(
    private val timelineDao: TimelineDao,
    private val outboxDao: OutboxDao
) {
    /**
     * Create a new timeline event
     */
    suspend fun createTimelineEvent(
        fowlId: String,
        eventType: String,
        title: String,
        description: String? = null,
        mediaReferences: List<String> = emptyList(),
        metadata: Map<String, String> = emptyMap(),
        latitude: Double? = null,
        longitude: Double? = null,
        createdBy: String
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val eventId = UUID.randomUUID().toString()
            val now = Date()
            
            val timelineEvent = TimelineEntity(
                id = eventId,
                fowlId = fowlId,
                eventType = eventType,
                title = title,
                description = description,
                mediaReferences = mediaReferences,
                metadata = metadata,
                latitude = latitude,
                longitude = longitude,
                createdAt = now,
                updatedAt = now,
                createdBy = createdBy
            )
            
            timelineDao.insert(timelineEvent)
            
            // Add to outbox for sync
            val outboxEntry = OutboxEntity(
                id = UUID.randomUUID().toString(),
                entityType = "TIMELINE_EVENT",
                entityId = eventId,
                operationType = "CREATE",
                entityData = null, // Will be serialized by sync service
                createdAt = now,
                updatedAt = now,
                syncStatus = "PENDING",
                priority = 2 // Low priority
            )
            
            outboxDao.insert(outboxEntry)
            
            Result.Success(eventId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Get timeline events for a fowl
     */
    suspend fun getTimelineForFowl(fowlId: String): Result<List<TimelineEntity>> = 
        withContext(Dispatchers.IO) {
            return@withContext try {
                val events = timelineDao.getTimelineForFowl(fowlId)
                Result.Success(events)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    
    /**
     * Get timeline event by ID
     */
    suspend fun getTimelineEventById(eventId: String): Result<TimelineEntity?> = 
        withContext(Dispatchers.IO) {
            return@withContext try {
                val event = timelineDao.getById(eventId)
                Result.Success(event)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    
    /**
     * Update a timeline event
     */
    suspend fun updateTimelineEvent(
        eventId: String,
        title: String? = null,
        description: String? = null,
        mediaReferences: List<String>? = null,
        metadata: Map<String, String>? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            val existingEvent = timelineDao.getById(eventId)
            if (existingEvent == null || existingEvent.isDeleted) {
                return@withContext Result.Error(Exception("Timeline event not found"))
            }
            
            val now = Date()
            val updatedEvent = existingEvent.copy(
                title = title ?: existingEvent.title,
                description = description ?: existingEvent.description,
                mediaReferences = mediaReferences ?: existingEvent.mediaReferences,
                metadata = metadata ?: existingEvent.metadata,
                latitude = latitude ?: existingEvent.latitude,
                longitude = longitude ?: existingEvent.longitude,
                updatedAt = now
            )
            
            timelineDao.update(updatedEvent)
            
            // Add to outbox for sync
            val outboxEntry = OutboxEntity(
                id = UUID.randomUUID().toString(),
                entityType = "TIMELINE_EVENT",
                entityId = eventId,
                operationType = "UPDATE",
                entityData = null, // Will be serialized by sync service
                createdAt = now,
                updatedAt = now,
                syncStatus = "PENDING",
                priority = 2 // Low priority
            )
            
            outboxDao.insert(outboxEntry)
            
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Delete a timeline event
     */
    suspend fun deleteTimelineEvent(eventId: String): Result<Boolean> = 
        withContext(Dispatchers.IO) {
            return@withContext try {
                val now = Date()
                timelineDao.markAsDeleted(eventId, now)
                
                // Add to outbox for sync
                val outboxEntry = OutboxEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "TIMELINE_EVENT",
                    entityId = eventId,
                    operationType = "DELETE",
                    entityData = null, // Will be serialized by sync service
                    createdAt = now,
                    updatedAt = now,
                    syncStatus = "PENDING",
                    priority = 2 // Low priority
                )
                
                outboxDao.insert(outboxEntry)
                
                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
}
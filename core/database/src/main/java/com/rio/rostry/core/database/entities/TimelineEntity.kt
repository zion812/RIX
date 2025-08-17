package com.rio.rostry.core.database.entities

import androidx.room.*
import java.util.*

/**
 * Room entity for timeline events with offline-first capabilities
 * Tracks important events in a fowl's life such as health records, breeding events, transfers, etc.
 */
@Entity(
    tableName = "timeline_events",
    indices = [
        Index(value = ["fowl_id"]),
        Index(value = ["event_type"]),
        Index(value = ["created_at"]),
        Index(value = ["is_deleted"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = FowlEntity::class,
            parentColumns = ["id"],
            childColumns = ["fowl_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TimelineEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "fowl_id")
    val fowlId: String,
    
    @ColumnInfo(name = "event_type")
    val eventType: String, // BREEDING, HEALTH, TRANSFER, VACCINATION, SALE, DEATH, etc.
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "media_references")
    val mediaReferences: List<String> = emptyList(), // Photo/video references
    
    @ColumnInfo(name = "metadata")
    val metadata: Map<String, String> = emptyMap(), // Additional event-specific data
    
    // Location information
    @ColumnInfo(name = "latitude")
    val latitude: Double? = null,
    
    @ColumnInfo(name = "longitude")
    val longitude: Double? = null,
    
    // Audit fields
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date,
    
    @ColumnInfo(name = "created_by")
    val createdBy: String, // User ID who created the event
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)

/**
 * DAO for timeline events
 */
@Dao
interface TimelineDao {
    
    @Query("SELECT * FROM timeline_events WHERE fowl_id = :fowlId AND is_deleted = 0 ORDER BY created_at DESC")
    suspend fun getTimelineForFowl(fowlId: String): List<TimelineEntity>
    
    @Query("SELECT * FROM timeline_events WHERE id = :id AND is_deleted = 0")
    suspend fun getById(id: String): TimelineEntity?
    
    @Query("SELECT * FROM timeline_events WHERE event_type = :eventType AND is_deleted = 0 ORDER BY created_at DESC LIMIT :limit")
    suspend fun getEventsByType(eventType: String, limit: Int = 50): List<TimelineEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timelineEvent: TimelineEntity): Long
    
    @Update
    suspend fun update(timelineEvent: TimelineEntity): Int
    
    @Query("UPDATE timeline_events SET is_deleted = 1, updated_at = :deletedAt WHERE id = :id")
    suspend fun markAsDeleted(id: String, deletedAt: Date): Int
    
    @Query("DELETE FROM timeline_events WHERE is_deleted = 1 AND updated_at < :olderThan")
    suspend fun deleteOldDeletedItems(olderThan: Date): Int
}


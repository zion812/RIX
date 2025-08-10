package com.rio.rostry.core.database.entities

import androidx.room.*
import java.util.*

/**
 * Room entity for rooster data with family tree capabilities
 * Represents individual roosters/fowl with breeding and lineage information
 */
@Entity(
    tableName = "roosters",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["breed"]),
        Index(value = ["gender"]),
        Index(value = ["fatherId"]),
        Index(value = ["motherId"]),
        Index(value = ["healthStatus"]),
        Index(value = ["lineageVerified"]),
        Index(value = ["healthCertified"]),
        Index(value = ["isSynced"]),
        Index(value = ["region", "district"]),
        Index(value = ["birthDate"]),
        Index(value = ["created_at"]),
        Index(value = ["updated_at"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoosterEntity::class,
            parentColumns = ["id"],
            childColumns = ["fatherId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = RoosterEntity::class,
            parentColumns = ["id"],
            childColumns = ["motherId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class RoosterEntity(
    @PrimaryKey
    override val id: String,

    // Owner information
    @ColumnInfo(name = "ownerId")
    val ownerId: String,

    // Basic information
    val name: String,
    val breed: String,
    val gender: String, // 'male', 'female'

    @ColumnInfo(name = "birthDate")
    val birthDate: Date?,

    val color: String?,
    val weight: Double?,
    val height: Double?,

    // Family tree information
    @ColumnInfo(name = "fatherId")
    val fatherId: String?,

    @ColumnInfo(name = "motherId")
    val motherId: String?,

    val generation: Int = 1,

    // Health and verification
    @ColumnInfo(name = "healthStatus")
    val healthStatus: String, // 'excellent', 'good', 'fair', 'poor'

    @ColumnInfo(name = "lineageVerified")
    val lineageVerified: Boolean = false,

    @ColumnInfo(name = "healthCertified")
    val healthCertified: Boolean = false,

    // Photos and documentation
    @ColumnInfo(name = "primaryPhoto")
    val primaryPhoto: String?,

    val photos: String?, // JSON array of photo URLs

    @ColumnInfo(name = "registrationNumber")
    val registrationNumber: String?,

    @ColumnInfo(name = "qrCode")
    val qrCode: String?,

    val notes: String?,
    val tags: String?, // JSON array of tags

    // Regional information (flattened)
    val region: String = "",
    val district: String = "",
    val mandal: String? = null,
    val village: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,

    // Sync fields (flattened)
    @ColumnInfo(name = "last_sync_time")
    override val lastSyncTime: Date? = null,

    @ColumnInfo(name = "sync_status")
    val syncStatusString: String = "PENDING_UPLOAD",

    @ColumnInfo(name = "conflict_version")
    override val conflictVersion: Long = 1L,

    @ColumnInfo(name = "is_deleted")
    override val isDeleted: Boolean = false,

    @ColumnInfo(name = "created_at")
    override val createdAt: Date = Date(),

    @ColumnInfo(name = "updated_at")
    override val updatedAt: Date = Date(),

    // Additional sync fields
    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = false,

    @ColumnInfo(name = "syncedAt")
    val syncedAt: Date? = null,

    @ColumnInfo(name = "sync_priority")
    val syncPriority: Int = 1,

    @ColumnInfo(name = "has_conflict")
    val hasConflict: Boolean = false,

    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,

    @ColumnInfo(name = "data_size")
    val dataSize: Long = 0L
) : SyncableEntity {

    // SyncableEntity implementation
    override val syncStatus: SyncStatus
        get() = SyncStatus.valueOf(syncStatusString)
}
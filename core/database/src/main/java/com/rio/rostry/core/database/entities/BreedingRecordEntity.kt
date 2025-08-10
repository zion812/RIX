package com.rio.rostry.core.database.entities

import androidx.room.*
import java.util.*

/**
 * Room entity for breeding records
 */
@Entity(
    tableName = "breeding_records",
    indices = [
        Index(value = ["sireId"]),
        Index(value = ["damId"]),
        Index(value = ["breederId"]),
        Index(value = ["breedingDate"]),
        Index(value = ["region", "district"]),
        Index(value = ["created_at"]),
        Index(value = ["updated_at"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = RoosterEntity::class,
            parentColumns = ["id"],
            childColumns = ["sireId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoosterEntity::class,
            parentColumns = ["id"],
            childColumns = ["damId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["breederId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BreedingRecordEntity(
    @PrimaryKey
    override val id: String,

    @ColumnInfo(name = "sireId")
    val sireId: String,

    @ColumnInfo(name = "damId")
    val damId: String,

    @ColumnInfo(name = "breederId")
    val breederId: String,

    @ColumnInfo(name = "breedingDate")
    val breedingDate: Date,

    @ColumnInfo(name = "expectedHatchDate")
    val expectedHatchDate: Date?,

    @ColumnInfo(name = "actualHatchDate")
    val actualHatchDate: Date?,

    @ColumnInfo(name = "eggsLaid")
    val eggsLaid: Int?,

    @ColumnInfo(name = "chicksHatched")
    val chicksHatched: Int?,

    @ColumnInfo(name = "breedingMethod")
    val breedingMethod: String, // NATURAL, ARTIFICIAL_INSEMINATION, etc.

    @ColumnInfo(name = "breedingPurpose")
    val breedingPurpose: String, // MEAT_PRODUCTION, EGG_PRODUCTION, etc.

    @ColumnInfo(name = "offspringIds")
    val offspringIds: String?, // JSON array of offspring IDs

    val notes: String?,

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
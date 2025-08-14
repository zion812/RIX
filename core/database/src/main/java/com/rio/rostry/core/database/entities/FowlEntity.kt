package com.rio.rostry.core.database.entities

import androidx.room.*
import java.util.*

/**
 * Room entity for fowl data with offline-first capabilities
 * Mirrors the Firestore fowls collection structure
 */
@Entity(
    tableName = "fowls",
    indices = [
        Index(value = ["owner_id"]),
        Index(value = ["region", "district"]),
        Index(value = ["breed_primary"]),
        Index(value = ["sync_status"]),
        Index(value = ["sync_priority"]),
        Index(value = ["created_at"]),
        Index(value = ["is_deleted"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["owner_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FowlEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    override val id: String,
    
    @ColumnInfo(name = "owner_id")
    val ownerId: String,
    
    @ColumnInfo(name = "name")
    val name: String? = null,
    
    // Breed information
    @ColumnInfo(name = "breed_primary")
    val breedPrimary: String,
    
    @ColumnInfo(name = "breed_secondary")
    val breedSecondary: String? = null,
    
    @ColumnInfo(name = "breed_purity")

    
    @ColumnInfo(name = "generation")
    val generation: Int = 1,
    
    @ColumnInfo(name = "inbreeding_coefficient")
    val inbreedingCoefficient: Double? = null,
    
    @ColumnInfo(name = "siblings")
    val siblings: List<String> = emptyList(),
    
    @ColumnInfo(name = "offspring")
    val offspring: List<String> = emptyList(),
    
    // Status information
    @ColumnInfo(name = "health_status")
    val healthStatus: String, // EXCELLENT, GOOD, FAIR, POOR, SICK, DECEASED
    
    @ColumnInfo(name = "availability_status")
    val availabilityStatus: String, // AVAILABLE, SOLD, BREEDING, DECEASED, MISSING, RESERVED
    
    @ColumnInfo(name = "current_farm")
    val currentFarm: String? = null,
    
    // Documentation
    @ColumnInfo(name = "registration_number")
    val registrationNumber: String? = null,
    
    @ColumnInfo(name = "microchip_id")
    val microchipId: String? = null,
    
    @ColumnInfo(name = "tattoo_id")
    val tattooId: String? = null,
    
    @ColumnInfo(name = "certificates")
    val certificates: List<String> = emptyList(),
    
    @ColumnInfo(name = "qr_code")
    val qrCode: String? = null,
    
    // Media information
    @ColumnInfo(name = "primary_photo")
    val primaryPhoto: String? = null,
    
    @ColumnInfo(name = "photo_count")
    val photoCount: Int = 0,
    
    @ColumnInfo(name = "video_count")
    val videoCount: Int = 0,
    
    @ColumnInfo(name = "photos")
    val photos: List<String> = emptyList(),
    
    // Performance metrics
    @ColumnInfo(name = "egg_production_monthly")
    val eggProductionMonthly: Int? = null,
    
    @ColumnInfo(name = "total_offspring")
    val totalOffspring: Int = 0,
    
    @ColumnInfo(name = "fighting_wins")
    val fightingWins: Int = 0,
    
    @ColumnInfo(name = "fighting_losses")
    val fightingLosses: Int = 0,
    
    @ColumnInfo(name = "show_wins")
    val showWins: Int = 0,
    
    @ColumnInfo(name = "awards")
    val awards: List<String> = emptyList(),
    
    // Search and categorization
    @ColumnInfo(name = "search_terms")
    val searchTerms: List<String> = emptyList(),
    
    @ColumnInfo(name = "tags")
    val tags: List<String> = emptyList(),
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
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

/**
 * DAO for fowl entities with offline-optimized queries
 */
@Dao
interface FowlDao : BaseSyncableDao<FowlEntity> {
    
    @Query("SELECT * FROM fowls WHERE id = :id AND is_deleted = 0")
    override suspend fun getById(id: String): FowlEntity?
    
    @Query("SELECT * FROM fowls WHERE sync_status = :status AND is_deleted = 0")
    override suspend fun getAllByStatus(status: SyncStatus): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE sync_priority = :priority AND is_deleted = 0")
    override suspend fun getAllByPriority(priority: SyncPriority): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE sync_status IN ('PENDING_UPLOAD', 'FAILED') AND is_deleted = 0 ORDER BY sync_priority ASC, created_at ASC")
    override suspend fun getAllPendingSync(): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE has_conflict = 1 AND is_deleted = 0")
    override suspend fun getAllConflicted(): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE region = :region AND district = :district AND is_deleted = 0")
    override suspend fun getAllInRegion(region: String, district: String): List<FowlEntity>
    
    // Fowl-specific queries
    @Query("SELECT * FROM fowls WHERE owner_id = :ownerId AND is_deleted = 0 ORDER BY created_at DESC")
    suspend fun getFowlsByOwner(ownerId: String): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE owner_id = :ownerId AND availability_status = 'AVAILABLE' AND is_deleted = 0")
    suspend fun getAvailableFowlsByOwner(ownerId: String): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE breed_primary = :breed AND region = :region AND availability_status = 'AVAILABLE' AND is_deleted = 0 LIMIT :limit")
    suspend fun getFowlsByBreedInRegion(breed: String, region: String, limit: Int = 50): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE father_id = :parentId OR mother_id = :parentId AND is_deleted = 0")
    suspend fun getOffspringByParent(parentId: String): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE (father_id = :fowlId OR mother_id = :fowlId) OR id IN (SELECT father_id FROM fowls WHERE id = :fowlId UNION SELECT mother_id FROM fowls WHERE id = :fowlId) AND is_deleted = 0")
    suspend fun getLineageRelatives(fowlId: String): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE health_status IN ('SICK', 'POOR') AND owner_id = :ownerId AND is_deleted = 0")
    suspend fun getFowlsNeedingAttention(ownerId: String): List<FowlEntity>
    
    @Query("SELECT COUNT(*) FROM fowls WHERE owner_id = :ownerId AND is_deleted = 0")
    suspend fun getFowlCountByOwner(ownerId: String): Int
    
    @Query("SELECT * FROM fowls WHERE (name LIKE '%' || :query || '%' OR breed_primary LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') AND region = :region AND is_deleted = 0 LIMIT :limit")
    suspend fun searchFowlsInRegion(query: String, region: String, limit: Int = 50): List<FowlEntity>
    
    // Sync operations
    @Query("UPDATE fowls SET sync_status = :status, last_sync_time = :lastSyncTime WHERE id = :id")
    override suspend fun updateSyncStatus(id: String, status: SyncStatus, lastSyncTime: Date)
    
    @Query("UPDATE fowls SET conflict_version = :version WHERE id = :id")
    override suspend fun updateConflictVersion(id: String, version: Long)
    
    @Query("UPDATE fowls SET is_deleted = 1, updated_at = :deletedAt WHERE id = :id")
    override suspend fun markAsDeleted(id: String, deletedAt: Date)
    
    @Query("UPDATE fowls SET retry_count = retry_count + 1 WHERE id = :id")
    override suspend fun incrementRetryCount(id: String)
    
    @Query("UPDATE fowls SET retry_count = 0 WHERE id = :id")
    override suspend fun clearRetryCount(id: String)
    
    // Cleanup operations
    @Query("DELETE FROM fowls WHERE sync_status = 'SYNCED' AND updated_at < :olderThan AND sync_priority = 'LOW'")
    override suspend fun deleteOldSyncedItems(olderThan: Date): Int
    
    @Query("DELETE FROM fowls WHERE id IN (SELECT id FROM fowls WHERE sync_priority = 'LOW' AND sync_status = 'SYNCED' ORDER BY last_sync_time ASC LIMIT :limit)")
    override suspend fun deleteLowPriorityItems(limit: Int): Int
    
    @Query("SELECT SUM(data_size) FROM fowls")
    override suspend fun getStorageSize(): Long
    
    // Batch operations for sync
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(entity: FowlEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertAll(entities: List<FowlEntity>): List<Long>
    
    @Update
    override suspend fun update(entity: FowlEntity): Int
    
    @Delete
    override suspend fun delete(entity: FowlEntity): Int
    
    @Query("DELETE FROM fowls WHERE id = :id")
    override suspend fun deleteById(id: String): Int
    
    // Performance optimization queries
    @Query("SELECT id, owner_id, breed_primary, gender, availability_status, primary_photo, region, district FROM fowls WHERE region = :region AND availability_status = 'AVAILABLE' AND is_deleted = 0 ORDER BY created_at DESC LIMIT :limit")
    suspend fun getFowlSummariesInRegion(region: String, limit: Int = 100): List<FowlSummary>
    
    @Query("SELECT COUNT(*) FROM fowls WHERE sync_status = 'PENDING_UPLOAD' AND is_deleted = 0")
    suspend fun getPendingSyncCount(): Int
    
    @Query("SELECT COUNT(*) FROM fowls WHERE has_conflict = 1 AND is_deleted = 0")
    suspend fun getConflictCount(): Int
}

/**
 * Lightweight fowl summary for list displays
 */
data class FowlSummary(
    val id: String,
    @ColumnInfo(name = "owner_id") val ownerId: String,
    @ColumnInfo(name = "breed_primary") val breedPrimary: String,
    val gender: String,
    @ColumnInfo(name = "availability_status") val availabilityStatus: String,
    @ColumnInfo(name = "primary_photo") val primaryPhoto: String?,
    val region: String,
    val district: String
)

package com.rio.rostry.core.database.entities

import androidx.room.*
import java.util.*

/**
 * Room entity for user data with offline-first capabilities
 * Mirrors the Firestore users collection structure
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["phone_number"], unique = true),
        Index(value = ["user_tier"]),
        Index(value = ["region", "district"]),
        Index(value = ["sync_status"]),
        Index(value = ["verification_status"]),
        Index(value = ["is_deleted"])
    ]
)
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    override val id: String,
    
    // Authentication information
    @ColumnInfo(name = "email")
    val email: String,
    
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String? = null,
    
    @ColumnInfo(name = "email_verified")
    val emailVerified: Boolean = false,
    
    @ColumnInfo(name = "phone_verified")
    val phoneVerified: Boolean = false,
    
    // User tier and verification
    @ColumnInfo(name = "user_tier")
    val userTier: String, // GENERAL, FARMER, ENTHUSIAST
    
    @ColumnInfo(name = "verification_status")
    val verificationStatus: String, // PENDING, VERIFIED, REJECTED
    
    @ColumnInfo(name = "verification_documents")
    val verificationDocuments: List<String> = emptyList(),
    
    @ColumnInfo(name = "verification_notes")
    val verificationNotes: String? = null,
    
    @ColumnInfo(name = "verified_at")
    val verifiedAt: Date? = null,
    
    @ColumnInfo(name = "verified_by")
    val verifiedBy: String? = null,
    
    // Personal information
    @ColumnInfo(name = "display_name")
    val displayName: String,
    
    @ColumnInfo(name = "first_name")
    val firstName: String? = null,
    
    @ColumnInfo(name = "last_name")
    val lastName: String? = null,
    
    @ColumnInfo(name = "date_of_birth")
    val dateOfBirth: Date? = null,
    
    @ColumnInfo(name = "gender")
    val gender: String? = null,
    
    @ColumnInfo(name = "profile_photo")
    val profilePhoto: String? = null,
    
    @ColumnInfo(name = "bio")
    val bio: String? = null,
    
    // Contact information
    @ColumnInfo(name = "address")
    val address: String? = null,
    
    @ColumnInfo(name = "pincode")
    val pincode: String? = null,
    
    @ColumnInfo(name = "whatsapp_number")
    val whatsappNumber: String? = null,
    
    @ColumnInfo(name = "preferred_contact_method")
    val preferredContactMethod: String = "MESSAGE", // MESSAGE, PHONE, EMAIL
    
    // Farm information (for farmers)
    @ColumnInfo(name = "farm_name")
    val farmName: String? = null,
    
    @ColumnInfo(name = "farm_size_acres")
    val farmSizeAcres: Double? = null,
    
    @ColumnInfo(name = "farm_type")
    val farmType: String? = null, // COMMERCIAL, BACKYARD, BREEDING
    
    @ColumnInfo(name = "farm_established_year")
    val farmEstablishedYear: Int? = null,
    
    @ColumnInfo(name = "farm_license_number")
    val farmLicenseNumber: String? = null,
    
    @ColumnInfo(name = "specializations")
    val specializations: List<String> = emptyList(),
    
    @ColumnInfo(name = "breeding_experience_years")
    val breedingExperienceYears: Int? = null,
    
    @ColumnInfo(name = "preferred_breeds")
    val preferredBreeds: List<String> = emptyList(),
    
    // Professional information (for enthusiasts)
    @ColumnInfo(name = "organization")
    val organization: String? = null,
    
    @ColumnInfo(name = "designation")
    val designation: String? = null,
    
    @ColumnInfo(name = "professional_credentials")
    val professionalCredentials: List<String> = emptyList(),
    
    @ColumnInfo(name = "research_interests")
    val researchInterests: List<String> = emptyList(),
    
    @ColumnInfo(name = "publications")
    val publications: List<String> = emptyList(),
    
    // Platform statistics
    @ColumnInfo(name = "fowl_count")
    val fowlCount: Int = 0,
    
    @ColumnInfo(name = "listing_count")
    val listingCount: Int = 0,
    
    @ColumnInfo(name = "successful_transactions")
    val successfulTransactions: Int = 0,
    
    @ColumnInfo(name = "rating")
    val rating: Double = 0.0,
    
    @ColumnInfo(name = "review_count")
    val reviewCount: Int = 0,
    
    @ColumnInfo(name = "total_sales")
    val totalSales: Double = 0.0,
    
    @ColumnInfo(name = "total_purchases")
    val totalPurchases: Double = 0.0,
    
    // Activity tracking
    @ColumnInfo(name = "last_active_at")
    val lastActiveAt: Date? = null,
    
    @ColumnInfo(name = "last_login_at")
    val lastLoginAt: Date? = null,
    
    @ColumnInfo(name = "login_count")
    val loginCount: Int = 0,
    
    @ColumnInfo(name = "device_tokens")
    val deviceTokens: List<String> = emptyList(),
    
    // Preferences
    @ColumnInfo(name = "language")
    val language: String = "en", // en, te, hi
    
    @ColumnInfo(name = "currency")
    val currency: String = "INR",
    
    @ColumnInfo(name = "timezone")
    val timezone: String = "Asia/Kolkata",
    
    @ColumnInfo(name = "notification_preferences")
    val notificationPreferences: Map<String, Boolean> = emptyMap(),
    
    @ColumnInfo(name = "privacy_settings")
    val privacySettings: Map<String, String> = emptyMap(),
    
    // Account status
    @ColumnInfo(name = "account_status")
    val accountStatus: String = "ACTIVE", // ACTIVE, SUSPENDED, BANNED, DELETED
    
    @ColumnInfo(name = "suspension_reason")
    val suspensionReason: String? = null,
    
    @ColumnInfo(name = "suspension_until")
    val suspensionUntil: Date? = null,
    
    // Simplified sync fields (flattened for compilation)
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

    // Regional fields (flattened)
    val region: String = "",
    val district: String = "",

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
    
    // SyncableEntity implementation (using flattened fields)
    override val syncStatus: SyncStatus
        get() = SyncStatus.valueOf(syncStatusString)
}

/**
 * DAO for user entities with offline-optimized queries
 */
@Dao
interface UserDao : BaseSyncableDao<UserEntity> {
    
    @Query("SELECT * FROM users WHERE id = :id AND is_deleted = 0")
    override suspend fun getById(id: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE sync_status = :status AND is_deleted = 0")
    override suspend fun getAllByStatus(status: SyncStatus): List<UserEntity>
    
    @Query("SELECT * FROM users WHERE sync_priority = :priority AND is_deleted = 0")
    override suspend fun getAllByPriority(priority: SyncPriority): List<UserEntity>
    
    @Query("SELECT * FROM users WHERE sync_status IN ('PENDING_UPLOAD', 'FAILED') AND is_deleted = 0 ORDER BY sync_priority ASC, created_at ASC")
    override suspend fun getAllPendingSync(): List<UserEntity>
    
    @Query("SELECT * FROM users WHERE has_conflict = 1 AND is_deleted = 0")
    override suspend fun getAllConflicted(): List<UserEntity>
    
    @Query("SELECT * FROM users WHERE region = :region AND district = :district AND is_deleted = 0")
    override suspend fun getAllInRegion(region: String, district: String): List<UserEntity>
    
    // User-specific queries
    @Query("SELECT * FROM users WHERE email = :email AND is_deleted = 0")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE phone_number = :phoneNumber AND is_deleted = 0")
    suspend fun getUserByPhoneNumber(phoneNumber: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE user_tier = :tier AND region = :region AND verification_status = 'VERIFIED' AND is_deleted = 0 LIMIT :limit")
    suspend fun getUsersByTierInRegion(tier: String, region: String, limit: Int = 50): List<UserEntity>
    
    @Query("SELECT * FROM users WHERE user_tier = 'FARMER' AND region = :region AND district = :district AND verification_status = 'VERIFIED' AND is_deleted = 0")
    suspend fun getFarmersInDistrict(region: String, district: String): List<UserEntity>
    
    @Query("SELECT * FROM users WHERE user_tier = 'ENTHUSIAST' AND verification_status = 'VERIFIED' AND is_deleted = 0")
    suspend fun getAllEnthusiasts(): List<UserEntity>
    
    @Query("SELECT * FROM users WHERE specializations LIKE '%' || :specialization || '%' AND region = :region AND is_deleted = 0")
    suspend fun getUsersBySpecialization(specialization: String, region: String): List<UserEntity>
    
    @Query("SELECT * FROM users WHERE (display_name LIKE '%' || :query || '%' OR farm_name LIKE '%' || :query || '%' OR organization LIKE '%' || :query || '%') AND region = :region AND is_deleted = 0 LIMIT :limit")
    suspend fun searchUsersInRegion(query: String, region: String, limit: Int = 50): List<UserEntity>
    
    @Query("SELECT COUNT(*) FROM users WHERE user_tier = :tier AND region = :region AND is_deleted = 0")
    suspend fun getUserCountByTierInRegion(tier: String, region: String): Int
    
    @Query("SELECT * FROM users WHERE verification_status = 'PENDING' AND user_tier IN ('FARMER', 'ENTHUSIAST') AND is_deleted = 0")
    suspend fun getPendingVerifications(): List<UserEntity>
    
    // Sync operations
    @Query("UPDATE users SET sync_status = :status, last_sync_time = :lastSyncTime WHERE id = :id")
    override suspend fun updateSyncStatus(id: String, status: SyncStatus, lastSyncTime: Date)
    
    @Query("UPDATE users SET conflict_version = :version WHERE id = :id")
    override suspend fun updateConflictVersion(id: String, version: Long)
    
    @Query("UPDATE users SET is_deleted = 1, updated_at = :deletedAt WHERE id = :id")
    override suspend fun markAsDeleted(id: String, deletedAt: Date)
    
    @Query("UPDATE users SET retry_count = retry_count + 1 WHERE id = :id")
    override suspend fun incrementRetryCount(id: String)
    
    @Query("UPDATE users SET retry_count = 0 WHERE id = :id")
    override suspend fun clearRetryCount(id: String)
    
    // Activity updates
    @Query("UPDATE users SET last_active_at = :timestamp WHERE id = :id")
    suspend fun updateLastActive(id: String, timestamp: Date = Date())
    
    @Query("UPDATE users SET last_login_at = :timestamp, login_count = login_count + 1 WHERE id = :id")
    suspend fun updateLastLogin(id: String, timestamp: Date = Date())
    
    @Query("UPDATE users SET fowl_count = :count WHERE id = :id")
    suspend fun updateFowlCount(id: String, count: Int)
    
    @Query("UPDATE users SET listing_count = :count WHERE id = :id")
    suspend fun updateListingCount(id: String, count: Int)
    
    @Query("UPDATE users SET rating = :rating, review_count = :reviewCount WHERE id = :id")
    suspend fun updateRating(id: String, rating: Double, reviewCount: Int)
    
    // Cleanup operations
    @Query("DELETE FROM users WHERE sync_status = 'SYNCED' AND updated_at < :olderThan AND sync_priority = 'LOW'")
    override suspend fun deleteOldSyncedItems(olderThan: Date): Int
    
    @Query("DELETE FROM users WHERE id IN (SELECT id FROM users WHERE sync_priority = 'LOW' AND sync_status = 'SYNCED' ORDER BY last_sync_time ASC LIMIT :limit)")
    override suspend fun deleteLowPriorityItems(limit: Int): Int
    
    @Query("SELECT SUM(data_size) FROM users")
    override suspend fun getStorageSize(): Long
    
    // Batch operations for sync
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(entity: UserEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertAll(entities: List<UserEntity>): List<Long>
    
    @Update
    override suspend fun update(entity: UserEntity): Int
    
    @Delete
    override suspend fun delete(entity: UserEntity): Int
    
    @Query("DELETE FROM users WHERE id = :id")
    override suspend fun deleteById(id: String): Int
    
    // Performance optimization queries
    @Query("SELECT id, display_name, user_tier, profile_photo, region, district, rating, verification_status FROM users WHERE region = :region AND verification_status = 'VERIFIED' AND is_deleted = 0 ORDER BY rating DESC LIMIT :limit")
    suspend fun getUserSummariesInRegion(region: String, limit: Int = 100): List<UserSummary>
    
    @Query("SELECT COUNT(*) FROM users WHERE sync_status = 'PENDING_UPLOAD' AND is_deleted = 0")
    suspend fun getPendingSyncCount(): Int
    
    @Query("SELECT COUNT(*) FROM users WHERE has_conflict = 1 AND is_deleted = 0")
    suspend fun getConflictCount(): Int
}

/**
 * Lightweight user summary for list displays
 */
data class UserSummary(
    val id: String,
    val displayName: String,
    val userTier: String,
    val profilePhoto: String?,
    val region: String,
    val district: String,
    val rating: Double,
    val verificationStatus: String
)

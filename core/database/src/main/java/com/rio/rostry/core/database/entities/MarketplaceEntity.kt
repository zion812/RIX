package com.rio.rostry.core.database.entities

import androidx.room.*
import java.util.*

/**
 * Room entity for marketplace listings with offline-first capabilities
 * Mirrors the Firestore marketplace collection structure
 */
@Entity(
    tableName = "marketplace_listings",
    indices = [
        Index(value = ["seller_id"]),
        Index(value = ["fowl_id"]),
        Index(value = ["region", "district"]),
        Index(value = ["listing_type"]),
        Index(value = ["listing_status"]),
        Index(value = ["base_price"]),
        Index(value = ["breed"]),
        Index(value = ["sync_status"]),
        Index(value = ["expires_at"]),
        Index(value = ["is_deleted"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["seller_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FowlEntity::class,
            parentColumns = ["id"],
            childColumns = ["fowl_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MarketplaceEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    override val id: String,
    
    @ColumnInfo(name = "seller_id")
    val sellerId: String,
    
    @ColumnInfo(name = "fowl_id")
    val fowlId: String,
    
    // Listing type and pricing
    @ColumnInfo(name = "listing_type")
    val listingType: String, // FIXED_PRICE, AUCTION, NEGOTIABLE, BREEDING_SERVICE
    
    @ColumnInfo(name = "base_price")
    val basePrice: Double,
    
    @ColumnInfo(name = "currency")
    val currency: String = "INR",
    
    @ColumnInfo(name = "reserve_price")
    val reservePrice: Double? = null,
    
    @ColumnInfo(name = "buy_now_price")
    val buyNowPrice: Double? = null,
    
    @ColumnInfo(name = "current_bid")
    val currentBid: Double? = null,
    
    @ColumnInfo(name = "bid_increment")
    val bidIncrement: Double? = null,
    
    @ColumnInfo(name = "breeding_fee")
    val breedingFee: Double? = null,
    
    @ColumnInfo(name = "stud_fee")
    val studFee: Double? = null,
    
    // Listing details
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "highlights")
    val highlights: List<String> = emptyList(),
    
    @ColumnInfo(name = "breed")
    val breed: String,
    
    @ColumnInfo(name = "gender")
    val gender: String,
    
    @ColumnInfo(name = "age")
    val age: String,
    
    @ColumnInfo(name = "weight")
    val weight: Double,
    
    @ColumnInfo(name = "color")
    val color: String,
    
    @ColumnInfo(name = "health_status")
    val healthStatus: String,
    
    @ColumnInfo(name = "vaccinated")
    val vaccinated: Boolean,
    
    @ColumnInfo(name = "health_certificate")
    val healthCertificate: String? = null,
    
    @ColumnInfo(name = "pedigree_available")
    val pedigreeAvailable: Boolean,
    
    @ColumnInfo(name = "registration_papers")
    val registrationPapers: Boolean,
    
    // Media
    @ColumnInfo(name = "primary_photo_url")
    val primaryPhotoUrl: String? = null,
    
    @ColumnInfo(name = "photos")
    val photos: List<String> = emptyList(),
    
    @ColumnInfo(name = "videos")
    val videos: List<String> = emptyList(),
    
    // Delivery information
    @ColumnInfo(name = "delivery_available")
    val deliveryAvailable: Boolean,
    
    @ColumnInfo(name = "delivery_radius")
    val deliveryRadius: Int = 0,
    
    @ColumnInfo(name = "delivery_cost")
    val deliveryCost: Double = 0.0,
    
    @ColumnInfo(name = "delivery_methods")
    val deliveryMethods: List<String> = emptyList(),
    
    // Auction information (if applicable)
    @ColumnInfo(name = "auction_start_time")
    val auctionStartTime: Date? = null,
    
    @ColumnInfo(name = "auction_end_time")
    val auctionEndTime: Date? = null,
    
    @ColumnInfo(name = "auto_extend")
    val autoExtend: Boolean = false,
    
    @ColumnInfo(name = "extension_time_minutes")
    val extensionTimeMinutes: Int = 0,
    
    @ColumnInfo(name = "minimum_bidders")
    val minimumBidders: Int = 0,
    
    @ColumnInfo(name = "total_bids")
    val totalBids: Int = 0,
    
    @ColumnInfo(name = "highest_bidder_id")
    val highestBidderId: String? = null,
    
    // Status and visibility
    @ColumnInfo(name = "listing_status")
    val listingStatus: String, // DRAFT, ACTIVE, PAUSED, SOLD, EXPIRED, CANCELLED
    
    @ColumnInfo(name = "visibility")
    val visibility: String = "PUBLIC", // PUBLIC, PRIVATE, TIER_RESTRICTED
    
    @ColumnInfo(name = "featured")
    val featured: Boolean = false,
    
    @ColumnInfo(name = "promoted")
    val promoted: Boolean = false,
    
    @ColumnInfo(name = "is_available")
    val isAvailable: Boolean = true,
    
    @ColumnInfo(name = "reserved_by")
    val reservedBy: String? = null,
    
    @ColumnInfo(name = "reserved_until")
    val reservedUntil: Date? = null,
    
    @ColumnInfo(name = "sold_to")
    val soldTo: String? = null,
    
    @ColumnInfo(name = "sold_at")
    val soldAt: Date? = null,
    
    // Engagement metrics
    @ColumnInfo(name = "views")
    val views: Int = 0,
    
    @ColumnInfo(name = "unique_views")
    val uniqueViews: Int = 0,
    
    @ColumnInfo(name = "favorites")
    val favorites: Int = 0,
    
    @ColumnInfo(name = "shares")
    val shares: Int = 0,
    
    @ColumnInfo(name = "inquiries")
    val inquiries: Int = 0,
    
    @ColumnInfo(name = "contact_clicks")
    val contactClicks: Int = 0,
    
    // Search and categorization
    @ColumnInfo(name = "keywords")
    val keywords: List<String> = emptyList(),
    
    @ColumnInfo(name = "tags")
    val tags: List<String> = emptyList(),
    
    @ColumnInfo(name = "category")
    val category: String,
    
    @ColumnInfo(name = "subcategory")
    val subcategory: String? = null,
    
    // Timeline
    @ColumnInfo(name = "published_at")
    val publishedAt: Date? = null,
    
    @ColumnInfo(name = "expires_at")
    val expiresAt: Date? = null,
    
    @ColumnInfo(name = "renewal_count")
    val renewalCount: Int = 0,
    
    @ColumnInfo(name = "last_renewed_at")
    val lastRenewedAt: Date? = null,
    
    @ColumnInfo(name = "auto_renew")
    val autoRenew: Boolean = false,
    
    // Seller preferences
    @ColumnInfo(name = "accept_offers")
    val acceptOffers: Boolean = true,
    
    @ColumnInfo(name = "minimum_offer")
    val minimumOffer: Double? = null,
    
    @ColumnInfo(name = "preferred_buyers")
    val preferredBuyers: List<String> = emptyList(),
    
    @ColumnInfo(name = "blacklisted_buyers")
    val blacklistedBuyers: List<String> = emptyList(),
    
    @ColumnInfo(name = "allow_direct_messages")
    val allowDirectMessages: Boolean = true,
    
    @ColumnInfo(name = "allow_phone_calls")
    val allowPhoneCalls: Boolean = false,
    
    @ColumnInfo(name = "preferred_contact_method")
    val preferredContactMethod: String = "MESSAGE",
    
    @ColumnInfo(name = "response_time")
    val responseTime: String = "Within 24 hours",
    
    // Moderation
    @ColumnInfo(name = "moderation_status")
    val moderationStatus: String = "PENDING", // PENDING, APPROVED, REJECTED, FLAGGED
    
    @ColumnInfo(name = "moderated_by")
    val moderatedBy: String? = null,
    
    @ColumnInfo(name = "moderated_at")
    val moderatedAt: Date? = null,
    
    @ColumnInfo(name = "listing_quality")
    val listingQuality: String = "MEDIUM", // HIGH, MEDIUM, LOW
    
    // Regional metadata
    @Embedded
    val regionalMetadata: RegionalMetadata,
    
    // Sync metadata
    @Embedded
    val syncMetadata: SyncMetadata,
    
    // Conflict metadata
    @Embedded
    val conflictMetadata: ConflictMetadata = ConflictMetadata()
) : SyncableEntity {
    
    override val lastSyncTime: Date?
        get() = syncMetadata.lastSyncTime
    
    override val syncStatus: SyncStatus
        get() = syncMetadata.syncStatus
    
    override val conflictVersion: Long
        get() = syncMetadata.conflictVersion
    
    override val isDeleted: Boolean
        get() = syncMetadata.isDeleted
    
    override val createdAt: Date
        get() = syncMetadata.createdAt
    
    override val updatedAt: Date
        get() = syncMetadata.updatedAt
}

/**
 * DAO for marketplace entities with offline-optimized queries
 */
@Dao
interface MarketplaceDao : BaseSyncableDao<MarketplaceEntity> {
    
    @Query("SELECT * FROM marketplace_listings WHERE id = :id AND is_deleted = 0")
    override suspend fun getById(id: String): MarketplaceEntity?
    
    @Query("SELECT * FROM marketplace_listings WHERE sync_status = :status AND is_deleted = 0")
    override suspend fun getAllByStatus(status: SyncStatus): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE sync_priority = :priority AND is_deleted = 0")
    override suspend fun getAllByPriority(priority: SyncPriority): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE sync_status IN ('PENDING_UPLOAD', 'FAILED') AND is_deleted = 0 ORDER BY sync_priority ASC, created_at ASC")
    override suspend fun getAllPendingSync(): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE has_conflict = 1 AND is_deleted = 0")
    override suspend fun getAllConflicted(): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE region = :region AND district = :district AND is_deleted = 0")
    override suspend fun getAllInRegion(region: String, district: String): List<MarketplaceEntity>
    
    // Marketplace-specific queries
    @Query("SELECT * FROM marketplace_listings WHERE seller_id = :sellerId AND is_deleted = 0 ORDER BY created_at DESC")
    suspend fun getListingsBySeller(sellerId: String): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE region = :region AND listing_status = 'ACTIVE' AND expires_at > :currentTime AND is_deleted = 0 ORDER BY created_at DESC LIMIT :limit")
    suspend fun getActiveListingsInRegion(region: String, currentTime: Date = Date(), limit: Int = 50): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE region = :region AND district = :district AND listing_status = 'ACTIVE' AND expires_at > :currentTime AND is_deleted = 0 ORDER BY created_at DESC LIMIT :limit")
    suspend fun getActiveListingsInDistrict(region: String, district: String, currentTime: Date = Date(), limit: Int = 50): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE breed = :breed AND region = :region AND listing_status = 'ACTIVE' AND expires_at > :currentTime AND is_deleted = 0 LIMIT :limit")
    suspend fun getListingsByBreedInRegion(breed: String, region: String, currentTime: Date = Date(), limit: Int = 50): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE base_price BETWEEN :minPrice AND :maxPrice AND region = :region AND listing_status = 'ACTIVE' AND expires_at > :currentTime AND is_deleted = 0 ORDER BY base_price ASC LIMIT :limit")
    suspend fun getListingsByPriceRange(minPrice: Double, maxPrice: Double, region: String, currentTime: Date = Date(), limit: Int = 50): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE listing_type = 'AUCTION' AND auction_end_time > :currentTime AND listing_status = 'ACTIVE' AND region = :region AND is_deleted = 0 ORDER BY auction_end_time ASC")
    suspend fun getActiveAuctionsInRegion(region: String, currentTime: Date = Date()): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE listing_type = 'AUCTION' AND auction_end_time BETWEEN :currentTime AND :endingSoon AND listing_status = 'ACTIVE' AND is_deleted = 0 ORDER BY auction_end_time ASC")
    suspend fun getEndingSoonAuctions(currentTime: Date = Date(), endingSoon: Date): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR breed LIKE '%' || :query || '%' OR keywords LIKE '%' || :query || '%') AND region = :region AND listing_status = 'ACTIVE' AND expires_at > :currentTime AND is_deleted = 0 LIMIT :limit")
    suspend fun searchListingsInRegion(query: String, region: String, currentTime: Date = Date(), limit: Int = 50): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE expires_at < :currentTime AND listing_status = 'ACTIVE' AND is_deleted = 0")
    suspend fun getExpiredListings(currentTime: Date = Date()): List<MarketplaceEntity>
    
    @Query("SELECT COUNT(*) FROM marketplace_listings WHERE seller_id = :sellerId AND listing_status = 'ACTIVE' AND is_deleted = 0")
    suspend fun getActiveListingCountBySeller(sellerId: String): Int
    
    // Engagement updates
    @Query("UPDATE marketplace_listings SET views = views + 1 WHERE id = :id")
    suspend fun incrementViews(id: String)
    
    @Query("UPDATE marketplace_listings SET unique_views = unique_views + 1 WHERE id = :id")
    suspend fun incrementUniqueViews(id: String)
    
    @Query("UPDATE marketplace_listings SET favorites = favorites + 1 WHERE id = :id")
    suspend fun incrementFavorites(id: String)
    
    @Query("UPDATE marketplace_listings SET inquiries = inquiries + 1 WHERE id = :id")
    suspend fun incrementInquiries(id: String)
    
    @Query("UPDATE marketplace_listings SET contact_clicks = contact_clicks + 1 WHERE id = :id")
    suspend fun incrementContactClicks(id: String)
    
    // Auction updates
    @Query("UPDATE marketplace_listings SET current_bid = :bidAmount, highest_bidder_id = :bidderId, total_bids = total_bids + 1 WHERE id = :id")
    suspend fun updateHighestBid(id: String, bidAmount: Double, bidderId: String)
    
    @Query("UPDATE marketplace_listings SET listing_status = 'EXPIRED' WHERE expires_at < :currentTime AND listing_status = 'ACTIVE'")
    suspend fun markExpiredListings(currentTime: Date = Date()): Int
    
    // Sync operations
    @Query("UPDATE marketplace_listings SET sync_status = :status, last_sync_time = :lastSyncTime WHERE id = :id")
    override suspend fun updateSyncStatus(id: String, status: SyncStatus, lastSyncTime: Date)
    
    @Query("UPDATE marketplace_listings SET conflict_version = :version WHERE id = :id")
    override suspend fun updateConflictVersion(id: String, version: Long)
    
    @Query("UPDATE marketplace_listings SET is_deleted = 1, updated_at = :deletedAt WHERE id = :id")
    override suspend fun markAsDeleted(id: String)
    
    @Query("UPDATE marketplace_listings SET retry_count = retry_count + 1 WHERE id = :id")
    override suspend fun incrementRetryCount(id: String)
    
    @Query("UPDATE marketplace_listings SET retry_count = 0 WHERE id = :id")
    override suspend fun clearRetryCount(id: String)
    
    // Cleanup operations
    @Query("DELETE FROM marketplace_listings WHERE sync_status = 'SYNCED' AND updated_at < :olderThan AND sync_priority = 'LOW'")
    override suspend fun deleteOldSyncedItems(olderThan: Date): Int
    
    @Query("DELETE FROM marketplace_listings WHERE sync_priority = 'LOW' AND sync_status = 'SYNCED' ORDER BY last_sync_time ASC LIMIT :limit")
    override suspend fun deleteLowPriorityItems(limit: Int): Int
    
    @Query("SELECT SUM(data_size) FROM marketplace_listings")
    override suspend fun getStorageSize(): Long
    
    // Batch operations for sync
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(entity: MarketplaceEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertAll(entities: List<MarketplaceEntity>): List<Long>
    
    @Update
    override suspend fun update(entity: MarketplaceEntity): Int
    
    @Delete
    override suspend fun delete(entity: MarketplaceEntity): Int
    
    @Query("DELETE FROM marketplace_listings WHERE id = :id")
    override suspend fun deleteById(id: String): Int
    
    // Performance optimization queries
    @Query("SELECT id, title, base_price, breed, primary_photo_url, region, district, listing_status, seller_id FROM marketplace_listings WHERE region = :region AND listing_status = 'ACTIVE' AND expires_at > :currentTime AND is_deleted = 0 ORDER BY created_at DESC LIMIT :limit")
    suspend fun getListingSummariesInRegion(region: String, currentTime: Date = Date(), limit: Int = 100): List<MarketplaceSummary>
    
    @Query("SELECT COUNT(*) FROM marketplace_listings WHERE sync_status = 'PENDING_UPLOAD' AND is_deleted = 0")
    suspend fun getPendingSyncCount(): Int
    
    @Query("SELECT COUNT(*) FROM marketplace_listings WHERE has_conflict = 1 AND is_deleted = 0")
    suspend fun getConflictCount(): Int
}

/**
 * Lightweight marketplace summary for list displays
 */
data class MarketplaceSummary(
    val id: String,
    val title: String,
    val basePrice: Double,
    val breed: String,
    val primaryPhotoUrl: String?,
    val region: String,
    val district: String,
    val listingStatus: String,
    val sellerId: String
)

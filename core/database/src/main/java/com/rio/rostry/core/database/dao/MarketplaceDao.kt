package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.MarketplaceEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Data Access Object for marketplace operations with optimized queries for filters
 */
@Dao
interface MarketplaceDao : BaseSyncableDao<MarketplaceEntity> {
    
    @Query("SELECT * FROM marketplace_listings WHERE id = :listingId AND is_deleted = 0")
    suspend fun getListingById(listingId: String): MarketplaceEntity?
    
    @Query("SELECT * FROM marketplace_listings WHERE id = :listingId AND is_deleted = 0")
    fun observeListingById(listingId: String): Flow<MarketplaceEntity?>
    
    @Query("SELECT * FROM marketplace_listings WHERE seller_id = :sellerId AND is_deleted = 0 ORDER BY created_at DESC")
    fun getListingBySeller(sellerId: String): Flow<List<MarketplaceEntity>>
    
    @Query("SELECT * FROM marketplace_listings WHERE fowl_id = :fowlId AND is_deleted = 0")
    suspend fun getListingByFowl(fowlId: String): MarketplaceEntity?
    
    @Query("SELECT * FROM marketplace_listings WHERE listing_status = 'ACTIVE' AND is_deleted = 0 ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getActiveListings(limit: Int, offset: Int): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE listing_status = 'ACTIVE' AND breed = :breed AND is_deleted = 0 ORDER BY created_at DESC LIMIT :limit")
    suspend fun getActiveListingsByBreed(breed: String, limit: Int = 50): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE listing_status = 'ACTIVE' AND category = :category AND is_deleted = 0 ORDER BY created_at DESC LIMIT :limit")
    suspend fun getActiveListingsByCategory(category: String, limit: Int = 50): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE listing_status = 'ACTIVE' AND region = :region AND district = :district AND is_deleted = 0 ORDER BY created_at DESC LIMIT :limit")
    suspend fun getActiveListingsByRegion(region: String, district: String, limit: Int = 50): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE listing_status = 'ACTIVE' AND base_price BETWEEN :minPrice AND :maxPrice AND is_deleted = 0 ORDER BY created_at DESC LIMIT :limit")
    suspend fun getActiveListingsByPriceRange(minPrice: Double, maxPrice: Double, limit: Int = 50): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE listing_status = 'ACTIVE' AND is_deleted = 0 AND (tags LIKE '%' || :tag || '%' OR keywords LIKE '%' || :keyword || '%') ORDER BY created_at DESC LIMIT :limit")
    suspend fun searchListingsByTagOrKeyword(tag: String, keyword: String, limit: Int = 50): List<MarketplaceEntity>
    
    /**
     * Search marketplace listings with advanced filters
     */
    @Query("""
        SELECT * FROM marketplace_listings 
        WHERE is_deleted = 0
        AND (:query IS NULL OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        AND (:minPrice IS NULL OR price_in_coins >= :minPrice)
        AND (:maxPrice IS NULL OR price_in_coins <= :maxPrice)
        AND (:breed IS NULL OR breed_primary = :breed)
        AND (:gender IS NULL OR gender = :gender)
        AND (:ageWeeksMin IS NULL OR age_weeks >= :ageWeeksMin)
        AND (:ageWeeksMax IS NULL OR age_weeks <= :ageWeeksMax)
        AND (:location IS NULL OR location LIKE '%' || :location || '%')
        AND (:availabilityOnly = 0 OR availability_status = 'AVAILABLE')
        ORDER BY 
            CASE 
                WHEN :sortBy = 'NEWEST' THEN created_at DESC
                WHEN :sortBy = 'OLDEST' THEN created_at ASC
                WHEN :sortBy = 'PRICE_LOW_TO_HIGH' THEN price_in_coins ASC
                WHEN :sortBy = 'PRICE_HIGH_TO_LOW' THEN price_in_coins DESC
                WHEN :sortBy = 'AGE_YOUNG_TO_OLD' THEN age_weeks ASC
                WHEN :sortBy = 'AGE_OLD_TO_YOUNG' THEN age_weeks DESC
                ELSE created_at DESC
            END
        LIMIT :limit
    """)
    suspend fun searchListings(
        query: String?,
        minPrice: Double?,
        maxPrice: Double?,
        breed: String?,
        gender: String?,
        ageWeeksMin: Int?,
        ageWeeksMax: Int?,
        location: String?,
        sortBy: String,
        availabilityOnly: Boolean,
        limit: Int = 50
    ): List<MarketplaceEntity>
    
    /**
     * Get distinct breeds available in the marketplace
     */
    @Query("SELECT DISTINCT breed_primary FROM marketplace_listings WHERE is_deleted = 0 AND availability_status = 'AVAILABLE'")
    suspend fun getAvailableBreeds(): List<String>
    
    /**
     * Get distinct locations available in the marketplace
     */
    @Query("SELECT DISTINCT location FROM marketplace_listings WHERE is_deleted = 0 AND availability_status = 'AVAILABLE'")
    suspend fun getAvailableLocations(): List<String>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: MarketplaceEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<MarketplaceEntity>)
    
    @Update
    suspend fun updateListing(listing: MarketplaceEntity)
    
    @Query("UPDATE marketplace_listings SET listing_status = :status, sold_to = :soldTo, sold_at = :soldAt, updated_at = :updatedAt WHERE id = :listingId")
    suspend fun updateSaleStatus(listingId: String, status: String, soldTo: String?, soldAt: Date?, updatedAt: Date = Date())
    
    @Delete
    suspend fun deleteListing(listing: MarketplaceEntity)
    
    @Query("UPDATE marketplace_listings SET is_deleted = 1, updated_at = :deletedAt WHERE id = :listingId")
    suspend fun markAsDeleted(listingId: String, deletedAt: Date = Date())
    
    // Sync operations
    @Query("SELECT * FROM marketplace_listings WHERE sync_status = 'PENDING_UPLOAD' AND is_deleted = 0")
    suspend fun getUnsyncedListings(): List<MarketplaceEntity>
    
    @Query("UPDATE marketplace_listings SET sync_status = 'SYNCED' WHERE id = :listingId")
    suspend fun markListingAsSynced(listingId: String)
}
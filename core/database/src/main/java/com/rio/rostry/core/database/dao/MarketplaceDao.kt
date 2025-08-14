package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.MarketplaceEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Data Access Object for marketplace-related operations
 */
@Dao
interface MarketplaceDao {
    
    @Query("SELECT * FROM marketplace_listings WHERE id = :listingId")
    suspend fun getListingById(listingId: String): MarketplaceEntity?
    
    @Query("SELECT * FROM marketplace_listings WHERE id = :listingId")
    fun observeListing(listingId: String): Flow<MarketplaceEntity?>
    
    @Query("SELECT * FROM marketplace_listings WHERE isActive = 1 ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    fun getActiveListings(limit: Int = 20, offset: Int = 0): Flow<List<MarketplaceEntity>>
    
    @Query("SELECT * FROM marketplace_listings WHERE sellerId = :sellerId ORDER BY createdAt DESC")
    fun getListingsBySeller(sellerId: String): Flow<List<MarketplaceEntity>>
    
    @Query("SELECT * FROM marketplace_listings WHERE category = :category AND isActive = 1 ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getListingsByCategory(category: String, limit: Int = 50): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE isFeatured = 1 AND isActive = 1 ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getFeaturedListings(limit: Int = 10): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE isPremium = 1 AND isActive = 1 ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getPremiumListings(limit: Int = 20): List<MarketplaceEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE priceInCoins BETWEEN :minPrice AND :maxPrice AND isActive = 1 ORDER BY priceInCoins ASC LIMIT :limit")
    suspend fun getListingsByPriceRange(minPrice: Int, maxPrice: Int, limit: Int = 50): List<MarketplaceEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: MarketplaceEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<MarketplaceEntity>)
    
    @Update
    suspend fun updateListing(listing: MarketplaceEntity)
    
    @Query("UPDATE marketplace_listings SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :listingId")
    suspend fun updateListingStatus(listingId: String, isActive: Boolean, updatedAt: Date = Date())
    
    @Query("UPDATE marketplace_listings SET viewCount = viewCount + 1, updatedAt = :updatedAt WHERE id = :listingId")
    suspend fun incrementViewCount(listingId: String, updatedAt: Date = Date())
    
    @Query("UPDATE marketplace_listings SET favoriteCount = favoriteCount + :increment, updatedAt = :updatedAt WHERE id = :listingId")
    suspend fun updateFavoriteCount(listingId: String, increment: Int, updatedAt: Date = Date())
    
    @Query("UPDATE marketplace_listings SET inquiryCount = inquiryCount + 1, updatedAt = :updatedAt WHERE id = :listingId")
    suspend fun incrementInquiryCount(listingId: String, updatedAt: Date = Date())
    
    @Query("UPDATE marketplace_listings SET soldAt = :soldAt, buyerId = :buyerId, isActive = 0, updatedAt = :updatedAt WHERE id = :listingId")
    suspend fun markAsSold(listingId: String, buyerId: String, soldAt: Date = Date(), updatedAt: Date = Date())
    
    @Delete
    suspend fun deleteListing(listing: MarketplaceEntity)
    
    @Query("DELETE FROM marketplace_listings WHERE id = :listingId")
    suspend fun deleteListingById(listingId: String)
    
    // Sync operations
    @Query("SELECT * FROM marketplace_listings WHERE isSynced = 0")
    suspend fun getUnsyncedListings(): List<MarketplaceEntity>
    
    @Query("UPDATE marketplace_listings SET isSynced = 1 WHERE id = :listingId")
    suspend fun markListingAsSynced(listingId: String)
    
    // Search operations
    @Query("SELECT * FROM marketplace_listings WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') AND isActive = 1 ORDER BY createdAt DESC LIMIT :limit")
    suspend fun searchListings(query: String, limit: Int = 50): List<MarketplaceListingEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE location LIKE '%' || :location || '%' AND isActive = 1 ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getListingsByLocation(location: String, limit: Int = 50): List<MarketplaceListingEntity>
    
    @Query("SELECT * FROM marketplace_listings WHERE tags LIKE '%' || :tag || '%' AND isActive = 1 ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getListingsByTag(tag: String, limit: Int = 50): List<MarketplaceListingEntity>
    
    // Analytics
    @Query("SELECT COUNT(*) FROM marketplace_listings WHERE sellerId = :sellerId")
    suspend fun getListingCountBySeller(sellerId: String): Int
    
    @Query("SELECT COUNT(*) FROM marketplace_listings WHERE sellerId = :sellerId AND isActive = 1")
    suspend fun getActiveListingCountBySeller(sellerId: String): Int
    
    @Query("SELECT COUNT(*) FROM marketplace_listings WHERE sellerId = :sellerId AND soldAt IS NOT NULL")
    suspend fun getSoldListingCountBySeller(sellerId: String): Int
    
    @Query("SELECT AVG(priceInCoins) FROM marketplace_listings WHERE category = :category AND isActive = 1")
    suspend fun getAveragePriceByCategory(category: String): Double?
    
    @Query("SELECT category, COUNT(*) as count FROM marketplace_listings WHERE isActive = 1 GROUP BY category ORDER BY count DESC")
    suspend fun getCategoryDistribution(): List<CategoryCount>
    
    // Expired listings cleanup
    @Query("UPDATE marketplace_listings SET isActive = 0 WHERE expiresAt < :currentTime AND isActive = 1")
    suspend fun deactivateExpiredListings(currentTime: Date = Date())
    
    @Query("SELECT * FROM marketplace_listings WHERE expiresAt < :currentTime AND isActive = 1")
    suspend fun getExpiredListings(currentTime: Date = Date()): List<MarketplaceListingEntity>
}

data class CategoryCount(
    val category: String,
    val count: Int
)
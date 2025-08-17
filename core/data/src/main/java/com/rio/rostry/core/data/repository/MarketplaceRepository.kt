package com.rio.rostry.core.data.repository

import com.rio.rostry.core.database.dao.MarketplaceDao
import com.rio.rostry.core.database.entities.MarketplaceEntity
import com.rio.rostry.core.data.model.MarketplaceListing
import com.rio.rostry.core.data.util.DataSyncManager
import com.rio.rostry.core.data.util.SyncOperation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository for managing marketplace listings with offline-first capabilities
 */
class MarketplaceRepository @Inject constructor(
    private val marketplaceDao: MarketplaceDao,
    private val syncManager: DataSyncManager
) {
    
    /**
     * Create a new marketplace listing
     */
    suspend fun createListing(listing: MarketplaceListing): Result<String> {
        return try {
            val listingEntity = listing.toEntity()
            marketplaceDao.insertListing(listingEntity)
            
            // Queue for sync
            syncManager.queueSyncOperation(
                SyncOperation.Create(
                    collection = "marketplace",
                    documentId = listingEntity.id,
                    data = listingEntity.toMap()
                )
            )
            
            Result.success(listingEntity.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a listing by ID
     */
    suspend fun getListing(listingId: String): Result<MarketplaceListing> {
        return try {
            val listingEntity = marketplaceDao.getListingById(listingId)
            if (listingEntity != null) {
                Result.success(listingEntity.toModel())
            } else {
                Result.failure(IllegalStateException("Listing not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all active listings
     */
    fun getActiveListings(): Flow<List<MarketplaceListing>> {
        return marketplaceDao.getActiveListings()
            .map { entities ->
                entities.map { it.toModel() }
            }
    }
    
    /**
     * Get listings by seller
     */
    fun getListingsBySeller(sellerId: String): Flow<List<MarketplaceListing>> {
        return marketplaceDao.getListingsBySeller(sellerId)
            .map { entities ->
                entities.map { it.toModel() }
            }
    }
    
    /**
     * Update a listing
     */
    suspend fun updateListing(listing: MarketplaceListing): Result<Unit> {
        return try {
            val listingEntity = listing.toEntity()
            marketplaceDao.updateListing(listingEntity)
            
            // Queue for sync
            syncManager.queueSyncOperation(
                SyncOperation.Update(
                    collection = "marketplace",
                    documentId = listingEntity.id,
                    data = listingEntity.toMap()
                )
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a listing
     */
    suspend fun deleteListing(listingId: String): Result<Unit> {
        return try {
            marketplaceDao.deleteListing(listingId)
            
            // Queue for sync
            syncManager.queueSyncOperation(
                SyncOperation.Delete(
                    collection = "marketplace",
                    documentId = listingId
                )
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search marketplace listings with advanced filters
     */
    suspend fun searchListings(filter: MarketplaceFilter, limit: Int = 50): Result<List<MarketplaceEntity>> = 
        withContext(Dispatchers.IO) {
            return@withContext try {
                val listings = marketplaceDao.searchListings(
                    query = filter.query,
                    minPrice = filter.minPrice,
                    maxPrice = filter.maxPrice,
                    breed = filter.breed,
                    gender = filter.gender,
                    ageWeeksMin = filter.ageWeeksMin,
                    ageWeeksMax = filter.ageWeeksMax,
                    location = filter.location,
                    sortBy = filter.sortBy.name,
                    availabilityOnly = filter.availability == MarketplaceFilter.AvailabilityFilter.AVAILABLE_ONLY,
                    limit = limit
                )
                Result.Success(listings)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    
    /**
     * Get distinct breeds available in the marketplace
     */
    suspend fun getAvailableBreeds(): Result<List<String>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val breeds = marketplaceDao.getAvailableBreeds()
            Result.Success(breeds)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Get distinct locations available in the marketplace
     */
    suspend fun getAvailableLocations(): Result<List<String>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val locations = marketplaceDao.getAvailableLocations()
            Result.Success(locations)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Extension function to convert MarketplaceListing model to MarketplaceEntity
     */
    private fun MarketplaceListing.toEntity(): MarketplaceEntity {
        return MarketplaceEntity(
            id = this.id,
            fowlId = this.fowlId,
            sellerId = this.sellerId,
            purpose = this.purpose,
            priceCents = this.priceCents,
            status = this.status,
            region = this.location?.get("region") as? String ?: "",
            district = this.location?.get("district") as? String ?: "",
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            syncStatus = "pending",
            syncPriority = 1,
            isDeleted = false
        )
    }
    
    /**
     * Extension function to convert MarketplaceEntity to MarketplaceListing model
     */
    private fun MarketplaceEntity.toModel(): MarketplaceListing {
        return MarketplaceListing(
            id = this.id,
            fowlId = this.fowlId,
            sellerId = this.sellerId,
            purpose = this.purpose,
            priceCents = this.priceCents,
            status = this.status,
            location = mapOf(
                "region" to this.region,
                "district" to this.district
            ),
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
    
    /**
     * Extension function to convert MarketplaceEntity to Map for syncing
     */
    private fun MarketplaceEntity.toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "fowl_id" to fowlId,
            "seller_id" to sellerId,
            "purpose" to purpose,
            "price_cents" to priceCents,
            "status" to status,
            "region" to region,
            "district" to district,
            "created_at" to createdAt,
            "updated_at" to updatedAt,
            "sync_status" to syncStatus,
            "sync_priority" to syncPriority,
            "is_deleted" to isDeleted
        )
    }
}
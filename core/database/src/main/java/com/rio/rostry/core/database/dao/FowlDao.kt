package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.FowlEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Data Access Object for fowl-related operations
 */
@Dao
interface FowlDaoV2 {
    
    @Query("SELECT * FROM fowls WHERE id = :fowlId")
    suspend fun getFowlById(fowlId: String): FowlEntity?
    
    @Query("SELECT * FROM fowls WHERE id = :fowlId")
    fun observeFowl(fowlId: String): Flow<FowlEntity?>
    
    @Query("SELECT * FROM fowls WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getFowlsByOwner(ownerId: String): Flow<List<FowlEntity>>
    
    @Query("SELECT * FROM fowls WHERE ownerId = :ownerId AND isForSale = 1 ORDER BY createdAt DESC")
    suspend fun getFowlsForSaleByOwner(ownerId: String): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE breed = :breed ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getFowlsByBreed(breed: String, limit: Int = 50): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE parentMaleId = :parentId OR parentFemaleId = :parentId")
    suspend fun getOffspring(parentId: String): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE id = :maleId OR id = :femaleId")
    suspend fun getParents(maleId: String?, femaleId: String?): List<FowlEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFowl(fowl: FowlEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFowls(fowls: List<FowlEntity>)
    
    @Update
    suspend fun updateFowl(fowl: FowlEntity)
    
    @Query("UPDATE fowls SET isForSale = :isForSale, priceInCoins = :priceInCoins, updatedAt = :updatedAt WHERE id = :fowlId")
    suspend fun updateSaleStatus(fowlId: String, isForSale: Boolean, priceInCoins: Int?, updatedAt: Date = Date())
    
    @Query("UPDATE fowls SET ownerId = :newOwnerId, updatedAt = :updatedAt WHERE id = :fowlId")
    suspend fun transferOwnership(fowlId: String, newOwnerId: String, updatedAt: Date = Date())
    
    @Delete
    suspend fun deleteFowl(fowl: FowlEntity)
    
    @Query("DELETE FROM fowls WHERE id = :fowlId")
    suspend fun deleteFowlById(fowlId: String)
    
    // Sync operations
    @Query("SELECT * FROM fowls WHERE isSynced = 0")
    suspend fun getUnsyncedFowls(): List<FowlEntity>
    
    @Query("UPDATE fowls SET isSynced = 1 WHERE id = :fowlId")
    suspend fun markFowlAsSynced(fowlId: String)
    
    // Search operations
    @Query("SELECT * FROM fowls WHERE name LIKE '%' || :query || '%' OR breed LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY createdAt DESC LIMIT :limit")
    suspend fun searchFowls(query: String, limit: Int = 50): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE location LIKE '%' || :location || '%' ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getFowlsByLocation(location: String, limit: Int = 50): List<FowlEntity>
    
    // Analytics
    @Query("SELECT COUNT(*) FROM fowls WHERE ownerId = :ownerId")
    suspend fun getFowlCountByOwner(ownerId: String): Int
    
    @Query("SELECT COUNT(*) FROM fowls WHERE ownerId = :ownerId AND isForSale = 1")
    suspend fun getForSaleCountByOwner(ownerId: String): Int
    
    @Query("SELECT breed, COUNT(*) as count FROM fowls WHERE ownerId = :ownerId GROUP BY breed ORDER BY count DESC")
    suspend fun getBreedDistributionByOwner(ownerId: String): List<BreedCount>
}

data class BreedCount(
    val breed: String,
    val count: Int
)
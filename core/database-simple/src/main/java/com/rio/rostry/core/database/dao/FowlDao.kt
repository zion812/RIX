package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.FowlEntity
import kotlinx.coroutines.flow.Flow

/**
 * Simplified Fowl DAO for Phase 2
 */
@Dao
interface FowlDao {
    
    @Query("SELECT * FROM fowls WHERE id = :fowlId")
    suspend fun getFowlById(fowlId: String): FowlEntity?
    
    @Query("SELECT * FROM fowls WHERE id = :fowlId")
    fun getFowlByIdFlow(fowlId: String): Flow<FowlEntity?>
    
    @Query("SELECT * FROM fowls WHERE ownerId = :ownerId AND status = 'ACTIVE' ORDER BY createdAt DESC")
    suspend fun getFowlsByOwner(ownerId: String): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE ownerId = :ownerId AND status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getFowlsByOwnerFlow(ownerId: String): Flow<List<FowlEntity>>
    
    @Query("SELECT * FROM fowls WHERE breed = :breed AND status = 'ACTIVE' ORDER BY createdAt DESC")
    suspend fun getFowlsByBreed(breed: String): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE region = :region AND district = :district AND status = 'ACTIVE' ORDER BY createdAt DESC")
    suspend fun getFowlsByLocation(region: String, district: String): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE isForSale = 1 AND status = 'ACTIVE' ORDER BY createdAt DESC")
    suspend fun getFowlsForSale(): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE isForSale = 1 AND status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getFowlsForSaleFlow(): Flow<List<FowlEntity>>
    
    @Query("SELECT * FROM fowls WHERE isForSale = 1 AND region = :region AND status = 'ACTIVE' ORDER BY createdAt DESC")
    suspend fun getFowlsForSaleByRegion(region: String): List<FowlEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFowl(fowl: FowlEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFowls(fowls: List<FowlEntity>)
    
    @Update
    suspend fun updateFowl(fowl: FowlEntity)
    
    @Delete
    suspend fun deleteFowl(fowl: FowlEntity)
    
    @Query("DELETE FROM fowls WHERE id = :fowlId")
    suspend fun deleteFowlById(fowlId: String)
    
    @Query("UPDATE fowls SET status = :status WHERE id = :fowlId")
    suspend fun updateFowlStatus(fowlId: String, status: String)
    
    @Query("UPDATE fowls SET isForSale = :isForSale, price = :price WHERE id = :fowlId")
    suspend fun updateFowlSaleStatus(fowlId: String, isForSale: Boolean, price: Double?)
    
    @Query("SELECT COUNT(*) FROM fowls WHERE ownerId = :ownerId AND status = 'ACTIVE'")
    suspend fun getFowlCountByOwner(ownerId: String): Int
    
    @Query("SELECT COUNT(*) FROM fowls WHERE region = :region AND status = 'ACTIVE'")
    suspend fun getFowlCountByRegion(region: String): Int
    
    @Query("SELECT DISTINCT breed FROM fowls WHERE status = 'ACTIVE' ORDER BY breed")
    suspend fun getAllBreeds(): List<String>
}

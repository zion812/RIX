package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.BreedingRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for breeding record operations
 */
@Dao
interface BreedingDao {
    
    @Query("SELECT * FROM breeding_records WHERE id = :breedingId")
    suspend fun getById(breedingId: String): BreedingRecordEntity?
    
    @Query("SELECT * FROM breeding_records WHERE breeder_id = :breederId ORDER BY breeding_date DESC")
    suspend fun getBreedingRecordsByBreeder(breederId: String): List<BreedingRecordEntity>
    
    @Query("SELECT * FROM breeding_records WHERE breeder_id = :breederId ORDER BY breeding_date DESC")
    fun observeBreedingRecordsByBreeder(breederId: String): Flow<List<BreedingRecordEntity>>
    
    @Query("SELECT * FROM breeding_records WHERE male_id = :fowlId OR female_id = :fowlId ORDER BY breeding_date DESC")
    suspend fun getBreedingRecordsByParent(fowlId: String): List<BreedingRecordEntity>
    
    @Query("SELECT * FROM breeding_records WHERE status = :status ORDER BY breeding_date DESC")
    suspend fun getBreedingRecordsByStatus(status: String): List<BreedingRecordEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(breedingRecord: BreedingRecordEntity)
    
    @Update
    suspend fun update(breedingRecord: BreedingRecordEntity)
    
    @Delete
    suspend fun delete(breedingRecord: BreedingRecordEntity)
    
    @Query("UPDATE breeding_records SET status = :status, updated_at = :updatedAt WHERE id = :breedingId")
    suspend fun updateStatus(breedingId: String, status: String, updatedAt: java.util.Date)
    
    @Query("SELECT * FROM breeding_records WHERE is_synced = 0 ORDER BY created_at ASC")
    suspend fun getUnsyncedRecords(): List<BreedingRecordEntity>
    
    @Query("UPDATE breeding_records SET is_synced = 1 WHERE id = :breedingId")
    suspend fun markAsSynced(breedingId: String)
}

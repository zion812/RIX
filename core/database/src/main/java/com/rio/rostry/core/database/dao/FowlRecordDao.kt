package com.rio.rostry.core.database.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.rio.rostry.core.database.entities.FowlRecordEntity
import com.rio.rostry.core.database.entities.FowlRecordListItem
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Fowl Records (timeline entries)
 * Provides CRUD operations and queries for fowl timeline records
 */
@Dao
interface FowlRecordDao : BaseSyncableDao<FowlRecordEntity> {
    
    /**
     * Get all records for a specific fowl ordered by date (newest first)
     */
    @Query("SELECT * FROM fowl_records WHERE fowl_id = :fowlId AND is_deleted = 0 ORDER BY record_date DESC")
    fun getRecordsByFowlId(fowlId: String): Flow<List<FowlRecordEntity>>
    
    /**
     * Get all records for a specific fowl ordered by date (newest first) - suspend version
     */
    @Query("SELECT * FROM fowl_records WHERE fowl_id = :fowlId AND is_deleted = 0 ORDER BY record_date DESC")
    suspend fun getRecordsByFowlIdSuspend(fowlId: String): List<FowlRecordEntity>
    
    /**
     * Get a specific record by ID
     */
    @Query("SELECT * FROM fowl_records WHERE id = :id AND is_deleted = 0")
    suspend fun getRecordById(id: String): FowlRecordEntity?
    
    /**
     * Get records by type for a specific fowl
     */
    @Query("SELECT * FROM fowl_records WHERE fowl_id = :fowlId AND record_type = :recordType AND is_deleted = 0 ORDER BY record_date DESC")
    fun getRecordsByType(fowlId: String, recordType: String): Flow<List<FowlRecordEntity>>
    
    /**
     * Get records by date range for a specific fowl
     */
    @Query("SELECT * FROM fowl_records WHERE fowl_id = :fowlId AND record_date BETWEEN :startDate AND :endDate AND is_deleted = 0 ORDER BY record_date DESC")
    suspend fun getRecordsByDateRange(fowlId: String, startDate: Long, endDate: Long): List<FowlRecordEntity>
    
    /**
     * Get milestone records (5w, 20w, weekly) for reminder processing
     */
    @Query("SELECT * FROM fowl_records WHERE record_type IN ('MILESTONE_5W', 'MILESTONE_20W', 'WEEKLY_UPDATE') AND is_deleted = 0 ORDER BY record_date DESC")
    suspend fun getMilestoneRecords(): List<FowlRecordEntity>
    
    /**
     * Delete all records for a specific fowl (mark as deleted)
     */
    @Query("UPDATE fowl_records SET is_deleted = 1, updated_at = :updatedAt WHERE fowl_id = :fowlId")
    suspend fun deleteRecordsByFowlId(fowlId: String, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Get records for a specific fowl with pagination support
     */
    @Query("SELECT * FROM fowl_records WHERE fowl_id = :fowlId AND is_deleted = 0 ORDER BY record_date DESC")
    fun getRecordsByFowlIdPaged(fowlId: String): PagingSource<Int, FowlRecordEntity>
    
    /**
     * Get records for a specific fowl with pagination support using limit and offset
     */
    @Query("SELECT * FROM fowl_records WHERE fowl_id = :fowlId AND is_deleted = 0 ORDER BY record_date DESC LIMIT :limit OFFSET :offset")
    suspend fun getRecordsByFowlIdPaged(fowlId: String, limit: Int, offset: Int): List<FowlRecordEntity>
    
    /**
     * Get lightweight projection of records for a specific fowl with pagination support
     * Minimizes Map/List deserialization for better performance on low-end devices
     */
    @Query("SELECT id, fowl_id, record_type, record_date, description, proof_count, created_by, created_at, updated_at, version FROM fowl_records WHERE fowl_id = :fowlId AND is_deleted = 0 ORDER BY record_date DESC LIMIT :limit OFFSET :offset")
    suspend fun getRecordListItemsByFowlIdPaged(fowlId: String, limit: Int, offset: Int): List<FowlRecordListItem>
}
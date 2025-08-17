package com.rio.rostry.core.data.repository

import com.rio.rostry.core.common.model.Result
import com.rio.rostry.core.data.model.FowlRecord
import com.rio.rostry.core.data.model.FowlRecordListItem
import com.rio.rostry.core.data.model.TimelineSummary
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for fowl record operations with offline-first support
 */
interface FowlRecordRepository {
    
    /**
     * Get all records for a specific fowl ordered by date (newest first)
     */
    fun getRecordsByFowlId(fowlId: String): Flow<List<FowlRecord>>
    
    /**
     * Get records by type for a specific fowl
     */
    fun getRecordsByType(fowlId: String, type: String): Flow<List<FowlRecord>>
    
    /**
     * Add a new fowl record
     */
    suspend fun addRecord(record: FowlRecord): Result<Unit>
    
    /**
     * Update an existing fowl record
     */
    suspend fun updateRecord(record: FowlRecord): Result<Unit>
    
    /**
     * Delete a fowl record
     */
    suspend fun deleteRecord(recordId: String): Result<Unit>
    
    /**
     * Get a specific record by ID
     */
    suspend fun getRecordById(recordId: String): Result<FowlRecord?>
    
    /**
     * Get records for a specific fowl with pagination support
     */
    suspend fun getRecordsByFowlIdPaged(fowlId: String, limit: Int, offset: Int): Result<List<FowlRecord>>
    
    /**
     * Get lightweight projection of records for a specific fowl with pagination support
     * Minimizes Map/List deserialization for better performance on low-end devices
     */
    suspend fun getRecordListItemsByFowlIdPaged(fowlId: String, limit: Int, offset: Int): Result<List<FowlRecordListItem>>
    
    /**
     * Get compact timeline summary for a specific fowl
     * Used for quick display during transfer flow
     */
    suspend fun getTimelineSummary(fowlId: String, limit: Int = 5): Result<List<TimelineSummary>>
}
package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.DisputeEntity

@Dao
interface DisputeDao {
    @Query("SELECT * FROM disputes WHERE disputant_id = :userId OR respondent_id = :userId ORDER BY created_at DESC")
    suspend fun getDisputesByUser(userId: String): List<DisputeEntity>
    
    @Query("SELECT * FROM disputes WHERE transaction_id = :transactionId")
    suspend fun getDisputeByTransactionId(transactionId: String): DisputeEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dispute: DisputeEntity)
    
    @Update
    suspend fun update(dispute: DisputeEntity)
}

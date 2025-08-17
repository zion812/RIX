package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.TransferEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Data Access Object for transfer operations
 */
@Dao
interface TransferDaoV2 {
    
    @Query("SELECT * FROM transfers WHERE id = :transferId")
    suspend fun getTransferById(transferId: String): TransferEntity?
    
    @Query("SELECT * FROM transfers WHERE id = :transferId")
    fun observeTransfer(transferId: String): Flow<TransferEntity?>
    
    @Query("SELECT * FROM transfers WHERE fromUserId = :userId OR toUserId = :userId ORDER BY createdAt DESC")
    fun getTransfersByUser(userId: String): Flow<List<TransferEntity>>
    
    @Query("SELECT * FROM transfers WHERE fromUserId = :userId ORDER BY createdAt DESC")
    fun getOutgoingTransfers(userId: String): Flow<List<TransferEntity>>
    
    @Query("SELECT * FROM transfers WHERE toUserId = :userId ORDER BY createdAt DESC")
    fun getIncomingTransfers(userId: String): Flow<List<TransferEntity>>
    
    @Query("SELECT * FROM transfers WHERE fowlId = :fowlId ORDER BY createdAt DESC")
    suspend fun getTransfersByFowl(fowlId: String): List<TransferEntity>
    
    @Query("SELECT * FROM transfers WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getTransfersByStatus(status: String): List<TransferEntity>
    
    @Query("SELECT * FROM transfers WHERE status = 'pending' AND verificationRequired = 1 ORDER BY createdAt ASC")
    suspend fun getPendingVerificationTransfers(): List<TransferEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfers(transfers: List<TransferEntity>)
    
    @Update
    suspend fun updateTransfer(transfer: TransferEntity)
    
    @Query("UPDATE transfers SET status = :status, updatedAt = :updatedAt WHERE id = :transferId")
    suspend fun updateTransferStatus(transferId: String, status: String, updatedAt: Date = Date())
    
    @Query("UPDATE transfers SET verifiedBy = :verifierId, verifiedAt = :verifiedAt, status = 'approved', updatedAt = :updatedAt WHERE id = :transferId")
    suspend fun approveTransfer(transferId: String, verifierId: String, verifiedAt: Date = Date(), updatedAt: Date = Date())
    
    @Query("UPDATE transfers SET status = 'rejected', reason = :reason, updatedAt = :updatedAt WHERE id = :transferId")
    suspend fun rejectTransfer(transferId: String, reason: String, updatedAt: Date = Date())
    
    @Query("UPDATE transfers SET status = 'completed', completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :transferId")
    suspend fun completeTransfer(transferId: String, completedAt: Date = Date(), updatedAt: Date = Date())
    
    @Delete
    suspend fun deleteTransfer(transfer: TransferEntity)
    
    @Query("DELETE FROM transfers WHERE id = :transferId")
    suspend fun deleteTransferById(transferId: String)
    
    // Sync operations
    @Query("SELECT * FROM transfers WHERE isSynced = 0")
    suspend fun getUnsyncedTransfers(): List<TransferEntity>
    
    @Query("UPDATE transfers SET isSynced = 1 WHERE id = :transferId")
    suspend fun markTransferAsSynced(transferId: String)
    
    // Analytics
    @Query("SELECT COUNT(*) FROM transfers WHERE fromUserId = :userId")
    suspend fun getOutgoingTransferCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM transfers WHERE toUserId = :userId")
    suspend fun getIncomingTransferCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM transfers WHERE (fromUserId = :userId OR toUserId = :userId) AND status = 'completed'")
    suspend fun getCompletedTransferCount(userId: String): Int
    
    @Query("SELECT SUM(priceInCoins) FROM transfers WHERE fromUserId = :userId AND status = 'completed' AND priceInCoins IS NOT NULL")
    suspend fun getTotalEarningsInCoins(userId: String): Int?
    
    @Query("SELECT SUM(priceInCoins) FROM transfers WHERE toUserId = :userId AND status = 'completed' AND priceInCoins IS NOT NULL")
    suspend fun getTotalSpentInCoins(userId: String): Int?
    
    @Query("SELECT transferType, COUNT(*) as count FROM transfers WHERE fromUserId = :userId OR toUserId = :userId GROUP BY transferType ORDER BY count DESC")
    suspend fun getTransferTypeDistribution(userId: String): List<TransferTypeCount>
    
    @Query("SELECT * FROM transfers WHERE (fromUserId = :userId OR toUserId = :userId) AND createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC")
    suspend fun getTransfersByDateRange(userId: String, startDate: Date, endDate: Date): List<TransferEntity>
    
    // Verification queue
    @Query("SELECT * FROM transfers WHERE verificationRequired = 1 AND status = 'pending' AND verifiedBy IS NULL ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getTransfersAwaitingVerification(limit: Int = 50): List<TransferEntity>
    
    @Query("SELECT COUNT(*) FROM transfers WHERE verificationRequired = 1 AND status = 'pending' AND verifiedBy IS NULL")
    suspend fun getVerificationQueueCount(): Int
}

data class TransferTypeCount(
    val transferType: String,
    val count: Int
)
package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.CoinTransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for coin transaction operations
 */
@Dao
interface CoinTransactionDao {
    
    @Query("SELECT * FROM coin_transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: String): CoinTransactionEntity?
    
    @Query("SELECT * FROM coin_transactions WHERE user_id = :userId ORDER BY created_at DESC")
    suspend fun getTransactionsByUserId(userId: String): List<CoinTransactionEntity>
    
    @Query("SELECT * FROM coin_transactions WHERE user_id = :userId ORDER BY created_at DESC")
    fun observeTransactionsByUserId(userId: String): Flow<List<CoinTransactionEntity>>
    
    @Query("SELECT * FROM coin_transactions WHERE user_id = :userId AND status = 'PENDING' ORDER BY created_at DESC")
    suspend fun getPendingTransactions(userId: String): List<CoinTransactionEntity>
    
    @Query("SELECT * FROM coin_transactions WHERE user_id = :userId AND transaction_type = :type ORDER BY created_at DESC LIMIT :limit")
    suspend fun getTransactionsByType(userId: String, type: String, limit: Int = 50): List<CoinTransactionEntity>
    
    @Query("SELECT * FROM coin_transactions WHERE user_id = :userId AND purpose = :purpose ORDER BY created_at DESC")
    suspend fun getTransactionsByPurpose(userId: String, purpose: String): List<CoinTransactionEntity>
    
    @Query("SELECT SUM(amount) FROM coin_transactions WHERE user_id = :userId AND transaction_type = 'CREDIT' AND status = 'COMPLETED'")
    suspend fun getTotalCredits(userId: String): Int?
    
    @Query("SELECT SUM(amount) FROM coin_transactions WHERE user_id = :userId AND transaction_type = 'DEBIT' AND status = 'COMPLETED'")
    suspend fun getTotalDebits(userId: String): Int?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: CoinTransactionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<CoinTransactionEntity>)
    
    @Update
    suspend fun update(transaction: CoinTransactionEntity)
    
    @Delete
    suspend fun delete(transaction: CoinTransactionEntity)
    
    @Query("UPDATE coin_transactions SET status = :status WHERE id = :transactionId")
    suspend fun updateTransactionStatus(transactionId: String, status: String)
    
    @Query("UPDATE coin_transactions SET status = :status, updated_at = :updatedAt WHERE id = :transactionId")
    suspend fun updateTransactionStatusWithTimestamp(transactionId: String, status: String, updatedAt: Long)
    
    @Query("DELETE FROM coin_transactions WHERE user_id = :userId AND status = 'FAILED' AND created_at < :cutoffTime")
    suspend fun cleanupFailedTransactions(userId: String, cutoffTime: Long)
    
    @Query("SELECT COUNT(*) FROM coin_transactions WHERE user_id = :userId AND status = 'PENDING'")
    suspend fun getPendingTransactionCount(userId: String): Int
    
    @Query("SELECT * FROM coin_transactions WHERE is_synced = 0 ORDER BY created_at ASC")
    suspend fun getUnsyncedTransactions(): List<CoinTransactionEntity>
    
    @Query("UPDATE coin_transactions SET is_synced = 1, synced_at = :syncedAt WHERE id = :transactionId")
    suspend fun markAsSynced(transactionId: String, syncedAt: Long)

    /**
     * Get transactions by user (alias for getTransactionsByUserId)
     */
    suspend fun getTransactionsByUser(userId: String): List<CoinTransactionEntity> {
        return getTransactionsByUserId(userId)
    }

    /**
     * Delete transactions by user
     */
    @Query("DELETE FROM coin_transactions WHERE user_id = :userId")
    suspend fun deleteTransactionsByUser(userId: String)
}

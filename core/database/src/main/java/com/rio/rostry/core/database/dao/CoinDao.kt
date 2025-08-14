package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.CoinTransactionEntity
import com.rio.rostry.core.database.entities.UserCoinBalanceEntity
import com.rio.rostry.core.common.model.TransactionStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for coin-related operations
 */
@Dao
interface CoinDao {
    
    // Transaction operations
    @Query("SELECT * FROM coin_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTransactionsByUser(userId: String): Flow<List<CoinTransactionEntity>>
    
    @Query("SELECT * FROM coin_transactions WHERE userId = :userId AND status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingTransactions(userId: String): List<CoinTransactionEntity>
    
    @Query("SELECT * FROM coin_transactions WHERE userId = :userId AND status = 'FAILED' ORDER BY createdAt ASC")
    suspend fun getFailedTransactions(userId: String): List<CoinTransactionEntity>
    
    @Query("SELECT * FROM coin_transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: String): CoinTransactionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CoinTransactionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<CoinTransactionEntity>)
    
    @Update
    suspend fun updateTransaction(transaction: CoinTransactionEntity)
    
    @Query("UPDATE coin_transactions SET status = :status WHERE id = :transactionId")
    suspend fun updateTransactionStatus(transactionId: String, status: TransactionStatus)
    
    @Delete
    suspend fun deleteTransaction(transaction: CoinTransactionEntity)
    
    // Balance operations
    @Query("SELECT * FROM user_coin_balances WHERE userId = :userId")
    suspend fun getUserBalance(userId: String): UserCoinBalanceEntity?
    
    @Query("SELECT balance FROM user_coin_balances WHERE userId = :userId")
    suspend fun getLastConfirmedBalance(userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBalance(balance: UserCoinBalanceEntity)
    
    @Query("UPDATE user_coin_balances SET balance = :balance, pendingBalance = :pendingBalance, lastUpdated = :lastUpdated WHERE userId = :userId")
    suspend fun updateBalance(userId: String, balance: Int, pendingBalance: Int, lastUpdated: java.util.Date)
    
    // Sync operations
    @Query("SELECT * FROM coin_transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<CoinTransactionEntity>
    
    @Query("UPDATE coin_transactions SET isSynced = 1 WHERE id = :transactionId")
    suspend fun markTransactionAsSynced(transactionId: String)
    
    @Query("SELECT COUNT(*) FROM coin_transactions WHERE userId = :userId AND status = 'PENDING'")
    suspend fun getPendingTransactionCount(userId: String): Int
    
    // Analytics
    @Query("SELECT SUM(amount) FROM coin_transactions WHERE userId = :userId AND status = 'COMPLETED' AND transactionType = 'COIN_PURCHASE'")
    suspend fun getTotalCoinsPurchased(userId: String): Int?
    
    @Query("SELECT SUM(amount) FROM coin_transactions WHERE userId = :userId AND status = 'COMPLETED' AND transactionType != 'COIN_PURCHASE'")
    suspend fun getTotalCoinsSpent(userId: String): Int?
    
    @Query("SELECT * FROM coin_transactions WHERE userId = :userId AND createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC")
    suspend fun getTransactionsByDateRange(userId: String, startDate: java.util.Date, endDate: java.util.Date): List<CoinTransactionEntity>
}
package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.UserCoinBalanceEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for user coin balance operations
 */
@Dao
interface UserCoinBalanceDao {
    
    @Query("SELECT * FROM user_coin_balances WHERE user_id = :userId")
    suspend fun getBalance(userId: String): UserCoinBalanceEntity?
    
    @Query("SELECT * FROM user_coin_balances WHERE user_id = :userId")
    fun observeBalance(userId: String): Flow<UserCoinBalanceEntity?>
    
    @Query("SELECT balance FROM user_coin_balances WHERE user_id = :userId")
    suspend fun getCurrentBalance(userId: String): Int?
    
    @Query("SELECT balance FROM user_coin_balances WHERE user_id = :userId")
    fun observeCurrentBalance(userId: String): Flow<Int?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(balance: UserCoinBalanceEntity)
    
    @Update
    suspend fun update(balance: UserCoinBalanceEntity)
    
    @Delete
    suspend fun delete(balance: UserCoinBalanceEntity)
    
    @Query("UPDATE user_coin_balances SET balance = :newBalance, last_updated = :timestamp WHERE user_id = :userId")
    suspend fun updateBalance(userId: String, newBalance: Int, timestamp: Long)
    
    @Query("UPDATE user_coin_balances SET balance = balance + :amount, total_earned = total_earned + :amount, last_updated = :timestamp WHERE user_id = :userId")
    suspend fun addCoins(userId: String, amount: Int, timestamp: Long)
    
    @Query("UPDATE user_coin_balances SET balance = balance - :amount, total_spent = total_spent + :amount, last_updated = :timestamp WHERE user_id = :userId")
    suspend fun deductCoins(userId: String, amount: Int, timestamp: Long)
    
    @Query("UPDATE user_coin_balances SET balance = balance + :amount, total_purchased = total_purchased + :amount, last_updated = :timestamp WHERE user_id = :userId")
    suspend fun addPurchasedCoins(userId: String, amount: Int, timestamp: Long)
    
    @Query("UPDATE user_coin_balances SET last_transaction_id = :transactionId, last_updated = :timestamp WHERE user_id = :userId")
    suspend fun updateLastTransaction(userId: String, transactionId: String, timestamp: Long)
    
    @Query("SELECT * FROM user_coin_balances WHERE is_synced = 0")
    suspend fun getUnsyncedBalances(): List<UserCoinBalanceEntity>
    
    @Query("UPDATE user_coin_balances SET is_synced = 1, synced_at = :syncedAt WHERE user_id = :userId")
    suspend fun markAsSynced(userId: String, syncedAt: Long)
    
    @Query("SELECT COUNT(*) FROM user_coin_balances WHERE balance > 0")
    suspend fun getActiveBalanceCount(): Int

    /**
     * Insert or update balance entity
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(balance: UserCoinBalanceEntity)

    /**
     * Update balance with current timestamp
     */
    suspend fun updateBalance(userId: String, newBalance: Int) {
        updateBalance(userId, newBalance, System.currentTimeMillis())
    }
}

package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.*
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Data Access Objects for RIO coin-based payment system
 * Provides offline-first database operations with sync support
 */

/**
 * Coin transaction DAO
 */
@Dao
interface CoinTransactionDao {
    
    @Query("SELECT * FROM coin_transactions WHERE user_id = :userId ORDER BY created_at DESC")
    suspend fun getTransactionsByUser(userId: String): List<CoinTransactionEntity>
    
    @Query("SELECT * FROM coin_transactions WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecentTransactions(userId: String, limit: Int = 20): List<CoinTransactionEntity>
    
    @Query("SELECT * FROM coin_transactions WHERE id = :transactionId")
    suspend fun getById(transactionId: String): CoinTransactionEntity?
    
    @Query("SELECT * FROM coin_transactions WHERE status = 'PENDING' AND is_synced = 0")
    suspend fun getPendingTransactions(): List<CoinTransactionEntity>
    
    @Query("SELECT * FROM coin_transactions WHERE user_id = :userId AND transaction_type = :type ORDER BY created_at DESC")
    suspend fun getTransactionsByType(userId: String, type: String): List<CoinTransactionEntity>
    
    @Query("SELECT * FROM coin_transactions WHERE user_id = :userId AND purpose = :purpose ORDER BY created_at DESC")
    suspend fun getTransactionsByPurpose(userId: String, purpose: String): List<CoinTransactionEntity>
    
    @Query("SELECT * FROM coin_transactions WHERE user_id = :userId AND created_at >= :startDate AND created_at <= :endDate")
    suspend fun getTransactionsByDateRange(userId: String, startDate: Date, endDate: Date): List<CoinTransactionEntity>
    
    @Query("SELECT SUM(amount) FROM coin_transactions WHERE user_id = :userId AND transaction_type = 'SPEND' AND created_at >= :startDate")
    suspend fun getTotalSpentSince(userId: String, startDate: Date): Int?
    
    @Query("SELECT SUM(amount) FROM coin_transactions WHERE user_id = :userId AND transaction_type = 'CREDIT' AND created_at >= :startDate")
    suspend fun getTotalEarnedSince(userId: String, startDate: Date): Int?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: CoinTransactionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<CoinTransactionEntity>)
    
    @Update
    suspend fun update(transaction: CoinTransactionEntity)
    
    @Query("UPDATE coin_transactions SET status = :status, payment_id = :paymentId, updated_at = :updatedAt WHERE id = :transactionId")
    suspend fun updateTransactionStatus(transactionId: String, status: String, paymentId: String?, updatedAt: Date = Date())
    
    @Query("UPDATE coin_transactions SET is_synced = 1, synced_at = :syncedAt WHERE id = :transactionId")
    suspend fun markAsSynced(transactionId: String, syncedAt: Date = Date())
    
    @Query("DELETE FROM coin_transactions WHERE id = :transactionId")
    suspend fun deleteById(transactionId: String)
    
    @Query("DELETE FROM coin_transactions WHERE user_id = :userId AND created_at < :cutoffDate AND status = 'COMPLETED'")
    suspend fun deleteOldTransactions(userId: String, cutoffDate: Date)
    
    // Flow-based queries for reactive UI
    @Query("SELECT * FROM coin_transactions WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    fun observeRecentTransactions(userId: String, limit: Int = 20): Flow<List<CoinTransactionEntity>>
    
    @Query("SELECT COUNT(*) FROM coin_transactions WHERE user_id = :userId AND status = 'PENDING'")
    fun observePendingCount(userId: String): Flow<Int>
}

/**
 * Coin order DAO
 */
@Dao
interface CoinOrderDao {
    
    @Query("SELECT * FROM coin_orders WHERE user_id = :userId ORDER BY created_at DESC")
    suspend fun getOrdersByUser(userId: String): List<CoinOrderEntity>
    
    @Query("SELECT * FROM coin_orders WHERE order_id = :orderId")
    suspend fun getByOrderId(orderId: String): CoinOrderEntity?
    
    @Query("SELECT * FROM coin_orders WHERE id = :id")
    suspend fun getById(id: String): CoinOrderEntity?
    
    @Query("SELECT * FROM coin_orders WHERE status = :status ORDER BY created_at DESC")
    suspend fun getOrdersByStatus(status: String): List<CoinOrderEntity>
    
    @Query("SELECT * FROM coin_orders WHERE user_id = :userId AND status = :status ORDER BY created_at DESC")
    suspend fun getUserOrdersByStatus(userId: String, status: String): List<CoinOrderEntity>
    
    @Query("SELECT * FROM coin_orders WHERE expires_at < :currentTime AND status IN ('CREATED', 'PENDING')")
    suspend fun getExpiredOrders(currentTime: Date = Date()): List<CoinOrderEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: CoinOrderEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<CoinOrderEntity>)
    
    @Update
    suspend fun update(order: CoinOrderEntity)
    
    @Query("UPDATE coin_orders SET status = :status, payment_id = :paymentId, updated_at = :updatedAt WHERE order_id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, paymentId: String? = null, updatedAt: Date = Date())
    
    @Query("UPDATE coin_orders SET status = 'COMPLETED', completed_at = :completedAt, updated_at = :updatedAt WHERE order_id = :orderId")
    suspend fun markAsCompleted(orderId: String, completedAt: Date = Date(), updatedAt: Date = Date())
    
    @Query("DELETE FROM coin_orders WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM coin_orders WHERE created_at < :cutoffDate AND status IN ('COMPLETED', 'FAILED', 'CANCELLED')")
    suspend fun deleteOldOrders(cutoffDate: Date)
    
    // Flow-based queries
    @Query("SELECT * FROM coin_orders WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    fun observeUserOrders(userId: String, limit: Int = 10): Flow<List<CoinOrderEntity>>
}

/**
 * Refund request DAO
 */
@Dao
interface RefundRequestDao {
    
    @Query("SELECT * FROM refund_requests WHERE user_id = :userId ORDER BY created_at DESC")
    suspend fun getRefundsByUser(userId: String): List<RefundRequestEntity>
    
    @Query("SELECT * FROM refund_requests WHERE id = :id")
    suspend fun getById(id: String): RefundRequestEntity?
    
    @Query("SELECT * FROM refund_requests WHERE order_id = :orderId")
    suspend fun getByOrderId(orderId: String): List<RefundRequestEntity>
    
    @Query("SELECT * FROM refund_requests WHERE status = :status ORDER BY priority DESC, created_at ASC")
    suspend fun getRefundsByStatus(status: String): List<RefundRequestEntity>
    
    @Query("SELECT * FROM refund_requests WHERE status = 'PENDING' AND auto_process_eligible = 1 ORDER BY created_at ASC")
    suspend fun getAutoProcessEligibleRefunds(): List<RefundRequestEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(refund: RefundRequestEntity)
    
    @Update
    suspend fun update(refund: RefundRequestEntity)
    
    @Query("UPDATE refund_requests SET status = :status, processed_amount = :processedAmount, razorpay_refund_id = :refundId, processed_at = :processedAt WHERE id = :id")
    suspend fun markAsProcessed(id: String, status: String, processedAmount: Double, refundId: String, processedAt: Date = Date())
    
    @Query("UPDATE refund_requests SET status = 'FAILED', failure_reason = :reason WHERE id = :id")
    suspend fun markAsFailed(id: String, reason: String)
    
    @Query("DELETE FROM refund_requests WHERE id = :id")
    suspend fun deleteById(id: String)
    
    // Flow-based queries
    @Query("SELECT * FROM refund_requests WHERE user_id = :userId ORDER BY created_at DESC")
    fun observeUserRefunds(userId: String): Flow<List<RefundRequestEntity>>
}

/**
 * Dispute DAO
 */
@Dao
interface DisputeDao {
    
    @Query("SELECT * FROM disputes WHERE disputant_id = :userId OR respondent_id = :userId ORDER BY created_at DESC")
    suspend fun getDisputesByUser(userId: String): List<DisputeEntity>
    
    @Query("SELECT * FROM disputes WHERE id = :id")
    suspend fun getById(id: String): DisputeEntity?
    
    @Query("SELECT * FROM disputes WHERE transaction_id = :transactionId")
    suspend fun getByTransactionId(transactionId: String): List<DisputeEntity>
    
    @Query("SELECT * FROM disputes WHERE status = :status ORDER BY priority DESC, created_at ASC")
    suspend fun getDisputesByStatus(status: String): List<DisputeEntity>
    
    @Query("SELECT * FROM disputes WHERE mediator_id = :mediatorId AND status IN ('OPEN', 'ESCALATED') ORDER BY created_at ASC")
    suspend fun getDisputesByMediator(mediatorId: String): List<DisputeEntity>
    
    @Query("SELECT * FROM disputes WHERE resolution_deadline < :currentTime AND status = 'OPEN'")
    suspend fun getOverdueDisputes(currentTime: Date = Date()): List<DisputeEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dispute: DisputeEntity)
    
    @Update
    suspend fun update(dispute: DisputeEntity)
    
    @Query("UPDATE disputes SET status = :status, resolution = :resolution, reasoning = :reasoning, resolver_id = :resolverId, compensation_amount = :compensationAmount, resolved_at = :resolvedAt WHERE id = :id")
    suspend fun markAsResolved(
        id: String, 
        status: String, 
        resolution: String, 
        reasoning: String, 
        resolverId: String, 
        compensationAmount: Double, 
        resolvedAt: Date = Date()
    )
    
    @Query("UPDATE disputes SET escalation_level = escalation_level + 1, status = 'ESCALATED', escalated_at = :escalatedAt WHERE id = :id")
    suspend fun escalateDispute(id: String, escalatedAt: Date = Date())
    
    @Query("UPDATE disputes SET mediator_id = :mediatorId WHERE id = :id")
    suspend fun assignMediator(id: String, mediatorId: String)
    
    @Query("DELETE FROM disputes WHERE id = :id")
    suspend fun deleteById(id: String)
    
    // Flow-based queries
    @Query("SELECT * FROM disputes WHERE disputant_id = :userId OR respondent_id = :userId ORDER BY created_at DESC")
    fun observeUserDisputes(userId: String): Flow<List<DisputeEntity>>
    
    @Query("SELECT COUNT(*) FROM disputes WHERE (disputant_id = :userId OR respondent_id = :userId) AND status IN ('OPEN', 'ESCALATED')")
    fun observeActiveDisputeCount(userId: String): Flow<Int>
}

/**
 * User coin balance DAO
 */
@Dao
interface UserCoinBalanceDao {
    
    @Query("SELECT * FROM user_coin_balances WHERE user_id = :userId")
    suspend fun getBalance(userId: String): UserCoinBalanceEntity?
    
    @Query("SELECT balance FROM user_coin_balances WHERE user_id = :userId")
    suspend fun getCurrentBalance(userId: String): Int?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(balance: UserCoinBalanceEntity)
    
    @Update
    suspend fun update(balance: UserCoinBalanceEntity)
    
    @Query("UPDATE user_coin_balances SET balance = :newBalance, last_transaction_id = :transactionId, last_updated = :lastUpdated WHERE user_id = :userId")
    suspend fun updateBalance(userId: String, newBalance: Int, transactionId: String, lastUpdated: Date = Date())
    
    @Query("UPDATE user_coin_balances SET balance = balance + :amount, total_earned = total_earned + :amount, last_transaction_id = :transactionId, last_updated = :lastUpdated WHERE user_id = :userId")
    suspend fun addCoins(userId: String, amount: Int, transactionId: String, lastUpdated: Date = Date())
    
    @Query("UPDATE user_coin_balances SET balance = balance - :amount, total_spent = total_spent + :amount, last_transaction_id = :transactionId, last_updated = :lastUpdated WHERE user_id = :userId")
    suspend fun spendCoins(userId: String, amount: Int, transactionId: String, lastUpdated: Date = Date())
    
    @Query("UPDATE user_coin_balances SET is_synced = 1, synced_at = :syncedAt WHERE user_id = :userId")
    suspend fun markAsSynced(userId: String, syncedAt: Date = Date())
    
    @Query("SELECT * FROM user_coin_balances WHERE is_synced = 0")
    suspend fun getUnsyncedBalances(): List<UserCoinBalanceEntity>
    
    @Query("DELETE FROM user_coin_balances WHERE user_id = :userId")
    suspend fun deleteByUserId(userId: String)
    
    // Flow-based queries
    @Query("SELECT * FROM user_coin_balances WHERE user_id = :userId")
    fun observeBalance(userId: String): Flow<UserCoinBalanceEntity?>
    
    @Query("SELECT balance FROM user_coin_balances WHERE user_id = :userId")
    fun observeCurrentBalance(userId: String): Flow<Int?>
}

/**
 * Coin package DAO
 */
@Dao
interface CoinPackageDao {
    
    @Query("SELECT * FROM coin_packages WHERE active = 1 AND (user_tier = :userTier OR user_tier = 'ALL') ORDER BY price ASC")
    suspend fun getPackagesForTier(userTier: String): List<CoinPackageEntity>
    
    @Query("SELECT * FROM coin_packages WHERE package_id = :packageId")
    suspend fun getByPackageId(packageId: String): CoinPackageEntity?
    
    @Query("SELECT * FROM coin_packages WHERE active = 1 AND featured = 1 ORDER BY price ASC")
    suspend fun getFeaturedPackages(): List<CoinPackageEntity>
    
    @Query("SELECT * FROM coin_packages WHERE active = 1 AND valid_from <= :currentTime AND (valid_until IS NULL OR valid_until >= :currentTime)")
    suspend fun getValidPackages(currentTime: Date = Date()): List<CoinPackageEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(package: CoinPackageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(packages: List<CoinPackageEntity>)
    
    @Update
    suspend fun update(package: CoinPackageEntity)
    
    @Query("UPDATE coin_packages SET active = 0 WHERE package_id = :packageId")
    suspend fun deactivatePackage(packageId: String)
    
    @Query("DELETE FROM coin_packages WHERE package_id = :packageId")
    suspend fun deleteByPackageId(packageId: String)
    
    // Flow-based queries
    @Query("SELECT * FROM coin_packages WHERE active = 1 AND (user_tier = :userTier OR user_tier = 'ALL') ORDER BY price ASC")
    fun observePackagesForTier(userTier: String): Flow<List<CoinPackageEntity>>
}

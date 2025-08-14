package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.CoinOrderEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for coin order operations
 */
@Dao
interface CoinOrderDao {
    
    @Query("SELECT * FROM coin_orders WHERE id = :orderId")
    suspend fun getById(orderId: String): CoinOrderEntity?
    
    @Query("SELECT * FROM coin_orders WHERE order_id = :orderId")
    suspend fun getByOrderId(orderId: String): CoinOrderEntity?
    
    @Query("SELECT * FROM coin_orders WHERE user_id = :userId ORDER BY created_at DESC")
    suspend fun getOrdersByUserId(userId: String): List<CoinOrderEntity>
    
    @Query("SELECT * FROM coin_orders WHERE user_id = :userId ORDER BY created_at DESC")
    fun observeOrdersByUserId(userId: String): Flow<List<CoinOrderEntity>>
    
    @Query("SELECT * FROM coin_orders WHERE user_id = :userId AND status = :status ORDER BY created_at DESC")
    suspend fun getOrdersByStatus(userId: String, status: String): List<CoinOrderEntity>
    
    @Query("SELECT * FROM coin_orders WHERE status = 'PENDING' AND expires_at < :currentTime")
    suspend fun getExpiredOrders(currentTime: Long): List<CoinOrderEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: CoinOrderEntity)
    
    @Update
    suspend fun update(order: CoinOrderEntity)
    
    @Delete
    suspend fun delete(order: CoinOrderEntity)
    
    @Query("UPDATE coin_orders SET status = :status, updated_at = :updatedAt WHERE id = :orderId")
    suspend fun updateStatus(orderId: String, status: String, updatedAt: Long)
    
    @Query("UPDATE coin_orders SET payment_id = :paymentId, status = :status, updated_at = :updatedAt WHERE id = :orderId")
    suspend fun updatePaymentInfo(orderId: String, paymentId: String, status: String, updatedAt: Long)
    
    @Query("UPDATE coin_orders SET status = 'COMPLETED', completed_at = :completedAt, updated_at = :updatedAt WHERE id = :orderId")
    suspend fun markAsCompleted(orderId: String, completedAt: Long, updatedAt: Long)
    
    @Query("DELETE FROM coin_orders WHERE status IN ('FAILED', 'CANCELLED') AND created_at < :cutoffTime")
    suspend fun cleanupOldOrders(cutoffTime: Long)
}

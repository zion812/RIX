package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.RefundRequestEntity

@Dao
interface RefundRequestDao {
    @Query("SELECT * FROM refund_requests WHERE user_id = :userId ORDER BY created_at DESC")
    suspend fun getRefundRequestsByUser(userId: String): List<RefundRequestEntity>
    
    @Query("SELECT * FROM refund_requests WHERE order_id = :orderId")
    suspend fun getRefundRequestByOrderId(orderId: String): RefundRequestEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(refundRequest: RefundRequestEntity)
    
    @Update
    suspend fun update(refundRequest: RefundRequestEntity)
}

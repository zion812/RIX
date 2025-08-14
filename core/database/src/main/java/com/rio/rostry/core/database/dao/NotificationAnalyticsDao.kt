package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.NotificationAnalyticsEntity

@Dao
interface NotificationAnalyticsDao {
    @Query("SELECT * FROM notification_analytics WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAnalyticsByUser(userId: String): List<NotificationAnalyticsEntity>
    
    @Query("SELECT * FROM notification_analytics WHERE notificationId = :notificationId ORDER BY timestamp DESC")
    suspend fun getAnalyticsByNotification(notificationId: String): List<NotificationAnalyticsEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(analytics: NotificationAnalyticsEntity)
    
    @Query("DELETE FROM notification_analytics WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldAnalytics(cutoffTime: Long)
}

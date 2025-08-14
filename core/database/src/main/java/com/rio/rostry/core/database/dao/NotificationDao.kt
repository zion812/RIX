package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Data Access Object for notification-related operations
 */
@Dao
interface NotificationDao {
    
    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: String): NotificationEntity?
    
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsByUser(userId: String): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = 0 ORDER BY createdAt DESC")
    suspend fun getUnreadNotifications(userId: String): List<NotificationEntity>
    
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    suspend fun getUnreadNotificationCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun observeUnreadNotificationCount(userId: String): Flow<Int>
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND category = :category ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getNotificationsByCategory(userId: String, category: String, limit: Int = 50): List<NotificationEntity>
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND priority = :priority ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getNotificationsByPriority(userId: String, priority: String, limit: Int = 50): List<NotificationEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)
    
    @Update
    suspend fun updateNotification(notification: NotificationEntity)
    
    @Query("UPDATE notifications SET isRead = 1, readAt = :readAt, updatedAt = :updatedAt WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String, readAt: Date = Date(), updatedAt: Date = Date())
    
    @Query("UPDATE notifications SET isRead = 1, readAt = :readAt, updatedAt = :updatedAt WHERE userId = :userId AND isRead = 0")
    suspend fun markAllAsRead(userId: String, readAt: Date = Date(), updatedAt: Date = Date())
    
    @Query("UPDATE notifications SET isRead = 1, readAt = :readAt, updatedAt = :updatedAt WHERE userId = :userId AND category = :category AND isRead = 0")
    suspend fun markCategoryAsRead(userId: String, category: String, readAt: Date = Date(), updatedAt: Date = Date())
    
    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)
    
    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotificationById(notificationId: String)
    
    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteAllNotificationsByUser(userId: String)
    
    @Query("DELETE FROM notifications WHERE userId = :userId AND category = :category")
    suspend fun deleteNotificationsByCategory(userId: String, category: String)
    
    // Sync operations
    @Query("SELECT * FROM notifications WHERE isSynced = 0")
    suspend fun getUnsyncedNotifications(): List<NotificationEntity>
    
    @Query("UPDATE notifications SET isSynced = 1 WHERE id = :notificationId")
    suspend fun markNotificationAsSynced(notificationId: String)
    
    // Cleanup operations
    @Query("DELETE FROM notifications WHERE expiresAt IS NOT NULL AND expiresAt < :currentTime")
    suspend fun deleteExpiredNotifications(currentTime: Date = Date())
    
    @Query("DELETE FROM notifications WHERE createdAt < :cutoffDate")
    suspend fun deleteOldNotifications(cutoffDate: Date)
    
    @Query("SELECT * FROM notifications WHERE expiresAt IS NOT NULL AND expiresAt < :currentTime")
    suspend fun getExpiredNotifications(currentTime: Date = Date()): List<NotificationEntity>
    
    // Analytics
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId")
    suspend fun getTotalNotificationCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 1")
    suspend fun getReadNotificationCount(userId: String): Int
    
    @Query("SELECT category, COUNT(*) as count FROM notifications WHERE userId = :userId GROUP BY category ORDER BY count DESC")
    suspend fun getNotificationCategoryDistribution(userId: String): List<CategoryCount>
    
    @Query("SELECT priority, COUNT(*) as count FROM notifications WHERE userId = :userId GROUP BY priority ORDER BY count DESC")
    suspend fun getNotificationPriorityDistribution(userId: String): List<PriorityCount>
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC")
    suspend fun getNotificationsByDateRange(userId: String, startDate: Date, endDate: Date): List<NotificationEntity>
    
    // Search operations
    @Query("SELECT * FROM notifications WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR message LIKE '%' || :query || '%') ORDER BY createdAt DESC LIMIT :limit")
    suspend fun searchNotifications(userId: String, query: String, limit: Int = 50): List<NotificationEntity>
    
    // Pagination
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getNotificationsPaginated(userId: String, limit: Int = 20, offset: Int = 0): List<NotificationEntity>
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = 0 ORDER BY priority DESC, createdAt DESC LIMIT :limit")
    suspend fun getUnreadNotificationsPrioritized(userId: String, limit: Int = 10): List<NotificationEntity>
}

data class CategoryCount(
    val category: String,
    val count: Int
)

data class PriorityCount(
    val priority: String,
    val count: Int
)
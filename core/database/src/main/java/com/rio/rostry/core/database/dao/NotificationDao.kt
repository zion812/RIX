package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.NotificationEntity
import com.rio.rostry.core.database.entities.NotificationPreferenceEntity
import com.rio.rostry.core.database.entities.TopicSubscriptionEntity
import com.rio.rostry.core.database.entities.NotificationAnalyticsEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Data Access Object for notification operations
 * Supports offline-first architecture with sync capabilities
 */
@Dao
interface NotificationDao {
    
    // Notification CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)
    
    @Update
    suspend fun update(notification: NotificationEntity)
    
    @Delete
    suspend fun delete(notification: NotificationEntity)
    
    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteById(notificationId: String)
    
    @Query("DELETE FROM notifications WHERE createdAt < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: Date)
    
    // Query operations
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentNotifications(limit: Int): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notifications WHERE category = :category ORDER BY createdAt DESC")
    fun getNotificationsByCategory(category: String): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notifications WHERE isRead = 0 ORDER BY createdAt DESC")
    fun getUnreadNotifications(): Flow<List<NotificationEntity>>
    
    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>
    
    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    suspend fun getById(notificationId: String): NotificationEntity?
    
    @Query("SELECT * FROM notifications WHERE deepLink LIKE :pattern")
    fun getNotificationsByDeepLink(pattern: String): Flow<List<NotificationEntity>>
    
    // Mark as read operations
    @Query("UPDATE notifications SET isRead = 1, readAt = :readAt WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String, readAt: Date = Date())
    
    @Query("UPDATE notifications SET isRead = 1, readAt = :readAt WHERE isRead = 0")
    suspend fun markAllAsRead(readAt: Date = Date())
    
    @Query("UPDATE notifications SET isRead = 1, readAt = :readAt WHERE category = :category AND isRead = 0")
    suspend fun markCategoryAsRead(category: String, readAt: Date = Date())
    
    // Sync operations
    @Query("SELECT * FROM notifications WHERE isSynced = 0")
    suspend fun getUnsyncedNotifications(): List<NotificationEntity>
    
    @Query("UPDATE notifications SET isSynced = 1, syncedAt = :syncedAt WHERE id = :notificationId")
    suspend fun markAsSynced(notificationId: String, syncedAt: Date = Date())
    
    // Search operations
    @Query("SELECT * FROM notifications WHERE title LIKE :query OR body LIKE :query ORDER BY createdAt DESC")
    fun searchNotifications(query: String): Flow<List<NotificationEntity>>
    
    // Statistics
    @Query("SELECT COUNT(*) FROM notifications WHERE category = :category")
    suspend fun getCountByCategory(category: String): Int
    
    @Query("SELECT COUNT(*) FROM notifications WHERE createdAt >= :startDate AND createdAt <= :endDate")
    suspend fun getCountInDateRange(startDate: Date, endDate: Date): Int
}

/**
 * Data Access Object for notification preferences
 */
@Dao
interface NotificationPreferenceDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preferences: NotificationPreferenceEntity)
    
    @Update
    suspend fun update(preferences: NotificationPreferenceEntity)
    
    @Delete
    suspend fun delete(preferences: NotificationPreferenceEntity)
    
    @Query("SELECT * FROM notification_preferences WHERE userId = :userId")
    fun getPreferences(userId: String): Flow<NotificationPreferenceEntity?>
    
    @Query("SELECT * FROM notification_preferences WHERE userId = :userId")
    suspend fun getPreferencesSync(userId: String): NotificationPreferenceEntity?
    
    @Query("UPDATE notification_preferences SET marketplaceNotifications = :enabled WHERE userId = :userId")
    suspend fun updateMarketplaceNotifications(userId: String, enabled: Boolean)
    
    @Query("UPDATE notification_preferences SET transferNotifications = :enabled WHERE userId = :userId")
    suspend fun updateTransferNotifications(userId: String, enabled: Boolean)
    
    @Query("UPDATE notification_preferences SET communicationNotifications = :enabled WHERE userId = :userId")
    suspend fun updateCommunicationNotifications(userId: String, enabled: Boolean)
    
    @Query("UPDATE notification_preferences SET breedingNotifications = :enabled WHERE userId = :userId")
    suspend fun updateBreedingNotifications(userId: String, enabled: Boolean)
    
    @Query("UPDATE notification_preferences SET paymentNotifications = :enabled WHERE userId = :userId")
    suspend fun updatePaymentNotifications(userId: String, enabled: Boolean)
    
    @Query("UPDATE notification_preferences SET systemNotifications = :enabled WHERE userId = :userId")
    suspend fun updateSystemNotifications(userId: String, enabled: Boolean)
    
    @Query("UPDATE notification_preferences SET quietHoursEnabled = :enabled, quietHoursStart = :start, quietHoursEnd = :end WHERE userId = :userId")
    suspend fun updateQuietHours(userId: String, enabled: Boolean, start: String, end: String)
    
    @Query("UPDATE notification_preferences SET soundEnabled = :enabled WHERE userId = :userId")
    suspend fun updateSoundEnabled(userId: String, enabled: Boolean)
    
    @Query("UPDATE notification_preferences SET vibrationEnabled = :enabled WHERE userId = :userId")
    suspend fun updateVibrationEnabled(userId: String, enabled: Boolean)
}

/**
 * Data Access Object for topic subscriptions
 */
@Dao
interface TopicSubscriptionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: TopicSubscriptionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subscriptions: List<TopicSubscriptionEntity>)
    
    @Update
    suspend fun update(subscription: TopicSubscriptionEntity)
    
    @Delete
    suspend fun delete(subscription: TopicSubscriptionEntity)
    
    @Query("SELECT * FROM topic_subscriptions WHERE userId = :userId")
    fun getUserSubscriptions(userId: String): Flow<List<TopicSubscriptionEntity>>
    
    @Query("SELECT * FROM topic_subscriptions WHERE userId = :userId AND isSubscribed = 1")
    fun getActiveSubscriptions(userId: String): Flow<List<TopicSubscriptionEntity>>
    
    @Query("SELECT * FROM topic_subscriptions WHERE userId = :userId AND topicName = :topicName")
    suspend fun getSubscription(userId: String, topicName: String): TopicSubscriptionEntity?
    
    @Query("UPDATE topic_subscriptions SET isSubscribed = :subscribed, unsubscribedAt = :timestamp WHERE userId = :userId AND topicName = :topicName")
    suspend fun updateSubscriptionStatus(userId: String, topicName: String, subscribed: Boolean, timestamp: Date = Date())
    
    @Query("SELECT * FROM topic_subscriptions WHERE isSynced = 0")
    suspend fun getUnsyncedSubscriptions(): List<TopicSubscriptionEntity>
    
    @Query("UPDATE topic_subscriptions SET isSynced = 1, syncedAt = :syncedAt WHERE id = :subscriptionId")
    suspend fun markAsSynced(subscriptionId: String, syncedAt: Date = Date())
}

/**
 * Data Access Object for notification analytics
 */
@Dao
interface NotificationAnalyticsDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(analytics: NotificationAnalyticsEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(analytics: List<NotificationAnalyticsEntity>)
    
    @Query("SELECT * FROM notification_analytics WHERE notificationId = :notificationId ORDER BY timestamp DESC")
    fun getAnalyticsForNotification(notificationId: String): Flow<List<NotificationAnalyticsEntity>>
    
    @Query("SELECT * FROM notification_analytics WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getUserAnalytics(userId: String, limit: Int): Flow<List<NotificationAnalyticsEntity>>
    
    @Query("SELECT * FROM notification_analytics WHERE eventType = :eventType ORDER BY timestamp DESC")
    fun getAnalyticsByEventType(eventType: String): Flow<List<NotificationAnalyticsEntity>>
    
    @Query("SELECT * FROM notification_analytics WHERE isSynced = 0")
    suspend fun getUnsyncedAnalytics(): List<NotificationAnalyticsEntity>
    
    @Query("UPDATE notification_analytics SET isSynced = 1, syncedAt = :syncedAt WHERE id = :analyticsId")
    suspend fun markAsSynced(analyticsId: String, syncedAt: Date = Date())
    
    @Query("DELETE FROM notification_analytics WHERE timestamp < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: Date)
    
    // Analytics queries
    @Query("SELECT COUNT(*) FROM notification_analytics WHERE eventType = :eventType AND timestamp >= :startDate")
    suspend fun getEventCountSince(eventType: String, startDate: Date): Int
    
    @Query("SELECT eventType, COUNT(*) as count FROM notification_analytics WHERE timestamp >= :startDate GROUP BY eventType")
    suspend fun getEventCountsByType(startDate: Date): Map<String, Int>
}

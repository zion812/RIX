package com.rio.rostry.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * Notification entity for storing FCM notifications locally
 * Supports offline-first architecture with sync capabilities
 */
@Entity(tableName = "notifications")
@TypeConverters(NotificationConverters::class)
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val category: String,
    val priority: String,
    val deepLink: String? = null,
    val data: Map<String, String> = emptyMap(),
    val isRead: Boolean = false,
    val createdAt: Date,
    val receivedAt: Date,
    val readAt: Date? = null,
    val syncedAt: Date? = null,
    val isSynced: Boolean = false
)

/**
 * Notification preference entity for user settings
 */
@Entity(tableName = "notification_preferences")
data class NotificationPreferenceEntity(
    @PrimaryKey
    val userId: String,
    val marketplaceNotifications: Boolean = true,
    val transferNotifications: Boolean = true,
    val communicationNotifications: Boolean = true,
    val breedingNotifications: Boolean = true,
    val paymentNotifications: Boolean = true,
    val systemNotifications: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "08:00",
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * Topic subscription entity for managing FCM topic subscriptions
 */
@Entity(tableName = "topic_subscriptions")
data class TopicSubscriptionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val topicName: String,
    val isSubscribed: Boolean = true,
    val subscribedAt: Date = Date(),
    val unsubscribedAt: Date? = null,
    val syncedAt: Date? = null,
    val isSynced: Boolean = false
)

/**
 * Notification analytics entity for tracking engagement
 */
@Entity(tableName = "notification_analytics")
data class NotificationAnalyticsEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val notificationId: String,
    val userId: String,
    val eventType: String, // received, opened, clicked, dismissed
    val eventData: Map<String, String> = emptyMap(),
    val timestamp: Date = Date(),
    val syncedAt: Date? = null,
    val isSynced: Boolean = false
)

/**
 * Type converters for complex data types
 */
class NotificationConverters {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, mapType) ?: emptyMap()
    }
    
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}

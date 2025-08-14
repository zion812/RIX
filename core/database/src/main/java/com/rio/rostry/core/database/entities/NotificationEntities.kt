package com.rio.rostry.core.database.entities

import androidx.room.*
import java.util.*

/**
 * Notification preference entity for user settings
 */
@Entity(
    tableName = "notification_preferences",
    indices = [
        Index(value = ["userId"], unique = true)
    ]
)
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
    
    val createdAt: Date,
    val updatedAt: Date = Date()
)

/**
 * Topic subscription entity for FCM topics
 */
@Entity(
    tableName = "topic_subscriptions",
    indices = [
        Index(value = ["userId", "topicName"]),
        Index(value = ["isSubscribed"])
    ]
)
data class TopicSubscriptionEntity(
    @PrimaryKey
    val id: String,
    
    val userId: String,
    val topicName: String,
    val isSubscribed: Boolean = true,
    
    val subscribedAt: Date,
    val unsubscribedAt: Date? = null,
    
    val syncedAt: Date? = null,
    val isSynced: Boolean = false
)

/**
 * Notification analytics entity for tracking
 */
@Entity(
    tableName = "notification_analytics",
    indices = [
        Index(value = ["notificationId"]),
        Index(value = ["userId", "eventType"]),
        Index(value = ["timestamp"])
    ]
)
data class NotificationAnalyticsEntity(
    @PrimaryKey
    val id: String,
    
    val notificationId: String,
    val userId: String,
    val eventType: String, // DELIVERED, OPENED, CLICKED, DISMISSED
    val eventData: String, // JSON data
    
    val timestamp: Date,
    
    val syncedAt: Date? = null,
    val isSynced: Boolean = false
)



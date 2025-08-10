package com.rio.rostry.core.notifications

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.rio.rostry.core.common.exceptions.SyncException
import com.rio.rostry.core.database.RIOLocalDatabase
import com.rio.rostry.core.database.entities.NotificationEntity
import com.rio.rostry.core.database.entities.NotificationPreferenceEntity
import com.rio.rostry.core.network.NetworkStateManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Notification management service for RIO platform
 * Handles FCM token management, preferences, and notification history
 */
@Singleton
class NotificationManagerService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: RIOLocalDatabase,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val messaging: FirebaseMessaging,
    private val networkStateManager: NetworkStateManager,
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        private const val TAG = "NotificationManager"
        private const val PREF_FCM_TOKEN = "fcm_token"
        private const val PREF_TOKEN_UPDATED = "fcm_token_updated"
    }

    /**
     * Initialize notification system
     */
    suspend fun initialize() {
        try {
            // Get FCM token
            val token = messaging.token.await()
            updateFCMToken(token)
            
            // Subscribe to default topics
            subscribeToDefaultTopics()
            
            // Sync notification preferences
            syncNotificationPreferences()
            
        } catch (e: Exception) {
            throw SyncException.NetworkError.ConnectionError("Failed to initialize notifications", e)
        }
    }

    /**
     * Update FCM token
     */
    suspend fun updateFCMToken(token: String) {
        try {
            val currentUserId = auth.currentUser?.uid ?: return
            val lastToken = sharedPreferences.getString(PREF_FCM_TOKEN, null)
            
            if (token != lastToken) {
                // Store token locally
                sharedPreferences.edit()
                    .putString(PREF_FCM_TOKEN, token)
                    .putLong(PREF_TOKEN_UPDATED, System.currentTimeMillis())
                    .apply()

                // Update token in Firestore if online
                if (networkStateManager.isConnected.value) {
                    updateTokenInFirestore(currentUserId, token)
                } else {
                    // Queue for later sync
                    queueTokenUpdate(currentUserId, token)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error updating FCM token", e)
        }
    }

    /**
     * Subscribe to notification topics based on user preferences
     */
    suspend fun subscribeToTopics(topics: List<String>) {
        try {
            for (topic in topics) {
                messaging.subscribeToTopic(topic).await()
                android.util.Log.d(TAG, "Subscribed to topic: $topic")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error subscribing to topics", e)
        }
    }

    /**
     * Unsubscribe from notification topics
     */
    suspend fun unsubscribeFromTopics(topics: List<String>) {
        try {
            for (topic in topics) {
                messaging.unsubscribeFromTopic(topic).await()
                android.util.Log.d(TAG, "Unsubscribed from topic: $topic")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error unsubscribing from topics", e)
        }
    }

    /**
     * Get notification history
     */
    fun getNotificationHistory(limit: Int = 50): Flow<List<NotificationEntity>> {
        return database.notificationDao().getRecentNotifications(limit)
    }

    /**
     * Get unread notification count
     */
    fun getUnreadCount(): Flow<Int> {
        return database.notificationDao().getUnreadCount()
    }

    /**
     * Mark notification as read
     */
    suspend fun markAsRead(notificationId: String) {
        try {
            database.notificationDao().markAsRead(notificationId)
            
            // Track read event
            trackNotificationEvent(notificationId, "read", emptyMap())
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error marking notification as read", e)
        }
    }

    /**
     * Mark all notifications as read
     */
    suspend fun markAllAsRead() {
        try {
            database.notificationDao().markAllAsRead()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error marking all notifications as read", e)
        }
    }

    /**
     * Delete notification
     */
    suspend fun deleteNotification(notificationId: String) {
        try {
            database.notificationDao().deleteById(notificationId)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error deleting notification", e)
        }
    }

    /**
     * Get notification preferences
     */
    fun getNotificationPreferences(): Flow<NotificationPreferenceEntity?> {
        val userId = auth.currentUser?.uid ?: return flowOf(null)
        return database.notificationPreferenceDao().getPreferences(userId)
    }

    /**
     * Update notification preferences
     */
    suspend fun updateNotificationPreferences(preferences: NotificationPreferenceEntity) {
        try {
            database.notificationPreferenceDao().insert(preferences)
            
            // Update topic subscriptions based on preferences
            updateTopicSubscriptions(preferences)
            
            // Sync to Firestore if online
            if (networkStateManager.isConnected.value) {
                syncPreferencesToFirestore(preferences)
            }
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error updating notification preferences", e)
        }
    }

    /**
     * Track notification event for analytics
     */
    suspend fun trackNotificationEvent(
        notificationId: String,
        eventType: String,
        data: Map<String, String>
    ) {
        try {
            if (networkStateManager.isConnected.value) {
                val eventData = mapOf(
                    "notificationId" to notificationId,
                    "eventType" to eventType,
                    "timestamp" to System.currentTimeMillis().toString(),
                    "userId" to (auth.currentUser?.uid ?: ""),
                    "data" to data.toString()
                )
                
                firestore.collection("notification_analytics")
                    .add(eventData)
                    .await()
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error tracking notification event", e)
        }
    }

    /**
     * Sync notification preferences from server
     */
    private suspend fun syncNotificationPreferences() {
        try {
            val userId = auth.currentUser?.uid ?: return
            
            if (networkStateManager.isConnected.value) {
                val doc = firestore.collection("user_notification_preferences")
                    .document(userId)
                    .get()
                    .await()
                
                if (doc.exists()) {
                    val serverPreferences = doc.toObject(NotificationPreferenceEntity::class.java)
                    serverPreferences?.let {
                        database.notificationPreferenceDao().insert(it)
                        updateTopicSubscriptions(it)
                    }
                } else {
                    // Create default preferences
                    createDefaultPreferences(userId)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error syncing notification preferences", e)
        }
    }

    /**
     * Create default notification preferences
     */
    private suspend fun createDefaultPreferences(userId: String) {
        val defaultPreferences = NotificationPreferenceEntity(
            userId = userId,
            marketplaceNotifications = true,
            transferNotifications = true,
            communicationNotifications = true,
            breedingNotifications = true,
            paymentNotifications = true,
            systemNotifications = true,
            quietHoursEnabled = false,
            quietHoursStart = "22:00",
            quietHoursEnd = "08:00",
            soundEnabled = true,
            vibrationEnabled = true,
            createdAt = Date(),
            updatedAt = Date()
        )
        
        database.notificationPreferenceDao().insert(defaultPreferences)
        
        if (networkStateManager.isConnected.value) {
            syncPreferencesToFirestore(defaultPreferences)
        }
    }

    /**
     * Update topic subscriptions based on preferences
     */
    private suspend fun updateTopicSubscriptions(preferences: NotificationPreferenceEntity) {
        val topicsToSubscribe = mutableListOf<String>()
        val topicsToUnsubscribe = mutableListOf<String>()
        
        // Marketplace notifications
        if (preferences.marketplaceNotifications) {
            topicsToSubscribe.add("marketplace_updates")
        } else {
            topicsToUnsubscribe.add("marketplace_updates")
        }
        
        // System notifications
        if (preferences.systemNotifications) {
            topicsToSubscribe.add("system_announcements")
        } else {
            topicsToUnsubscribe.add("system_announcements")
        }
        
        // Breeding notifications
        if (preferences.breedingNotifications) {
            topicsToSubscribe.add("breeding_tips")
            topicsToSubscribe.add("health_alerts")
        } else {
            topicsToUnsubscribe.add("breeding_tips")
            topicsToUnsubscribe.add("health_alerts")
        }
        
        // Subscribe/unsubscribe
        if (topicsToSubscribe.isNotEmpty()) {
            subscribeToTopics(topicsToSubscribe)
        }
        if (topicsToUnsubscribe.isNotEmpty()) {
            unsubscribeFromTopics(topicsToUnsubscribe)
        }
    }

    /**
     * Subscribe to default topics
     */
    private suspend fun subscribeToDefaultTopics() {
        val defaultTopics = listOf(
            "all_users",
            "system_announcements"
        )
        
        subscribeToTopics(defaultTopics)
    }

    /**
     * Update token in Firestore
     */
    private suspend fun updateTokenInFirestore(userId: String, token: String) {
        try {
            val userDoc = firestore.collection("users").document(userId)
            
            userDoc.update(
                mapOf(
                    "fcmTokens" to com.google.firebase.firestore.FieldValue.arrayUnion(token),
                    "lastTokenUpdate" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            ).await()
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error updating token in Firestore", e)
        }
    }

    /**
     * Queue token update for later sync
     */
    private suspend fun queueTokenUpdate(userId: String, token: String) {
        // This would typically use the existing offline sync mechanism
        // For now, we'll just log it
        android.util.Log.d(TAG, "Queued FCM token update for user: $userId")
    }

    /**
     * Sync preferences to Firestore
     */
    private suspend fun syncPreferencesToFirestore(preferences: NotificationPreferenceEntity) {
        try {
            firestore.collection("user_notification_preferences")
                .document(preferences.userId)
                .set(preferences)
                .await()
                
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error syncing preferences to Firestore", e)
        }
    }
}

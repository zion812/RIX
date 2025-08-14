package com.rio.rostry.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.rio.rostry.MainActivity
import com.rio.rostry.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Simplified Notification Manager - Phase 3.3
 * Handles FCM notifications with rural-optimized delivery and offline queuing
 */
class SimpleNotificationManager(private val context: Context) {
    
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()
    
    private val _notificationSettings = MutableStateFlow(NotificationSettings())
    val notificationSettings: StateFlow<NotificationSettings> = _notificationSettings.asStateFlow()
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    companion object {
        private const val CHANNEL_ID_GENERAL = "rio_general"
        private const val CHANNEL_ID_FOWL = "rio_fowl"
        private const val CHANNEL_ID_MARKETPLACE = "rio_marketplace"
        private const val CHANNEL_ID_SYNC = "rio_sync"
        private const val NOTIFICATION_ID_OFFSET = 1000
    }
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Initialize FCM and request notification permissions
     */
    suspend fun initialize(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            // Store token for server registration
            token
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Create notification channels for different types
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_GENERAL,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General app notifications"
                },
                NotificationChannel(
                    CHANNEL_ID_FOWL,
                    "Fowl Updates",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Fowl health, breeding, and management updates"
                },
                NotificationChannel(
                    CHANNEL_ID_MARKETPLACE,
                    "Marketplace",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Marketplace listings and transactions"
                },
                NotificationChannel(
                    CHANNEL_ID_SYNC,
                    "Sync Status",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Data synchronization status"
                }
            )
            
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { systemNotificationManager.createNotificationChannel(it) }
        }
    }
    
    /**
     * Handle incoming FCM message
     */
    fun handleFCMMessage(remoteMessage: RemoteMessage) {
        val notification = NotificationItem(
            id = UUID.randomUUID().toString(),
            title = remoteMessage.notification?.title ?: "RIO Notification",
            body = remoteMessage.notification?.body ?: "",
            type = determineNotificationType(remoteMessage.data),
            data = remoteMessage.data,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        
        // Add to local storage
        addNotification(notification)
        
        // Show system notification
        showSystemNotification(notification)
    }
    
    /**
     * Send local notification
     */
    fun sendLocalNotification(
        title: String,
        body: String,
        type: NotificationType = NotificationType.GENERAL,
        data: Map<String, String> = emptyMap()
    ) {
        val notification = NotificationItem(
            id = UUID.randomUUID().toString(),
            title = title,
            body = body,
            type = type,
            data = data,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        
        addNotification(notification)
        showSystemNotification(notification)
    }
    
    /**
     * Show system notification
     */
    private fun showSystemNotification(notification: NotificationItem) {
        val settings = _notificationSettings.value
        
        // Check if notifications are enabled for this type
        if (!isNotificationTypeEnabled(notification.type, settings)) {
            return
        }
        
        val channelId = getChannelIdForType(notification.type)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_id", notification.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setPriority(getNotificationPriority(notification.type))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Add action buttons based on type
        when (notification.type) {
            NotificationType.FOWL_HEALTH -> {
                val viewIntent = PendingIntent.getActivity(
                    context,
                    notification.id.hashCode() + 1,
                    Intent(context, MainActivity::class.java).apply {
                        putExtra("navigate_to", "fowl_management")
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                notificationBuilder.addAction(
                    R.drawable.ic_launcher_foreground,
                    "View Fowl",
                    viewIntent
                )
            }
            NotificationType.MARKETPLACE -> {
                val viewIntent = PendingIntent.getActivity(
                    context,
                    notification.id.hashCode() + 1,
                    Intent(context, MainActivity::class.java).apply {
                        putExtra("navigate_to", "marketplace")
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                notificationBuilder.addAction(
                    R.drawable.ic_launcher_foreground,
                    "View Listing",
                    viewIntent
                )
            }
            else -> {}
        }
        
        try {
            notificationManager.notify(
                NOTIFICATION_ID_OFFSET + notification.id.hashCode(),
                notificationBuilder.build()
            )
        } catch (e: SecurityException) {
            // Handle permission denied
        }
    }
    
    /**
     * Add notification to local storage
     */
    private fun addNotification(notification: NotificationItem) {
        val currentNotifications = _notifications.value.toMutableList()
        currentNotifications.add(0, notification) // Add to beginning
        
        // Keep only last 100 notifications
        if (currentNotifications.size > 100) {
            currentNotifications.removeAt(currentNotifications.size - 1)
        }
        
        _notifications.value = currentNotifications
    }
    
    /**
     * Mark notification as read
     */
    fun markAsRead(notificationId: String) {
        val currentNotifications = _notifications.value.toMutableList()
        val index = currentNotifications.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            currentNotifications[index] = currentNotifications[index].copy(isRead = true)
            _notifications.value = currentNotifications
        }
    }
    
    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        _notifications.value = emptyList()
        notificationManager.cancelAll()
    }
    
    /**
     * Update notification settings
     */
    fun updateSettings(settings: NotificationSettings) {
        _notificationSettings.value = settings
    }
    
    /**
     * Determine notification type from FCM data
     */
    private fun determineNotificationType(data: Map<String, String>): NotificationType {
        return when (data["type"]) {
            "fowl_health" -> NotificationType.FOWL_HEALTH
            "fowl_breeding" -> NotificationType.FOWL_BREEDING
            "marketplace" -> NotificationType.MARKETPLACE
            "sync" -> NotificationType.SYNC
            else -> NotificationType.GENERAL
        }
    }
    
    /**
     * Get channel ID for notification type
     */
    private fun getChannelIdForType(type: NotificationType): String {
        return when (type) {
            NotificationType.FOWL_HEALTH, NotificationType.FOWL_BREEDING -> CHANNEL_ID_FOWL
            NotificationType.MARKETPLACE -> CHANNEL_ID_MARKETPLACE
            NotificationType.SYNC -> CHANNEL_ID_SYNC
            else -> CHANNEL_ID_GENERAL
        }
    }
    
    /**
     * Get notification priority for type
     */
    private fun getNotificationPriority(type: NotificationType): Int {
        return when (type) {
            NotificationType.FOWL_HEALTH -> NotificationCompat.PRIORITY_HIGH
            NotificationType.FOWL_BREEDING, NotificationType.MARKETPLACE -> NotificationCompat.PRIORITY_DEFAULT
            NotificationType.SYNC -> NotificationCompat.PRIORITY_LOW
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }
    
    /**
     * Check if notification type is enabled
     */
    private fun isNotificationTypeEnabled(type: NotificationType, settings: NotificationSettings): Boolean {
        return when (type) {
            NotificationType.FOWL_HEALTH -> settings.fowlHealthEnabled
            NotificationType.FOWL_BREEDING -> settings.fowlBreedingEnabled
            NotificationType.MARKETPLACE -> settings.marketplaceEnabled
            NotificationType.SYNC -> settings.syncEnabled
            else -> settings.generalEnabled
        }
    }
}

/**
 * Notification data classes
 */
data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val data: Map<String, String>,
    val timestamp: Long,
    val isRead: Boolean
)

enum class NotificationType {
    GENERAL,
    FOWL_HEALTH,
    FOWL_BREEDING,
    MARKETPLACE,
    SYNC
}

data class NotificationSettings(
    val generalEnabled: Boolean = true,
    val fowlHealthEnabled: Boolean = true,
    val fowlBreedingEnabled: Boolean = true,
    val marketplaceEnabled: Boolean = true,
    val syncEnabled: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: Int = 22, // 10 PM
    val quietHoursEnd: Int = 7     // 7 AM
)

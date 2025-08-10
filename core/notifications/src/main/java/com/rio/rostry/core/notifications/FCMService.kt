package com.rio.rostry.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rio.rostry.core.common.exceptions.SyncException
import com.rio.rostry.core.database.RIOLocalDatabase
import com.rio.rostry.core.database.entities.NotificationEntity
import com.rio.rostry.core.network.NetworkStateManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.IOException
import java.net.URL
import java.util.*
import javax.inject.Inject

/**
 * Firebase Cloud Messaging service for RIO platform
 * Handles real-time notifications with offline-first architecture integration
 */
@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var database: RIOLocalDatabase
    
    @Inject
    lateinit var networkStateManager: NetworkStateManager
    
    @Inject
    lateinit var notificationManager: NotificationManagerService
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "FCMService"
        private const val NOTIFICATION_ID_OFFSET = 1000
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        serviceScope.launch {
            try {
                handleIncomingMessage(remoteMessage)
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error handling FCM message", e)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        serviceScope.launch {
            try {
                notificationManager.updateFCMToken(token)
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error updating FCM token", e)
            }
        }
    }

    /**
     * Handle incoming FCM message
     */
    private suspend fun handleIncomingMessage(remoteMessage: RemoteMessage) {
        val notification = remoteMessage.notification
        val data = remoteMessage.data

        if (notification != null) {
            // Store notification in local database
            val notificationEntity = createNotificationEntity(notification, data)
            database.notificationDao().insert(notificationEntity)

            // Show notification if app is in background or data-only message
            if (!isAppInForeground() || notification.title.isNullOrEmpty()) {
                showNotification(notificationEntity, data)
            }

            // Track notification received
            notificationManager.trackNotificationEvent(
                notificationEntity.id,
                "received",
                data
            )
        }
    }

    /**
     * Create notification entity from FCM message
     */
    private fun createNotificationEntity(
        notification: RemoteMessage.Notification,
        data: Map<String, String>
    ): NotificationEntity {
        return NotificationEntity(
            id = data["notificationId"] ?: UUID.randomUUID().toString(),
            title = notification.title ?: "",
            body = notification.body ?: "",
            imageUrl = notification.imageUrl?.toString(),
            category = data["category"] ?: "general",
            priority = data["priority"] ?: "normal",
            deepLink = data["deepLink"],
            data = data.toMap(),
            isRead = false,
            createdAt = Date(),
            receivedAt = Date()
        )
    }

    /**
     * Show notification to user
     */
    private suspend fun showNotification(
        notificationEntity: NotificationEntity,
        data: Map<String, String>
    ) {
        val channelId = getChannelIdForCategory(notificationEntity.category)
        val notificationId = notificationEntity.id.hashCode() + NOTIFICATION_ID_OFFSET

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notificationEntity.title)
            .setContentText(notificationEntity.body)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(getNotificationPriority(notificationEntity.priority))
            .setColor(getNotificationColor(notificationEntity.category))

        // Add large icon/image if available
        notificationEntity.imageUrl?.let { imageUrl ->
            val bitmap = loadImageFromUrl(imageUrl)
            bitmap?.let {
                builder.setLargeIcon(it)
                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(it)
                        .bigLargeIcon(null as Bitmap?)
                )
            }
        }

        // Add deep link intent
        val intent = createDeepLinkIntent(notificationEntity.deepLink, data)
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent)

        // Add action buttons
        addNotificationActions(builder, notificationEntity, data)

        // Show notification
        val notificationManager = NotificationManagerCompat.from(this)
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(notificationId, builder.build())
        }
    }

    /**
     * Create notification channels for different categories
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(
                    "marketplace_channel",
                    "Marketplace",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "New listings, price alerts, and marketplace updates"
                    enableVibration(true)
                    setShowBadge(true)
                },
                NotificationChannel(
                    "transfer_channel",
                    "Transfers & Verification",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Rooster transfers and verification updates"
                    enableVibration(true)
                    setShowBadge(true)
                },
                NotificationChannel(
                    "communication_channel",
                    "Messages & Chat",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "New messages and chat notifications"
                    enableVibration(true)
                    setShowBadge(true)
                },
                NotificationChannel(
                    "breeding_channel",
                    "Breeding & Health",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Breeding reminders and health alerts"
                    enableVibration(false)
                    setShowBadge(true)
                },
                NotificationChannel(
                    "payment_channel",
                    "Payments & Coins",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Payment confirmations and coin transactions"
                    enableVibration(true)
                    setShowBadge(true)
                },
                NotificationChannel(
                    "system_channel",
                    "System Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "App updates and system announcements"
                    enableVibration(false)
                    setShowBadge(false)
                }
            )

            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    /**
     * Add action buttons to notification
     */
    private fun addNotificationActions(
        builder: NotificationCompat.Builder,
        notification: NotificationEntity,
        data: Map<String, String>
    ) {
        when (notification.category) {
            "marketplace" -> {
                // View listing action
                val viewIntent = createDeepLinkIntent(notification.deepLink, data)
                val viewPendingIntent = PendingIntent.getActivity(
                    this,
                    notification.id.hashCode() + 1,
                    viewIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    R.drawable.ic_view,
                    "View",
                    viewPendingIntent
                )

                // Contact seller action (if available)
                data["sellerId"]?.let { sellerId ->
                    val chatIntent = createDeepLinkIntent("rio://chat/user/$sellerId", data)
                    val chatPendingIntent = PendingIntent.getActivity(
                        this,
                        notification.id.hashCode() + 2,
                        chatIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    builder.addAction(
                        R.drawable.ic_chat,
                        "Contact",
                        chatPendingIntent
                    )
                }
            }

            "communication" -> {
                // Reply action
                val replyIntent = createDeepLinkIntent(notification.deepLink, data)
                val replyPendingIntent = PendingIntent.getActivity(
                    this,
                    notification.id.hashCode() + 1,
                    replyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    R.drawable.ic_reply,
                    "Reply",
                    replyPendingIntent
                )
            }

            "payment" -> {
                // View wallet action
                val walletIntent = createDeepLinkIntent("rio://wallet", data)
                val walletPendingIntent = PendingIntent.getActivity(
                    this,
                    notification.id.hashCode() + 1,
                    walletIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    R.drawable.ic_wallet,
                    "Wallet",
                    walletPendingIntent
                )
            }
        }
    }

    /**
     * Create deep link intent
     */
    private fun createDeepLinkIntent(deepLink: String?, data: Map<String, String>): Intent {
        return if (!deepLink.isNullOrEmpty()) {
            Intent(Intent.ACTION_VIEW).apply {
                setData(android.net.Uri.parse(deepLink))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("notification_data", HashMap(data))
            }
        } else {
            packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("notification_data", HashMap(data))
            } ?: Intent()
        }
    }

    /**
     * Load image from URL for notification
     */
    private suspend fun loadImageFromUrl(imageUrl: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                BitmapFactory.decodeStream(url.openConnection().getInputStream())
            } catch (e: IOException) {
                android.util.Log.w(TAG, "Failed to load notification image: $imageUrl", e)
                null
            }
        }
    }

    /**
     * Get notification channel ID for category
     */
    private fun getChannelIdForCategory(category: String): String {
        return when (category) {
            "marketplace" -> "marketplace_channel"
            "transfer" -> "transfer_channel"
            "communication" -> "communication_channel"
            "breeding" -> "breeding_channel"
            "payment" -> "payment_channel"
            "system" -> "system_channel"
            else -> "system_channel"
        }
    }

    /**
     * Get notification priority
     */
    private fun getNotificationPriority(priority: String): Int {
        return when (priority) {
            "low" -> NotificationCompat.PRIORITY_LOW
            "normal" -> NotificationCompat.PRIORITY_DEFAULT
            "high" -> NotificationCompat.PRIORITY_HIGH
            "urgent" -> NotificationCompat.PRIORITY_MAX
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }

    /**
     * Get notification color for category
     */
    private fun getNotificationColor(category: String): Int {
        return when (category) {
            "marketplace" -> 0xFF4CAF50.toInt() // Green
            "transfer" -> 0xFF2196F3.toInt()    // Blue
            "communication" -> 0xFF9C27B0.toInt() // Purple
            "breeding" -> 0xFFFF9800.toInt()    // Orange
            "payment" -> 0xFFFF6B35.toInt()     // RIO Orange
            "system" -> 0xFF607D8B.toInt()      // Blue Grey
            else -> 0xFFFF6B35.toInt()          // Default RIO Orange
        }
    }

    /**
     * Check if app is in foreground
     */
    private fun isAppInForeground(): Boolean {
        // This would typically use ActivityLifecycleCallbacks or similar
        // For now, return false to always show notifications
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

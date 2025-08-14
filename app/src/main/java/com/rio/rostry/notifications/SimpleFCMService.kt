package com.rio.rostry.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Simple FCM Service - Phase 3.3
 * Handles Firebase Cloud Messaging for rural-optimized notifications
 */
class SimpleFCMService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "SimpleFCMService"
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "From: ${remoteMessage.from}")
        
        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage)
        }
        
        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            handleNotificationMessage(remoteMessage)
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        
        // Send token to server
        sendTokenToServer(token)
    }
    
    /**
     * Handle data message from FCM
     */
    private fun handleDataMessage(remoteMessage: RemoteMessage) {
        val notificationManager = SimpleNotificationManager(this)
        notificationManager.handleFCMMessage(remoteMessage)
    }
    
    /**
     * Handle notification message from FCM
     */
    private fun handleNotificationMessage(remoteMessage: RemoteMessage) {
        val notificationManager = SimpleNotificationManager(this)
        notificationManager.handleFCMMessage(remoteMessage)
    }
    
    /**
     * Send FCM token to server for registration
     */
    private fun sendTokenToServer(token: String) {
        // TODO: Implement server token registration
        Log.d(TAG, "Token sent to server: $token")
    }
}

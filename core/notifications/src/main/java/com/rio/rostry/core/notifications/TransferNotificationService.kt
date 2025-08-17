package com.rio.rostry.core.notifications

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.rio.rostry.core.common.model.Result
import com.rio.rostry.core.database.entities.FowlEntity
import com.rio.rostry.core.database.entities.TransferLogEntity
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for sending transfer-related notifications
 */
@Singleton
class TransferNotificationService @Inject constructor(
    private val context: Context,
    private val notificationManagerService: NotificationManagerService
) {
    
    companion object {
        private const val TAG = "TransferNotificationService"
        private const val TRANSFER_TOPIC = "transfers"
    }
    
    /**
     * Send notification when a transfer is initiated
     */
    suspend fun sendTransferInitiatedNotification(
        transfer: TransferLogEntity,
        fowl: FowlEntity
    ): Result<Boolean> {
        return try {
            val title = "Transfer Initiated"
            val body = "Transfer of ${fowl.name ?: "a fowl"} to ${transfer.toUserId} has been initiated"
            
            // Send FCM message
            sendFcmMessage(
                title = title,
                body = body,
                category = "transfer",
                data = mapOf(
                    "transferId" to transfer.id,
                    "fowlId" to fowl.id,
                    "eventType" to "transfer_initiated",
                    "toUserId" to transfer.toUserId
                )
            )
            
            // Store local notification
            notificationManagerService.createLocalNotification(
                title = title,
                body = body,
                category = "transfer",
                priority = "high",
                data = mapOf(
                    "transferId" to transfer.id,
                    "fowlId" to fowl.id,
                    "eventType" to "transfer_initiated"
                )
            )
            
            Result.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending transfer initiated notification", e)
            Result.Error(e)
        }
    }
    
    /**
     * Send notification when a transfer is verified
     */
    suspend fun sendTransferVerifiedNotification(
        transfer: TransferLogEntity,
        fowl: FowlEntity
    ): Result<Boolean> {
        return try {
            val title = "Transfer Verified"
            val body = "Transfer of ${fowl.name ?: "a fowl"} from ${transfer.fromUserId} has been verified"
            
            // Send FCM message
            sendFcmMessage(
                title = title,
                body = body,
                category = "transfer",
                data = mapOf(
                    "transferId" to transfer.id,
                    "fowlId" to fowl.id,
                    "eventType" to "transfer_verified",
                    "fromUserId" to transfer.fromUserId
                )
            )
            
            // Store local notification
            notificationManagerService.createLocalNotification(
                title = title,
                body = body,
                category = "transfer",
                priority = "high",
                data = mapOf(
                    "transferId" to transfer.id,
                    "fowlId" to fowl.id,
                    "eventType" to "transfer_verified"
                )
            )
            
            Result.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending transfer verified notification", e)
            Result.Error(e)
        }
    }
    
    /**
     * Send notification when a transfer is rejected
     */
    suspend fun sendTransferRejectedNotification(
        transfer: TransferLogEntity,
        fowl: FowlEntity,
        reason: String
    ): Result<Boolean> {
        return try {
            val title = "Transfer Rejected"
            val body = "Transfer of ${fowl.name ?: "a fowl"} from ${transfer.fromUserId} has been rejected: $reason"
            
            // Send FCM message
            sendFcmMessage(
                title = title,
                body = body,
                category = "transfer",
                data = mapOf(
                    "transferId" to transfer.id,
                    "fowlId" to fowl.id,
                    "eventType" to "transfer_rejected",
                    "fromUserId" to transfer.fromUserId,
                    "reason" to reason
                )
            )
            
            // Store local notification
            notificationManagerService.createLocalNotification(
                title = title,
                body = body,
                category = "transfer",
                priority = "high",
                data = mapOf(
                    "transferId" to transfer.id,
                    "fowlId" to fowl.id,
                    "eventType" to "transfer_rejected",
                    "reason" to reason
                )
            )
            
            Result.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending transfer rejected notification", e)
            Result.Error(e)
        }
    }
    
    /**
     * Subscribe user to transfer notifications
     */
    suspend fun subscribeToTransferNotifications(): Result<Boolean> {
        return try {
            FirebaseMessaging.getInstance()
                .subscribeToTopic(TRANSFER_TOPIC)
                .await()
            Result.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to transfer notifications", e)
            Result.Error(e)
        }
    }
    
    /**
     * Unsubscribe user from transfer notifications
     */
    suspend fun unsubscribeFromTransferNotifications(): Result<Boolean> {
        return try {
            FirebaseMessaging.getInstance()
                .unsubscribeFromTopic(TRANSFER_TOPIC)
                .await()
            Result.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error unsubscribing from transfer notifications", e)
            Result.Error(e)
        }
    }
    
    /**
     * Send FCM message
     */
    private suspend fun sendFcmMessage(
        title: String,
        body: String,
        category: String,
        data: Map<String, String>
    ) {
        // In a real implementation, this would send a message to FCM
        // For now, we'll just log it
        Log.d(TAG, "Would send FCM message: $title - $body with data: $data")
    }
}
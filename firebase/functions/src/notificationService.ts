import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

const db = admin.firestore();
const messaging = admin.messaging();
const REGION = 'asia-south1';

/**
 * Comprehensive FCM-based notification service for RIO platform
 * Supports real-time messaging with offline-first architecture integration
 */

export interface NotificationPayload {
  title: string;
  body: string;
  imageUrl?: string;
  data?: { [key: string]: string };
  category: NotificationCategory;
  priority: NotificationPriority;
  targetUsers?: string[];
  targetTopics?: string[];
  scheduledTime?: Date;
  deepLink?: string;
  actions?: NotificationAction[];
}

export interface NotificationAction {
  id: string;
  title: string;
  icon?: string;
  deepLink?: string;
}

export enum NotificationCategory {
  MARKETPLACE = 'marketplace',
  TRANSFER = 'transfer',
  COMMUNICATION = 'communication',
  BREEDING = 'breeding',
  PAYMENT = 'payment',
  SYSTEM = 'system'
}

export enum NotificationPriority {
  LOW = 'low',
  NORMAL = 'normal',
  HIGH = 'high',
  URGENT = 'urgent'
}

/**
 * Send notification to specific users or topics
 */
export const sendNotification = functions
  .region(REGION)
  .https
  .onCall(async (data: NotificationPayload, context) => {
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    try {
      const notificationId = await processNotification(data, context.auth.uid);
      
      return {
        success: true,
        notificationId: notificationId,
        message: 'Notification sent successfully'
      };

    } catch (error) {
      console.error('Error sending notification:', error);
      throw new functions.https.HttpsError('internal', 'Failed to send notification');
    }
  });

/**
 * Process and send notification with proper targeting and personalization
 */
export async function processNotification(payload: NotificationPayload, senderId: string): Promise<string> {
  const notificationId = `notif_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  // Create notification record
  const notificationRecord = {
    id: notificationId,
    senderId: senderId,
    title: payload.title,
    body: payload.body,
    imageUrl: payload.imageUrl,
    category: payload.category,
    priority: payload.priority,
    data: payload.data || {},
    deepLink: payload.deepLink,
    actions: payload.actions || [],
    targetUsers: payload.targetUsers || [],
    targetTopics: payload.targetTopics || [],
    scheduledTime: payload.scheduledTime || new Date(),
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    status: 'pending',
    deliveryStats: {
      sent: 0,
      delivered: 0,
      failed: 0,
      opened: 0
    }
  };

  // Save notification record
  await db.collection('notifications').doc(notificationId).set(notificationRecord);

  // Process immediate or scheduled sending
  if (payload.scheduledTime && payload.scheduledTime > new Date()) {
    await scheduleNotification(notificationId, payload.scheduledTime);
  } else {
    await sendImmediateNotification(notificationId, payload);
  }

  return notificationId;
}

/**
 * Send immediate notification
 */
async function sendImmediateNotification(notificationId: string, payload: NotificationPayload): Promise<void> {
  const fcmPayload = await buildFCMPayload(payload);
  
  try {
    let response;
    
    if (payload.targetUsers && payload.targetUsers.length > 0) {
      // Send to specific users
      const tokens = await getUserTokens(payload.targetUsers);
      if (tokens.length > 0) {
        response = await messaging.sendMulticast({
          tokens: tokens,
          ...fcmPayload
        });
        
        await updateDeliveryStats(notificationId, response.successCount, response.failureCount);
      }
    }
    
    if (payload.targetTopics && payload.targetTopics.length > 0) {
      // Send to topics
      for (const topic of payload.targetTopics) {
        await messaging.send({
          topic: topic,
          ...fcmPayload
        });
      }
    }

    // Update notification status
    await db.collection('notifications').doc(notificationId).update({
      status: 'sent',
      sentAt: admin.firestore.FieldValue.serverTimestamp()
    });

  } catch (error) {
    console.error('Error sending FCM notification:', error);
    
    await db.collection('notifications').doc(notificationId).update({
      status: 'failed',
      failureReason: error instanceof Error ? error.message : 'Unknown error',
      failedAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    throw error;
  }
}

/**
 * Build FCM payload with proper formatting
 */
async function buildFCMPayload(payload: NotificationPayload): Promise<any> {
  const fcmPayload: any = {
    notification: {
      title: payload.title,
      body: payload.body
    },
    data: {
      category: payload.category,
      priority: payload.priority,
      notificationId: `notif_${Date.now()}`,
      deepLink: payload.deepLink || '',
      ...payload.data
    },
    android: {
      priority: getAndroidPriority(payload.priority),
      notification: {
        channelId: getNotificationChannel(payload.category),
        icon: 'ic_notification',
        color: '#FF6B35',
        sound: 'default',
        clickAction: payload.deepLink || 'FLUTTER_NOTIFICATION_CLICK'
      }
    },
    apns: {
      payload: {
        aps: {
          sound: 'default',
          badge: 1
        }
      }
    }
  };

  // Add image if provided
  if (payload.imageUrl) {
    fcmPayload.notification.imageUrl = payload.imageUrl;
    fcmPayload.android.notification.imageUrl = payload.imageUrl;
  }

  // Add actions for Android
  if (payload.actions && payload.actions.length > 0) {
    fcmPayload.android.notification.actions = payload.actions.map(action => ({
      action: action.id,
      title: action.title,
      icon: action.icon || 'ic_action_default'
    }));
  }

  return fcmPayload;
}

/**
 * Get user FCM tokens
 */
async function getUserTokens(userIds: string[]): Promise<string[]> {
  const tokens: string[] = [];
  
  for (const userId of userIds) {
    try {
      const userDoc = await db.collection('users').doc(userId).get();
      if (userDoc.exists) {
        const userData = userDoc.data();
        if (userData?.fcmTokens && Array.isArray(userData.fcmTokens)) {
          tokens.push(...userData.fcmTokens);
        }
      }
    } catch (error) {
      console.error(`Error getting tokens for user ${userId}:`, error);
    }
  }
  
  return tokens.filter(token => token && token.length > 0);
}

/**
 * Update delivery statistics
 */
async function updateDeliveryStats(notificationId: string, successCount: number, failureCount: number): Promise<void> {
  await db.collection('notifications').doc(notificationId).update({
    'deliveryStats.sent': admin.firestore.FieldValue.increment(successCount + failureCount),
    'deliveryStats.delivered': admin.firestore.FieldValue.increment(successCount),
    'deliveryStats.failed': admin.firestore.FieldValue.increment(failureCount)
  });
}

/**
 * Schedule notification for later delivery
 */
async function scheduleNotification(notificationId: string, scheduledTime: Date): Promise<void> {
  // In a real implementation, you would use Cloud Scheduler or Pub/Sub
  // For now, we'll store it and process via a scheduled function
  await db.collection('scheduled_notifications').doc(notificationId).set({
    notificationId: notificationId,
    scheduledTime: admin.firestore.Timestamp.fromDate(scheduledTime),
    status: 'scheduled',
    createdAt: admin.firestore.FieldValue.serverTimestamp()
  });
}

/**
 * Get Android priority from notification priority
 */
function getAndroidPriority(priority: NotificationPriority): string {
  switch (priority) {
    case NotificationPriority.LOW:
      return 'min';
    case NotificationPriority.NORMAL:
      return 'default';
    case NotificationPriority.HIGH:
      return 'high';
    case NotificationPriority.URGENT:
      return 'max';
    default:
      return 'default';
  }
}

/**
 * Get notification channel ID based on category
 */
function getNotificationChannel(category: NotificationCategory): string {
  switch (category) {
    case NotificationCategory.MARKETPLACE:
      return 'marketplace_channel';
    case NotificationCategory.TRANSFER:
      return 'transfer_channel';
    case NotificationCategory.COMMUNICATION:
      return 'communication_channel';
    case NotificationCategory.BREEDING:
      return 'breeding_channel';
    case NotificationCategory.PAYMENT:
      return 'payment_channel';
    case NotificationCategory.SYSTEM:
      return 'system_channel';
    default:
      return 'default_channel';
  }
}

/**
 * Process scheduled notifications
 */
export const processScheduledNotifications = functions
  .region(REGION)
  .pubsub
  .schedule('every 5 minutes')
  .onRun(async (context) => {
    const now = admin.firestore.Timestamp.now();
    
    try {
      const scheduledSnapshot = await db.collection('scheduled_notifications')
        .where('status', '==', 'scheduled')
        .where('scheduledTime', '<=', now)
        .limit(100)
        .get();

      const batch = db.batch();
      const notifications = [];

      for (const doc of scheduledSnapshot.docs) {
        const scheduledData = doc.data();
        
        // Get the original notification
        const notificationDoc = await db.collection('notifications')
          .doc(scheduledData.notificationId)
          .get();
        
        if (notificationDoc.exists) {
          const notificationData = notificationDoc.data();
          if (notificationData) {
            notifications.push(notificationData);

            // Mark as processing
            batch.update(doc.ref, { status: 'processing' });
          }
        }
      }

      await batch.commit();

      // Process each notification
      for (const notification of notifications) {
        try {
          await sendImmediateNotification(notification.id, notification as NotificationPayload);

          // Mark as sent
          await db.collection('scheduled_notifications')
            .doc(notification.id)
            .update({ status: 'sent' });

        } catch (error) {
          console.error(`Error processing scheduled notification ${notification.id}:`, error);

          await db.collection('scheduled_notifications')
            .doc(notification.id)
            .update({
              status: 'failed',
              failureReason: error instanceof Error ? error.message : 'Unknown error'
            });
        }
      }

      console.log(`Processed ${notifications.length} scheduled notifications`);

    } catch (error) {
      console.error('Error processing scheduled notifications:', error);
    }
  });

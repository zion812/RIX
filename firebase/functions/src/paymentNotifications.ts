import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import { NotificationPayload, NotificationCategory, NotificationPriority, processNotification } from './notificationService';

const db = admin.firestore();
const REGION = 'asia-south1';

/**
 * Payment system notification triggers for RIO coin-based payments
 * Handles purchase confirmations, refunds, disputes, and balance alerts
 */

/**
 * Send payment notification based on type
 */
export const sendPaymentNotification = functions
  .region(REGION)
  .https
  .onCall(async (data, context) => {
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { userId, notificationType, paymentData } = data;

    try {
      let notification: NotificationPayload;

      switch (notificationType) {
        case 'purchase_success':
          notification = await buildPurchaseSuccessNotification(userId, paymentData);
          break;
        case 'purchase_failed':
          notification = await buildPurchaseFailedNotification(userId, paymentData);
          break;
        case 'refund_processed':
          notification = await buildRefundProcessedNotification(userId, paymentData);
          break;
        case 'dispute_created':
          notification = await buildDisputeCreatedNotification(userId, paymentData);
          break;
        case 'low_balance':
          notification = await buildLowBalanceNotification(userId, paymentData);
          break;
        default:
          throw new functions.https.HttpsError('invalid-argument', 'Invalid notification type');
      }

      await processNotification(notification, 'system');

      return { success: true, message: 'Payment notification sent' };

    } catch (error) {
      console.error('Error sending payment notification:', error);
      throw new functions.https.HttpsError('internal', 'Failed to send payment notification');
    }
  });

/**
 * Trigger when coin transaction is created
 */
export const onCoinTransactionCreated = functions
  .region(REGION)
  .firestore
  .document('coin_transactions/{transactionId}')
  .onCreate(async (snap, context) => {
    const transaction = snap.data();
    const transactionId = context.params.transactionId;

    try {
      let notification: NotificationPayload | null = null;

      switch (transaction.type) {
        case 'CREDIT':
          if (transaction.purpose === 'COIN_PURCHASE') {
            notification = {
              title: '🪙 Coins Added Successfully',
              body: `${transaction.amount} coins added to your account`,
              category: NotificationCategory.PAYMENT,
              priority: NotificationPriority.HIGH,
              targetUsers: [transaction.userId],
              deepLink: `rio://wallet/transactions/${transactionId}`,
              data: {
                transactionId: transactionId,
                amount: transaction.amount.toString(),
                type: transaction.type,
                purpose: transaction.purpose,
                newBalance: transaction.balanceAfter.toString()
              },
              actions: [
                {
                  id: 'view_wallet',
                  title: 'View Wallet',
                  deepLink: 'rio://wallet'
                },
                {
                  id: 'shop_now',
                  title: 'Shop Now',
                  deepLink: 'rio://marketplace'
                }
              ]
            };
          }
          break;

        case 'SPEND':
          notification = {
            title: '💸 Coins Spent',
            body: `${transaction.amount} coins spent on ${transaction.purpose}`,
            category: NotificationCategory.PAYMENT,
            priority: NotificationPriority.NORMAL,
            targetUsers: [transaction.userId],
            deepLink: `rio://wallet/transactions/${transactionId}`,
            data: {
              transactionId: transactionId,
              amount: transaction.amount.toString(),
              type: transaction.type,
              purpose: transaction.purpose,
              newBalance: transaction.balanceAfter.toString()
            },
            actions: [
              {
                id: 'view_transaction',
                title: 'View Details',
                deepLink: `rio://wallet/transactions/${transactionId}`
              }
            ]
          };
          break;

        case 'REFUND':
          notification = {
            title: '💰 Refund Processed',
            body: `${transaction.amount} coins refunded to your account`,
            category: NotificationCategory.PAYMENT,
            priority: NotificationPriority.HIGH,
            targetUsers: [transaction.userId],
            deepLink: `rio://wallet/transactions/${transactionId}`,
            data: {
              transactionId: transactionId,
              amount: transaction.amount.toString(),
              type: transaction.type,
              purpose: transaction.purpose,
              newBalance: transaction.balanceAfter.toString()
            },
            actions: [
              {
                id: 'view_wallet',
                title: 'View Wallet',
                deepLink: 'rio://wallet'
              }
            ]
          };
          break;
      }

      if (notification) {
        await processNotification(notification, 'system');
      }

      // Check for low balance warning
      if (transaction.balanceAfter <= 10 && transaction.type === 'SPEND') {
        await sendLowBalanceWarning(transaction.userId, transaction.balanceAfter);
      }

    } catch (error) {
      console.error('Error sending transaction notification:', error);
    }
  });

/**
 * Trigger when coin order status changes
 */
export const onCoinOrderStatusChange = functions
  .region(REGION)
  .firestore
  .document('coin_orders/{orderId}')
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    const orderId = context.params.orderId;

    // Check if status changed
    if (before.status !== after.status) {
      try {
        let notification: NotificationPayload | null = null;

        switch (after.status) {
          case 'COMPLETED':
            notification = {
              title: '✅ Payment Successful',
              body: `Your coin purchase of ${after.totalCoins} coins is complete`,
              category: NotificationCategory.PAYMENT,
              priority: NotificationPriority.HIGH,
              targetUsers: [after.userId],
              deepLink: `rio://wallet/orders/${orderId}`,
              data: {
                orderId: orderId,
                coinAmount: after.totalCoins.toString(),
                paymentAmount: after.amount.toString(),
                paymentMethod: after.paymentMethod
              },
              actions: [
                {
                  id: 'view_wallet',
                  title: 'View Wallet',
                  deepLink: 'rio://wallet'
                },
                {
                  id: 'shop_now',
                  title: 'Start Shopping',
                  deepLink: 'rio://marketplace'
                }
              ]
            };
            break;

          case 'FAILED':
            notification = {
              title: '❌ Payment Failed',
              body: `Your coin purchase failed. Please try again.`,
              category: NotificationCategory.PAYMENT,
              priority: NotificationPriority.HIGH,
              targetUsers: [after.userId],
              deepLink: `rio://wallet/orders/${orderId}`,
              data: {
                orderId: orderId,
                coinAmount: after.totalCoins.toString(),
                paymentAmount: after.amount.toString(),
                paymentMethod: after.paymentMethod
              },
              actions: [
                {
                  id: 'retry_payment',
                  title: 'Try Again',
                  deepLink: 'rio://wallet/purchase'
                },
                {
                  id: 'contact_support',
                  title: 'Get Help',
                  deepLink: 'rio://support'
                }
              ]
            };
            break;

          case 'CANCELLED':
            notification = {
              title: '🚫 Payment Cancelled',
              body: `Your coin purchase was cancelled`,
              category: NotificationCategory.PAYMENT,
              priority: NotificationPriority.NORMAL,
              targetUsers: [after.userId],
              deepLink: `rio://wallet/orders/${orderId}`,
              data: {
                orderId: orderId,
                coinAmount: after.totalCoins.toString(),
                paymentAmount: after.amount.toString()
              }
            };
            break;
        }

        if (notification) {
          await processNotification(notification, 'system');
        }

      } catch (error) {
        console.error('Error sending order status notification:', error);
      }
    }
  });

/**
 * Send low balance warning
 */
async function sendLowBalanceWarning(userId: string, currentBalance: number): Promise<void> {
  try {
    // Check if we've already sent a warning recently
    const recentWarningSnapshot = await db.collection('notification_history')
      .where('userId', '==', userId)
      .where('type', '==', 'low_balance_warning')
      .where('createdAt', '>', new Date(Date.now() - 24 * 60 * 60 * 1000)) // Last 24 hours
      .limit(1)
      .get();

    if (!recentWarningSnapshot.empty) {
      return; // Already sent warning in last 24 hours
    }

    const notification: NotificationPayload = {
      title: '⚠️ Low Coin Balance',
      body: `You have only ${currentBalance} coins left. Add more to continue shopping.`,
      category: NotificationCategory.PAYMENT,
      priority: NotificationPriority.NORMAL,
      targetUsers: [userId],
      deepLink: 'rio://wallet/purchase',
      data: {
        currentBalance: currentBalance.toString(),
        type: 'low_balance_warning'
      },
      actions: [
        {
          id: 'buy_coins',
          title: 'Buy Coins',
          deepLink: 'rio://wallet/purchase'
        },
        {
          id: 'view_wallet',
          title: 'View Wallet',
          deepLink: 'rio://wallet'
        }
      ]
    };

    await processNotification(notification, 'system');

    // Record that we sent this warning
    await db.collection('notification_history').add({
      userId: userId,
      type: 'low_balance_warning',
      balance: currentBalance,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });

  } catch (error) {
    console.error('Error sending low balance warning:', error);
  }
}

/**
 * Build purchase success notification
 */
async function buildPurchaseSuccessNotification(userId: string, paymentData: any): Promise<NotificationPayload> {
  return {
    title: '🎉 Purchase Successful',
    body: `${paymentData.coinAmount} coins added for ₹${paymentData.paymentAmount}`,
    category: NotificationCategory.PAYMENT,
    priority: NotificationPriority.HIGH,
    targetUsers: [userId],
    deepLink: `rio://wallet/orders/${paymentData.orderId}`,
    data: {
      orderId: paymentData.orderId,
      coinAmount: paymentData.coinAmount.toString(),
      paymentAmount: paymentData.paymentAmount.toString()
    },
    actions: [
      {
        id: 'view_wallet',
        title: 'View Wallet',
        deepLink: 'rio://wallet'
      },
      {
        id: 'start_shopping',
        title: 'Start Shopping',
        deepLink: 'rio://marketplace'
      }
    ]
  };
}

/**
 * Build purchase failed notification
 */
async function buildPurchaseFailedNotification(userId: string, paymentData: any): Promise<NotificationPayload> {
  return {
    title: '❌ Purchase Failed',
    body: `Payment of ₹${paymentData.paymentAmount} failed. Please try again.`,
    category: NotificationCategory.PAYMENT,
    priority: NotificationPriority.HIGH,
    targetUsers: [userId],
    deepLink: 'rio://wallet/purchase',
    data: {
      orderId: paymentData.orderId,
      paymentAmount: paymentData.paymentAmount.toString(),
      failureReason: paymentData.failureReason || 'Unknown error'
    },
    actions: [
      {
        id: 'retry_payment',
        title: 'Try Again',
        deepLink: 'rio://wallet/purchase'
      },
      {
        id: 'contact_support',
        title: 'Get Help',
        deepLink: 'rio://support'
      }
    ]
  };
}

/**
 * Build refund processed notification
 */
async function buildRefundProcessedNotification(userId: string, paymentData: any): Promise<NotificationPayload> {
  return {
    title: '💰 Refund Processed',
    body: `₹${paymentData.refundAmount} refund has been processed to your account`,
    category: NotificationCategory.PAYMENT,
    priority: NotificationPriority.HIGH,
    targetUsers: [userId],
    deepLink: `rio://wallet/refunds/${paymentData.refundId}`,
    data: {
      refundId: paymentData.refundId,
      refundAmount: paymentData.refundAmount.toString(),
      orderId: paymentData.orderId
    },
    actions: [
      {
        id: 'view_refund',
        title: 'View Details',
        deepLink: `rio://wallet/refunds/${paymentData.refundId}`
      }
    ]
  };
}

/**
 * Build dispute created notification
 */
async function buildDisputeCreatedNotification(userId: string, paymentData: any): Promise<NotificationPayload> {
  return {
    title: '⚖️ Dispute Created',
    body: `Your dispute for order ${paymentData.orderId} has been submitted`,
    category: NotificationCategory.PAYMENT,
    priority: NotificationPriority.HIGH,
    targetUsers: [userId],
    deepLink: `rio://disputes/${paymentData.disputeId}`,
    data: {
      disputeId: paymentData.disputeId,
      orderId: paymentData.orderId,
      disputeType: paymentData.disputeType
    },
    actions: [
      {
        id: 'view_dispute',
        title: 'View Dispute',
        deepLink: `rio://disputes/${paymentData.disputeId}`
      }
    ]
  };
}

/**
 * Build low balance notification
 */
async function buildLowBalanceNotification(userId: string, paymentData: any): Promise<NotificationPayload> {
  return {
    title: '⚠️ Low Coin Balance',
    body: `You have ${paymentData.currentBalance} coins left. Add more to continue shopping.`,
    category: NotificationCategory.PAYMENT,
    priority: NotificationPriority.NORMAL,
    targetUsers: [userId],
    deepLink: 'rio://wallet/purchase',
    data: {
      currentBalance: paymentData.currentBalance.toString()
    },
    actions: [
      {
        id: 'buy_coins',
        title: 'Buy Coins',
        deepLink: 'rio://wallet/purchase'
      }
    ]
  };
}

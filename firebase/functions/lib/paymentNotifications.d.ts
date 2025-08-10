import * as functions from 'firebase-functions';
/**
 * Payment system notification triggers for RIO coin-based payments
 * Handles purchase confirmations, refunds, disputes, and balance alerts
 */
/**
 * Send payment notification based on type
 */
export declare const sendPaymentNotification: functions.HttpsFunction & functions.Runnable<any>;
/**
 * Trigger when coin transaction is created
 */
export declare const onCoinTransactionCreated: functions.CloudFunction<functions.firestore.QueryDocumentSnapshot>;
/**
 * Trigger when coin order status changes
 */
export declare const onCoinOrderStatusChange: functions.CloudFunction<functions.Change<functions.firestore.QueryDocumentSnapshot>>;
//# sourceMappingURL=paymentNotifications.d.ts.map
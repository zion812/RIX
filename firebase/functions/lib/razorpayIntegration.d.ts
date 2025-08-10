import * as functions from 'firebase-functions';
/**
 * Razorpay webhook handler for payment events
 */
export declare const razorpayWebhook: functions.HttpsFunction;
/**
 * Create UPI payment link for coin purchase
 */
export declare const createUPIPaymentLink: functions.HttpsFunction & functions.Runnable<any>;
/**
 * Process Google Pay payment
 */
export declare const processGooglePayPayment: functions.HttpsFunction & functions.Runnable<any>;
/**
 * Create refund for coin purchase
 */
export declare const createRefund: functions.HttpsFunction & functions.Runnable<any>;
/**
 * Get payment methods available for user
 */
export declare const getAvailablePaymentMethods: functions.HttpsFunction & functions.Runnable<any>;
//# sourceMappingURL=razorpayIntegration.d.ts.map
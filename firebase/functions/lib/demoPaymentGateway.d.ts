import * as functions from 'firebase-functions';
/**
 * Demo Payment Gateway - Fully Functional Simulation
 * Simulates real payment processing with realistic responses and delays
 */
/**
 * Create demo payment order
 */
export declare const createDemoPaymentOrder: functions.HttpsFunction & functions.Runnable<any>;
/**
 * Process demo payment
 */
export declare const processDemoPayment: functions.HttpsFunction & functions.Runnable<any>;
/**
 * Demo UPI payment processing
 */
export declare const processDemoUPIPayment: functions.HttpsFunction & functions.Runnable<any>;
/**
 * Demo Google Pay payment processing
 */
export declare const processDemoGooglePay: functions.HttpsFunction & functions.Runnable<any>;
/**
 * Demo webhook endpoint for testing
 */
export declare const demoWebhookEndpoint: functions.HttpsFunction;
//# sourceMappingURL=demoPaymentGateway.d.ts.map
import * as functions from 'firebase-functions';
/**
 * Comprehensive refund and dispute management system
 * Handles automatic refunds, dispute resolution, and chargeback protection
 */
/**
 * Create refund request
 */
export declare const createRefundRequest: functions.HttpsFunction & functions.Runnable<any>;
/**
 * Create dispute for marketplace transactions
 */
export declare const createDispute: functions.HttpsFunction & functions.Runnable<any>;
/**
 * Process automatic refunds
 */
export declare const processAutomaticRefunds: functions.CloudFunction<unknown>;
/**
 * Escalate unresolved disputes
 */
export declare const escalateDisputes: functions.CloudFunction<unknown>;
/**
 * Resolve dispute
 */
export declare const resolveDispute: functions.HttpsFunction & functions.Runnable<any>;
//# sourceMappingURL=refundDispute.d.ts.map
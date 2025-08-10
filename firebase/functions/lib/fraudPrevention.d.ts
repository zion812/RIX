import * as functions from 'firebase-functions';
/**
 * Fraud prevention and security system for RIO coin payments
 * Implements ML-based detection and rule-based validation
 */
/**
 * Real-time fraud detection for payment transactions
 */
export declare const detectFraud: functions.HttpsFunction & functions.Runnable<any>;
/**
 * Verify user identity for high-risk transactions
 */
export declare const verifyUserIdentity: functions.HttpsFunction & functions.Runnable<any>;
/**
 * Monitor suspicious activity patterns
 */
export declare const monitorSuspiciousActivity: functions.CloudFunction<unknown>;
/**
 * Block suspicious users and transactions
 */
export declare const blockSuspiciousUser: functions.HttpsFunction & functions.Runnable<any>;
//# sourceMappingURL=fraudPrevention.d.ts.map
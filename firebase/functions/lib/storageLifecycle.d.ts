import * as functions from 'firebase-functions';
/**
 * Storage lifecycle management for cost optimization
 * Automatically manages storage classes and cleanup for rural India cost constraints
 */
/**
 * Daily cleanup function to optimize storage costs
 * Target: <₹2 per user per month
 */
export declare const dailyStorageCleanup: functions.CloudFunction<unknown>;
/**
 * Weekly storage optimization for cost management
 */
export declare const weeklyStorageOptimization: functions.CloudFunction<unknown>;
//# sourceMappingURL=storageLifecycle.d.ts.map
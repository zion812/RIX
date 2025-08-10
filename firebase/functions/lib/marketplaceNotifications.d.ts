import * as functions from 'firebase-functions';
/**
 * Marketplace notification triggers for RIO platform
 * Handles listing updates, price alerts, and purchase notifications
 */
/**
 * Trigger when new marketplace listing is created
 */
export declare const onNewMarketplaceListing: functions.CloudFunction<functions.firestore.QueryDocumentSnapshot>;
/**
 * Trigger when listing price is updated
 */
export declare const onListingPriceUpdate: functions.CloudFunction<functions.Change<functions.firestore.QueryDocumentSnapshot>>;
/**
 * Trigger when listing is about to expire
 */
export declare const checkExpiringListings: functions.CloudFunction<unknown>;
/**
 * Send purchase confirmation notification
 */
export declare const sendPurchaseConfirmation: functions.HttpsFunction & functions.Runnable<any>;
//# sourceMappingURL=marketplaceNotifications.d.ts.map
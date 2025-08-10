/**
 * Firebase Cloud Functions Entry Point for RIO Payment System
 * Exports all payment-related functions with proper error handling and monitoring
 */

import * as admin from 'firebase-admin';

// Initialize Firebase Admin SDK
admin.initializeApp();

// Export payment processing functions
export {
  createCoinPurchaseOrder,
  verifyPaymentAndCreditCoins,
  spendCoins,
  getCoinBalance
} from './paymentProcessing';

// Export demo payment gateway functions
export {
  createDemoPaymentOrder,
  processDemoPayment,
  processDemoUPIPayment,
  processDemoGooglePay,
  demoWebhookEndpoint
} from './demoPaymentGateway';

// Export Razorpay integration functions
export {
  razorpayWebhook
} from './razorpayIntegration';

// Export fraud prevention functions
export {
  monitorSuspiciousActivity,
  blockSuspiciousUser
} from './fraudPrevention';

// Export refund and dispute functions
export {
  createRefundRequest,
  createDispute,
  resolveDispute,
  escalateDisputes
} from './refundDispute';

// Notification system functions
export {
  sendNotification,
  processScheduledNotifications
} from './notificationService';

export {
  onNewMarketplaceListing,
  onListingPriceUpdate,
  checkExpiringListings,
  sendPurchaseConfirmation
} from './marketplaceNotifications';

export {
  sendPaymentNotification,
  onCoinTransactionCreated,
  onCoinOrderStatusChange
} from './paymentNotifications';

// Health check function
export { healthCheck } from './healthCheck';

// Monitoring and analytics
export { generatePaymentAnalytics } from './analytics';

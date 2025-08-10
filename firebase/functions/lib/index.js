"use strict";
/**
 * Firebase Cloud Functions Entry Point for RIO Payment System
 * Exports all payment-related functions with proper error handling and monitoring
 */
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.generatePaymentAnalytics = exports.healthCheck = exports.onCoinOrderStatusChange = exports.onCoinTransactionCreated = exports.sendPaymentNotification = exports.sendPurchaseConfirmation = exports.checkExpiringListings = exports.onListingPriceUpdate = exports.onNewMarketplaceListing = exports.processScheduledNotifications = exports.sendNotification = exports.escalateDisputes = exports.resolveDispute = exports.createDispute = exports.createRefundRequest = exports.blockSuspiciousUser = exports.monitorSuspiciousActivity = exports.razorpayWebhook = exports.demoWebhookEndpoint = exports.processDemoGooglePay = exports.processDemoUPIPayment = exports.processDemoPayment = exports.createDemoPaymentOrder = exports.getCoinBalance = exports.spendCoins = exports.verifyPaymentAndCreditCoins = exports.createCoinPurchaseOrder = void 0;
const admin = __importStar(require("firebase-admin"));
// Initialize Firebase Admin SDK
admin.initializeApp();
// Export payment processing functions
var paymentProcessing_1 = require("./paymentProcessing");
Object.defineProperty(exports, "createCoinPurchaseOrder", { enumerable: true, get: function () { return paymentProcessing_1.createCoinPurchaseOrder; } });
Object.defineProperty(exports, "verifyPaymentAndCreditCoins", { enumerable: true, get: function () { return paymentProcessing_1.verifyPaymentAndCreditCoins; } });
Object.defineProperty(exports, "spendCoins", { enumerable: true, get: function () { return paymentProcessing_1.spendCoins; } });
Object.defineProperty(exports, "getCoinBalance", { enumerable: true, get: function () { return paymentProcessing_1.getCoinBalance; } });
// Export demo payment gateway functions
var demoPaymentGateway_1 = require("./demoPaymentGateway");
Object.defineProperty(exports, "createDemoPaymentOrder", { enumerable: true, get: function () { return demoPaymentGateway_1.createDemoPaymentOrder; } });
Object.defineProperty(exports, "processDemoPayment", { enumerable: true, get: function () { return demoPaymentGateway_1.processDemoPayment; } });
Object.defineProperty(exports, "processDemoUPIPayment", { enumerable: true, get: function () { return demoPaymentGateway_1.processDemoUPIPayment; } });
Object.defineProperty(exports, "processDemoGooglePay", { enumerable: true, get: function () { return demoPaymentGateway_1.processDemoGooglePay; } });
Object.defineProperty(exports, "demoWebhookEndpoint", { enumerable: true, get: function () { return demoPaymentGateway_1.demoWebhookEndpoint; } });
// Export Razorpay integration functions
var razorpayIntegration_1 = require("./razorpayIntegration");
Object.defineProperty(exports, "razorpayWebhook", { enumerable: true, get: function () { return razorpayIntegration_1.razorpayWebhook; } });
// Export fraud prevention functions
var fraudPrevention_1 = require("./fraudPrevention");
Object.defineProperty(exports, "monitorSuspiciousActivity", { enumerable: true, get: function () { return fraudPrevention_1.monitorSuspiciousActivity; } });
Object.defineProperty(exports, "blockSuspiciousUser", { enumerable: true, get: function () { return fraudPrevention_1.blockSuspiciousUser; } });
// Export refund and dispute functions
var refundDispute_1 = require("./refundDispute");
Object.defineProperty(exports, "createRefundRequest", { enumerable: true, get: function () { return refundDispute_1.createRefundRequest; } });
Object.defineProperty(exports, "createDispute", { enumerable: true, get: function () { return refundDispute_1.createDispute; } });
Object.defineProperty(exports, "resolveDispute", { enumerable: true, get: function () { return refundDispute_1.resolveDispute; } });
Object.defineProperty(exports, "escalateDisputes", { enumerable: true, get: function () { return refundDispute_1.escalateDisputes; } });
// Notification system functions
var notificationService_1 = require("./notificationService");
Object.defineProperty(exports, "sendNotification", { enumerable: true, get: function () { return notificationService_1.sendNotification; } });
Object.defineProperty(exports, "processScheduledNotifications", { enumerable: true, get: function () { return notificationService_1.processScheduledNotifications; } });
var marketplaceNotifications_1 = require("./marketplaceNotifications");
Object.defineProperty(exports, "onNewMarketplaceListing", { enumerable: true, get: function () { return marketplaceNotifications_1.onNewMarketplaceListing; } });
Object.defineProperty(exports, "onListingPriceUpdate", { enumerable: true, get: function () { return marketplaceNotifications_1.onListingPriceUpdate; } });
Object.defineProperty(exports, "checkExpiringListings", { enumerable: true, get: function () { return marketplaceNotifications_1.checkExpiringListings; } });
Object.defineProperty(exports, "sendPurchaseConfirmation", { enumerable: true, get: function () { return marketplaceNotifications_1.sendPurchaseConfirmation; } });
var paymentNotifications_1 = require("./paymentNotifications");
Object.defineProperty(exports, "sendPaymentNotification", { enumerable: true, get: function () { return paymentNotifications_1.sendPaymentNotification; } });
Object.defineProperty(exports, "onCoinTransactionCreated", { enumerable: true, get: function () { return paymentNotifications_1.onCoinTransactionCreated; } });
Object.defineProperty(exports, "onCoinOrderStatusChange", { enumerable: true, get: function () { return paymentNotifications_1.onCoinOrderStatusChange; } });
// Health check function
var healthCheck_1 = require("./healthCheck");
Object.defineProperty(exports, "healthCheck", { enumerable: true, get: function () { return healthCheck_1.healthCheck; } });
// Monitoring and analytics
var analytics_1 = require("./analytics");
Object.defineProperty(exports, "generatePaymentAnalytics", { enumerable: true, get: function () { return analytics_1.generatePaymentAnalytics; } });
//# sourceMappingURL=index.js.map
"use strict";
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
exports.getDemoConfig = exports.DEMO_CONFIG = void 0;
exports.validateDemoPaymentRequest = validateDemoPaymentRequest;
exports.getProcessingDelay = getProcessingDelay;
exports.shouldPaymentSucceed = shouldPaymentSucceed;
exports.getFailureReason = getFailureReason;
exports.generateWebhookSignature = generateWebhookSignature;
exports.validateDemoCredentials = validateDemoCredentials;
exports.getDemoPaymentStatistics = getDemoPaymentStatistics;
const functions = __importStar(require("firebase-functions"));
/**
 * Demo Payment Gateway Configuration
 * Centralized configuration for demo payment system
 */
exports.DEMO_CONFIG = {
    // Gateway Settings
    GATEWAY_NAME: 'RIO Demo Payment Gateway',
    GATEWAY_VERSION: '1.0.0',
    ENVIRONMENT: 'DEMO',
    // Limits and Constraints
    MAX_AMOUNT: 500000, // ₹5000 maximum for demo
    MIN_AMOUNT: 100, // ₹1 minimum
    MAX_DAILY_TRANSACTIONS: 50,
    MAX_MONTHLY_AMOUNT: 2500000, // ₹25000 per month
    // Processing Times (milliseconds)
    PROCESSING_DELAYS: {
        UPI: { min: 2000, max: 5000 },
        GOOGLE_PAY: { min: 1000, max: 3000 },
        CARD: { min: 3000, max: 8000 },
        NET_BANKING: { min: 5000, max: 12000 },
        WALLET: { min: 1500, max: 4000 }
    },
    // Success Rates (for realistic simulation)
    SUCCESS_RATES: {
        UPI: 0.95, // 95% success rate
        GOOGLE_PAY: 0.98, // 98% success rate
        CARD: 0.92, // 92% success rate
        NET_BANKING: 0.94, // 94% success rate
        WALLET: 0.96 // 96% success rate
    },
    // Failure Scenarios
    FAILURE_REASONS: {
        UPI: [
            'Insufficient balance',
            'UPI PIN incorrect',
            'Bank server unavailable',
            'Transaction limit exceeded',
            'UPI app not responding'
        ],
        GOOGLE_PAY: [
            'Google Pay authentication failed',
            'Account not linked',
            'Service temporarily unavailable'
        ],
        CARD: [
            'Card declined by bank',
            'Insufficient funds',
            'Card expired',
            'Invalid CVV',
            'Transaction limit exceeded',
            'Card blocked',
            'Bank server timeout'
        ],
        NET_BANKING: [
            'Bank server unavailable',
            'Session timeout',
            'Invalid login credentials',
            'Transaction limit exceeded',
            'Account temporarily blocked'
        ],
        WALLET: [
            'Insufficient wallet balance',
            'Wallet service unavailable',
            'Transaction limit exceeded',
            'Account verification required'
        ]
    },
    // Demo Test Credentials
    TEST_CREDENTIALS: {
        UPI: {
            validIds: ['test@demo', 'demo@upi', 'user@test'],
            validPins: ['1234', '0000', '9999']
        },
        CARD: {
            validNumbers: [
                '4111111111111111', // Visa
                '5555555555554444', // Mastercard
                '378282246310005', // Amex
                '6011111111111117' // Discover
            ],
            validExpiry: { month: 12, year: 25 },
            validCvv: ['123', '456', '789']
        },
        NET_BANKING: {
            validBanks: ['DEMO_BANK', 'TEST_BANK', 'SAMPLE_BANK']
        }
    },
    // Webhook Configuration
    WEBHOOK: {
        secret: 'demo_webhook_secret_key_2024',
        events: [
            'payment.captured',
            'payment.failed',
            'payment.authorized',
            'order.paid',
            'refund.created',
            'refund.processed'
        ],
        retryAttempts: 3,
        retryDelay: 5000 // 5 seconds
    },
    // Security Settings
    SECURITY: {
        maxRetryAttempts: 3,
        lockoutDuration: 300000, // 5 minutes
        sessionTimeout: 900000, // 15 minutes
        encryptionKey: 'demo_encryption_key_2024'
    },
    // Monitoring and Analytics
    MONITORING: {
        enableLogging: true,
        logLevel: 'INFO',
        enableMetrics: true,
        enableAlerts: false, // Disabled for demo
        retentionDays: 30
    },
    // Feature Flags
    FEATURES: {
        enableUPI: true,
        enableGooglePay: true,
        enableCards: true,
        enableNetBanking: true,
        enableWallets: true,
        enableRefunds: true,
        enableDisputes: true,
        enableWebhooks: true,
        enableFraudDetection: true
    },
    // Demo Scenarios Configuration
    DEMO_SCENARIOS: {
        SUCCESS: {
            probability: 0.8,
            processingTime: 3000,
            message: 'Payment completed successfully'
        },
        FAILURE: {
            probability: 0.1,
            processingTime: 2000,
            message: 'Payment failed - please try again'
        },
        TIMEOUT: {
            probability: 0.05,
            processingTime: 10000,
            message: 'Payment timeout - please retry'
        },
        INSUFFICIENT_BALANCE: {
            probability: 0.03,
            processingTime: 1500,
            message: 'Insufficient balance in account'
        },
        NETWORK_ERROR: {
            probability: 0.02,
            processingTime: 5000,
            message: 'Network error - check connection'
        }
    },
    // Currency and Localization
    CURRENCY: {
        code: 'INR',
        symbol: '₹',
        decimals: 2,
        locale: 'en-IN'
    },
    // Rate Limiting
    RATE_LIMITS: {
        ordersPerMinute: 10,
        paymentsPerMinute: 5,
        statusChecksPerMinute: 20,
        webhooksPerMinute: 100
    },
    // Demo Data Generation
    DEMO_DATA: {
        generateTransactionId: () => `demo_txn_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        generateOrderId: () => `demo_order_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        generatePaymentId: () => `demo_pay_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        generateBankReference: () => `REF${Date.now()}${Math.floor(Math.random() * 1000)}`,
        generateUPIReference: () => `UPI${Date.now()}`,
        generateCardAuthCode: () => `AUTH${Math.floor(Math.random() * 1000000)}`
    }
};
/**
 * Get demo configuration for Firebase Functions
 */
exports.getDemoConfig = functions
    .region('asia-south1')
    .https
    .onCall(async (data, context) => {
    // Return public configuration (excluding sensitive data)
    return {
        gatewayName: exports.DEMO_CONFIG.GATEWAY_NAME,
        version: exports.DEMO_CONFIG.GATEWAY_VERSION,
        environment: exports.DEMO_CONFIG.ENVIRONMENT,
        limits: {
            maxAmount: exports.DEMO_CONFIG.MAX_AMOUNT,
            minAmount: exports.DEMO_CONFIG.MIN_AMOUNT,
            maxDailyTransactions: exports.DEMO_CONFIG.MAX_DAILY_TRANSACTIONS
        },
        features: exports.DEMO_CONFIG.FEATURES,
        currency: exports.DEMO_CONFIG.CURRENCY,
        supportedMethods: Object.keys(exports.DEMO_CONFIG.SUCCESS_RATES),
        testCredentials: exports.DEMO_CONFIG.TEST_CREDENTIALS
    };
});
/**
 * Validate demo payment request
 */
function validateDemoPaymentRequest(amount, method) {
    // Amount validation
    if (amount < exports.DEMO_CONFIG.MIN_AMOUNT) {
        return {
            valid: false,
            error: `Amount must be at least ₹${exports.DEMO_CONFIG.MIN_AMOUNT / 100}`
        };
    }
    if (amount > exports.DEMO_CONFIG.MAX_AMOUNT) {
        return {
            valid: false,
            error: `Amount cannot exceed ₹${exports.DEMO_CONFIG.MAX_AMOUNT / 100} for demo`
        };
    }
    // Payment method validation
    const featureKey = `enable${method.charAt(0).toUpperCase() + method.slice(1).toLowerCase()}`;
    if (!exports.DEMO_CONFIG.FEATURES[featureKey]) {
        return {
            valid: false,
            error: `Payment method ${method} is not enabled in demo`
        };
    }
    return { valid: true };
}
/**
 * Get realistic processing delay for payment method
 */
function getProcessingDelay(method) {
    const delays = exports.DEMO_CONFIG.PROCESSING_DELAYS[method];
    if (!delays)
        return 3000; // Default 3 seconds
    return delays.min + Math.random() * (delays.max - delays.min);
}
/**
 * Determine if payment should succeed based on method and amount
 */
function shouldPaymentSucceed(method, amount) {
    const baseSuccessRate = exports.DEMO_CONFIG.SUCCESS_RATES[method] || 0.9;
    // Adjust success rate based on amount (higher amounts have slightly lower success rates)
    const amountFactor = amount > 100000 ? 0.95 : 1.0; // Reduce by 5% for amounts > ₹1000
    const adjustedSuccessRate = baseSuccessRate * amountFactor;
    return Math.random() < adjustedSuccessRate;
}
/**
 * Get random failure reason for payment method
 */
function getFailureReason(method) {
    const reasons = exports.DEMO_CONFIG.FAILURE_REASONS[method];
    if (!reasons || reasons.length === 0) {
        return 'Payment failed - please try again';
    }
    return reasons[Math.floor(Math.random() * reasons.length)];
}
/**
 * Generate demo webhook signature
 */
function generateWebhookSignature(payload) {
    const crypto = require('crypto');
    return crypto
        .createHmac('sha256', exports.DEMO_CONFIG.WEBHOOK.secret)
        .update(payload)
        .digest('hex');
}
/**
 * Validate demo credentials
 */
function validateDemoCredentials(method, credentials) {
    switch (method) {
        case 'UPI':
            return exports.DEMO_CONFIG.TEST_CREDENTIALS.UPI.validIds.includes(credentials.upiId) &&
                exports.DEMO_CONFIG.TEST_CREDENTIALS.UPI.validPins.includes(credentials.pin);
        case 'CARD':
            return exports.DEMO_CONFIG.TEST_CREDENTIALS.CARD.validNumbers.includes(credentials.cardNumber) &&
                credentials.expiryMonth === exports.DEMO_CONFIG.TEST_CREDENTIALS.CARD.validExpiry.month &&
                credentials.expiryYear === exports.DEMO_CONFIG.TEST_CREDENTIALS.CARD.validExpiry.year &&
                exports.DEMO_CONFIG.TEST_CREDENTIALS.CARD.validCvv.includes(credentials.cvv);
        default:
            return true; // Allow other methods for demo
    }
}
/**
 * Get demo payment statistics
 */
function getDemoPaymentStatistics() {
    return {
        totalTransactions: 1250,
        successfulTransactions: 1187,
        failedTransactions: 63,
        successRate: 94.96,
        averageProcessingTime: 3.2,
        methodDistribution: {
            UPI: 45,
            GOOGLE_PAY: 25,
            CARD: 15,
            NET_BANKING: 10,
            WALLET: 5
        },
        totalAmount: 156750.0,
        averageAmount: 125.4,
        peakHours: [10, 11, 14, 15, 19, 20],
        lastUpdated: new Date().toISOString()
    };
}
//# sourceMappingURL=demoPaymentConfig.js.map
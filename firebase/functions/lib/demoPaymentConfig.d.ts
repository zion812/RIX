import * as functions from 'firebase-functions';
/**
 * Demo Payment Gateway Configuration
 * Centralized configuration for demo payment system
 */
export declare const DEMO_CONFIG: {
    GATEWAY_NAME: string;
    GATEWAY_VERSION: string;
    ENVIRONMENT: string;
    MAX_AMOUNT: number;
    MIN_AMOUNT: number;
    MAX_DAILY_TRANSACTIONS: number;
    MAX_MONTHLY_AMOUNT: number;
    PROCESSING_DELAYS: {
        UPI: {
            min: number;
            max: number;
        };
        GOOGLE_PAY: {
            min: number;
            max: number;
        };
        CARD: {
            min: number;
            max: number;
        };
        NET_BANKING: {
            min: number;
            max: number;
        };
        WALLET: {
            min: number;
            max: number;
        };
    };
    SUCCESS_RATES: {
        UPI: number;
        GOOGLE_PAY: number;
        CARD: number;
        NET_BANKING: number;
        WALLET: number;
    };
    FAILURE_REASONS: {
        UPI: string[];
        GOOGLE_PAY: string[];
        CARD: string[];
        NET_BANKING: string[];
        WALLET: string[];
    };
    TEST_CREDENTIALS: {
        UPI: {
            validIds: string[];
            validPins: string[];
        };
        CARD: {
            validNumbers: string[];
            validExpiry: {
                month: number;
                year: number;
            };
            validCvv: string[];
        };
        NET_BANKING: {
            validBanks: string[];
        };
    };
    WEBHOOK: {
        secret: string;
        events: string[];
        retryAttempts: number;
        retryDelay: number;
    };
    SECURITY: {
        maxRetryAttempts: number;
        lockoutDuration: number;
        sessionTimeout: number;
        encryptionKey: string;
    };
    MONITORING: {
        enableLogging: boolean;
        logLevel: string;
        enableMetrics: boolean;
        enableAlerts: boolean;
        retentionDays: number;
    };
    FEATURES: {
        enableUPI: boolean;
        enableGooglePay: boolean;
        enableCards: boolean;
        enableNetBanking: boolean;
        enableWallets: boolean;
        enableRefunds: boolean;
        enableDisputes: boolean;
        enableWebhooks: boolean;
        enableFraudDetection: boolean;
    };
    DEMO_SCENARIOS: {
        SUCCESS: {
            probability: number;
            processingTime: number;
            message: string;
        };
        FAILURE: {
            probability: number;
            processingTime: number;
            message: string;
        };
        TIMEOUT: {
            probability: number;
            processingTime: number;
            message: string;
        };
        INSUFFICIENT_BALANCE: {
            probability: number;
            processingTime: number;
            message: string;
        };
        NETWORK_ERROR: {
            probability: number;
            processingTime: number;
            message: string;
        };
    };
    CURRENCY: {
        code: string;
        symbol: string;
        decimals: number;
        locale: string;
    };
    RATE_LIMITS: {
        ordersPerMinute: number;
        paymentsPerMinute: number;
        statusChecksPerMinute: number;
        webhooksPerMinute: number;
    };
    DEMO_DATA: {
        generateTransactionId: () => string;
        generateOrderId: () => string;
        generatePaymentId: () => string;
        generateBankReference: () => string;
        generateUPIReference: () => string;
        generateCardAuthCode: () => string;
    };
};
/**
 * Get demo configuration for Firebase Functions
 */
export declare const getDemoConfig: functions.HttpsFunction & functions.Runnable<any>;
/**
 * Validate demo payment request
 */
export declare function validateDemoPaymentRequest(amount: number, method: string): {
    valid: boolean;
    error?: string;
};
/**
 * Get realistic processing delay for payment method
 */
export declare function getProcessingDelay(method: string): number;
/**
 * Determine if payment should succeed based on method and amount
 */
export declare function shouldPaymentSucceed(method: string, amount: number): boolean;
/**
 * Get random failure reason for payment method
 */
export declare function getFailureReason(method: string): string;
/**
 * Generate demo webhook signature
 */
export declare function generateWebhookSignature(payload: string): string;
/**
 * Validate demo credentials
 */
export declare function validateDemoCredentials(method: string, credentials: any): boolean;
/**
 * Get demo payment statistics
 */
export declare function getDemoPaymentStatistics(): any;
//# sourceMappingURL=demoPaymentConfig.d.ts.map
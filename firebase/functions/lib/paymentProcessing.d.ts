import * as functions from 'firebase-functions';
/**
 * Create payment order for coin purchase
 */
export declare const createCoinPurchaseOrder: functions.HttpsFunction & functions.Runnable<any>;
/**
 * Verify payment and credit coins
 */
export declare const verifyPaymentAndCreditCoins: functions.HttpsFunction & functions.Runnable<any>;
/**
 * Spend coins for marketplace transactions
 */
export declare const spendCoins: functions.HttpsFunction & functions.Runnable<any>;
/**
 * Get user's coin balance and transaction history
 */
export declare const getCoinBalance: functions.HttpsFunction & functions.Runnable<any>;
//# sourceMappingURL=paymentProcessing.d.ts.map
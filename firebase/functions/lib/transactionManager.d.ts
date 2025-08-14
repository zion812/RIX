/**
 * ✅ Atomic transaction manager for coin operations
 * Ensures ACID properties for all coin-related transactions
 */
export declare class AtomicTransactionManager {
    private firestore;
    /**
     * ✅ Execute atomic coin transaction with automatic rollback
     */
    executeAtomicCoinTransaction(userId: string, amount: number, purpose: string, metadata: any, serviceCallback: () => Promise<any>): Promise<TransactionResult>;
    /**
     * ✅ Rollback transaction and restore coin balance
     */
    private rollbackTransaction;
    /**
     * ✅ Credit coins to user account (for purchases)
     */
    creditCoins(userId: string, amount: number, orderId: string, paymentId: string): Promise<TransactionResult>;
    /**
     * ✅ Validate transaction integrity
     */
    validateTransactionIntegrity(transactionId: string): Promise<boolean>;
    private markTransactionFailed;
    private logTransactionEvent;
    private logCriticalError;
}
/**
 * Transaction result interface
 */
interface TransactionResult {
    success: boolean;
    transactionId: string;
    newBalance: number;
    serviceResult?: any;
    error?: string;
}
/**
 * ✅ Cloud Function: Create marketplace listing with atomic coin transaction
 */
export declare const createMarketplaceListing: any;
/**
 * ✅ Cloud Function: Process coin purchase
 */
export declare const processCoinPurchase: any;
export {};
//# sourceMappingURL=transactionManager.d.ts.map
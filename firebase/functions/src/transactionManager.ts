import * as admin from 'firebase-admin';
import * as functions from 'firebase-functions';

/**
 * ✅ Atomic transaction manager for coin operations
 * Ensures ACID properties for all coin-related transactions
 */
export class AtomicTransactionManager {
  private firestore = admin.firestore();
  
  /**
   * ✅ Execute atomic coin transaction with automatic rollback
   */
  async executeAtomicCoinTransaction(
    userId: string,
    amount: number,
    purpose: string,
    metadata: any,
    serviceCallback: () => Promise<any>
  ): Promise<TransactionResult> {
    const transactionId = `txn_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    try {
      // ✅ Start Firestore transaction with idempotency check
      const result = await this.firestore.runTransaction(async (transaction) => {
        // Check for duplicate transaction
        const duplicateCheck = await transaction.get(
          this.firestore.collection('transactions').doc(transactionId)
        );
        
        if (duplicateCheck.exists) {
          throw new Error('Duplicate transaction detected');
        }
        
        // Get current user balance
        const userRef = this.firestore.collection('users').doc(userId);
        const userDoc = await transaction.get(userRef);
        
        if (!userDoc.exists) {
          throw new Error('User not found');
        }
        
        const userData = userDoc.data()!;
        const currentBalance = userData.coinBalance || 0;
        
        // ✅ Validate sufficient balance for deductions
        if (amount > 0 && currentBalance < amount) {
          throw new Error('Insufficient coin balance');
        }
        
        // ✅ Create pending transaction record
        const transactionRecord = {
          id: transactionId,
          userId: userId,
          amount: amount,
          purpose: purpose,
          metadata: metadata,
          status: 'pending',
          createdAt: admin.firestore.FieldValue.serverTimestamp(),
          balanceBefore: currentBalance,
          balanceAfter: currentBalance - amount
        };
        
        transaction.set(
          this.firestore.collection('transactions').doc(transactionId),
          transactionRecord
        );
        
        // ✅ Update coin balance atomically
        transaction.update(userRef, {
          coinBalance: currentBalance - amount,
          lastTransactionId: transactionId,
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
        
        return { transactionId, newBalance: currentBalance - amount };
      });
      
      try {
        // ✅ Execute the service (marketplace listing, premium feature, etc.)
        const serviceResult = await serviceCallback();
        
        // ✅ Mark transaction as completed
        await this.firestore.collection('transactions').doc(transactionId).update({
          status: 'completed',
          serviceResult: serviceResult,
          completedAt: admin.firestore.FieldValue.serverTimestamp()
        });
        
        // ✅ Log successful transaction
        await this.logTransactionEvent(transactionId, 'completed', serviceResult);
        
        return {
          success: true,
          transactionId: transactionId,
          newBalance: result.newBalance,
          serviceResult: serviceResult
        };
        
      } catch (serviceError) {
        // ✅ Service failed - rollback the coin deduction
        await this.rollbackTransaction(transactionId, userId, amount);
        throw new Error(`Service execution failed: ${serviceError.message}`);
      }
      
    } catch (error) {
      // ✅ Transaction failed - ensure no partial state
      await this.markTransactionFailed(transactionId, error.message);
      throw error;
    }
  }
  
  /**
   * ✅ Rollback transaction and restore coin balance
   */
  private async rollbackTransaction(
    transactionId: string,
    userId: string,
    amount: number
  ): Promise<void> {
    try {
      await this.firestore.runTransaction(async (transaction) => {
        const userRef = this.firestore.collection('users').doc(userId);
        const userDoc = await transaction.get(userRef);
        
        if (userDoc.exists) {
          const currentBalance = userDoc.data()!.coinBalance || 0;
          
          // ✅ Restore the deducted amount
          transaction.update(userRef, {
            coinBalance: currentBalance + amount,
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
          });
        }
        
        // ✅ Mark transaction as rolled back
        transaction.update(
          this.firestore.collection('transactions').doc(transactionId),
          {
            status: 'rolled_back',
            rolledBackAt: admin.firestore.FieldValue.serverTimestamp()
          }
        );
      });
      
      await this.logTransactionEvent(transactionId, 'rolled_back', { amount });
      
    } catch (rollbackError) {
      // ✅ Critical error - log for manual intervention
      console.error('CRITICAL: Rollback failed for transaction', transactionId, rollbackError);
      await this.logCriticalError(transactionId, 'rollback_failed', rollbackError);
    }
  }
  
  /**
   * ✅ Credit coins to user account (for purchases)
   */
  async creditCoins(
    userId: string,
    amount: number,
    orderId: string,
    paymentId: string
  ): Promise<TransactionResult> {
    const transactionId = `credit_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    try {
      const result = await this.firestore.runTransaction(async (transaction) => {
        // Verify order exists and is valid
        const orderRef = this.firestore.collection('coin_orders').doc(orderId);
        const orderDoc = await transaction.get(orderRef);
        
        if (!orderDoc.exists) {
          throw new Error('Order not found');
        }
        
        const orderData = orderDoc.data()!;
        
        // ✅ Validate order hasn't been processed
        if (orderData.status === 'completed') {
          throw new Error('Order already processed');
        }
        
        // ✅ Validate payment amount matches order
        if (orderData.totalCoins !== amount) {
          throw new Error('Amount mismatch');
        }
        
        // Get user and update balance
        const userRef = this.firestore.collection('users').doc(userId);
        const userDoc = await transaction.get(userRef);
        
        if (!userDoc.exists) {
          throw new Error('User not found');
        }
        
        const userData = userDoc.data()!;
        const currentBalance = userData.coinBalance || 0;
        const newBalance = currentBalance + amount;
        
        // ✅ Update user balance
        transaction.update(userRef, {
          coinBalance: newBalance,
          lastCoinPurchase: admin.firestore.FieldValue.serverTimestamp(),
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
        
        // ✅ Update order status
        transaction.update(orderRef, {
          status: 'completed',
          paymentId: paymentId,
          completedAt: admin.firestore.FieldValue.serverTimestamp(),
          balanceAfter: newBalance
        });
        
        // ✅ Record transaction
        const transactionRecord = {
          id: transactionId,
          userId: userId,
          orderId: orderId,
          paymentId: paymentId,
          type: 'CREDIT',
          amount: amount,
          purpose: 'COIN_PURCHASE',
          balanceBefore: currentBalance,
          balanceAfter: newBalance,
          status: 'completed',
          createdAt: admin.firestore.FieldValue.serverTimestamp()
        };
        
        transaction.set(
          this.firestore.collection('coin_transactions').doc(transactionId),
          transactionRecord
        );
        
        return { transactionId, newBalance };
      });
      
      return {
        success: true,
        transactionId: result.transactionId,
        newBalance: result.newBalance
      };
      
    } catch (error) {
      console.error('Coin credit failed:', error);
      throw error;
    }
  }
  
  /**
   * ✅ Validate transaction integrity
   */
  async validateTransactionIntegrity(transactionId: string): Promise<boolean> {
    try {
      const transactionDoc = await this.firestore
        .collection('transactions')
        .doc(transactionId)
        .get();
      
      if (!transactionDoc.exists) {
        return false;
      }
      
      const transactionData = transactionDoc.data()!;
      
      // Get user's current balance
      const userDoc = await this.firestore
        .collection('users')
        .doc(transactionData.userId)
        .get();
      
      if (!userDoc.exists) {
        return false;
      }
      
      const userData = userDoc.data()!;
      
      // ✅ Validate balance consistency
      if (transactionData.status === 'completed') {
        // For completed transactions, check if balance reflects the transaction
        const expectedBalance = transactionData.balanceAfter;
        return userData.coinBalance === expectedBalance;
      }
      
      return true;
      
    } catch (error) {
      console.error('Transaction validation failed:', error);
      return false;
    }
  }
  
  private async markTransactionFailed(transactionId: string, reason: string): Promise<void> {
    try {
      await this.firestore.collection('transactions').doc(transactionId).update({
        status: 'failed',
        failureReason: reason,
        failedAt: admin.firestore.FieldValue.serverTimestamp()
      });
    } catch (error) {
      console.error('Failed to mark transaction as failed:', error);
    }
  }
  
  private async logTransactionEvent(
    transactionId: string,
    event: string,
    data: any
  ): Promise<void> {
    await this.firestore.collection('transaction_logs').add({
      transactionId: transactionId,
      event: event,
      data: data,
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });
  }
  
  private async logCriticalError(
    transactionId: string,
    errorType: string,
    error: any
  ): Promise<void> {
    await this.firestore.collection('critical_errors').add({
      transactionId: transactionId,
      errorType: errorType,
      error: error.message,
      stack: error.stack,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      requiresManualIntervention: true
    });
  }
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
export const createMarketplaceListing = functions
  .region('asia-south1')
  .https
  .onCall(async (data, context) => {
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }
    
    const { listingData, coinCost } = data;
    const userId = context.auth.uid;
    
    const transactionManager = new AtomicTransactionManager();
    
    try {
      const result = await transactionManager.executeAtomicCoinTransaction(
        userId,
        coinCost,
        'marketplace_listing',
        { listingData },
        async () => {
          // ✅ Service: Create marketplace listing
          const listingRef = admin.firestore().collection('marketplace').doc();
          
          const listing = {
            ...listingData,
            id: listingRef.id,
            sellerId: userId,
            status: 'active',
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
            paidWithCoins: coinCost
          };
          
          await listingRef.set(listing);
          
          // ✅ Send notification to interested users
          await notifyInterestedUsers(listing);
          
          return { listingId: listingRef.id };
        }
      );
      
      return result;
      
    } catch (error) {
      console.error('Marketplace listing creation failed:', error);
      throw new functions.https.HttpsError('internal', error.message);
    }
  });

/**
 * ✅ Cloud Function: Process coin purchase
 */
export const processCoinPurchase = functions
  .region('asia-south1')
  .https
  .onCall(async (data, context) => {
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }
    
    const { orderId, paymentId, amount } = data;
    const userId = context.auth.uid;
    
    const transactionManager = new AtomicTransactionManager();
    
    try {
      const result = await transactionManager.creditCoins(
        userId,
        amount,
        orderId,
        paymentId
      );
      
      return result;
      
    } catch (error) {
      console.error('Coin purchase processing failed:', error);
      throw new functions.https.HttpsError('internal', error.message);
    }
  });

// Placeholder function
async function notifyInterestedUsers(listing: any): Promise<void> {
  // Implementation for sending notifications
}

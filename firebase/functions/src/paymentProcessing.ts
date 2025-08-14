import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import Razorpay from 'razorpay';
import * as crypto from 'crypto';

// Initialize Firebase Admin
admin.initializeApp();
const db = admin.firestore();

// Initialize Razorpay
const razorpay = new Razorpay({
  key_id: functions.config().razorpay.key_id,
  key_secret: functions.config().razorpay.key_secret,
});

// Regional configuration for India
const REGION = 'asia-south1';
const COIN_RATE = 5; // ₹5 per coin

/**
 * Create payment order for coin purchase
 */
export const createCoinPurchaseOrder = functions
  .region(REGION)
  .https
  .onCall(async (data, context) => {
    // Verify authentication
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { packageId, paymentMethod } = data;
    const userId = context.auth.uid;

    try {
      // Get user data and validate tier
      const userDoc = await db.collection('users').doc(userId).get();
      if (!userDoc.exists) {
        throw new functions.https.HttpsError('not-found', 'User not found');
      }

      const userData = userDoc.data()!;
      const userTier = userData.userTier || 'GENERAL';

      // Get package details
      const packageDetails = getCoinPackage(packageId, userTier);
      if (!packageDetails) {
        throw new functions.https.HttpsError('invalid-argument', 'Invalid package ID');
      }

      // Validate daily/monthly limits
      await validatePurchaseLimits(userId, packageDetails.totalAmount);

      // Create Razorpay order
      const orderOptions = {
        amount: packageDetails.totalAmount * 100, // Amount in paise
        currency: 'INR',
        receipt: `coin_purchase_${userId}_${Date.now()}`,
        notes: {
          userId: userId,
          packageId: packageId,
          coins: packageDetails.coins.toString(),
          bonusCoins: packageDetails.bonusCoins.toString(),
          userTier: userTier
        }
      };

      const razorpayOrder = await razorpay.orders.create(orderOptions);

      // Store order in Firestore
      const orderData = {
        orderId: razorpayOrder.id,
        userId: userId,
        packageId: packageId,
        coins: packageDetails.coins,
        bonusCoins: packageDetails.bonusCoins,
        totalCoins: packageDetails.totalCoins,
        amount: packageDetails.totalAmount,
        currency: 'INR',
        status: 'CREATED',
        paymentMethod: paymentMethod,
        userTier: userTier,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        expiresAt: new Date(Date.now() + 15 * 60 * 1000) // 15 minutes
      };

      await db.collection('coin_orders').doc(razorpayOrder.id).set(orderData);

      // Log order creation
      await logTransaction({
        type: 'ORDER_CREATED',
        userId: userId,
        orderId: razorpayOrder.id,
        amount: packageDetails.totalAmount,
        coins: packageDetails.totalCoins,
        metadata: { packageId, userTier }
      });

      return {
        orderId: razorpayOrder.id,
        amount: razorpayOrder.amount,
        currency: razorpayOrder.currency,
        packageDetails: packageDetails
      };

    } catch (error) {
      console.error('Error creating coin purchase order:', error);
      
      if (error instanceof functions.https.HttpsError) {
        throw error;
      }
      
      throw new functions.https.HttpsError('internal', 'Failed to create order');
    }
  });

/**
 * Verify payment and credit coins
 */
export const verifyPaymentAndCreditCoins = functions
  .region(REGION)
  .https
  .onCall(async (data, context) => {
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { orderId, paymentId, signature } = data;
    const userId = context.auth.uid;

    try {
      // Check if this is a demo order first
      let orderDoc = await db.collection('demo_orders').doc(orderId).get();
      let isDemo = orderDoc.exists;

      // If not demo, check regular orders
      if (!isDemo) {
        orderDoc = await db.collection('coin_orders').doc(orderId).get();
        if (!orderDoc.exists) {
          throw new functions.https.HttpsError('not-found', 'Order not found');
        }
      }

      const orderData = orderDoc.data()!;

      // Verify order belongs to user
      if (orderData.userId !== userId) {
        throw new functions.https.HttpsError('permission-denied', 'Order does not belong to user');
      }

      // Check if already processed
      if (orderData.status === 'COMPLETED') {
        throw new functions.https.HttpsError('already-exists', 'Payment already processed');
      }

      if (isDemo) {
        // Handle demo payment verification
        return await verifyDemoPayment(orderId, paymentId, signature, userId, orderData);
      }

      // Handle real payment verification
      // Verify payment signature
      const isValidSignature = verifyRazorpaySignature(orderId, paymentId, signature);
      if (!isValidSignature) {
        await updateOrderStatus(orderId, 'FAILED', 'Invalid signature');
        throw new functions.https.HttpsError('invalid-argument', 'Invalid payment signature');
      }

      // Verify payment with Razorpay
      const payment = await razorpay.payments.fetch(paymentId);
      if (payment.status !== 'captured' && payment.status !== 'authorized') {
        await updateOrderStatus(orderId, 'FAILED', 'Payment not successful');
        throw new functions.https.HttpsError('failed-precondition', 'Payment not successful');
      }

      // Credit coins to user account
      const coinCreditResult = await creditCoinsToUser(userId, orderData);
      
      // Update order status
      await updateOrderStatus(orderId, 'COMPLETED', 'Payment verified and coins credited', {
        paymentId: paymentId,
        coinTransactionId: coinCreditResult.transactionId
      });

      // Send confirmation notification
      await sendPaymentConfirmation(userId, orderData, coinCreditResult);

      return {
        success: true,
        transactionId: coinCreditResult.transactionId,
        coinsAdded: orderData.totalCoins,
        newBalance: coinCreditResult.newBalance
      };

    } catch (error) {
      console.error('Error verifying payment:', error);
      
      // Update order status on error
      if (orderId) {
        await updateOrderStatus(orderId, 'FAILED', error instanceof Error ? error.message : 'Unknown error');
      }
      
      if (error instanceof functions.https.HttpsError) {
        throw error;
      }
      
      throw new functions.https.HttpsError('internal', 'Payment verification failed');
    }
  });

/**
 * Spend coins for marketplace transactions
 */
export const spendCoins = functions
  .region(REGION)
  .https
  .onCall(async (data, context) => {
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { amount, purpose, metadata } = data;
    const userId = context.auth.uid;

    try {
      // Validate spend amount
      if (!amount || amount <= 0) {
        throw new functions.https.HttpsError('invalid-argument', 'Invalid coin amount');
      }

      // Get user's current coin balance
      const userDoc = await db.collection('users').doc(userId).get();
      if (!userDoc.exists) {
        throw new functions.https.HttpsError('not-found', 'User not found');
      }

      const userData = userDoc.data()!;
      const currentBalance = userData.coinBalance || 0;

      // Check sufficient balance
      if (currentBalance < amount) {
        throw new functions.https.HttpsError('failed-precondition', 'Insufficient coin balance');
      }

      // Apply tier-based discounts
      const userTier = userData.userTier || 'GENERAL';
      const discountedAmount = applyTierDiscount(amount, purpose, userTier);

      // Create spend transaction
      const transactionId = `spend_${userId}_${Date.now()}`;
      const transactionData = {
        id: transactionId,
        userId: userId,
        type: 'SPEND',
        amount: discountedAmount,
        originalAmount: amount,
        purpose: purpose,
        metadata: metadata || {},
        status: 'COMPLETED',
        balanceBefore: currentBalance,
        balanceAfter: currentBalance - discountedAmount,
        createdAt: admin.firestore.FieldValue.serverTimestamp()
      };

      // Use transaction to ensure atomicity
      await db.runTransaction(async (transaction) => {
        // Update user balance
        transaction.update(db.collection('users').doc(userId), {
          coinBalance: currentBalance - discountedAmount,
          lastCoinSpent: admin.firestore.FieldValue.serverTimestamp()
        });

        // Record transaction
        transaction.set(db.collection('coin_transactions').doc(transactionId), transactionData);
      });

      // Log spend transaction
      await logTransaction({
        type: 'COINS_SPENT',
        userId: userId,
        transactionId: transactionId,
        amount: discountedAmount,
        purpose: purpose,
        metadata: metadata
      });

      return {
        success: true,
        transactionId: transactionId,
        coinsSpent: discountedAmount,
        newBalance: currentBalance - discountedAmount,
        discountApplied: amount - discountedAmount
      };

    } catch (error) {
      console.error('Error spending coins:', error);
      
      if (error instanceof functions.https.HttpsError) {
        throw error;
      }
      
      throw new functions.https.HttpsError('internal', 'Failed to spend coins');
    }
  });

/**
 * Get user's coin balance and transaction history
 */
export const getCoinBalance = functions
  .region(REGION)
  .https
  .onCall(async (data, context) => {
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const userId = context.auth.uid;

    try {
      // Get user data
      const userDoc = await db.collection('users').doc(userId).get();
      if (!userDoc.exists) {
        throw new functions.https.HttpsError('not-found', 'User not found');
      }

      const userData = userDoc.data()!;
      const coinBalance = userData.coinBalance || 0;

      // Get recent transactions
      const transactionsSnapshot = await db.collection('coin_transactions')
        .where('userId', '==', userId)
        .orderBy('createdAt', 'desc')
        .limit(20)
        .get();

      const transactions = transactionsSnapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));

      // Calculate spending analytics
      const analytics = await calculateSpendingAnalytics(userId);

      return {
        balance: coinBalance,
        balanceInINR: coinBalance * COIN_RATE,
        transactions: transactions,
        analytics: analytics
      };

    } catch (error) {
      console.error('Error getting coin balance:', error);
      throw new functions.https.HttpsError('internal', 'Failed to get balance');
    }
  });

/**
 * Helper Functions
 */

function getCoinPackage(packageId: string, userTier: string): any {
  const packages: { [key: string]: any } = {
    // General user packages
    'basic_10': { coins: 10, bonusCoins: 0, totalCoins: 10, totalAmount: 50 },
    'standard_25': { coins: 25, bonusCoins: 2, totalCoins: 27, totalAmount: 125 },
    'premium_50': { coins: 50, bonusCoins: 5, totalCoins: 55, totalAmount: 250 },
    
    // Farmer packages
    'farmer_basic_50': { coins: 50, bonusCoins: 5, totalCoins: 55, totalAmount: 250 },
    'farmer_pro_100': { coins: 100, bonusCoins: 15, totalCoins: 115, totalAmount: 500 },
    'farmer_elite_200': { coins: 200, bonusCoins: 40, totalCoins: 240, totalAmount: 1000 },
    
    // Enthusiast packages
    'enthusiast_standard_100': { coins: 100, bonusCoins: 15, totalCoins: 115, totalAmount: 500 },
    'enthusiast_pro_250': { coins: 250, bonusCoins: 50, totalCoins: 300, totalAmount: 1250 },
    'enthusiast_elite_500': { coins: 500, bonusCoins: 125, totalCoins: 625, totalAmount: 2500 }
  };

  return packages[packageId];
}

async function validatePurchaseLimits(userId: string, amount: number): Promise<void> {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  const thisMonth = new Date();
  thisMonth.setDate(1);
  thisMonth.setHours(0, 0, 0, 0);

  // Check daily limit
  const dailySpent = await getDailySpent(userId, today);
  if (dailySpent + amount > 10000) { // ₹10,000 daily limit
    throw new functions.https.HttpsError('failed-precondition', 'Daily purchase limit exceeded');
  }

  // Check monthly limit
  const monthlySpent = await getMonthlySpent(userId, thisMonth);
  if (monthlySpent + amount > 50000) { // ₹50,000 monthly limit
    throw new functions.https.HttpsError('failed-precondition', 'Monthly purchase limit exceeded');
  }
}

function verifyRazorpaySignature(orderId: string, paymentId: string, signature: string): boolean {
  const body = orderId + '|' + paymentId;
  const expectedSignature = crypto
    .createHmac('sha256', functions.config().razorpay.key_secret)
    .update(body.toString())
    .digest('hex');
  
  return expectedSignature === signature;
}

async function creditCoinsToUser(userId: string, orderData: any): Promise<any> {
  const transactionId = `credit_${userId}_${Date.now()}`;
  
  return await db.runTransaction(async (transaction) => {
    const userRef = db.collection('users').doc(userId);
    const userDoc = await transaction.get(userRef);
    
    const currentBalance = userDoc.data()?.coinBalance || 0;
    const newBalance = currentBalance + orderData.totalCoins;
    
    // Update user balance
    transaction.update(userRef, {
      coinBalance: newBalance,
      lastCoinPurchase: admin.firestore.FieldValue.serverTimestamp(),
      totalCoinsEarned: admin.firestore.FieldValue.increment(orderData.totalCoins)
    });
    
    // Record transaction
    const transactionData = {
      id: transactionId,
      userId: userId,
      type: 'CREDIT',
      amount: orderData.totalCoins,
      purpose: 'COIN_PURCHASE',
      orderId: orderData.orderId,
      balanceBefore: currentBalance,
      balanceAfter: newBalance,
      status: 'COMPLETED',
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    };
    
    transaction.set(db.collection('coin_transactions').doc(transactionId), transactionData);
    
    return { transactionId, newBalance };
  });
}

async function updateOrderStatus(orderId: string, status: string, message?: string, additionalData?: any): Promise<void> {
  const updateData: any = {
    status: status,
    updatedAt: admin.firestore.FieldValue.serverTimestamp()
  };
  
  if (message) updateData.statusMessage = message;
  if (additionalData) Object.assign(updateData, additionalData);
  
  await db.collection('coin_orders').doc(orderId).update(updateData);
}

/**
 * Verify demo payment and credit coins
 */
async function verifyDemoPayment(
  orderId: string,
  paymentId: string,
  signature: string,
  userId: string,
  orderData: any
): Promise<any> {
  try {
    // Validate demo payment ID format
    if (!paymentId.startsWith('demo_pay_')) {
      throw new functions.https.HttpsError('invalid-argument', 'Invalid demo payment ID');
    }

    // Validate demo signature (simple validation for demo)
    const expectedSignature = `demo_signature_${orderId}_${paymentId}`;
    if (!signature.includes('demo_signature_')) {
      throw new functions.https.HttpsError('invalid-argument', 'Invalid demo signature');
    }

    // Get package details for coin calculation
    const packageDetails = getDemoPackageDetails(orderData.packageId);

    // Credit coins to user account
    const coinCreditResult = await creditCoinsToUser(userId, {
      ...orderData,
      totalCoins: packageDetails.totalCoins
    });

    // Update demo order status
    await db.collection('demo_orders').doc(orderId).update({
      status: 'COMPLETED',
      paymentId: paymentId,
      signature: signature,
      completedAt: admin.firestore.FieldValue.serverTimestamp(),
      coinTransactionId: coinCreditResult.transactionId
    });

    // Log demo transaction
    await logDemoTransaction({
      type: 'PAYMENT_VERIFIED',
      orderId: orderId,
      paymentId: paymentId,
      userId: userId,
      coinsAdded: packageDetails.totalCoins,
      transactionId: coinCreditResult.transactionId
    });

    // Send demo confirmation
    await sendDemoPaymentConfirmation(userId, orderData, coinCreditResult);

    return {
      success: true,
      transactionId: coinCreditResult.transactionId,
      coinsAdded: packageDetails.totalCoins,
      newBalance: coinCreditResult.newBalance,
      isDemoPayment: true
    };

  } catch (error) {
    console.error('Error verifying demo payment:', error);

    // Update demo order status on error
    await db.collection('demo_orders').doc(orderId).update({
      status: 'FAILED',
      failureReason: error instanceof Error ? error.message : 'Unknown error',
      failedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    throw error;
  }
}

/**
 * Get demo package details
 */
function getDemoPackageDetails(packageId: string) {
  const packages: { [key: string]: any } = {
    'package_100': { coins: 20, bonusCoins: 0, priceInRupees: 100 },
    'package_500': { coins: 100, bonusCoins: 10, priceInRupees: 500 },
    'package_1000': { coins: 200, bonusCoins: 25, priceInRupees: 1000 },
    'package_2000': { coins: 400, bonusCoins: 60, priceInRupees: 2000 },
    'package_5000': { coins: 1000, bonusCoins: 200, priceInRupees: 5000 }
  };

  const packageDetails = packages[packageId] || packages['package_100'];
  return {
    ...packageDetails,
    totalCoins: packageDetails.coins + packageDetails.bonusCoins
  };
}

/**
 * Log demo transaction for tracking
 */
async function logDemoTransaction(data: any) {
  try {
    await db.collection('demo_transaction_logs').add({
      ...data,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      environment: 'demo'
    });
  } catch (error) {
    console.error('Error logging demo transaction:', error);
    // Don't throw error for logging failures
  }
}

/**
 * Send demo payment confirmation
 */
async function sendDemoPaymentConfirmation(userId: string, orderData: any, coinCreditResult: any) {
  try {
    // Create notification for demo payment
    await db.collection('notifications').add({
      userId: userId,
      type: 'DEMO_PAYMENT_SUCCESS',
      title: 'Demo Payment Successful! 🎉',
      message: `${coinCreditResult.coinsAdded} coins have been added to your account. This was a demo transaction.`,
      data: {
        orderId: orderData.id,
        coinsAdded: coinCreditResult.coinsAdded,
        newBalance: coinCreditResult.newBalance,
        isDemoPayment: true
      },
      isRead: false,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
  } catch (error) {
    console.error('Error sending demo payment confirmation:', error);
    // Don't throw error for notification failures
  }
}

function applyTierDiscount(amount: number, purpose: string, userTier: string): number {
  const discounts: { [key: string]: { [key: string]: number } } = {
    'FARMER': {
      'MARKETPLACE_LISTING': 0.8, // 20% discount
      'PREMIUM_FEATURES': 0.9     // 10% discount
    },
    'ENTHUSIAST': {
      'MARKETPLACE_LISTING': 0.7, // 30% discount
      'PREMIUM_FEATURES': 0.7     // 30% discount
    }
  };
  
  const tierDiscounts = discounts[userTier];
  if (!tierDiscounts) return amount;
  
  const discount = tierDiscounts[purpose];
  return discount ? Math.ceil(amount * discount) : amount;
}

async function logTransaction(data: any): Promise<void> {
  await db.collection('transaction_logs').add({
    ...data,
    timestamp: admin.firestore.FieldValue.serverTimestamp()
  });
}

async function sendPaymentConfirmation(userId: string, orderData: any, creditResult: any): Promise<void> {
  // Implementation for sending confirmation notification
  await db.collection('notifications').add({
    userId: userId,
    type: 'PAYMENT_SUCCESS',
    title: 'Coins Added Successfully!',
    message: `${orderData.totalCoins} coins have been added to your account.`,
    data: {
      coinsAdded: orderData.totalCoins,
      newBalance: creditResult.newBalance,
      orderId: orderData.orderId
    },
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    read: false
  });
}

async function getDailySpent(userId: string, date: Date): Promise<number> {
  // Implementation to calculate daily spending
  return 0; // Placeholder
}

async function getMonthlySpent(userId: string, date: Date): Promise<number> {
  // Implementation to calculate monthly spending
  return 0; // Placeholder
}

async function calculateSpendingAnalytics(userId: string): Promise<any> {
  // Implementation for spending analytics
  return {
    thisMonth: 0,
    lastMonth: 0,
    topCategories: [],
    savingsOpportunities: []
  };
}

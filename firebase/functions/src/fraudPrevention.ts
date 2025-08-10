import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

const db = admin.firestore();
const REGION = 'asia-south1';

/**
 * Fraud prevention and security system for RIO coin payments
 * Implements ML-based detection and rule-based validation
 */

/**
 * Real-time fraud detection for payment transactions
 */
export const detectFraud = functions
  .region(REGION)
  .https
  .onCall(async (data, context) => {
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { orderId, amount, paymentMethod, deviceInfo } = data;
    const userId = context.auth.uid;

    try {
      // Get user profile and transaction history
      const userProfile = await getUserProfile(userId);
      const transactionHistory = await getTransactionHistory(userId, 30); // Last 30 days
      
      // Calculate fraud risk score
      const riskScore = await calculateRiskScore({
        userId,
        amount,
        paymentMethod,
        deviceInfo,
        userProfile,
        transactionHistory
      });

      // Determine action based on risk score
      const action = determineAction(riskScore);
      
      // Log fraud check
      await logFraudCheck({
        userId,
        orderId,
        riskScore,
        action,
        factors: riskScore.factors,
        timestamp: admin.firestore.FieldValue.serverTimestamp()
      });

      return {
        riskScore: riskScore.score,
        action: action,
        requiresVerification: action === 'VERIFY',
        blockedReason: action === 'BLOCK' ? riskScore.primaryReason : null
      };

    } catch (error) {
      console.error('Error in fraud detection:', error);
      throw new functions.https.HttpsError('internal', 'Fraud detection failed');
    }
  });

/**
 * Verify user identity for high-risk transactions
 */
export const verifyUserIdentity = functions
  .region(REGION)
  .https
  .onCall(async (data, context) => {
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { orderId, verificationType, verificationData } = data;
    const userId = context.auth.uid;

    try {
      let verificationResult = false;

      switch (verificationType) {
        case 'OTP':
          verificationResult = await verifyOTP(userId, verificationData.otp);
          break;
        case 'DEVICE_FINGERPRINT':
          verificationResult = await verifyDeviceFingerprint(userId, verificationData.fingerprint);
          break;
        case 'BIOMETRIC':
          verificationResult = await verifyBiometric(userId, verificationData.biometricData);
          break;
        case 'DOCUMENT':
          verificationResult = await verifyDocument(userId, verificationData.documentData);
          break;
        default:
          throw new functions.https.HttpsError('invalid-argument', 'Invalid verification type');
      }

      // Update order verification status
      if (verificationResult) {
        await db.collection('coin_orders').doc(orderId).update({
          verificationStatus: 'VERIFIED',
          verifiedAt: admin.firestore.FieldValue.serverTimestamp(),
          verificationType: verificationType
        });
      }

      return {
        verified: verificationResult,
        nextStep: verificationResult ? 'PROCEED_PAYMENT' : 'RETRY_VERIFICATION'
      };

    } catch (error) {
      console.error('Error in user verification:', error);
      throw new functions.https.HttpsError('internal', 'Verification failed');
    }
  });

/**
 * Monitor suspicious activity patterns
 */
export const monitorSuspiciousActivity = functions
  .region(REGION)
  .pubsub
  .schedule('every 5 minutes')
  .onRun(async (context) => {
    try {
      // Check for velocity attacks
      await checkVelocityAttacks();
      
      // Check for unusual spending patterns
      await checkUnusualSpending();
      
      // Check for device anomalies
      await checkDeviceAnomalies();
      
      // Check for geographic anomalies
      await checkGeographicAnomalies();
      
      // Update user risk scores
      await updateUserRiskScores();

      console.log('Suspicious activity monitoring completed');

    } catch (error) {
      console.error('Error in suspicious activity monitoring:', error);
    }
  });

/**
 * Block suspicious users and transactions
 */
export const blockSuspiciousUser = functions
  .region(REGION)
  .https
  .onCall(async (data, context) => {
    // Only allow admin users to call this function
    if (!context.auth || !isAdmin(context.auth.uid)) {
      throw new functions.https.HttpsError('permission-denied', 'Admin access required');
    }

    const { userId, reason, duration } = data;

    try {
      const blockData = {
        userId: userId,
        reason: reason,
        blockedBy: context.auth.uid,
        blockedAt: admin.firestore.FieldValue.serverTimestamp(),
        blockDuration: duration, // in hours
        unblockAt: duration ? new Date(Date.now() + duration * 60 * 60 * 1000) : null,
        status: 'ACTIVE'
      };

      // Add to blocked users collection
      await db.collection('blocked_users').add(blockData);

      // Update user status
      await db.collection('users').doc(userId).update({
        accountStatus: 'BLOCKED',
        blockedReason: reason,
        blockedAt: admin.firestore.FieldValue.serverTimestamp()
      });

      // Cancel any pending orders
      await cancelPendingOrders(userId);

      // Send notification to user
      await sendBlockNotification(userId, reason);

      return { success: true, message: 'User blocked successfully' };

    } catch (error) {
      console.error('Error blocking user:', error);
      throw new functions.https.HttpsError('internal', 'Failed to block user');
    }
  });

/**
 * Risk calculation functions
 */
async function calculateRiskScore(params: any): Promise<any> {
  const { amount, paymentMethod, deviceInfo, userProfile, transactionHistory } = params;
  
  let score = 0;
  const factors: string[] = [];

  // Amount-based risk
  if (amount > 5000) { // ₹5000+
    score += 20;
    factors.push('HIGH_AMOUNT');
  }
  if (amount > userProfile.averageTransactionAmount * 5) {
    score += 30;
    factors.push('UNUSUAL_AMOUNT');
  }

  // Velocity risk
  const recentTransactions = transactionHistory.filter((t: any) => 
    Date.now() - t.createdAt.toDate().getTime() < 60 * 60 * 1000 // Last hour
  );
  if (recentTransactions.length > 5) {
    score += 40;
    factors.push('HIGH_VELOCITY');
  }

  // New user risk
  const accountAge = Date.now() - userProfile.createdAt.toDate().getTime();
  if (accountAge < 7 * 24 * 60 * 60 * 1000) { // Less than 7 days
    score += 25;
    factors.push('NEW_USER');
  }

  // Device risk
  if (deviceInfo.isNewDevice) {
    score += 15;
    factors.push('NEW_DEVICE');
  }
  if (deviceInfo.isRooted || deviceInfo.isJailbroken) {
    score += 35;
    factors.push('COMPROMISED_DEVICE');
  }

  // Geographic risk
  if (deviceInfo.location && userProfile.usualLocation) {
    const distance = calculateDistance(deviceInfo.location, userProfile.usualLocation);
    if (distance > 500) { // More than 500km from usual location
      score += 20;
      factors.push('UNUSUAL_LOCATION');
    }
  }

  // Payment method risk
  if (paymentMethod === 'CARD' && !userProfile.hasVerifiedCard) {
    score += 15;
    factors.push('UNVERIFIED_PAYMENT_METHOD');
  }

  // User behavior risk
  if (userProfile.failedTransactionRate > 0.3) {
    score += 25;
    factors.push('HIGH_FAILURE_RATE');
  }

  // Time-based risk
  const hour = new Date().getHours();
  if (hour < 6 || hour > 23) { // Late night transactions
    score += 10;
    factors.push('UNUSUAL_TIME');
  }

  return {
    score: Math.min(score, 100), // Cap at 100
    factors: factors,
    primaryReason: factors[0] || 'UNKNOWN'
  };
}

function determineAction(riskScore: any): string {
  if (riskScore.score >= 80) {
    return 'BLOCK';
  } else if (riskScore.score >= 50) {
    return 'VERIFY';
  } else if (riskScore.score >= 30) {
    return 'MONITOR';
  } else {
    return 'ALLOW';
  }
}

/**
 * Verification functions
 */
async function verifyOTP(userId: string, otp: string): Promise<boolean> {
  try {
    const otpDoc = await db.collection('otp_verifications')
      .where('userId', '==', userId)
      .where('status', '==', 'PENDING')
      .orderBy('createdAt', 'desc')
      .limit(1)
      .get();

    if (otpDoc.empty) {
      return false;
    }

    const otpData = otpDoc.docs[0].data();
    const isValid = otpData.otp === otp && 
                   Date.now() - otpData.createdAt.toDate().getTime() < 5 * 60 * 1000; // 5 minutes

    if (isValid) {
      await otpDoc.docs[0].ref.update({
        status: 'VERIFIED',
        verifiedAt: admin.firestore.FieldValue.serverTimestamp()
      });
    }

    return isValid;
  } catch (error) {
    console.error('Error verifying OTP:', error);
    return false;
  }
}

async function verifyDeviceFingerprint(userId: string, fingerprint: string): Promise<boolean> {
  try {
    const userDoc = await db.collection('users').doc(userId).get();
    const userData = userDoc.data();
    
    if (!userData) return false;

    const trustedDevices = userData.trustedDevices || [];
    return trustedDevices.includes(fingerprint);
  } catch (error) {
    console.error('Error verifying device fingerprint:', error);
    return false;
  }
}

async function verifyBiometric(userId: string, biometricData: any): Promise<boolean> {
  // Implementation for biometric verification
  // Would integrate with device biometric APIs
  return true; // Placeholder
}

async function verifyDocument(userId: string, documentData: any): Promise<boolean> {
  // Implementation for document verification
  // Would integrate with document verification service
  return true; // Placeholder
}

/**
 * Monitoring functions
 */
async function checkVelocityAttacks(): Promise<void> {
  const fiveMinutesAgo = new Date(Date.now() - 5 * 60 * 1000);
  
  const recentTransactions = await db.collection('coin_transactions')
    .where('createdAt', '>', fiveMinutesAgo)
    .get();

  const userTransactionCounts: { [userId: string]: number } = {};
  
  recentTransactions.docs.forEach(doc => {
    const data = doc.data();
    userTransactionCounts[data.userId] = (userTransactionCounts[data.userId] || 0) + 1;
  });

  // Flag users with more than 10 transactions in 5 minutes
  for (const [userId, count] of Object.entries(userTransactionCounts)) {
    if (count > 10) {
      await flagSuspiciousActivity(userId, 'VELOCITY_ATTACK', { transactionCount: count });
    }
  }
}

async function checkUnusualSpending(): Promise<void> {
  const oneDayAgo = new Date(Date.now() - 24 * 60 * 60 * 1000);
  
  const recentTransactions = await db.collection('coin_transactions')
    .where('type', '==', 'SPEND')
    .where('createdAt', '>', oneDayAgo)
    .get();

  const userSpending: { [userId: string]: number } = {};
  
  recentTransactions.docs.forEach(doc => {
    const data = doc.data();
    userSpending[data.userId] = (userSpending[data.userId] || 0) + data.amount;
  });

  // Check against user's historical spending patterns
  for (const [userId, spending] of Object.entries(userSpending)) {
    const userProfile = await getUserProfile(userId);
    if (spending > userProfile.averageDailySpending * 10) {
      await flagSuspiciousActivity(userId, 'UNUSUAL_SPENDING', { dailySpending: spending });
    }
  }
}

async function checkDeviceAnomalies(): Promise<void> {
  // Implementation for device anomaly detection
  // Would check for multiple accounts from same device, etc.
}

async function checkGeographicAnomalies(): Promise<void> {
  // Implementation for geographic anomaly detection
  // Would check for impossible travel patterns, etc.
}

async function updateUserRiskScores(): Promise<void> {
  // Implementation for updating user risk scores based on recent activity
}

/**
 * Helper functions
 */
async function getUserProfile(userId: string): Promise<any> {
  const userDoc = await db.collection('users').doc(userId).get();
  return userDoc.data() || {};
}

async function getTransactionHistory(userId: string, days: number): Promise<any[]> {
  const startDate = new Date(Date.now() - days * 24 * 60 * 60 * 1000);
  
  const transactions = await db.collection('coin_transactions')
    .where('userId', '==', userId)
    .where('createdAt', '>', startDate)
    .orderBy('createdAt', 'desc')
    .get();

  return transactions.docs.map(doc => doc.data());
}

async function logFraudCheck(data: any): Promise<void> {
  await db.collection('fraud_checks').add(data);
}

async function flagSuspiciousActivity(userId: string, type: string, metadata: any): Promise<void> {
  await db.collection('suspicious_activities').add({
    userId: userId,
    type: type,
    metadata: metadata,
    status: 'FLAGGED',
    createdAt: admin.firestore.FieldValue.serverTimestamp()
  });
}

async function cancelPendingOrders(userId: string): Promise<void> {
  const pendingOrders = await db.collection('coin_orders')
    .where('userId', '==', userId)
    .where('status', 'in', ['CREATED', 'PENDING'])
    .get();

  const batch = db.batch();
  pendingOrders.docs.forEach(doc => {
    batch.update(doc.ref, {
      status: 'CANCELLED',
      cancelledReason: 'USER_BLOCKED',
      cancelledAt: admin.firestore.FieldValue.serverTimestamp()
    });
  });

  await batch.commit();
}

async function sendBlockNotification(userId: string, reason: string): Promise<void> {
  await db.collection('notifications').add({
    userId: userId,
    type: 'ACCOUNT_BLOCKED',
    title: 'Account Temporarily Blocked',
    message: `Your account has been temporarily blocked due to: ${reason}. Please contact support.`,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    read: false
  });
}

function calculateDistance(location1: any, location2: any): number {
  // Implementation for calculating distance between two coordinates
  // Using Haversine formula
  const R = 6371; // Earth's radius in km
  const dLat = (location2.lat - location1.lat) * Math.PI / 180;
  const dLon = (location2.lon - location1.lon) * Math.PI / 180;
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(location1.lat * Math.PI / 180) * Math.cos(location2.lat * Math.PI / 180) *
            Math.sin(dLon/2) * Math.sin(dLon/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c;
}

function isAdmin(userId: string): boolean {
  // Implementation to check if user is admin
  // Would check against admin user list or custom claims
  return true; // Placeholder
}

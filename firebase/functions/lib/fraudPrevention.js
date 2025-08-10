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
exports.blockSuspiciousUser = exports.monitorSuspiciousActivity = exports.verifyUserIdentity = exports.detectFraud = void 0;
const functions = __importStar(require("firebase-functions"));
const admin = __importStar(require("firebase-admin"));
const db = admin.firestore();
const REGION = 'asia-south1';
/**
 * Fraud prevention and security system for RIO coin payments
 * Implements ML-based detection and rule-based validation
 */
/**
 * Real-time fraud detection for payment transactions
 */
exports.detectFraud = functions
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
    }
    catch (error) {
        console.error('Error in fraud detection:', error);
        throw new functions.https.HttpsError('internal', 'Fraud detection failed');
    }
});
/**
 * Verify user identity for high-risk transactions
 */
exports.verifyUserIdentity = functions
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
    }
    catch (error) {
        console.error('Error in user verification:', error);
        throw new functions.https.HttpsError('internal', 'Verification failed');
    }
});
/**
 * Monitor suspicious activity patterns
 */
exports.monitorSuspiciousActivity = functions
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
    }
    catch (error) {
        console.error('Error in suspicious activity monitoring:', error);
    }
});
/**
 * Block suspicious users and transactions
 */
exports.blockSuspiciousUser = functions
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
    }
    catch (error) {
        console.error('Error blocking user:', error);
        throw new functions.https.HttpsError('internal', 'Failed to block user');
    }
});
/**
 * Risk calculation functions
 */
async function calculateRiskScore(params) {
    const { amount, paymentMethod, deviceInfo, userProfile, transactionHistory } = params;
    let score = 0;
    const factors = [];
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
    const recentTransactions = transactionHistory.filter((t) => Date.now() - t.createdAt.toDate().getTime() < 60 * 60 * 1000 // Last hour
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
function determineAction(riskScore) {
    if (riskScore.score >= 80) {
        return 'BLOCK';
    }
    else if (riskScore.score >= 50) {
        return 'VERIFY';
    }
    else if (riskScore.score >= 30) {
        return 'MONITOR';
    }
    else {
        return 'ALLOW';
    }
}
/**
 * Verification functions
 */
async function verifyOTP(userId, otp) {
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
    }
    catch (error) {
        console.error('Error verifying OTP:', error);
        return false;
    }
}
async function verifyDeviceFingerprint(userId, fingerprint) {
    try {
        const userDoc = await db.collection('users').doc(userId).get();
        const userData = userDoc.data();
        if (!userData)
            return false;
        const trustedDevices = userData.trustedDevices || [];
        return trustedDevices.includes(fingerprint);
    }
    catch (error) {
        console.error('Error verifying device fingerprint:', error);
        return false;
    }
}
async function verifyBiometric(userId, biometricData) {
    // Implementation for biometric verification
    // Would integrate with device biometric APIs
    return true; // Placeholder
}
async function verifyDocument(userId, documentData) {
    // Implementation for document verification
    // Would integrate with document verification service
    return true; // Placeholder
}
/**
 * Monitoring functions
 */
async function checkVelocityAttacks() {
    const fiveMinutesAgo = new Date(Date.now() - 5 * 60 * 1000);
    const recentTransactions = await db.collection('coin_transactions')
        .where('createdAt', '>', fiveMinutesAgo)
        .get();
    const userTransactionCounts = {};
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
async function checkUnusualSpending() {
    const oneDayAgo = new Date(Date.now() - 24 * 60 * 60 * 1000);
    const recentTransactions = await db.collection('coin_transactions')
        .where('type', '==', 'SPEND')
        .where('createdAt', '>', oneDayAgo)
        .get();
    const userSpending = {};
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
async function checkDeviceAnomalies() {
    // Implementation for device anomaly detection
    // Would check for multiple accounts from same device, etc.
}
async function checkGeographicAnomalies() {
    // Implementation for geographic anomaly detection
    // Would check for impossible travel patterns, etc.
}
async function updateUserRiskScores() {
    // Implementation for updating user risk scores based on recent activity
}
/**
 * Helper functions
 */
async function getUserProfile(userId) {
    const userDoc = await db.collection('users').doc(userId).get();
    return userDoc.data() || {};
}
async function getTransactionHistory(userId, days) {
    const startDate = new Date(Date.now() - days * 24 * 60 * 60 * 1000);
    const transactions = await db.collection('coin_transactions')
        .where('userId', '==', userId)
        .where('createdAt', '>', startDate)
        .orderBy('createdAt', 'desc')
        .get();
    return transactions.docs.map(doc => doc.data());
}
async function logFraudCheck(data) {
    await db.collection('fraud_checks').add(data);
}
async function flagSuspiciousActivity(userId, type, metadata) {
    await db.collection('suspicious_activities').add({
        userId: userId,
        type: type,
        metadata: metadata,
        status: 'FLAGGED',
        createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
}
async function cancelPendingOrders(userId) {
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
async function sendBlockNotification(userId, reason) {
    await db.collection('notifications').add({
        userId: userId,
        type: 'ACCOUNT_BLOCKED',
        title: 'Account Temporarily Blocked',
        message: `Your account has been temporarily blocked due to: ${reason}. Please contact support.`,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        read: false
    });
}
function calculateDistance(location1, location2) {
    // Implementation for calculating distance between two coordinates
    // Using Haversine formula
    const R = 6371; // Earth's radius in km
    const dLat = (location2.lat - location1.lat) * Math.PI / 180;
    const dLon = (location2.lon - location1.lon) * Math.PI / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(location1.lat * Math.PI / 180) * Math.cos(location2.lat * Math.PI / 180) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}
function isAdmin(userId) {
    // Implementation to check if user is admin
    // Would check against admin user list or custom claims
    return true; // Placeholder
}
//# sourceMappingURL=fraudPrevention.js.map
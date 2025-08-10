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
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.resolveDispute = exports.escalateDisputes = exports.processAutomaticRefunds = exports.createDispute = exports.createRefundRequest = void 0;
const functions = __importStar(require("firebase-functions"));
const admin = __importStar(require("firebase-admin"));
const razorpay_1 = __importDefault(require("razorpay"));
const db = admin.firestore();
const razorpay = new razorpay_1.default({
    key_id: functions.config().razorpay.key_id,
    key_secret: functions.config().razorpay.key_secret,
});
const REGION = 'asia-south1';
/**
 * Comprehensive refund and dispute management system
 * Handles automatic refunds, dispute resolution, and chargeback protection
 */
/**
 * Create refund request
 */
exports.createRefundRequest = functions
    .region(REGION)
    .https
    .onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }
    const { orderId, reason, refundType, amount, evidence } = data;
    const userId = context.auth.uid;
    try {
        // Get order details
        const orderDoc = await db.collection('coin_orders').doc(orderId).get();
        if (!orderDoc.exists) {
            throw new functions.https.HttpsError('not-found', 'Order not found');
        }
        const orderData = orderDoc.data();
        if (orderData.userId !== userId) {
            throw new functions.https.HttpsError('permission-denied', 'Order does not belong to user');
        }
        // Validate refund eligibility
        const eligibility = await validateRefundEligibility(orderData, refundType);
        if (!eligibility.eligible) {
            throw new functions.https.HttpsError('failed-precondition', eligibility.reason);
        }
        // Calculate refund amount
        const refundAmount = amount || calculateRefundAmount(orderData, refundType);
        // Create refund request
        const refundRequestId = `refund_${orderId}_${Date.now()}`;
        const refundRequestData = {
            id: refundRequestId,
            orderId: orderId,
            userId: userId,
            refundType: refundType,
            requestedAmount: refundAmount,
            reason: reason,
            evidence: evidence || {},
            status: 'PENDING',
            priority: determineRefundPriority(refundType, reason),
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
            estimatedProcessingTime: getEstimatedProcessingTime(refundType),
            autoProcessEligible: isAutoProcessEligible(refundType, reason, orderData)
        };
        await db.collection('refund_requests').doc(refundRequestId).set(refundRequestData);
        // Process automatic refunds immediately
        if (refundRequestData.autoProcessEligible) {
            await processAutomaticRefund(refundRequestId, refundRequestData);
        }
        else {
            // Queue for manual review
            await queueForManualReview(refundRequestId, refundRequestData);
        }
        // Send confirmation to user
        await sendRefundRequestConfirmation(userId, refundRequestData);
        return {
            refundRequestId: refundRequestId,
            status: refundRequestData.status,
            estimatedProcessingTime: refundRequestData.estimatedProcessingTime,
            autoProcess: refundRequestData.autoProcessEligible
        };
    }
    catch (error) {
        console.error('Error creating refund request:', error);
        if (error instanceof functions.https.HttpsError) {
            throw error;
        }
        throw new functions.https.HttpsError('internal', 'Failed to create refund request');
    }
});
/**
 * Create dispute for marketplace transactions
 */
exports.createDispute = functions
    .region(REGION)
    .https
    .onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }
    const { transactionId, disputeType, reason, evidence, requestedResolution } = data;
    const userId = context.auth.uid;
    try {
        // Get transaction details
        const transactionDoc = await db.collection('coin_transactions').doc(transactionId).get();
        if (!transactionDoc.exists) {
            throw new functions.https.HttpsError('not-found', 'Transaction not found');
        }
        const transactionData = transactionDoc.data();
        // Validate user can dispute this transaction
        const canDispute = await validateDisputeEligibility(userId, transactionData);
        if (!canDispute.eligible) {
            throw new functions.https.HttpsError('failed-precondition', canDispute.reason);
        }
        // Create dispute
        const disputeId = `dispute_${transactionId}_${Date.now()}`;
        const disputeData = {
            id: disputeId,
            transactionId: transactionId,
            disputantId: userId,
            respondentId: getRespondentId(transactionData, userId),
            disputeType: disputeType,
            reason: reason,
            evidence: evidence,
            requestedResolution: requestedResolution,
            status: 'OPEN',
            priority: determineDisputePriority(disputeType, transactionData),
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
            escalationLevel: 0,
            mediatorId: null,
            resolutionDeadline: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000), // 7 days
            escrowAmount: transactionData.amount,
            escrowStatus: 'HELD'
        };
        await db.collection('disputes').doc(disputeId).set(disputeData);
        // Hold transaction amount in escrow
        await holdAmountInEscrow(transactionData, disputeId);
        // Notify respondent
        await notifyRespondent(disputeData);
        // Assign mediator for high-priority disputes
        if (disputeData.priority === 'HIGH') {
            await assignMediator(disputeId);
        }
        return {
            disputeId: disputeId,
            status: disputeData.status,
            resolutionDeadline: disputeData.resolutionDeadline,
            escrowAmount: disputeData.escrowAmount
        };
    }
    catch (error) {
        console.error('Error creating dispute:', error);
        if (error instanceof functions.https.HttpsError) {
            throw error;
        }
        throw new functions.https.HttpsError('internal', 'Failed to create dispute');
    }
});
/**
 * Process automatic refunds
 */
exports.processAutomaticRefunds = functions
    .region(REGION)
    .pubsub
    .schedule('every 5 minutes')
    .onRun(async (context) => {
    try {
        // Get pending automatic refunds
        const pendingRefunds = await db.collection('refund_requests')
            .where('status', '==', 'PENDING')
            .where('autoProcessEligible', '==', true)
            .limit(50)
            .get();
        console.log(`Processing ${pendingRefunds.size} automatic refunds`);
        for (const refundDoc of pendingRefunds.docs) {
            const refundData = refundDoc.data();
            try {
                await processAutomaticRefund(refundDoc.id, refundData);
            }
            catch (error) {
                console.error(`Error processing refund ${refundDoc.id}:`, error);
                // Mark as failed and queue for manual review
                await refundDoc.ref.update({
                    status: 'FAILED',
                    failureReason: error instanceof Error ? error.message : 'Unknown error',
                    requiresManualReview: true,
                    updatedAt: admin.firestore.FieldValue.serverTimestamp()
                });
            }
        }
    }
    catch (error) {
        console.error('Error in automatic refund processing:', error);
    }
});
/**
 * Escalate unresolved disputes
 */
exports.escalateDisputes = functions
    .region(REGION)
    .pubsub
    .schedule('every 1 hours')
    .onRun(async (context) => {
    try {
        const now = new Date();
        // Get disputes approaching deadline
        const approachingDeadline = await db.collection('disputes')
            .where('status', '==', 'OPEN')
            .where('resolutionDeadline', '<=', new Date(now.getTime() + 24 * 60 * 60 * 1000)) // 24 hours
            .get();
        for (const disputeDoc of approachingDeadline.docs) {
            const disputeData = disputeDoc.data();
            if (disputeData.escalationLevel < 2) {
                await escalateDispute(disputeDoc.id, disputeData);
            }
            else {
                // Auto-resolve in favor of platform policy
                await autoResolveDispute(disputeDoc.id, disputeData);
            }
        }
    }
    catch (error) {
        console.error('Error in dispute escalation:', error);
    }
});
/**
 * Resolve dispute
 */
exports.resolveDispute = functions
    .region(REGION)
    .https
    .onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }
    const { disputeId, resolution, reasoning, compensationAmount } = data;
    const resolverId = context.auth.uid;
    try {
        // Verify resolver has permission (mediator or admin)
        const hasPermission = await verifyResolverPermission(resolverId, disputeId);
        if (!hasPermission) {
            throw new functions.https.HttpsError('permission-denied', 'Insufficient permissions');
        }
        // Get dispute details
        const disputeDoc = await db.collection('disputes').doc(disputeId).get();
        if (!disputeDoc.exists) {
            throw new functions.https.HttpsError('not-found', 'Dispute not found');
        }
        const disputeData = disputeDoc.data();
        // Process resolution
        const resolutionResult = await processDisputeResolution(disputeData, resolution, compensationAmount);
        // Update dispute status
        await disputeDoc.ref.update({
            status: 'RESOLVED',
            resolution: resolution,
            reasoning: reasoning,
            resolverId: resolverId,
            resolvedAt: admin.firestore.FieldValue.serverTimestamp(),
            compensationAmount: compensationAmount || 0,
            resolutionDetails: resolutionResult
        });
        // Release escrow
        await releaseEscrow(disputeData, resolution, compensationAmount);
        // Notify parties
        await notifyDisputeResolution(disputeData, resolution, reasoning);
        // Update user ratings if applicable
        await updateUserRatings(disputeData, resolution);
        return {
            success: true,
            resolution: resolution,
            compensationAmount: compensationAmount || 0
        };
    }
    catch (error) {
        console.error('Error resolving dispute:', error);
        if (error instanceof functions.https.HttpsError) {
            throw error;
        }
        throw new functions.https.HttpsError('internal', 'Failed to resolve dispute');
    }
});
/**
 * Helper functions
 */
async function validateRefundEligibility(orderData, refundType) {
    const orderTime = orderData.createdAt.toDate();
    const hoursSinceOrder = (Date.now() - orderTime.getTime()) / (1000 * 60 * 60);
    switch (refundType) {
        case 'IMMEDIATE':
            // Allow immediate refunds within 1 hour
            return {
                eligible: hoursSinceOrder <= 1,
                reason: hoursSinceOrder > 1 ? 'Immediate refund window expired' : null
            };
        case 'STANDARD':
            // Allow standard refunds within 24 hours
            return {
                eligible: hoursSinceOrder <= 24 && orderData.status !== 'COMPLETED',
                reason: hoursSinceOrder > 24 ? 'Standard refund window expired' :
                    orderData.status === 'COMPLETED' ? 'Cannot refund completed order' : null
            };
        case 'DISPUTE':
            // Allow dispute refunds within 30 days
            return {
                eligible: hoursSinceOrder <= 24 * 30,
                reason: hoursSinceOrder > 24 * 30 ? 'Dispute window expired' : null
            };
        default:
            return { eligible: false, reason: 'Invalid refund type' };
    }
}
function calculateRefundAmount(orderData, refundType) {
    switch (refundType) {
        case 'IMMEDIATE':
            return orderData.amount; // Full refund
        case 'STANDARD':
            return orderData.amount; // Full refund
        case 'DISPUTE':
            return orderData.amount * 0.9; // 90% refund (10% processing fee)
        default:
            return 0;
    }
}
function determineRefundPriority(refundType, reason) {
    if (refundType === 'IMMEDIATE' || reason.includes('FRAUD')) {
        return 'HIGH';
    }
    else if (refundType === 'DISPUTE') {
        return 'MEDIUM';
    }
    else {
        return 'LOW';
    }
}
function getEstimatedProcessingTime(refundType) {
    switch (refundType) {
        case 'IMMEDIATE':
            return '5-10 minutes';
        case 'STANDARD':
            return '1-2 hours';
        case 'DISPUTE':
            return '3-5 business days';
        default:
            return '1-3 business days';
    }
}
function isAutoProcessEligible(refundType, reason, orderData) {
    // Auto-process immediate refunds and technical failures
    return refundType === 'IMMEDIATE' ||
        reason.includes('TECHNICAL_ERROR') ||
        reason.includes('PAYMENT_FAILED');
}
async function processAutomaticRefund(refundRequestId, refundData) {
    try {
        // Get original order
        const orderDoc = await db.collection('coin_orders').doc(refundData.orderId).get();
        const orderData = orderDoc.data();
        // Process Razorpay refund
        const refund = await razorpay.payments.refund(orderData.paymentId, {
            amount: refundData.requestedAmount * 100, // Convert to paise
            notes: {
                refundRequestId: refundRequestId,
                reason: refundData.reason
            }
        });
        // Deduct coins from user account
        await deductCoinsFromUser(refundData.userId, orderData.totalCoins, refundData.orderId);
        // Update refund request status
        await db.collection('refund_requests').doc(refundRequestId).update({
            status: 'PROCESSED',
            razorpayRefundId: refund.id,
            processedAmount: refundData.requestedAmount,
            processedAt: admin.firestore.FieldValue.serverTimestamp()
        });
        // Send confirmation
        await sendRefundConfirmation(refundData.userId, refundData, refund.id);
    }
    catch (error) {
        throw new Error(`Automatic refund processing failed: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
}
async function validateDisputeEligibility(userId, transactionData) {
    // Check if user is involved in the transaction
    const isInvolved = transactionData.userId === userId ||
        transactionData.recipientId === userId ||
        transactionData.senderId === userId;
    if (!isInvolved) {
        return { eligible: false, reason: 'User not involved in transaction' };
    }
    // Check if dispute window is open (30 days)
    const transactionTime = transactionData.createdAt.toDate();
    const daysSinceTransaction = (Date.now() - transactionTime.getTime()) / (1000 * 60 * 60 * 24);
    if (daysSinceTransaction > 30) {
        return { eligible: false, reason: 'Dispute window expired' };
    }
    // Check if there's already an open dispute
    const existingDispute = await db.collection('disputes')
        .where('transactionId', '==', transactionData.id)
        .where('status', 'in', ['OPEN', 'ESCALATED'])
        .limit(1)
        .get();
    if (!existingDispute.empty) {
        return { eligible: false, reason: 'Dispute already exists for this transaction' };
    }
    return { eligible: true, reason: null };
}
function getRespondentId(transactionData, disputantId) {
    // Return the other party in the transaction
    if (transactionData.userId === disputantId) {
        return transactionData.recipientId || transactionData.senderId;
    }
    else {
        return transactionData.userId;
    }
}
function determineDisputePriority(disputeType, transactionData) {
    if (disputeType === 'FRAUD' || transactionData.amount > 1000) {
        return 'HIGH';
    }
    else if (disputeType === 'SERVICE_NOT_DELIVERED' || transactionData.amount > 200) {
        return 'MEDIUM';
    }
    else {
        return 'LOW';
    }
}
async function holdAmountInEscrow(transactionData, disputeId) {
    // Implementation for holding transaction amount in escrow
    await db.collection('escrow_holds').add({
        disputeId: disputeId,
        transactionId: transactionData.id,
        amount: transactionData.amount,
        status: 'HELD',
        createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
}
async function notifyRespondent(disputeData) {
    await db.collection('notifications').add({
        userId: disputeData.respondentId,
        type: 'DISPUTE_CREATED',
        title: 'New Dispute Opened',
        message: `A dispute has been opened regarding your transaction. Please respond within 7 days.`,
        data: { disputeId: disputeData.id },
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        read: false
    });
}
async function assignMediator(disputeId) {
    // Implementation for assigning mediator to high-priority disputes
    const availableMediators = await db.collection('mediators')
        .where('status', '==', 'AVAILABLE')
        .where('caseLoad', '<', 10)
        .limit(1)
        .get();
    if (!availableMediators.empty) {
        const mediator = availableMediators.docs[0];
        await db.collection('disputes').doc(disputeId).update({
            mediatorId: mediator.id,
            assignedAt: admin.firestore.FieldValue.serverTimestamp()
        });
        await mediator.ref.update({
            caseLoad: admin.firestore.FieldValue.increment(1)
        });
    }
}
async function escalateDispute(disputeId, disputeData) {
    await db.collection('disputes').doc(disputeId).update({
        escalationLevel: disputeData.escalationLevel + 1,
        status: 'ESCALATED',
        escalatedAt: admin.firestore.FieldValue.serverTimestamp(),
        resolutionDeadline: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000) // 3 more days
    });
}
async function autoResolveDispute(disputeId, disputeData) {
    // Auto-resolve based on platform policy
    const resolution = 'PARTIAL_REFUND'; // Default policy
    const compensationAmount = disputeData.escrowAmount * 0.5; // 50% compensation
    await db.collection('disputes').doc(disputeId).update({
        status: 'AUTO_RESOLVED',
        resolution: resolution,
        reasoning: 'Auto-resolved due to deadline expiry',
        resolverId: 'SYSTEM',
        resolvedAt: admin.firestore.FieldValue.serverTimestamp(),
        compensationAmount: compensationAmount
    });
    await releaseEscrow(disputeData, resolution, compensationAmount);
}
async function processDisputeResolution(disputeData, resolution, compensationAmount) {
    // Implementation for processing dispute resolution
    return {
        resolution: resolution,
        compensationAmount: compensationAmount,
        processedAt: new Date()
    };
}
async function releaseEscrow(disputeData, resolution, compensationAmount) {
    // Implementation for releasing escrow based on resolution
    await db.collection('escrow_holds')
        .where('disputeId', '==', disputeData.id)
        .limit(1)
        .get()
        .then(snapshot => {
        if (!snapshot.empty) {
            snapshot.docs[0].ref.update({
                status: 'RELEASED',
                resolution: resolution,
                compensationAmount: compensationAmount,
                releasedAt: admin.firestore.FieldValue.serverTimestamp()
            });
        }
    });
}
async function notifyDisputeResolution(disputeData, resolution, reasoning) {
    // Notify both parties
    const notifications = [
        {
            userId: disputeData.disputantId,
            message: `Your dispute has been resolved: ${resolution}. ${reasoning}`
        },
        {
            userId: disputeData.respondentId,
            message: `The dispute against you has been resolved: ${resolution}. ${reasoning}`
        }
    ];
    for (const notification of notifications) {
        await db.collection('notifications').add(Object.assign(Object.assign({}, notification), { type: 'DISPUTE_RESOLVED', title: 'Dispute Resolved', data: { disputeId: disputeData.id, resolution: resolution }, createdAt: admin.firestore.FieldValue.serverTimestamp(), read: false }));
    }
}
async function updateUserRatings(disputeData, resolution) {
    // Implementation for updating user ratings based on dispute resolution
    // Would affect user reputation scores
}
async function verifyResolverPermission(resolverId, disputeId) {
    // Check if resolver is mediator assigned to dispute or admin
    const disputeDoc = await db.collection('disputes').doc(disputeId).get();
    const disputeData = disputeDoc.data();
    return (disputeData === null || disputeData === void 0 ? void 0 : disputeData.mediatorId) === resolverId || await isAdmin(resolverId);
}
async function isAdmin(userId) {
    // Implementation to check if user is admin
    return true; // Placeholder
}
async function deductCoinsFromUser(userId, coinAmount, orderId) {
    // Implementation for deducting coins from user account
    // Similar to implementation in razorpayIntegration.ts
}
async function sendRefundRequestConfirmation(userId, refundData) {
    // Implementation for sending refund request confirmation
}
async function sendRefundConfirmation(userId, refundData, refundId) {
    // Implementation for sending refund confirmation
}
async function queueForManualReview(refundRequestId, refundData) {
    // Implementation for queuing refund for manual review
}
//# sourceMappingURL=refundDispute.js.map
import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import Razorpay from 'razorpay';

const db = admin.firestore();
const razorpay = new Razorpay({
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
export const createRefundRequest = functions
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

      const orderData = orderDoc.data()!;
      
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
      } else {
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

    } catch (error) {
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
export const createDispute = functions
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

      const transactionData = transactionDoc.data()!;
      
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

    } catch (error) {
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
export const processAutomaticRefunds = functions
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
        } catch (error) {
          console.error(`Error processing refund ${refundDoc.id}:`, error);
          
          // Mark as failed and queue for manual review
          await refundDoc.ref.update({
            status: 'FAILED',
            failureReason: error.message,
            requiresManualReview: true,
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
          });
        }
      }

    } catch (error) {
      console.error('Error in automatic refund processing:', error);
    }
  });

/**
 * Escalate unresolved disputes
 */
export const escalateDisputes = functions
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
        } else {
          // Auto-resolve in favor of platform policy
          await autoResolveDispute(disputeDoc.id, disputeData);
        }
      }

    } catch (error) {
      console.error('Error in dispute escalation:', error);
    }
  });

/**
 * Resolve dispute
 */
export const resolveDispute = functions
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

      const disputeData = disputeDoc.data()!;

      // Process resolution
      const resolutionResult = await processDisputeResolution(
        disputeData,
        resolution,
        compensationAmount
      );

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

    } catch (error) {
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
async function validateRefundEligibility(orderData: any, refundType: string): Promise<any> {
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

function calculateRefundAmount(orderData: any, refundType: string): number {
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

function determineRefundPriority(refundType: string, reason: string): string {
  if (refundType === 'IMMEDIATE' || reason.includes('FRAUD')) {
    return 'HIGH';
  } else if (refundType === 'DISPUTE') {
    return 'MEDIUM';
  } else {
    return 'LOW';
  }
}

function getEstimatedProcessingTime(refundType: string): string {
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

function isAutoProcessEligible(refundType: string, reason: string, orderData: any): boolean {
  // Auto-process immediate refunds and technical failures
  return refundType === 'IMMEDIATE' || 
         reason.includes('TECHNICAL_ERROR') ||
         reason.includes('PAYMENT_FAILED');
}

async function processAutomaticRefund(refundRequestId: string, refundData: any): Promise<void> {
  try {
    // Get original order
    const orderDoc = await db.collection('coin_orders').doc(refundData.orderId).get();
    const orderData = orderDoc.data()!;

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

  } catch (error) {
    throw new Error(`Automatic refund processing failed: ${error.message}`);
  }
}

async function validateDisputeEligibility(userId: string, transactionData: any): Promise<any> {
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

function getRespondentId(transactionData: any, disputantId: string): string {
  // Return the other party in the transaction
  if (transactionData.userId === disputantId) {
    return transactionData.recipientId || transactionData.senderId;
  } else {
    return transactionData.userId;
  }
}

function determineDisputePriority(disputeType: string, transactionData: any): string {
  if (disputeType === 'FRAUD' || transactionData.amount > 1000) {
    return 'HIGH';
  } else if (disputeType === 'SERVICE_NOT_DELIVERED' || transactionData.amount > 200) {
    return 'MEDIUM';
  } else {
    return 'LOW';
  }
}

async function holdAmountInEscrow(transactionData: any, disputeId: string): Promise<void> {
  // Implementation for holding transaction amount in escrow
  await db.collection('escrow_holds').add({
    disputeId: disputeId,
    transactionId: transactionData.id,
    amount: transactionData.amount,
    status: 'HELD',
    createdAt: admin.firestore.FieldValue.serverTimestamp()
  });
}

async function notifyRespondent(disputeData: any): Promise<void> {
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

async function assignMediator(disputeId: string): Promise<void> {
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

async function escalateDispute(disputeId: string, disputeData: any): Promise<void> {
  await db.collection('disputes').doc(disputeId).update({
    escalationLevel: disputeData.escalationLevel + 1,
    status: 'ESCALATED',
    escalatedAt: admin.firestore.FieldValue.serverTimestamp(),
    resolutionDeadline: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000) // 3 more days
  });
}

async function autoResolveDispute(disputeId: string, disputeData: any): Promise<void> {
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

async function processDisputeResolution(disputeData: any, resolution: string, compensationAmount: number): Promise<any> {
  // Implementation for processing dispute resolution
  return {
    resolution: resolution,
    compensationAmount: compensationAmount,
    processedAt: new Date()
  };
}

async function releaseEscrow(disputeData: any, resolution: string, compensationAmount: number): Promise<void> {
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

async function notifyDisputeResolution(disputeData: any, resolution: string, reasoning: string): Promise<void> {
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
    await db.collection('notifications').add({
      ...notification,
      type: 'DISPUTE_RESOLVED',
      title: 'Dispute Resolved',
      data: { disputeId: disputeData.id, resolution: resolution },
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      read: false
    });
  }
}

async function updateUserRatings(disputeData: any, resolution: string): Promise<void> {
  // Implementation for updating user ratings based on dispute resolution
  // Would affect user reputation scores
}

async function verifyResolverPermission(resolverId: string, disputeId: string): Promise<boolean> {
  // Check if resolver is mediator assigned to dispute or admin
  const disputeDoc = await db.collection('disputes').doc(disputeId).get();
  const disputeData = disputeDoc.data();
  
  return disputeData?.mediatorId === resolverId || await isAdmin(resolverId);
}

async function isAdmin(userId: string): Promise<boolean> {
  // Implementation to check if user is admin
  return true; // Placeholder
}

async function deductCoinsFromUser(userId: string, coinAmount: number, orderId: string): Promise<void> {
  // Implementation for deducting coins from user account
  // Similar to implementation in razorpayIntegration.ts
}

async function sendRefundRequestConfirmation(userId: string, refundData: any): Promise<void> {
  // Implementation for sending refund request confirmation
}

async function sendRefundConfirmation(userId: string, refundData: any, refundId: string): Promise<void> {
  // Implementation for sending refund confirmation
}

async function queueForManualReview(refundRequestId: string, refundData: any): Promise<void> {
  // Implementation for queuing refund for manual review
}

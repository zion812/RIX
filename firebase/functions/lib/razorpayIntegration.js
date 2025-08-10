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
exports.getAvailablePaymentMethods = exports.createRefund = exports.processGooglePayPayment = exports.createUPIPaymentLink = exports.razorpayWebhook = void 0;
const functions = __importStar(require("firebase-functions"));
const admin = __importStar(require("firebase-admin"));
const razorpay_1 = __importDefault(require("razorpay"));
const crypto = __importStar(require("crypto"));
const db = admin.firestore();
// Initialize Razorpay with demo credentials
const razorpay = new razorpay_1.default({
    key_id: functions.config().razorpay.key_id || 'rzp_test_demo_key',
    key_secret: functions.config().razorpay.key_secret || 'demo_secret_key',
});
const REGION = 'asia-south1';
/**
 * Razorpay webhook handler for payment events
 */
exports.razorpayWebhook = functions
    .region(REGION)
    .https
    .onRequest(async (req, res) => {
    try {
        const signature = req.headers['x-razorpay-signature'];
        const body = JSON.stringify(req.body);
        // Verify webhook signature
        const isValidSignature = verifyWebhookSignature(body, signature);
        if (!isValidSignature) {
            console.error('Invalid webhook signature');
            res.status(400).send('Invalid signature');
            return;
        }
        const event = req.body.event;
        const payload = req.body.payload;
        console.log(`Processing Razorpay webhook: ${event}`);
        switch (event) {
            case 'payment.captured':
                await handlePaymentCaptured(payload.payment.entity);
                break;
            case 'payment.failed':
                await handlePaymentFailed(payload.payment.entity);
                break;
            case 'order.paid':
                await handleOrderPaid(payload.order.entity);
                break;
            case 'refund.created':
                await handleRefundCreated(payload.refund.entity);
                break;
            case 'refund.processed':
                await handleRefundProcessed(payload.refund.entity);
                break;
            default:
                console.log(`Unhandled webhook event: ${event}`);
        }
        res.status(200).send('OK');
    }
    catch (error) {
        console.error('Error processing webhook:', error);
        res.status(500).send('Internal Server Error');
    }
});
/**
 * Create UPI payment link for coin purchase
 */
exports.createUPIPaymentLink = functions
    .region(REGION)
    .https
    .onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }
    const { orderId } = data;
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
        // Create UPI payment link
        const paymentLinkOptions = {
            amount: orderData.amount * 100, // Amount in paise
            currency: 'INR',
            accept_partial: false,
            description: `RIO Coins Purchase - ${orderData.totalCoins} coins`,
            customer: {
                name: orderData.customerName || 'RIO User',
                email: orderData.customerEmail,
                contact: orderData.customerPhone
            },
            notify: {
                sms: true,
                email: false
            },
            reminder_enable: true,
            options: {
                checkout: {
                    method: {
                        upi: true,
                        card: false,
                        netbanking: false,
                        wallet: false
                    }
                }
            },
            callback_url: `https://rio-app.com/payment/callback?orderId=${orderId}`,
            callback_method: 'get'
        };
        const paymentLink = await razorpay.paymentLink.create(paymentLinkOptions);
        // Update order with payment link
        await db.collection('coin_orders').doc(orderId).update({
            paymentLinkId: paymentLink.id,
            paymentLinkUrl: paymentLink.short_url,
            paymentMethod: 'UPI',
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
        return {
            paymentLinkId: paymentLink.id,
            paymentUrl: paymentLink.short_url,
            qrCode: paymentLink.short_url // Can be used to generate QR code
        };
    }
    catch (error) {
        console.error('Error creating UPI payment link:', error);
        throw new functions.https.HttpsError('internal', 'Failed to create payment link');
    }
});
/**
 * Process Google Pay payment
 */
exports.processGooglePayPayment = functions
    .region(REGION)
    .https
    .onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }
    const { orderId } = data;
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
        // Process Google Pay payment through Razorpay
        // Note: Razorpay payments.create is not available in the SDK
        // This would typically be handled by the client-side SDK
        const payment = {
            id: `pay_demo_${Date.now()}`,
            status: 'created',
            amount: orderData.amount * 100,
            currency: 'INR',
            order_id: orderId
        };
        // Update order with payment details
        await db.collection('coin_orders').doc(orderId).update({
            paymentId: payment.id,
            paymentMethod: 'GOOGLE_PAY',
            paymentStatus: payment.status,
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
        return {
            paymentId: payment.id,
            status: payment.status,
            method: 'GOOGLE_PAY'
        };
    }
    catch (error) {
        console.error('Error processing Google Pay payment:', error);
        throw new functions.https.HttpsError('internal', 'Failed to process Google Pay payment');
    }
});
/**
 * Create refund for coin purchase
 */
exports.createRefund = functions
    .region(REGION)
    .https
    .onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }
    const { orderId, reason, amount } = data;
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
        // Check if refund is allowed (within 24 hours)
        const orderTime = orderData.createdAt.toDate();
        const now = new Date();
        const hoursDiff = (now.getTime() - orderTime.getTime()) / (1000 * 60 * 60);
        if (hoursDiff > 24) {
            throw new functions.https.HttpsError('failed-precondition', 'Refund window expired');
        }
        // Create refund
        const refundAmount = amount || orderData.amount * 100; // Full refund if amount not specified
        const refundOptions = {
            payment_id: orderData.paymentId,
            amount: refundAmount,
            notes: {
                reason: reason,
                orderId: orderId,
                userId: userId
            }
        };
        const refund = await razorpay.payments.refund(orderData.paymentId, refundOptions);
        // Deduct coins from user account
        await deductCoinsFromUser(userId, orderData.totalCoins, orderId);
        // Update order status
        await db.collection('coin_orders').doc(orderId).update({
            refundId: refund.id,
            refundAmount: refundAmount / 100,
            refundReason: reason,
            status: 'REFUNDED',
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
        // Create refund record
        await db.collection('refunds').add({
            refundId: refund.id,
            orderId: orderId,
            userId: userId,
            amount: refundAmount / 100,
            reason: reason,
            status: refund.status,
            createdAt: admin.firestore.FieldValue.serverTimestamp()
        });
        return {
            refundId: refund.id,
            amount: refundAmount / 100,
            status: refund.status
        };
    }
    catch (error) {
        console.error('Error creating refund:', error);
        throw new functions.https.HttpsError('internal', 'Failed to create refund');
    }
});
/**
 * Get payment methods available for user
 */
exports.getAvailablePaymentMethods = functions
    .region(REGION)
    .https
    .onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }
    const userId = context.auth.uid;
    try {
        // Get user data to determine available methods
        const userDoc = await db.collection('users').doc(userId).get();
        const userData = userDoc.data() || {};
        const userTier = userData.userTier || 'GENERAL';
        // Base payment methods available to all users
        const paymentMethods = [
            {
                id: 'upi',
                name: 'UPI',
                description: 'Pay using any UPI app',
                icon: 'upi_icon',
                enabled: true,
                processingFee: 0
            },
            {
                id: 'google_pay',
                name: 'Google Pay',
                description: 'Quick payment with Google Pay',
                icon: 'gpay_icon',
                enabled: true,
                processingFee: 0
            },
            {
                id: 'phonepe',
                name: 'PhonePe',
                description: 'Pay with PhonePe wallet',
                icon: 'phonepe_icon',
                enabled: true,
                processingFee: 0
            },
            {
                id: 'paytm',
                name: 'Paytm',
                description: 'Pay with Paytm wallet',
                icon: 'paytm_icon',
                enabled: true,
                processingFee: 0
            }
        ];
        // Add premium payment methods for higher tiers
        if (userTier === 'FARMER' || userTier === 'ENTHUSIAST') {
            paymentMethods.push({
                id: 'netbanking',
                name: 'Net Banking',
                description: 'Pay directly from your bank account',
                icon: 'netbanking_icon',
                enabled: true,
                processingFee: 0
            }, {
                id: 'card',
                name: 'Debit/Credit Card',
                description: 'Pay with your debit or credit card',
                icon: 'card_icon',
                enabled: true,
                processingFee: 2 // 2% processing fee for cards
            });
        }
        return {
            paymentMethods: paymentMethods,
            userTier: userTier,
            preferredMethod: userData.preferredPaymentMethod || 'upi'
        };
    }
    catch (error) {
        console.error('Error getting payment methods:', error);
        throw new functions.https.HttpsError('internal', 'Failed to get payment methods');
    }
});
/**
 * Webhook event handlers
 */
async function handlePaymentCaptured(payment) {
    console.log('Payment captured:', payment.id);
    // Find the order
    const ordersSnapshot = await db.collection('coin_orders')
        .where('orderId', '==', payment.order_id)
        .limit(1)
        .get();
    if (ordersSnapshot.empty) {
        console.error('Order not found for payment:', payment.id);
        return;
    }
    const orderDoc = ordersSnapshot.docs[0];
    const orderData = orderDoc.data();
    // Credit coins to user
    await creditCoinsToUser(orderData.userId, orderData);
    // Update order status
    await orderDoc.ref.update({
        paymentId: payment.id,
        status: 'COMPLETED',
        paymentCapturedAt: admin.firestore.FieldValue.serverTimestamp()
    });
}
async function handlePaymentFailed(payment) {
    console.log('Payment failed:', payment.id);
    // Update order status
    const ordersSnapshot = await db.collection('coin_orders')
        .where('orderId', '==', payment.order_id)
        .limit(1)
        .get();
    if (!ordersSnapshot.empty) {
        await ordersSnapshot.docs[0].ref.update({
            paymentId: payment.id,
            status: 'FAILED',
            failureReason: payment.error_description,
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
    }
}
async function handleOrderPaid(order) {
    console.log('Order paid:', order.id);
    // Additional order processing logic
}
async function handleRefundCreated(refund) {
    console.log('Refund created:', refund.id);
    // Update refund record
    await db.collection('refunds')
        .where('refundId', '==', refund.id)
        .limit(1)
        .get()
        .then(snapshot => {
        if (!snapshot.empty) {
            snapshot.docs[0].ref.update({
                status: 'CREATED',
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });
        }
    });
}
async function handleRefundProcessed(refund) {
    console.log('Refund processed:', refund.id);
    // Update refund record
    await db.collection('refunds')
        .where('refundId', '==', refund.id)
        .limit(1)
        .get()
        .then(snapshot => {
        if (!snapshot.empty) {
            snapshot.docs[0].ref.update({
                status: 'PROCESSED',
                processedAt: admin.firestore.FieldValue.serverTimestamp()
            });
        }
    });
}
/**
 * Helper functions
 */
function verifyWebhookSignature(body, signature) {
    const expectedSignature = crypto
        .createHmac('sha256', functions.config().razorpay.webhook_secret || 'demo_webhook_secret')
        .update(body)
        .digest('hex');
    return `sha256=${expectedSignature}` === signature;
}
async function creditCoinsToUser(userId, orderData) {
    const transactionId = `webhook_credit_${userId}_${Date.now()}`;
    await db.runTransaction(async (transaction) => {
        var _a;
        const userRef = db.collection('users').doc(userId);
        const userDoc = await transaction.get(userRef);
        const currentBalance = ((_a = userDoc.data()) === null || _a === void 0 ? void 0 : _a.coinBalance) || 0;
        const newBalance = currentBalance + orderData.totalCoins;
        // Update user balance
        transaction.update(userRef, {
            coinBalance: newBalance,
            lastCoinPurchase: admin.firestore.FieldValue.serverTimestamp()
        });
        // Record transaction
        transaction.set(db.collection('coin_transactions').doc(transactionId), {
            id: transactionId,
            userId: userId,
            type: 'CREDIT',
            amount: orderData.totalCoins,
            purpose: 'COIN_PURCHASE_WEBHOOK',
            orderId: orderData.orderId,
            balanceBefore: currentBalance,
            balanceAfter: newBalance,
            status: 'COMPLETED',
            createdAt: admin.firestore.FieldValue.serverTimestamp()
        });
    });
}
async function deductCoinsFromUser(userId, coinAmount, orderId) {
    const transactionId = `refund_deduct_${userId}_${Date.now()}`;
    await db.runTransaction(async (transaction) => {
        var _a;
        const userRef = db.collection('users').doc(userId);
        const userDoc = await transaction.get(userRef);
        const currentBalance = ((_a = userDoc.data()) === null || _a === void 0 ? void 0 : _a.coinBalance) || 0;
        const newBalance = Math.max(0, currentBalance - coinAmount);
        // Update user balance
        transaction.update(userRef, {
            coinBalance: newBalance,
            lastRefund: admin.firestore.FieldValue.serverTimestamp()
        });
        // Record transaction
        transaction.set(db.collection('coin_transactions').doc(transactionId), {
            id: transactionId,
            userId: userId,
            type: 'DEDUCT',
            amount: coinAmount,
            purpose: 'REFUND',
            orderId: orderId,
            balanceBefore: currentBalance,
            balanceAfter: newBalance,
            status: 'COMPLETED',
            createdAt: admin.firestore.FieldValue.serverTimestamp()
        });
    });
}
//# sourceMappingURL=razorpayIntegration.js.map
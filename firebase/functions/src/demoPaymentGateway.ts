import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import * as crypto from 'crypto';

const db = admin.firestore();
const REGION = 'asia-south1';

/**
 * Demo Payment Gateway - Fully Functional Simulation
 * Simulates real payment processing with realistic responses and delays
 */

/**
 * Create demo payment order
 */
export const createDemoPaymentOrder = functions
  .region(REGION)
  .https
  .onCall(async (data, context) => {
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { amount, currency, packageId, paymentMethod } = data;
    const userId = context.auth.uid;

    try {
      // Validate input
      if (!amount || amount <= 0) {
        throw new functions.https.HttpsError('invalid-argument', 'Invalid amount');
      }

      if (amount > 500000) { // ₹5000 max for demo
        throw new functions.https.HttpsError('invalid-argument', 'Amount exceeds demo limit of ₹5000');
      }

      // Generate demo order ID
      const orderId = `demo_order_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
      
      // Create order in database
      const orderData = {
        id: orderId,
        userId: userId,
        amount: amount,
        currency: currency || 'INR',
        packageId: packageId,
        paymentMethod: paymentMethod,
        status: 'CREATED',
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        expiresAt: new Date(Date.now() + 15 * 60 * 1000), // 15 minutes
        gateway: 'DEMO',
        notes: {
          demo: true,
          environment: 'testing'
        }
      };

      await db.collection('demo_orders').doc(orderId).set(orderData);

      // Log order creation
      await logDemoTransaction({
        type: 'ORDER_CREATED',
        orderId: orderId,
        userId: userId,
        amount: amount,
        paymentMethod: paymentMethod
      });

      return {
        orderId: orderId,
        amount: amount,
        currency: currency || 'INR',
        status: 'CREATED',
        paymentUrl: `https://demo-payment.rio-app.com/pay/${orderId}`,
        qrCode: generateDemoQRCode(orderId, amount),
        expiresAt: orderData.expiresAt
      };

    } catch (error) {
      console.error('Error creating demo order:', error);
      
      if (error instanceof functions.https.HttpsError) {
        throw error;
      }
      
      throw new functions.https.HttpsError('internal', 'Failed to create demo order');
    }
  });

/**
 * Process demo payment
 */
export const processDemoPayment = functions
  .region(REGION)
  .https
  .onCall(async (data, context) => {
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { orderId, paymentMethod, simulateFailure, customDelay } = data;
    const userId = context.auth.uid;

    try {
      // Get order details
      const orderDoc = await db.collection('demo_orders').doc(orderId).get();
      if (!orderDoc.exists) {
        throw new functions.https.HttpsError('not-found', 'Order not found');
      }

      const orderData = orderDoc.data()!;
      
      if (orderData.userId !== userId) {
        throw new functions.https.HttpsError('permission-denied', 'Order does not belong to user');
      }

      if (orderData.status !== 'CREATED') {
        throw new functions.https.HttpsError('failed-precondition', 'Order already processed');
      }

      // Check if order expired
      if (orderData.expiresAt.toDate() < new Date()) {
        await orderDoc.ref.update({
          status: 'EXPIRED',
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
        throw new functions.https.HttpsError('failed-precondition', 'Order expired');
      }

      // Simulate processing delay (realistic for different payment methods)
      const delay = customDelay || getRealisticDelay(paymentMethod);
      await new Promise(resolve => setTimeout(resolve, delay));

      // Simulate payment success/failure based on various factors
      const shouldFail = simulateFailure || shouldSimulateFailure(orderData, paymentMethod);
      
      if (shouldFail) {
        return await processDemoFailure(orderId, orderData, paymentMethod);
      } else {
        return await processDemoSuccess(orderId, orderData, paymentMethod);
      }

    } catch (error) {
      console.error('Error processing demo payment:', error);
      
      if (error instanceof functions.https.HttpsError) {
        throw error;
      }
      
      throw new functions.https.HttpsError('internal', 'Payment processing failed');
    }
  });

/**
 * Demo UPI payment processing
 */
export const processDemoUPIPayment = functions
  .region(REGION)
  .https
  .onCall(async (data, context) => {
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { orderId, upiId, pin } = data;
    const userId = context.auth.uid;

    try {
      // Validate UPI ID format
      if (!isValidUPIId(upiId)) {
        throw new functions.https.HttpsError('invalid-argument', 'Invalid UPI ID format');
      }

      // Simulate UPI PIN validation
      if (!pin || pin.length !== 4) {
        throw new functions.https.HttpsError('invalid-argument', 'Invalid UPI PIN');
      }

      // Get order
      const orderDoc = await db.collection('demo_orders').doc(orderId).get();
      if (!orderDoc.exists) {
        throw new functions.https.HttpsError('not-found', 'Order not found');
      }

      const orderData = orderDoc.data()!;

      // Simulate UPI processing (2-5 seconds)
      await new Promise(resolve => setTimeout(resolve, 2000 + Math.random() * 3000));

      // Simulate UPI-specific failure scenarios
      const upiFailureRate = 0.05; // 5% failure rate for UPI
      if (Math.random() < upiFailureRate) {
        const failureReasons = [
          'Insufficient balance',
          'UPI PIN incorrect',
          'Bank server unavailable',
          'Transaction limit exceeded'
        ];
        
        const failureReason = failureReasons[Math.floor(Math.random() * failureReasons.length)];
        
        await updateDemoOrderStatus(orderId, 'FAILED', {
          paymentMethod: 'UPI',
          upiId: upiId,
          failureReason: failureReason,
          errorCode: 'UPI_' + Math.floor(Math.random() * 1000)
        });

        return {
          success: false,
          status: 'FAILED',
          errorCode: 'UPI_FAILED',
          message: failureReason
        };
      }

      // Success case
      const paymentId = `demo_upi_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
      
      await updateDemoOrderStatus(orderId, 'SUCCESS', {
        paymentId: paymentId,
        paymentMethod: 'UPI',
        upiId: upiId,
        bankReferenceNumber: `UPI${Date.now()}`,
        processedAt: admin.firestore.FieldValue.serverTimestamp()
      });

      // Trigger webhook simulation
      await triggerDemoWebhook(orderId, 'payment.captured', {
        paymentId: paymentId,
        amount: orderData.amount,
        method: 'upi'
      });

      return {
        success: true,
        paymentId: paymentId,
        status: 'SUCCESS',
        bankReferenceNumber: `UPI${Date.now()}`,
        message: 'Payment successful'
      };

    } catch (error) {
      console.error('Error processing UPI payment:', error);
      
      if (error instanceof functions.https.HttpsError) {
        throw error;
      }
      
      throw new functions.https.HttpsError('internal', 'UPI payment failed');
    }
  });

/**
 * Demo Google Pay payment processing
 */
export const processDemoGooglePay = functions
  .region(REGION)
  .https
  .onCall(async (data, context) => {
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { orderId, paymentToken } = data;
    const userId = context.auth.uid;

    try {
      // Simulate Google Pay token validation
      if (!paymentToken || paymentToken.length < 10) {
        throw new functions.https.HttpsError('invalid-argument', 'Invalid payment token');
      }

      const orderDoc = await db.collection('demo_orders').doc(orderId).get();
      if (!orderDoc.exists) {
        throw new functions.https.HttpsError('not-found', 'Order not found');
      }

      const orderData = orderDoc.data()!;

      // Simulate Google Pay processing (1-3 seconds)
      await new Promise(resolve => setTimeout(resolve, 1000 + Math.random() * 2000));

      // Google Pay has higher success rate (98%)
      const gpayFailureRate = 0.02;
      if (Math.random() < gpayFailureRate) {
        await updateDemoOrderStatus(orderId, 'FAILED', {
          paymentMethod: 'GOOGLE_PAY',
          failureReason: 'Google Pay authentication failed',
          errorCode: 'GPAY_AUTH_FAILED'
        });

        return {
          success: false,
          status: 'FAILED',
          errorCode: 'GPAY_AUTH_FAILED',
          message: 'Google Pay authentication failed'
        };
      }

      // Success case
      const paymentId = `demo_gpay_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
      
      await updateDemoOrderStatus(orderId, 'SUCCESS', {
        paymentId: paymentId,
        paymentMethod: 'GOOGLE_PAY',
        paymentToken: paymentToken.substr(0, 10) + '***', // Mask token
        processedAt: admin.firestore.FieldValue.serverTimestamp()
      });

      // Trigger webhook
      await triggerDemoWebhook(orderId, 'payment.captured', {
        paymentId: paymentId,
        amount: orderData.amount,
        method: 'googlepay'
      });

      return {
        success: true,
        paymentId: paymentId,
        status: 'SUCCESS',
        message: 'Google Pay payment successful'
      };

    } catch (error) {
      console.error('Error processing Google Pay:', error);
      
      if (error instanceof functions.https.HttpsError) {
        throw error;
      }
      
      throw new functions.https.HttpsError('internal', 'Google Pay payment failed');
    }
  });

/**
 * Demo webhook endpoint for testing
 */
export const demoWebhookEndpoint = functions
  .region(REGION)
  .https
  .onRequest(async (req, res) => {
    try {
      const signature = req.headers['x-demo-signature'] as string;
      const body = JSON.stringify(req.body);
      
      // Verify webhook signature
      const expectedSignature = crypto
        .createHmac('sha256', 'demo_webhook_secret')
        .update(body)
        .digest('hex');
      
      if (`sha256=${expectedSignature}` !== signature) {
        res.status(400).send('Invalid signature');
        return;
      }

      const { event, orderId, paymentData } = req.body;

      console.log(`Demo webhook received: ${event} for order ${orderId}`);

      // Process webhook event
      switch (event) {
        case 'payment.captured':
          await handleDemoPaymentCaptured(orderId, paymentData);
          break;
        case 'payment.failed':
          await handleDemoPaymentFailed(orderId, paymentData);
          break;
        default:
          console.log(`Unhandled demo webhook event: ${event}`);
      }

      res.status(200).send('OK');

    } catch (error) {
      console.error('Error processing demo webhook:', error);
      res.status(500).send('Internal Server Error');
    }
  });

/**
 * Helper functions
 */
async function processDemoSuccess(orderId: string, orderData: any, paymentMethod: string): Promise<any> {
  const paymentId = `demo_pay_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  await updateDemoOrderStatus(orderId, 'SUCCESS', {
    paymentId: paymentId,
    paymentMethod: paymentMethod,
    processedAt: admin.firestore.FieldValue.serverTimestamp()
  });

  // Trigger webhook simulation
  await triggerDemoWebhook(orderId, 'payment.captured', {
    paymentId: paymentId,
    amount: orderData.amount,
    method: paymentMethod.toLowerCase()
  });

  return {
    success: true,
    paymentId: paymentId,
    status: 'SUCCESS',
    message: 'Payment processed successfully'
  };
}

async function processDemoFailure(orderId: string, orderData: any, paymentMethod: string): Promise<any> {
  const failureReasons = [
    'Insufficient funds',
    'Card declined',
    'Network timeout',
    'Bank server error',
    'Invalid card details'
  ];
  
  const failureReason = failureReasons[Math.floor(Math.random() * failureReasons.length)];
  const errorCode = `ERR_${Math.floor(Math.random() * 1000)}`;
  
  await updateDemoOrderStatus(orderId, 'FAILED', {
    paymentMethod: paymentMethod,
    failureReason: failureReason,
    errorCode: errorCode
  });

  // Trigger failure webhook
  await triggerDemoWebhook(orderId, 'payment.failed', {
    errorCode: errorCode,
    errorDescription: failureReason,
    amount: orderData.amount
  });

  return {
    success: false,
    status: 'FAILED',
    errorCode: errorCode,
    message: failureReason
  };
}

function shouldSimulateFailure(orderData: any, paymentMethod: string): boolean {
  // Simulate realistic failure rates
  const failureRates: { [key: string]: number } = {
    'UPI': 0.05,        // 5% failure rate
    'GOOGLE_PAY': 0.02, // 2% failure rate
    'CARD': 0.08,       // 8% failure rate
    'NETBANKING': 0.06, // 6% failure rate
    'WALLET': 0.04      // 4% failure rate
  };

  const failureRate = failureRates[paymentMethod] || 0.05;
  
  // Higher failure rate for large amounts
  const adjustedRate = orderData.amount > 100000 ? failureRate * 1.5 : failureRate;
  
  return Math.random() < adjustedRate;
}

function getRealisticDelay(paymentMethod: string): number {
  // Realistic processing delays in milliseconds
  const delays: { [key: string]: [number, number] } = {
    'UPI': [2000, 5000],        // 2-5 seconds
    'GOOGLE_PAY': [1000, 3000], // 1-3 seconds
    'CARD': [3000, 8000],       // 3-8 seconds
    'NETBANKING': [5000, 12000], // 5-12 seconds
    'WALLET': [1500, 4000]      // 1.5-4 seconds
  };

  const [min, max] = delays[paymentMethod] || [2000, 5000];
  return min + Math.random() * (max - min);
}

function isValidUPIId(upiId: string): boolean {
  // Basic UPI ID validation
  const upiRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$/;
  return upiRegex.test(upiId);
}

function generateDemoQRCode(orderId: string, amount: number): string {
  // Generate demo QR code data
  const qrData = {
    orderId: orderId,
    amount: amount,
    currency: 'INR',
    gateway: 'DEMO'
  };
  
  return `data:image/png;base64,${Buffer.from(JSON.stringify(qrData)).toString('base64')}`;
}

async function updateDemoOrderStatus(orderId: string, status: string, additionalData: any): Promise<void> {
  await db.collection('demo_orders').doc(orderId).update({
    status: status,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    ...additionalData
  });
}

async function triggerDemoWebhook(orderId: string, event: string, paymentData: any): Promise<void> {
  // Simulate webhook with delay
  setTimeout(async () => {
    try {
      const webhookData = {
        event: event,
        orderId: orderId,
        paymentData: paymentData,
        timestamp: Date.now()
      };

      // Store webhook event for testing
      await db.collection('demo_webhooks').add({
        ...webhookData,
        createdAt: admin.firestore.FieldValue.serverTimestamp()
      });

      console.log(`Demo webhook triggered: ${event} for order ${orderId}`);
    } catch (error) {
      console.error('Error triggering demo webhook:', error);
    }
  }, 1000); // 1 second delay
}

async function handleDemoPaymentCaptured(orderId: string, paymentData: any): Promise<void> {
  // Process successful payment
  console.log(`Demo payment captured for order ${orderId}`);
  
  // Here you would typically:
  // 1. Credit coins to user account
  // 2. Send confirmation notification
  // 3. Update order status
}

async function handleDemoPaymentFailed(orderId: string, paymentData: any): Promise<void> {
  // Process failed payment
  console.log(`Demo payment failed for order ${orderId}`);
  
  // Here you would typically:
  // 1. Send failure notification
  // 2. Log failure reason
  // 3. Suggest retry or alternative payment method
}

async function logDemoTransaction(data: any): Promise<void> {
  await db.collection('demo_transaction_logs').add({
    ...data,
    timestamp: admin.firestore.FieldValue.serverTimestamp()
  });
}

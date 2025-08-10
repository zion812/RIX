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
exports.healthCheck = void 0;
const functions = __importStar(require("firebase-functions"));
const admin = __importStar(require("firebase-admin"));
const db = admin.firestore();
const REGION = 'asia-south1';
/**
 * Health check function for monitoring system status
 */
exports.healthCheck = functions
    .region(REGION)
    .https
    .onRequest(async (req, res) => {
    try {
        const startTime = Date.now();
        // Check Firebase Admin SDK
        const adminStatus = await checkFirebaseAdmin();
        // Check Firestore connectivity
        const firestoreStatus = await checkFirestore();
        // Check payment system status
        const paymentStatus = await checkPaymentSystem();
        // Check demo gateway status
        const demoStatus = await checkDemoGateway();
        const endTime = Date.now();
        const responseTime = endTime - startTime;
        const healthStatus = {
            status: 'healthy',
            timestamp: new Date().toISOString(),
            responseTime: `${responseTime}ms`,
            version: '2.0.0',
            region: REGION,
            services: {
                firebaseAdmin: adminStatus,
                firestore: firestoreStatus,
                paymentSystem: paymentStatus,
                demoGateway: demoStatus
            },
            environment: process.env.NODE_ENV || 'development'
        };
        // Determine overall health
        const allHealthy = Object.values(healthStatus.services).every(service => service.status === 'healthy');
        if (!allHealthy) {
            healthStatus.status = 'degraded';
        }
        res.status(allHealthy ? 200 : 503).json(healthStatus);
    }
    catch (error) {
        console.error('Health check failed:', error);
        res.status(500).json({
            status: 'unhealthy',
            timestamp: new Date().toISOString(),
            error: error instanceof Error ? error.message : 'Unknown error',
            version: '2.0.0',
            region: REGION
        });
    }
});
/**
 * Check Firebase Admin SDK status
 */
async function checkFirebaseAdmin() {
    try {
        const app = admin.app();
        return {
            status: 'healthy',
            projectId: app.options.projectId,
            message: 'Firebase Admin SDK initialized'
        };
    }
    catch (error) {
        return {
            status: 'unhealthy',
            error: error instanceof Error ? error.message : 'Unknown error',
            message: 'Firebase Admin SDK not initialized'
        };
    }
}
/**
 * Check Firestore connectivity
 */
async function checkFirestore() {
    try {
        const startTime = Date.now();
        // Try to read from a system collection
        await db.collection('system_health').doc('test').get();
        const endTime = Date.now();
        const responseTime = endTime - startTime;
        return {
            status: 'healthy',
            responseTime: `${responseTime}ms`,
            message: 'Firestore connection successful'
        };
    }
    catch (error) {
        return {
            status: 'unhealthy',
            error: error instanceof Error ? error.message : 'Unknown error',
            message: 'Firestore connection failed'
        };
    }
}
/**
 * Check payment system status
 */
async function checkPaymentSystem() {
    try {
        // Check if payment collections exist and are accessible
        const collections = ['coin_transactions', 'coin_orders', 'user_coin_balances'];
        const checks = [];
        for (const collection of collections) {
            try {
                const snapshot = await db.collection(collection).limit(1).get();
                checks.push({
                    collection,
                    status: 'healthy',
                    documentCount: snapshot.size
                });
            }
            catch (error) {
                checks.push({
                    collection,
                    status: 'unhealthy',
                    error: error instanceof Error ? error.message : 'Unknown error'
                });
            }
        }
        const allHealthy = checks.every(check => check.status === 'healthy');
        return {
            status: allHealthy ? 'healthy' : 'degraded',
            collections: checks,
            message: allHealthy ? 'Payment system operational' : 'Payment system has issues'
        };
    }
    catch (error) {
        return {
            status: 'unhealthy',
            error: error instanceof Error ? error.message : 'Unknown error',
            message: 'Payment system check failed'
        };
    }
}
/**
 * Check demo gateway status
 */
async function checkDemoGateway() {
    try {
        // Check demo collections
        const demoCollections = ['demo_orders', 'demo_webhooks', 'demo_transaction_logs'];
        const checks = [];
        for (const collection of demoCollections) {
            try {
                const snapshot = await db.collection(collection).limit(1).get();
                checks.push({
                    collection,
                    status: 'healthy',
                    documentCount: snapshot.size
                });
            }
            catch (error) {
                checks.push({
                    collection,
                    status: 'unhealthy',
                    error: error instanceof Error ? error.message : 'Unknown error'
                });
            }
        }
        const allHealthy = checks.every(check => check.status === 'healthy');
        return {
            status: allHealthy ? 'healthy' : 'degraded',
            collections: checks,
            message: allHealthy ? 'Demo gateway operational' : 'Demo gateway has issues'
        };
    }
    catch (error) {
        return {
            status: 'unhealthy',
            error: error instanceof Error ? error.message : 'Unknown error',
            message: 'Demo gateway check failed'
        };
    }
}
//# sourceMappingURL=healthCheck.js.map
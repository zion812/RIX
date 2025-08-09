/**
 * Firebase Cloud Functions for RIO Rooster Community Platform
 * Handles user management, custom claims, and verification workflows
 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

const db = admin.firestore();
const auth = admin.auth();

// Default custom claims for new users
const getDefaultClaims = (tier = 'general') => {
  const baseClaims = {
    tier,
    permissions: {
      canCreateListings: false,
      canEditListings: false,
      canDeleteListings: false,
      canAccessMarketplace: true,
      canManageBreedingRecords: false,
      canAccessAnalytics: false,
      canAccessPremiumFeatures: false,
      canVerifyTransfers: false,
      canAccessPrioritySupport: false,
      canModerateContent: false
    },
    verificationStatus: {
      level: 'basic',
      emailVerified: false,
      phoneVerified: false,
      identityVerified: false,
      farmDocumentsVerified: false,
      referencesVerified: false
    },
    profile: {
      region: 'other',
      district: '',
      language: 'en',
      farmType: 'hobby',
      experienceLevel: 'beginner',
      specializations: []
    },
    limits: {
      maxListings: 0,
      maxPhotosPerListing: 0,
      maxBreedingRecords: 0,
      dailyMessageLimit: 10
    },
    metadata: {
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      lastUpdated: admin.firestore.FieldValue.serverTimestamp(),
      tierUpgradeHistory: []
    }
  };

  // Adjust permissions based on tier
  if (tier === 'farmer') {
    baseClaims.permissions = {
      ...baseClaims.permissions,
      canCreateListings: true,
      canEditListings: true,
      canDeleteListings: true,
      canManageBreedingRecords: true,
      canAccessAnalytics: true
    };
    baseClaims.verificationStatus.level = 'enhanced';
    baseClaims.limits = {
      maxListings: 50,
      maxPhotosPerListing: 10,
      maxBreedingRecords: 100,
      dailyMessageLimit: 50
    };
  } else if (tier === 'enthusiast') {
    baseClaims.permissions = {
      ...baseClaims.permissions,
      canCreateListings: true,
      canEditListings: true,
      canDeleteListings: true,
      canManageBreedingRecords: true,
      canAccessAnalytics: true,
      canAccessPremiumFeatures: true,
      canVerifyTransfers: true,
      canAccessPrioritySupport: true
    };
    baseClaims.verificationStatus.level = 'premium';
    baseClaims.limits = {
      maxListings: 200,
      maxPhotosPerListing: 20,
      maxBreedingRecords: 500,
      dailyMessageLimit: 200
    };
  }

  return baseClaims;
};

// Trigger when a new user is created
exports.onUserCreate = functions.auth.user().onCreate(async (user) => {
  try {
    console.log('Creating user profile for:', user.uid);

    // Set default custom claims
    const defaultClaims = getDefaultClaims('general');
    await auth.setCustomUserClaims(user.uid, defaultClaims);

    // Create user document in Firestore
    await db.collection('users').doc(user.uid).set({
      ...defaultClaims,
      email: user.email,
      phoneNumber: user.phoneNumber,
      displayName: user.displayName || '',
      photoURL: user.photoURL || '',
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });

    // Initialize user counters
    await db.collection('counters').doc(`user_${user.uid}_listings`).set({
      count: 0,
      lastReset: admin.firestore.FieldValue.serverTimestamp()
    });

    await db.collection('counters').doc(`user_${user.uid}_messages`).set({
      count: 0,
      lastReset: admin.firestore.FieldValue.serverTimestamp()
    });

    console.log('User profile created successfully for:', user.uid);
  } catch (error) {
    console.error('Error creating user profile:', error);
    throw new functions.https.HttpsError('internal', 'Failed to create user profile');
  }
});

// HTTP function to request tier upgrade
exports.requestTierUpgrade = functions.https.onCall(async (data, context) => {
  // Verify user is authenticated
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  const { requestedTier, documents } = data;
  const userId = context.auth.uid;

  // Validate requested tier
  if (!['farmer', 'enthusiast'].includes(requestedTier)) {
    throw new functions.https.HttpsError('invalid-argument', 'Invalid tier requested');
  }

  try {
    // Get current user data
    const userDoc = await db.collection('users').doc(userId).get();
    if (!userDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'User profile not found');
    }

    const userData = userDoc.data();
    const currentTier = userData.tier;

    // Check if upgrade is valid
    if (currentTier === requestedTier) {
      throw new functions.https.HttpsError('already-exists', 'User already has requested tier');
    }

    if (currentTier === 'enthusiast') {
      throw new functions.https.HttpsError('invalid-argument', 'Cannot downgrade from enthusiast tier');
    }

    // Create verification request
    const verificationRequest = {
      userId,
      requestedTier,
      currentTier,
      documents: documents || [],
      status: 'pending',
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    };

    await db.collection('verificationRequests').add(verificationRequest);

    // Send notification to user
    await db.collection('notifications').add({
      userId,
      type: 'verification_request_submitted',
      title: 'Tier Upgrade Request Submitted',
      message: `Your request to upgrade to ${requestedTier} tier has been submitted for review.`,
      read: false,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });

    return { success: true, message: 'Tier upgrade request submitted successfully' };
  } catch (error) {
    console.error('Error requesting tier upgrade:', error);
    throw new functions.https.HttpsError('internal', 'Failed to submit tier upgrade request');
  }
});

// Admin function to approve/reject tier upgrade requests
exports.processVerificationRequest = functions.https.onCall(async (data, context) => {
  // Verify admin authentication (implement admin check)
  if (!context.auth || !context.auth.token.canModerateContent) {
    throw new functions.https.HttpsError('permission-denied', 'Admin access required');
  }

  const { requestId, action, reason } = data;

  if (!['approve', 'reject'].includes(action)) {
    throw new functions.https.HttpsError('invalid-argument', 'Invalid action');
  }

  try {
    const requestDoc = await db.collection('verificationRequests').doc(requestId).get();
    if (!requestDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Verification request not found');
    }

    const requestData = requestDoc.data();
    const { userId, requestedTier } = requestData;

    if (action === 'approve') {
      // Update user tier and custom claims
      const newClaims = getDefaultClaims(requestedTier);
      await auth.setCustomUserClaims(userId, newClaims);

      // Update user document
      await db.collection('users').doc(userId).update({
        ...newClaims,
        'metadata.lastUpdated': admin.firestore.FieldValue.serverTimestamp(),
        'metadata.tierUpgradeHistory': admin.firestore.FieldValue.arrayUnion({
          fromTier: requestData.currentTier,
          toTier: requestedTier,
          upgradedAt: admin.firestore.FieldValue.serverTimestamp(),
          reason: 'Admin approved verification'
        })
      });

      // Update verification request status
      await db.collection('verificationRequests').doc(requestId).update({
        status: 'approved',
        processedAt: admin.firestore.FieldValue.serverTimestamp(),
        processedBy: context.auth.uid,
        adminReason: reason
      });

      // Send approval notification
      await db.collection('notifications').add({
        userId,
        type: 'tier_upgrade_approved',
        title: 'Tier Upgrade Approved',
        message: `Congratulations! Your upgrade to ${requestedTier} tier has been approved.`,
        read: false,
        createdAt: admin.firestore.FieldValue.serverTimestamp()
      });

    } else {
      // Reject the request
      await db.collection('verificationRequests').doc(requestId).update({
        status: 'rejected',
        processedAt: admin.firestore.FieldValue.serverTimestamp(),
        processedBy: context.auth.uid,
        adminReason: reason
      });

      // Send rejection notification
      await db.collection('notifications').add({
        userId,
        type: 'tier_upgrade_rejected',
        title: 'Tier Upgrade Request Rejected',
        message: `Your upgrade request to ${requestedTier} tier has been rejected. Reason: ${reason}`,
        read: false,
        createdAt: admin.firestore.FieldValue.serverTimestamp()
      });
    }

    return { success: true, message: `Verification request ${action}d successfully` };
  } catch (error) {
    console.error('Error processing verification request:', error);
    throw new functions.https.HttpsError('internal', 'Failed to process verification request');
  }
});

// Function to handle email verification
exports.onEmailVerified = functions.auth.user().onUpdate(async (change, context) => {
  const beforeData = change.before;
  const afterData = change.after;

  // Check if email was just verified
  if (!beforeData.emailVerified && afterData.emailVerified) {
    try {
      const userId = afterData.uid;

      // Update user's verification status
      await db.collection('users').doc(userId).update({
        'verificationStatus.emailVerified': true,
        'metadata.lastUpdated': admin.firestore.FieldValue.serverTimestamp()
      });

      // Update custom claims
      const userDoc = await db.collection('users').doc(userId).get();
      if (userDoc.exists) {
        const userData = userDoc.data();
        const updatedClaims = {
          ...userData,
          verificationStatus: {
            ...userData.verificationStatus,
            emailVerified: true
          }
        };

        await auth.setCustomUserClaims(userId, updatedClaims);
      }

      // Send welcome notification
      await db.collection('notifications').add({
        userId,
        type: 'email_verified',
        title: 'Email Verified',
        message: 'Your email has been successfully verified. You can now access more features.',
        read: false,
        createdAt: admin.firestore.FieldValue.serverTimestamp()
      });

      console.log('Email verification processed for user:', userId);
    } catch (error) {
      console.error('Error processing email verification:', error);
    }
  }
});

// Function to enforce daily limits
exports.checkDailyLimits = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  const { action } = data; // 'listing' or 'message'
  const userId = context.auth.uid;

  try {
    const counterDoc = await db.collection('counters').doc(`user_${userId}_${action}s`).get();

    if (!counterDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Counter not found');
    }

    const counterData = counterDoc.data();
    const today = new Date();
    const lastReset = counterData.lastReset.toDate();

    // Reset counter if it's a new day
    if (today.toDateString() !== lastReset.toDateString()) {
      await db.collection('counters').doc(`user_${userId}_${action}s`).update({
        count: 0,
        lastReset: admin.firestore.FieldValue.serverTimestamp()
      });
      return { canProceed: true, currentCount: 0 };
    }

    // Get user's limits from custom claims
    const userClaims = context.auth.token;
    const limit = action === 'listing' ? userClaims.limits.maxListings : userClaims.limits.dailyMessageLimit;

    if (counterData.count >= limit) {
      return { canProceed: false, currentCount: counterData.count, limit };
    }

    return { canProceed: true, currentCount: counterData.count, limit };
  } catch (error) {
    console.error('Error checking daily limits:', error);
    throw new functions.https.HttpsError('internal', 'Failed to check daily limits');
  }
});

// Function to increment counters
exports.incrementCounter = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  const { action } = data;
  const userId = context.auth.uid;

  try {
    await db.collection('counters').doc(`user_${userId}_${action}s`).update({
      count: admin.firestore.FieldValue.increment(1)
    });

    return { success: true };
  } catch (error) {
    console.error('Error incrementing counter:', error);
    throw new functions.https.HttpsError('internal', 'Failed to increment counter');
  }
});
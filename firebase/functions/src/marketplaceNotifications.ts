import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import { NotificationPayload, NotificationCategory, NotificationPriority, processNotification } from './notificationService';

const db = admin.firestore();
const REGION = 'asia-south1';

/**
 * Marketplace notification triggers for RIO platform
 * Handles listing updates, price alerts, and purchase notifications
 */

/**
 * Trigger when new marketplace listing is created
 */
export const onNewMarketplaceListing = functions
  .region(REGION)
  .firestore
  .document('marketplace/{listingId}')
  .onCreate(async (snap, context) => {
    const listing = snap.data();
    const listingId = context.params.listingId;

    try {
      // Find users interested in this breed and location
      const interestedUsers = await findInterestedUsers(listing);
      
      if (interestedUsers.length > 0) {
        const notification: NotificationPayload = {
          title: `New ${listing.breed} Available!`,
          body: `${listing.title} - ₹${listing.price} in ${listing.location}`,
          imageUrl: listing.images?.[0] || '',
          category: NotificationCategory.MARKETPLACE,
          priority: NotificationPriority.NORMAL,
          targetUsers: interestedUsers,
          deepLink: `rio://marketplace/listing/${listingId}`,
          data: {
            listingId: listingId,
            breed: listing.breed,
            price: listing.price.toString(),
            location: listing.location,
            sellerId: listing.sellerId
          },
          actions: [
            {
              id: 'view_listing',
              title: 'View Details',
              deepLink: `rio://marketplace/listing/${listingId}`
            },
            {
              id: 'contact_seller',
              title: 'Contact Seller',
              deepLink: `rio://chat/user/${listing.sellerId}`
            }
          ]
        };

        await processNotification(notification, 'system');
        
        // Log notification for analytics
        await logNotificationEvent('new_listing_notification', {
          listingId: listingId,
          targetUserCount: interestedUsers.length,
          breed: listing.breed,
          price: listing.price
        });
      }

    } catch (error) {
      console.error('Error sending new listing notification:', error);
    }
  });

/**
 * Trigger when listing price is updated
 */
export const onListingPriceUpdate = functions
  .region(REGION)
  .firestore
  .document('marketplace/{listingId}')
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    const listingId = context.params.listingId;

    // Check if price changed
    if (before.price !== after.price) {
      try {
        // Find users with price alerts for this listing
        const alertUsers = await getUsersWithPriceAlerts(listingId, after.price);
        
        if (alertUsers.length > 0) {
          const priceChange = after.price < before.price ? 'decreased' : 'increased';
          const emoji = after.price < before.price ? '📉' : '📈';
          
          const notification: NotificationPayload = {
            title: `${emoji} Price Alert: ${after.breed}`,
            body: `Price ${priceChange} from ₹${before.price} to ₹${after.price}`,
            imageUrl: after.images?.[0] || '',
            category: NotificationCategory.MARKETPLACE,
            priority: NotificationPriority.HIGH,
            targetUsers: alertUsers,
            deepLink: `rio://marketplace/listing/${listingId}`,
            data: {
              listingId: listingId,
              oldPrice: before.price.toString(),
              newPrice: after.price.toString(),
              priceChange: priceChange,
              breed: after.breed
            },
            actions: [
              {
                id: 'view_listing',
                title: 'View Now',
                deepLink: `rio://marketplace/listing/${listingId}`
              },
              {
                id: 'buy_now',
                title: 'Buy Now',
                deepLink: `rio://marketplace/purchase/${listingId}`
              }
            ]
          };

          await processNotification(notification, 'system');
        }

      } catch (error) {
        console.error('Error sending price alert notification:', error);
      }
    }
  });

/**
 * Trigger when listing is about to expire
 */
export const checkExpiringListings = functions
  .region(REGION)
  .pubsub
  .schedule('0 9 * * *') // Daily at 9 AM
  .onRun(async (context) => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    
    const nextWeek = new Date();
    nextWeek.setDate(nextWeek.getDate() + 7);

    try {
      // Find listings expiring tomorrow
      const expiringTomorrowSnapshot = await db.collection('marketplace')
        .where('expiresAt', '>=', admin.firestore.Timestamp.fromDate(tomorrow))
        .where('expiresAt', '<', admin.firestore.Timestamp.fromDate(nextWeek))
        .where('status', '==', 'active')
        .get();

      for (const doc of expiringTomorrowSnapshot.docs) {
        const listing = doc.data();
        
        const notification: NotificationPayload = {
          title: '⏰ Listing Expiring Soon',
          body: `Your ${listing.breed} listing expires tomorrow. Renew to keep it active.`,
          imageUrl: listing.images?.[0] || '',
          category: NotificationCategory.MARKETPLACE,
          priority: NotificationPriority.HIGH,
          targetUsers: [listing.sellerId],
          deepLink: `rio://marketplace/manage/${doc.id}`,
          data: {
            listingId: doc.id,
            breed: listing.breed,
            expiresAt: listing.expiresAt.toDate().toISOString()
          },
          actions: [
            {
              id: 'renew_listing',
              title: 'Renew Now',
              deepLink: `rio://marketplace/renew/${doc.id}`
            },
            {
              id: 'edit_listing',
              title: 'Edit Listing',
              deepLink: `rio://marketplace/edit/${doc.id}`
            }
          ]
        };

        await processNotification(notification, 'system');
      }

      console.log(`Sent expiration warnings for ${expiringTomorrowSnapshot.size} listings`);

    } catch (error) {
      console.error('Error checking expiring listings:', error);
    }
  });

/**
 * Send purchase confirmation notification
 */
export const sendPurchaseConfirmation = functions
  .region(REGION)
  .https
  .onCall(async (data, context) => {
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { listingId, buyerId, sellerId, transactionId, coinAmount } = data;

    try {
      // Get listing details
      const listingDoc = await db.collection('marketplace').doc(listingId).get();
      if (!listingDoc.exists) {
        throw new functions.https.HttpsError('not-found', 'Listing not found');
      }

      const listing = listingDoc.data()!;

      // Notification for buyer
      const buyerNotification: NotificationPayload = {
        title: '✅ Purchase Confirmed',
        body: `You successfully purchased ${listing.breed} for ${coinAmount} coins`,
        imageUrl: listing.images?.[0] || '',
        category: NotificationCategory.MARKETPLACE,
        priority: NotificationPriority.HIGH,
        targetUsers: [buyerId],
        deepLink: `rio://transactions/${transactionId}`,
        data: {
          listingId: listingId,
          transactionId: transactionId,
          coinAmount: coinAmount.toString(),
          breed: listing.breed,
          type: 'purchase_confirmation'
        },
        actions: [
          {
            id: 'view_transaction',
            title: 'View Details',
            deepLink: `rio://transactions/${transactionId}`
          },
          {
            id: 'contact_seller',
            title: 'Contact Seller',
            deepLink: `rio://chat/user/${sellerId}`
          }
        ]
      };

      // Notification for seller
      const sellerNotification: NotificationPayload = {
        title: '💰 Sale Completed',
        body: `Your ${listing.breed} was sold for ${coinAmount} coins`,
        imageUrl: listing.images?.[0] || '',
        category: NotificationCategory.MARKETPLACE,
        priority: NotificationPriority.HIGH,
        targetUsers: [sellerId],
        deepLink: `rio://transactions/${transactionId}`,
        data: {
          listingId: listingId,
          transactionId: transactionId,
          coinAmount: coinAmount.toString(),
          breed: listing.breed,
          type: 'sale_confirmation'
        },
        actions: [
          {
            id: 'view_transaction',
            title: 'View Details',
            deepLink: `rio://transactions/${transactionId}`
          },
          {
            id: 'contact_buyer',
            title: 'Contact Buyer',
            deepLink: `rio://chat/user/${buyerId}`
          }
        ]
      };

      await Promise.all([
        processNotification(buyerNotification, 'system'),
        processNotification(sellerNotification, 'system')
      ]);

      return { success: true, message: 'Purchase confirmations sent' };

    } catch (error) {
      console.error('Error sending purchase confirmation:', error);
      throw new functions.https.HttpsError('internal', 'Failed to send confirmation');
    }
  });

/**
 * Find users interested in a specific listing based on preferences
 */
async function findInterestedUsers(listing: any): Promise<string[]> {
  const interestedUsers: string[] = [];

  try {
    // Query users with matching breed preferences
    const breedPreferenceSnapshot = await db.collection('user_preferences')
      .where('interestedBreeds', 'array-contains', listing.breed)
      .where('notificationsEnabled', '==', true)
      .where('marketplaceNotifications', '==', true)
      .get();

    for (const doc of breedPreferenceSnapshot.docs) {
      const preferences = doc.data();
      const userId = doc.id;

      // Skip the seller
      if (userId === listing.sellerId) continue;

      // Check location preference
      if (preferences.maxDistance) {
        const distance = calculateDistance(
          preferences.location,
          listing.location
        );
        
        if (distance <= preferences.maxDistance) {
          interestedUsers.push(userId);
        }
      } else {
        interestedUsers.push(userId);
      }
    }

    // Limit to prevent spam
    return interestedUsers.slice(0, 100);

  } catch (error) {
    console.error('Error finding interested users:', error);
    return [];
  }
}

/**
 * Get users with price alerts for a specific listing
 */
async function getUsersWithPriceAlerts(listingId: string, newPrice: number): Promise<string[]> {
  try {
    const alertSnapshot = await db.collection('price_alerts')
      .where('listingId', '==', listingId)
      .where('targetPrice', '>=', newPrice)
      .where('active', '==', true)
      .get();

    return alertSnapshot.docs.map(doc => doc.data().userId);

  } catch (error) {
    console.error('Error getting price alert users:', error);
    return [];
  }
}

/**
 * Calculate distance between two locations (simplified)
 */
function calculateDistance(location1: any, location2: any): number {
  // Simplified distance calculation
  // In a real implementation, use proper geolocation formulas
  if (!location1 || !location2) return 0;
  
  const lat1 = location1.latitude || 0;
  const lon1 = location1.longitude || 0;
  const lat2 = location2.latitude || 0;
  const lon2 = location2.longitude || 0;

  const R = 6371; // Earth's radius in km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon/2) * Math.sin(dLon/2);
  
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c;
}

/**
 * Log notification events for analytics
 */
async function logNotificationEvent(eventType: string, data: any): Promise<void> {
  try {
    await db.collection('notification_analytics').add({
      eventType: eventType,
      data: data,
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });
  } catch (error) {
    console.error('Error logging notification event:', error);
  }
}

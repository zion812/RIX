import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import { Storage } from '@google-cloud/storage';

// Initialize Firebase Admin
const storage = new Storage();
const bucket = storage.bucket('rio-storage-bucket');

/**
 * Storage lifecycle management for cost optimization
 * Automatically manages storage classes and cleanup for rural India cost constraints
 */

/**
 * Daily cleanup function to optimize storage costs
 * Target: <₹2 per user per month
 */
export const dailyStorageCleanup = functions
  .region('asia-south1')
  .pubsub
  .schedule('0 2 * * *') // Run at 2 AM IST daily
  .timeZone('Asia/Kolkata')
  .onRun(async (context) => {
    console.log('Starting daily storage cleanup...');
    
    try {
      const cleanupResults = await Promise.all([
        cleanupTempFiles(),
        moveToNearlineStorage(),
        moveToColdlineStorage(),
        deleteExpiredFiles(),
        optimizeUserStorageQuotas(),
        generateStorageReport()
      ]);
      
      console.log('Daily cleanup completed:', cleanupResults);
      
      // Log results to Firestore for monitoring
      await admin.firestore().collection('storage_reports').add({
        date: admin.firestore.FieldValue.serverTimestamp(),
        cleanupResults: cleanupResults,
        type: 'daily_cleanup'
      });
      
      return null;
    } catch (error) {
      console.error('Error in daily cleanup:', error);
      throw error;
    }
  });

/**
 * Weekly storage optimization for cost management
 */
export const weeklyStorageOptimization = functions
  .region('asia-south1')
  .pubsub
  .schedule('0 1 * * 0') // Run at 1 AM IST every Sunday
  .timeZone('Asia/Kolkata')
  .onRun(async (context) => {
    console.log('Starting weekly storage optimization...');
    
    try {
      const optimizationResults = await Promise.all([
        analyzeStorageUsage(),
        optimizeImageFormats(),
        consolidateUserData(),
        archiveOldTransferData(),
        generateCostReport()
      ]);
      
      console.log('Weekly optimization completed:', optimizationResults);
      
      await admin.firestore().collection('storage_reports').add({
        date: admin.firestore.FieldValue.serverTimestamp(),
        optimizationResults: optimizationResults,
        type: 'weekly_optimization'
      });
      
      return null;
    } catch (error) {
      console.error('Error in weekly optimization:', error);
      throw error;
    }
  });

/**
 * Clean up temporary files older than 24 hours
 */
async function cleanupTempFiles(): Promise<{ deleted: number; savedBytes: number }> {
  const oneDayAgo = new Date(Date.now() - 24 * 60 * 60 * 1000);
  let deletedCount = 0;
  let savedBytes = 0;
  
  const [files] = await bucket.getFiles({
    prefix: 'temp/',
    maxResults: 1000
  });
  
  for (const file of files) {
    const [metadata] = await file.getMetadata();
    const createdTime = new Date(metadata.timeCreated);
    
    if (createdTime < oneDayAgo) {
      savedBytes += parseInt(metadata.size || '0');
      await file.delete();
      deletedCount++;
    }
  }
  
  console.log(`Cleaned up ${deletedCount} temp files, saved ${savedBytes} bytes`);
  return { deleted: deletedCount, savedBytes };
}

/**
 * Move files to Nearline storage (30-90 days old)
 * Cost: ~₹0.7 per GB vs ₹1.5 per GB for Standard
 */
async function moveToNearlineStorage(): Promise<{ moved: number; savedCost: number }> {
  const thirtyDaysAgo = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
  const ninetyDaysAgo = new Date(Date.now() - 90 * 24 * 60 * 60 * 1000);
  let movedCount = 0;
  let savedCost = 0;
  
  // Get files that are 30-90 days old and not in Nearline/Coldline
  const [files] = await bucket.getFiles({
    maxResults: 1000
  });
  
  for (const file of files) {
    const [metadata] = await file.getMetadata();
    const createdTime = new Date(metadata.timeCreated);
    const storageClass = metadata.storageClass;
    
    if (createdTime < thirtyDaysAgo && 
        createdTime > ninetyDaysAgo && 
        storageClass === 'STANDARD' &&
        !file.name.includes('/transfers/') && // Keep transfer docs in Standard
        !file.name.includes('/marketplace/')) { // Keep marketplace in Standard
      
      try {
        await file.setStorageClass('NEARLINE');
        movedCount++;
        
        const sizeGB = parseInt(metadata.size || '0') / (1024 * 1024 * 1024);
        savedCost += sizeGB * (1.5 - 0.7); // Cost difference per GB per month
        
      } catch (error) {
        console.error(`Error moving file ${file.name} to Nearline:`, error);
      }
    }
  }
  
  console.log(`Moved ${movedCount} files to Nearline, estimated monthly savings: ₹${savedCost.toFixed(2)}`);
  return { moved: movedCount, savedCost };
}

/**
 * Move files to Coldline storage (90+ days old)
 * Cost: ~₹0.3 per GB vs ₹1.5 per GB for Standard
 */
async function moveToColdlineStorage(): Promise<{ moved: number; savedCost: number }> {
  const ninetyDaysAgo = new Date(Date.now() - 90 * 24 * 60 * 60 * 1000);
  let movedCount = 0;
  let savedCost = 0;
  
  const [files] = await bucket.getFiles({
    maxResults: 1000
  });
  
  for (const file of files) {
    const [metadata] = await file.getMetadata();
    const createdTime = new Date(metadata.timeCreated);
    const storageClass = metadata.storageClass;
    
    if (createdTime < ninetyDaysAgo && 
        (storageClass === 'STANDARD' || storageClass === 'NEARLINE') &&
        !file.name.includes('/transfers/')) { // Keep transfer docs accessible
      
      try {
        await file.setStorageClass('COLDLINE');
        movedCount++;
        
        const sizeGB = parseInt(metadata.size || '0') / (1024 * 1024 * 1024);
        savedCost += sizeGB * (1.5 - 0.3); // Cost difference per GB per month
        
      } catch (error) {
        console.error(`Error moving file ${file.name} to Coldline:`, error);
      }
    }
  }
  
  console.log(`Moved ${movedCount} files to Coldline, estimated monthly savings: ₹${savedCost.toFixed(2)}`);
  return { moved: movedCount, savedCost };
}

/**
 * Delete expired files based on retention policies
 */
async function deleteExpiredFiles(): Promise<{ deleted: number; savedBytes: number }> {
  const oneYearAgo = new Date(Date.now() - 365 * 24 * 60 * 60 * 1000);
  let deletedCount = 0;
  let savedBytes = 0;
  
  const [files] = await bucket.getFiles({
    maxResults: 1000
  });
  
  for (const file of files) {
    const [metadata] = await file.getMetadata();
    const createdTime = new Date(metadata.timeCreated);
    
    // Delete old files based on type and retention policy
    const shouldDelete = 
      // Delete old temp files
      (file.name.includes('/temp/') && createdTime < oneYearAgo) ||
      // Delete old low-priority media
      (file.name.includes('/general/') && createdTime < oneYearAgo) ||
      // Delete old marketplace media for sold items
      (file.name.includes('/marketplace/') && await isMarketplaceItemSold(file.name) && createdTime < oneYearAgo);
    
    if (shouldDelete && !file.name.includes('/transfers/')) { // Never auto-delete transfer docs
      try {
        savedBytes += parseInt(metadata.size || '0');
        await file.delete();
        deletedCount++;
      } catch (error) {
        console.error(`Error deleting file ${file.name}:`, error);
      }
    }
  }
  
  console.log(`Deleted ${deletedCount} expired files, saved ${savedBytes} bytes`);
  return { deleted: deletedCount, savedBytes };
}

/**
 * Optimize user storage quotas and send notifications
 */
async function optimizeUserStorageQuotas(): Promise<{ usersNotified: number; quotaViolations: number }> {
  let usersNotified = 0;
  let quotaViolations = 0;
  
  // Get all users and their storage usage
  const usersSnapshot = await admin.firestore().collection('users').get();
  
  for (const userDoc of usersSnapshot.docs) {
    const userData = userDoc.data();
    const userId = userDoc.id;
    const userTier = userData.userTier || 'GENERAL';
    
    // Calculate user's storage usage
    const storageUsage = await calculateUserStorageUsage(userId);
    
    // Get quota based on tier
    const quota = getStorageQuota(userTier);
    
    if (storageUsage.totalBytes > quota.maxBytes) {
      quotaViolations++;
      
      // Send notification to user
      await sendStorageQuotaNotification(userId, storageUsage, quota);
      usersNotified++;
      
      // If severely over quota, start cleanup
      if (storageUsage.totalBytes > quota.maxBytes * 1.2) {
        await performUserStorageCleanup(userId, storageUsage, quota);
      }
    }
    
    // Update user storage stats in Firestore
    await admin.firestore().collection('users').doc(userId).update({
      storageUsage: storageUsage,
      lastStorageCheck: admin.firestore.FieldValue.serverTimestamp()
    });
  }
  
  console.log(`Notified ${usersNotified} users about storage, ${quotaViolations} quota violations`);
  return { usersNotified, quotaViolations };
}

/**
 * Generate daily storage report
 */
async function generateStorageReport(): Promise<any> {
  const [files] = await bucket.getFiles();
  
  let totalSize = 0;
  let filesByType = {
    images: { count: 0, size: 0 },
    videos: { count: 0, size: 0 },
    documents: { count: 0, size: 0 }
  };
  
  let storageClassDistribution = {
    STANDARD: { count: 0, size: 0 },
    NEARLINE: { count: 0, size: 0 },
    COLDLINE: { count: 0, size: 0 }
  };
  
  for (const file of files) {
    const [metadata] = await file.getMetadata();
    const size = parseInt(metadata.size || '0');
    const contentType = metadata.contentType || '';
    const storageClass = metadata.storageClass || 'STANDARD';
    
    totalSize += size;
    
    // Categorize by type
    if (contentType.startsWith('image/')) {
      filesByType.images.count++;
      filesByType.images.size += size;
    } else if (contentType.startsWith('video/')) {
      filesByType.videos.count++;
      filesByType.videos.size += size;
    } else {
      filesByType.documents.count++;
      filesByType.documents.size += size;
    }
    
    // Categorize by storage class
    if (storageClassDistribution[storageClass]) {
      storageClassDistribution[storageClass].count++;
      storageClassDistribution[storageClass].size += size;
    }
  }
  
  const report = {
    totalFiles: files.length,
    totalSizeGB: totalSize / (1024 * 1024 * 1024),
    filesByType,
    storageClassDistribution,
    estimatedMonthlyCost: calculateEstimatedCost(storageClassDistribution),
    generatedAt: new Date().toISOString()
  };
  
  console.log('Storage report generated:', report);
  return report;
}

/**
 * Helper functions
 */
async function isMarketplaceItemSold(filePath: string): Promise<boolean> {
  // Extract listing ID from path and check if sold
  const pathParts = filePath.split('/');
  const listingId = pathParts[pathParts.indexOf('marketplace') + 1];
  
  if (!listingId) return false;
  
  try {
    const listingDoc = await admin.firestore().collection('marketplace_listings').doc(listingId).get();
    return listingDoc.exists && listingDoc.data()?.listingStatus === 'SOLD';
  } catch (error) {
    return false;
  }
}

async function calculateUserStorageUsage(userId: string): Promise<any> {
  const [files] = await bucket.getFiles({
    prefix: `users/${userId}/`
  });
  
  let totalBytes = 0;
  let fileCount = 0;
  
  for (const file of files) {
    const [metadata] = await file.getMetadata();
    totalBytes += parseInt(metadata.size || '0');
    fileCount++;
  }
  
  return {
    totalBytes,
    totalMB: totalBytes / (1024 * 1024),
    fileCount,
    lastCalculated: new Date().toISOString()
  };
}

function getStorageQuota(userTier: string): any {
  switch (userTier) {
    case 'GENERAL':
      return { maxBytes: 500 * 1024 * 1024, maxFiles: 100 }; // 500MB
    case 'FARMER':
      return { maxBytes: 2 * 1024 * 1024 * 1024, maxFiles: 500 }; // 2GB
    case 'ENTHUSIAST':
      return { maxBytes: 5 * 1024 * 1024 * 1024, maxFiles: 1000 }; // 5GB
    default:
      return { maxBytes: 500 * 1024 * 1024, maxFiles: 100 };
  }
}

async function sendStorageQuotaNotification(userId: string, usage: any, quota: any): Promise<void> {
  await admin.firestore().collection('notifications').add({
    userId: userId,
    type: 'storage_quota_warning',
    title: 'Storage Quota Warning',
    message: `You are using ${usage.totalMB.toFixed(1)}MB of your ${(quota.maxBytes / (1024 * 1024)).toFixed(0)}MB storage quota.`,
    data: { usage, quota },
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    read: false
  });
}

async function performUserStorageCleanup(userId: string, usage: any, quota: any): Promise<void> {
  // Implementation for automatic cleanup of user's old files
  console.log(`Performing storage cleanup for user ${userId}`);
}

function calculateEstimatedCost(distribution: any): number {
  // Cost per GB per month in INR
  const costs = {
    STANDARD: 1.5,
    NEARLINE: 0.7,
    COLDLINE: 0.3
  };
  
  let totalCost = 0;
  for (const [storageClass, data] of Object.entries(distribution)) {
    const sizeGB = (data as any).size / (1024 * 1024 * 1024);
    totalCost += sizeGB * costs[storageClass as keyof typeof costs];
  }
  
  return totalCost;
}

// Additional optimization functions would be implemented here...
async function analyzeStorageUsage(): Promise<any> { return {}; }
async function optimizeImageFormats(): Promise<any> { return {}; }
async function consolidateUserData(): Promise<any> { return {}; }
async function archiveOldTransferData(): Promise<any> { return {}; }
async function generateCostReport(): Promise<any> { return {}; }

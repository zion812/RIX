import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

const db = admin.firestore();
const REGION = 'asia-south1';

/**
 * Generate payment analytics report
 */
export const generatePaymentAnalytics = functions
  .region(REGION)
  .pubsub
  .schedule('0 0 * * *') // Daily at midnight
  .onRun(async (context) => {
    try {
      console.log('Starting payment analytics generation...');
      
      const today = new Date();
      const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000);
      
      // Generate daily analytics
      const dailyAnalytics = await generateDailyAnalytics(yesterday);
      
      // Generate weekly analytics (if it's Monday)
      if (today.getDay() === 1) {
        const weeklyAnalytics = await generateWeeklyAnalytics();
        await saveDailyAnalytics(dailyAnalytics, weeklyAnalytics);
      } else {
        await saveDailyAnalytics(dailyAnalytics);
      }
      
      // Generate monthly analytics (if it's the 1st of the month)
      if (today.getDate() === 1) {
        const monthlyAnalytics = await generateMonthlyAnalytics();
        await saveMonthlyAnalytics(monthlyAnalytics);
      }
      
      console.log('Payment analytics generation completed');
      
    } catch (error) {
      console.error('Error generating payment analytics:', error);
    }
  });

/**
 * Generate daily analytics
 */
async function generateDailyAnalytics(date: Date): Promise<any> {
  const startOfDay = new Date(date.setHours(0, 0, 0, 0));
  const endOfDay = new Date(date.setHours(23, 59, 59, 999));
  
  try {
    // Get daily transactions
    const transactionsSnapshot = await db.collection('coin_transactions')
      .where('createdAt', '>=', admin.firestore.Timestamp.fromDate(startOfDay))
      .where('createdAt', '<=', admin.firestore.Timestamp.fromDate(endOfDay))
      .get();
    
    // Get daily orders
    const ordersSnapshot = await db.collection('coin_orders')
      .where('createdAt', '>=', admin.firestore.Timestamp.fromDate(startOfDay))
      .where('createdAt', '<=', admin.firestore.Timestamp.fromDate(endOfDay))
      .get();
    
    const transactions = transactionsSnapshot.docs.map(doc => doc.data());
    const orders = ordersSnapshot.docs.map(doc => doc.data());
    
    // Calculate metrics
    const totalTransactions = transactions.length;
    const totalOrders = orders.length;
    const successfulOrders = orders.filter(order => order.status === 'COMPLETED').length;
    const failedOrders = orders.filter(order => order.status === 'FAILED').length;
    
    const totalRevenue = orders
      .filter(order => order.status === 'COMPLETED')
      .reduce((sum, order) => sum + order.amount, 0);
    
    const totalCoinsIssued = orders
      .filter(order => order.status === 'COMPLETED')
      .reduce((sum, order) => sum + order.totalCoins, 0);
    
    const totalCoinsSpent = transactions
      .filter(tx => tx.type === 'SPEND' && tx.status === 'COMPLETED')
      .reduce((sum, tx) => sum + tx.amount, 0);
    
    // Payment method breakdown
    const paymentMethodBreakdown = orders.reduce((breakdown, order) => {
      const method = order.paymentMethod;
      if (!breakdown[method]) {
        breakdown[method] = { count: 0, revenue: 0 };
      }
      breakdown[method].count++;
      if (order.status === 'COMPLETED') {
        breakdown[method].revenue += order.amount;
      }
      return breakdown;
    }, {});
    
    // User tier breakdown
    const userTierBreakdown = orders.reduce((breakdown, order) => {
      const tier = order.userTier;
      if (!breakdown[tier]) {
        breakdown[tier] = { count: 0, revenue: 0 };
      }
      breakdown[tier].count++;
      if (order.status === 'COMPLETED') {
        breakdown[tier].revenue += order.amount;
      }
      return breakdown;
    }, {});
    
    return {
      date: admin.firestore.Timestamp.fromDate(date),
      totalTransactions,
      totalOrders,
      successfulOrders,
      failedOrders,
      successRate: totalOrders > 0 ? (successfulOrders / totalOrders) * 100 : 0,
      totalRevenue,
      totalCoinsIssued,
      totalCoinsSpent,
      averageOrderValue: successfulOrders > 0 ? totalRevenue / successfulOrders : 0,
      paymentMethodBreakdown,
      userTierBreakdown,
      generatedAt: admin.firestore.FieldValue.serverTimestamp()
    };
    
  } catch (error) {
    console.error('Error generating daily analytics:', error);
    throw error;
  }
}

/**
 * Generate weekly analytics
 */
async function generateWeeklyAnalytics(): Promise<any> {
  const today = new Date();
  const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
  
  try {
    // Get weekly data from daily analytics
    const weeklySnapshot = await db.collection('payment_analytics')
      .where('date', '>=', admin.firestore.Timestamp.fromDate(weekAgo))
      .where('date', '<', admin.firestore.Timestamp.fromDate(today))
      .get();
    
    const weeklyData = weeklySnapshot.docs.map(doc => doc.data());
    
    // Aggregate weekly metrics
    const totalRevenue = weeklyData.reduce((sum, day) => sum + (day.totalRevenue || 0), 0);
    const totalOrders = weeklyData.reduce((sum, day) => sum + (day.totalOrders || 0), 0);
    const successfulOrders = weeklyData.reduce((sum, day) => sum + (day.successfulOrders || 0), 0);
    const totalCoinsIssued = weeklyData.reduce((sum, day) => sum + (day.totalCoinsIssued || 0), 0);
    const totalCoinsSpent = weeklyData.reduce((sum, day) => sum + (day.totalCoinsSpent || 0), 0);
    
    return {
      weekStarting: admin.firestore.Timestamp.fromDate(weekAgo),
      weekEnding: admin.firestore.Timestamp.fromDate(today),
      totalRevenue,
      totalOrders,
      successfulOrders,
      successRate: totalOrders > 0 ? (successfulOrders / totalOrders) * 100 : 0,
      totalCoinsIssued,
      totalCoinsSpent,
      averageOrderValue: successfulOrders > 0 ? totalRevenue / successfulOrders : 0,
      dailyData: weeklyData,
      generatedAt: admin.firestore.FieldValue.serverTimestamp()
    };
    
  } catch (error) {
    console.error('Error generating weekly analytics:', error);
    throw error;
  }
}

/**
 * Generate monthly analytics
 */
async function generateMonthlyAnalytics(): Promise<any> {
  const today = new Date();
  const lastMonth = new Date(today.getFullYear(), today.getMonth() - 1, 1);
  const thisMonth = new Date(today.getFullYear(), today.getMonth(), 1);
  
  try {
    // Get monthly data
    const monthlySnapshot = await db.collection('payment_analytics')
      .where('date', '>=', admin.firestore.Timestamp.fromDate(lastMonth))
      .where('date', '<', admin.firestore.Timestamp.fromDate(thisMonth))
      .get();
    
    const monthlyData = monthlySnapshot.docs.map(doc => doc.data());
    
    // Aggregate monthly metrics
    const totalRevenue = monthlyData.reduce((sum, day) => sum + (day.totalRevenue || 0), 0);
    const totalOrders = monthlyData.reduce((sum, day) => sum + (day.totalOrders || 0), 0);
    const successfulOrders = monthlyData.reduce((sum, day) => sum + (day.successfulOrders || 0), 0);
    const totalCoinsIssued = monthlyData.reduce((sum, day) => sum + (day.totalCoinsIssued || 0), 0);
    const totalCoinsSpent = monthlyData.reduce((sum, day) => sum + (day.totalCoinsSpent || 0), 0);
    
    // Calculate growth metrics (compare with previous month)
    const previousMonth = new Date(today.getFullYear(), today.getMonth() - 2, 1);
    const previousMonthSnapshot = await db.collection('monthly_analytics')
      .where('month', '>=', admin.firestore.Timestamp.fromDate(previousMonth))
      .where('month', '<', admin.firestore.Timestamp.fromDate(lastMonth))
      .limit(1)
      .get();
    
    let growthMetrics = {};
    if (!previousMonthSnapshot.empty) {
      const previousData = previousMonthSnapshot.docs[0].data();
      growthMetrics = {
        revenueGrowth: calculateGrowthPercentage(totalRevenue, previousData.totalRevenue),
        orderGrowth: calculateGrowthPercentage(totalOrders, previousData.totalOrders),
        userGrowth: calculateGrowthPercentage(successfulOrders, previousData.successfulOrders)
      };
    }
    
    return {
      month: admin.firestore.Timestamp.fromDate(lastMonth),
      totalRevenue,
      totalOrders,
      successfulOrders,
      successRate: totalOrders > 0 ? (successfulOrders / totalOrders) * 100 : 0,
      totalCoinsIssued,
      totalCoinsSpent,
      averageOrderValue: successfulOrders > 0 ? totalRevenue / successfulOrders : 0,
      growthMetrics,
      dailyData: monthlyData,
      generatedAt: admin.firestore.FieldValue.serverTimestamp()
    };
    
  } catch (error) {
    console.error('Error generating monthly analytics:', error);
    throw error;
  }
}

/**
 * Save daily analytics
 */
async function saveDailyAnalytics(dailyAnalytics: any, weeklyAnalytics?: any): Promise<void> {
  const batch = db.batch();
  
  // Save daily analytics
  const dailyRef = db.collection('payment_analytics').doc();
  batch.set(dailyRef, dailyAnalytics);
  
  // Save weekly analytics if provided
  if (weeklyAnalytics) {
    const weeklyRef = db.collection('weekly_analytics').doc();
    batch.set(weeklyRef, weeklyAnalytics);
  }
  
  await batch.commit();
}

/**
 * Save monthly analytics
 */
async function saveMonthlyAnalytics(monthlyAnalytics: any): Promise<void> {
  const monthlyRef = db.collection('monthly_analytics').doc();
  await monthlyRef.set(monthlyAnalytics);
}

/**
 * Calculate growth percentage
 */
function calculateGrowthPercentage(current: number, previous: number): number {
  if (previous === 0) return current > 0 ? 100 : 0;
  return ((current - previous) / previous) * 100;
}

package com.rio.rostry.core.database.di

import android.content.Context
import androidx.room.Room
import com.rio.rostry.core.database.RIOLocalDatabase
import com.rio.rostry.core.database.dao.*
// import dagger.Module // Temporarily disabled for Phase 1
// import dagger.Provides // Temporarily disabled for Phase 1
// import dagger.hilt.InstallIn // Temporarily disabled for Phase 1
// import dagger.hilt.android.qualifiers.ApplicationContext // Temporarily disabled for Phase 1
// import dagger.hilt.components.SingletonComponent // Temporarily disabled for Phase 1
// import javax.inject.Singleton // Temporarily disabled for Phase 1

/**
 * Database dependency injection module
 * Provides RIOLocalDatabase as the single source of truth
 * TEMPORARILY DISABLED FOR PHASE 1 - FIXING BUILD ISSUES
 */
// @Module
// @InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Provide the main RIOLocalDatabase instance
     * TEMPORARILY DISABLED FOR PHASE 1
     */
    // @Provides
    // @Singleton
    fun provideRIOLocalDatabase(
        /* @ApplicationContext */ context: Context
    ): RIOLocalDatabase {
        return Room.databaseBuilder(
            context,
            RIOLocalDatabase::class.java,
            "rio_local_database"
        )
        .addMigrations(
            RIOLocalDatabase.MIGRATION_1_2,
            RIOLocalDatabase.MIGRATION_2_3
        )
        .fallbackToDestructiveMigration() // For development only - remove in production
        .enableMultiInstanceInvalidation()
        .setJournalMode(androidx.room.RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .build()
    }
    
    // Core DAOs - TEMPORARILY DISABLED FOR PHASE 1
    // @Provides
    fun provideUserDao(database: RIOLocalDatabase): UserDao = database.userDao()

    // @Provides
    fun provideFowlDao(database: RIOLocalDatabase): FowlDao = database.fowlDao()

    // @Provides
    fun provideMarketplaceDao(database: RIOLocalDatabase): MarketplaceDao = database.marketplaceDao()

    // @Provides
    fun provideMessageDao(database: RIOLocalDatabase): MessageDao = database.messageDao()

    // @Provides
    fun provideConversationDao(database: RIOLocalDatabase): ConversationDao = database.conversationDao()

    // @Provides
    fun provideTransferDao(database: RIOLocalDatabase): TransferDao = database.transferDao()

    // Coin payment system DAOs - TEMPORARILY DISABLED FOR PHASE 1
    // @Provides
    fun provideCoinTransactionDao(database: RIOLocalDatabase): CoinTransactionDao = database.coinTransactionDao()

    // @Provides
    fun provideCoinOrderDao(database: RIOLocalDatabase): CoinOrderDao = database.coinOrderDao()

    // @Provides
    fun provideRefundRequestDao(database: RIOLocalDatabase): RefundRequestDao = database.refundRequestDao()

    // @Provides
    fun provideDisputeDao(database: RIOLocalDatabase): DisputeDao = database.disputeDao()

    // @Provides
    fun provideUserCoinBalanceDao(database: RIOLocalDatabase): UserCoinBalanceDao = database.userCoinBalanceDao()

    // @Provides
    fun provideCoinPackageDao(database: RIOLocalDatabase): CoinPackageDao = database.coinPackageDao()

    // Sync and cache DAOs - TEMPORARILY DISABLED FOR PHASE 1
    // @Provides
    fun provideOfflineActionDao(database: RIOLocalDatabase): OfflineActionDao = database.offlineActionDao()

    // Notification system DAOs - TEMPORARILY DISABLED FOR PHASE 1
    // @Provides
    fun provideNotificationDao(database: RIOLocalDatabase): NotificationDao = database.notificationDao()

    // @Provides
    fun provideNotificationPreferenceDao(database: RIOLocalDatabase): NotificationPreferenceDao = database.notificationPreferenceDao()

    // @Provides
    fun provideTopicSubscriptionDao(database: RIOLocalDatabase): TopicSubscriptionDao = database.topicSubscriptionDao()

    // @Provides
    fun provideNotificationAnalyticsDao(database: RIOLocalDatabase): NotificationAnalyticsDao = database.notificationAnalyticsDao()

    // Legacy DAO mappings for backward compatibility - TEMPORARILY DISABLED FOR PHASE 1
    // @Provides
    fun provideCoinDao(coinTransactionDao: CoinTransactionDao): CoinDao {
        // Create an adapter that implements CoinDao interface using CoinTransactionDao
        return object : CoinDao {
            override suspend fun insert(transaction: com.rio.rostry.core.database.entities.CoinTransactionEntity) {
                coinTransactionDao.insert(transaction)
            }
            
            override suspend fun getTransactionsByUserId(userId: String): List<com.rio.rostry.core.database.entities.CoinTransactionEntity> {
                return coinTransactionDao.getTransactionsByUserId(userId)
            }
            
            override suspend fun getTransactionById(transactionId: String): com.rio.rostry.core.database.entities.CoinTransactionEntity? {
                return coinTransactionDao.getTransactionById(transactionId)
            }
            
            override suspend fun updateTransactionStatus(transactionId: String, status: String) {
                coinTransactionDao.updateTransactionStatus(transactionId, status)
            }
            
            override suspend fun getPendingTransactions(userId: String): List<com.rio.rostry.core.database.entities.CoinTransactionEntity> {
                return coinTransactionDao.getPendingTransactions(userId)
            }
        }
    }
    
    // Sync DAO mapping for backward compatibility - TEMPORARILY DISABLED FOR PHASE 1
    // @Provides
    fun provideSyncDao(offlineActionDao: OfflineActionDao): SyncDao {
        // Create an adapter that implements SyncDao interface using OfflineActionDao
        return object : SyncDao {
            override suspend fun insertSyncItem(item: com.rio.rostry.core.database.entities.SyncQueueEntity) {
                // Convert SyncQueueEntity to OfflineActionEntity
                val offlineAction = com.rio.rostry.core.database.entities.OfflineActionEntity(
                    id = item.id,
                    action = item.operation,
                    collection = item.entityType,
                    documentId = item.entityId,
                    data = item.payload ?: "",
                    timestamp = item.createdAt,
                    retryCount = item.retryCount,
                    maxRetries = item.maxRetries,
                    status = when {
                        item.syncMetadata.syncStatus == com.rio.rostry.core.common.model.SyncStatus.SYNCED -> "synced"
                        item.syncMetadata.syncStatus == com.rio.rostry.core.common.model.SyncStatus.FAILED -> "failed"
                        else -> "pending"
                    },
                    lastRetryAt = item.lastAttempt
                )
                offlineActionDao.insert(offlineAction)
            }
            
            override suspend fun getPendingSyncItems(): List<com.rio.rostry.core.database.entities.SyncQueueEntity> {
                return offlineActionDao.getAllPending().map { action ->
                    com.rio.rostry.core.database.entities.SyncQueueEntity(
                        id = action.id,
                        entityType = action.collection,
                        entityId = action.documentId,
                        operation = action.action,
                        priority = com.rio.rostry.core.common.model.SyncPriority.MEDIUM,
                        retryCount = action.retryCount,
                        maxRetries = action.maxRetries,
                        lastAttempt = action.lastRetryAt,
                        nextAttempt = null,
                        errorMessage = null,
                        payload = action.data,
                        createdAt = action.timestamp,
                        updatedAt = action.timestamp
                    )
                }
            }
            
            override suspend fun markSyncItemCompleted(itemId: String) {
                offlineActionDao.markAsSynced(itemId)
            }
            
            override suspend fun markSyncItemFailed(itemId: String, errorMessage: String) {
                offlineActionDao.markAsFailed(itemId)
            }
            
            override suspend fun deleteSyncItem(itemId: String) {
                // OfflineActionDao doesn't have direct delete, but we can mark as synced
                offlineActionDao.markAsSynced(itemId)
            }
        }
    }
}

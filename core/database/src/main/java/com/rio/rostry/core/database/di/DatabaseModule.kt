package com.rio.rostry.core.database.di

import android.content.Context
import androidx.room.Room
import com.rio.rostry.core.database.RIOLocalDatabase
import com.rio.rostry.core.database.dao.*
import com.rio.rostry.core.database.entities.OutboxDaoV2
import com.rio.rostry.core.database.entities.TransferLogDao
import com.rio.rostry.core.database.Migration1_2
import com.rio.rostry.core.database.Migration2_3
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Database dependency injection module
 * Provides RIOLocalDatabase as the single source of truth
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Provide the main RIOLocalDatabase instance
     */
    @Provides
    @Singleton
    fun provideRIOLocalDatabase(
        @ApplicationContext context: Context
    ): RIOLocalDatabase {
        return Room.databaseBuilder(
            context,
            RIOLocalDatabase::class.java,
            "rio_local_database"
        )
        .addMigrations(
            Migration1_2,
            Migration2_3
        )
        .fallbackToDestructiveMigration() // For development only - remove in production
        .enableMultiInstanceInvalidation()
        .setJournalMode(androidx.room.RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .build()
    }
    
    // Core DAOs
    @Provides
    fun provideUserDao(database: RIOLocalDatabase): UserDao = database.userDao()

    @Provides
    fun provideFowlDao(database: RIOLocalDatabase): FowlDaoV2 = database.fowlDao()

    @Provides
    fun provideMarketplaceDao(database: RIOLocalDatabase): MarketplaceDao = database.marketplaceDao()

    @Provides
    fun provideMessageDao(database: RIOLocalDatabase): MessageDaoV2 = database.messageDao()

    @Provides
    fun provideConversationDao(database: RIOLocalDatabase): ConversationDaoV2 = database.conversationDao()

    @Provides
    fun provideTransferDao(database: RIOLocalDatabase): TransferDaoV2 = database.transferDao()

    @Provides
    fun provideTransferLogDao(database: RIOLocalDatabase): TransferLogDao = database.transferLogDao()

    // Coin payment system DAOs
    @Provides
    fun provideCoinTransactionDao(database: RIOLocalDatabase): CoinTransactionDao = database.coinTransactionDao()

    @Provides
    fun provideCoinOrderDao(database: RIOLocalDatabase): CoinOrderDao = database.coinOrderDao()

    @Provides
    fun provideRefundRequestDao(database: RIOLocalDatabase): RefundRequestDao = database.refundRequestDao()

    @Provides
    fun provideDisputeDao(database: RIOLocalDatabase): DisputeDao = database.disputeDao()

    @Provides
    fun provideUserCoinBalanceDao(database: RIOLocalDatabase): UserCoinBalanceDao = database.userCoinBalanceDao()

    @Provides
    fun provideCoinPackageDao(database: RIOLocalDatabase): CoinPackageDao = database.coinPackageDao()

    // Sync and cache DAOs
    @Provides
    fun provideOfflineActionDao(database: RIOLocalDatabase): OutboxDaoV2 = database.outboxDao()

    // Notification system DAOs
    @Provides
    fun provideNotificationDao(database: RIOLocalDatabase): NotificationDao = database.notificationDao()

    @Provides
    fun provideNotificationPreferenceDao(database: RIOLocalDatabase): NotificationPreferenceDao = database.notificationPreferenceDao()

    @Provides
    fun provideTopicSubscriptionDao(database: RIOLocalDatabase): TopicSubscriptionDao = database.topicSubscriptionDao()

    @Provides
    fun provideNotificationAnalyticsDao(database: RIOLocalDatabase): NotificationAnalyticsDao = database.notificationAnalyticsDao()
    
    @Provides
    fun provideFowlRecordDao(database: RIOLocalDatabase): FowlRecordDao = database.fowlRecordDao()
}
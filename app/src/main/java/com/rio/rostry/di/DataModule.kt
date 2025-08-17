package com.rio.rostry.di

import com.rio.rostry.core.data.repository.FowlRepository
import com.rio.rostry.core.data.repository.TransferRepository
import com.rio.rostry.core.data.repository.TimelineRepository
import com.rio.rostry.core.database.RIOLocalDatabase
import com.rio.rostry.core.database.dao.FowlDaoV2
import com.rio.rostry.core.database.dao.OutboxDao
import com.rio.rostry.core.database.entities.TransferLogDao
import com.rio.rostry.core.database.dao.TimelineDao
import com.rio.rostry.core.notifications.TransferNotificationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for data repositories
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideFowlDao(database: RIOLocalDatabase): FowlDaoV2 = database.fowlDao()

    @Provides
    @Singleton
    fun provideTransferLogDao(database: RIOLocalDatabase): TransferLogDao = database.transferLogDao()

    @Provides
    @Singleton
    fun provideOutboxDao(database: RIOLocalDatabase): OutboxDao = database.outboxDao()
    
    @Provides
    @Singleton
    fun provideTimelineDao(database: RIOLocalDatabase): TimelineDao = database.timelineDao()

    @Provides
    @Singleton
    fun provideTransferRepository(
        transferLogDao: TransferLogDao,
        outboxDao: OutboxDao,
        fowlDao: FowlDaoV2,
        transferNotificationService: TransferNotificationService
    ): TransferRepository = TransferRepository(
        transferLogDao = transferLogDao,
        outboxDao = outboxDao,
        fowlDao = fowlDao,
        transferNotificationService = transferNotificationService
    )

    @Provides
    @Singleton
    fun provideFowlRepository(
        fowlDao: FowlDaoV2,
        outboxDao: OutboxDao
    ): FowlRepository = FowlRepository(
        fowlDao = fowlDao,
        outboxDao = outboxDao
    )
    
    @Provides
    @Singleton
    fun provideTimelineRepository(
        timelineDao: TimelineDao,
        outboxDao: OutboxDao
    ): TimelineRepository = TimelineRepository(
        timelineDao = timelineDao,
        outboxDao = outboxDao
    )
}
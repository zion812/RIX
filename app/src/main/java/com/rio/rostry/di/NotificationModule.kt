package com.rio.rostry.di

import android.content.Context
import com.rio.rostry.core.notifications.NotificationManagerService
import com.rio.rostry.core.notifications.TransferNotificationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for notification services
 */
@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideNotificationManagerService(
        @ApplicationContext context: Context
    ): NotificationManagerService = NotificationManagerService(context)

    @Provides
    @Singleton
    fun provideTransferNotificationService(
        @ApplicationContext context: Context,
        notificationManagerService: NotificationManagerService
    ): TransferNotificationService = TransferNotificationService(context, notificationManagerService)
}
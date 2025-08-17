package com.rio.rostry.core.data.di

import com.rio.rostry.core.data.repository.TransferRepository
import com.rio.rostry.core.data.repository.TransferRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for providing transfer repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TransferModule {

    @Binds
    abstract fun bindTransferRepository(
        transferRepositoryImpl: TransferRepositoryImpl
    ): TransferRepository
}
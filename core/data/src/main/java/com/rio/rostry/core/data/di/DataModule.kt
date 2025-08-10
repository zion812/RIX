package com.rio.rostry.core.data.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rio.rostry.core.data.repository.*
import com.rio.rostry.core.data.service.UserValidationService
import com.rio.rostry.core.database.dao.*
import com.rio.rostry.core.network.NetworkStateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * ✅ Data layer dependency injection module
 * Properly manages dependencies to avoid circular references
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    /**
     * ✅ Provide UserValidationService first (no dependencies on other repositories)
     */
    @Provides
    @Singleton
    fun provideUserValidationService(
        userDao: UserDao
    ): UserValidationService = UserValidationService(userDao)
    
    /**
     * ✅ Provide UserRepository (depends only on basic services)
     */
    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        firestore: FirebaseFirestore,
        networkStateManager: NetworkStateManager
    ): UserRepositoryImpl = UserRepositoryImpl(userDao, firestore, networkStateManager)
    
    /**
     * ✅ Provide FowlRepository (depends on UserValidationService, not UserRepository)
     */
    @Provides
    @Singleton
    fun provideFowlRepository(
        fowlDao: FowlDao,
        userValidationService: UserValidationService,
        firestore: FirebaseFirestore,
        networkStateManager: NetworkStateManager
    ): FowlRepositoryImpl = FowlRepositoryImpl(
        fowlDao, 
        userValidationService, 
        firestore, 
        networkStateManager
    )
    
    /**
     * ✅ Provide MarketplaceRepository (depends on UserValidationService)
     */
    @Provides
    @Singleton
    fun provideMarketplaceRepository(
        marketplaceDao: MarketplaceDao,
        userValidationService: UserValidationService,
        firestore: FirebaseFirestore,
        networkStateManager: NetworkStateManager
    ): MarketplaceRepositoryImpl = MarketplaceRepositoryImpl(
        marketplaceDao,
        firestore,
        networkStateManager
    )
    
    /**
     * ✅ Provide TransferRepository (depends on validation service)
     */
    @Provides
    @Singleton
    fun provideTransferRepository(
        transferDao: TransferDao,
        fowlDao: FowlDao,
        userValidationService: UserValidationService,
        firestore: FirebaseFirestore,
        networkStateManager: NetworkStateManager
    ): TransferRepositoryImpl = TransferRepositoryImpl(
        transferDao,
        fowlDao,
        userValidationService,
        firestore,
        networkStateManager
    )
    
    /**
     * ✅ Provide MediaRepository
     */
    @Provides
    @Singleton
    fun provideMediaRepository(
        storage: FirebaseStorage,
        networkStateManager: NetworkStateManager
    ): MediaRepositoryImpl = MediaRepositoryImpl(storage, networkStateManager)
    
    /**
     * ✅ Provide CoinRepository
     */
    @Provides
    @Singleton
    fun provideCoinRepository(
        coinDao: CoinDao,
        userValidationService: UserValidationService,
        firestore: FirebaseFirestore,
        networkStateManager: NetworkStateManager
    ): CoinRepositoryImpl = CoinRepositoryImpl(
        coinDao,
        userValidationService,
        firestore,
        networkStateManager
    )
    
    /**
     * ✅ Provide ChatRepository
     */
    @Provides
    @Singleton
    fun provideChatRepository(
        chatDao: ChatDao,
        userValidationService: UserValidationService,
        firestore: FirebaseFirestore,
        networkStateManager: NetworkStateManager
    ): ChatRepositoryImpl = ChatRepositoryImpl(
        chatDao,
        userValidationService,
        firestore,
        networkStateManager
    )
    
    /**
     * ✅ Provide BreedingRepository
     */
    @Provides
    @Singleton
    fun provideBreedingRepository(
        breedingDao: BreedingDao,
        fowlDao: FowlDao,
        userValidationService: UserValidationService,
        firestore: FirebaseFirestore,
        networkStateManager: NetworkStateManager
    ): BreedingRepositoryImpl = BreedingRepositoryImpl(
        breedingDao,
        fowlDao,
        userValidationService,
        firestore,
        networkStateManager
    )
    
    /**
     * ✅ Provide NotificationRepository
     */
    @Provides
    @Singleton
    fun provideNotificationRepository(
        notificationDao: NotificationDao,
        firestore: FirebaseFirestore,
        networkStateManager: NetworkStateManager
    ): NotificationRepositoryImpl = NotificationRepositoryImpl(
        notificationDao,
        firestore,
        networkStateManager
    )
}

/**
 * ✅ Repository interfaces module
 * Binds implementations to interfaces
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    // Note: If you have repository interfaces, bind them here
    // Example:
    // @Binds
    // abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}

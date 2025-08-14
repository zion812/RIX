package com.rio.rostry.core.data.di

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.core.data.repository.FowlRepositoryImpl
import com.rio.rostry.core.data.repository.UserRepositoryImpl
import com.rio.rostry.core.database.dao.FowlDao
import com.rio.rostry.core.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository implementations
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        firestore: FirebaseFirestore
    ): UserRepositoryImpl {
        return UserRepositoryImpl(userDao, firestore)
    }

    @Provides
    @Singleton
    fun provideFowlRepository(
        fowlDao: FowlDao,
        firestore: FirebaseFirestore
    ): FowlRepositoryImpl {
        return FowlRepositoryImpl(fowlDao, firestore)
    }
}

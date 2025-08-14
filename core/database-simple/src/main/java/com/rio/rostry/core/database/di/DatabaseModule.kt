package com.rio.rostry.core.database.di

import android.content.Context
import androidx.room.Room
import com.rio.rostry.core.database.ROSTRYDatabase
import com.rio.rostry.core.database.dao.FowlDao
import com.rio.rostry.core.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideROSTRYDatabase(@ApplicationContext context: Context): ROSTRYDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ROSTRYDatabase::class.java,
            ROSTRYDatabase.DATABASE_NAME
        )
        .addMigrations(ROSTRYDatabase.MIGRATION_1_2)
        .fallbackToDestructiveMigration() // For development only
        .build()
    }

    @Provides
    fun provideUserDao(database: ROSTRYDatabase): UserDao = database.userDao()

    @Provides
    fun provideFowlDao(database: ROSTRYDatabase): FowlDao = database.fowlDao()
}

/**
 * Legacy database provider for backward compatibility
 * TODO: Remove once all manual DI is migrated to Hilt
 */
object DatabaseProvider {

    @Volatile
    private var INSTANCE: ROSTRYDatabase? = null

    fun getDatabase(context: Context): ROSTRYDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                ROSTRYDatabase::class.java,
                ROSTRYDatabase.DATABASE_NAME
            )
            .addMigrations(ROSTRYDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration() // For development only
            .build()
            INSTANCE = instance
            instance
        }
    }
}

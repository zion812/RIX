package com.rio.rostry.core.database.di

import android.content.Context
import androidx.room.Room
import com.rio.rostry.core.database.RIOLocalDatabase

/**
 * Legacy database provider for backward compatibility
 * Provides access to RIOLocalDatabase for components not yet migrated to Hilt
 * TODO: Remove once all manual DI is migrated to Hilt
 */
object DatabaseProvider {
    @Volatile
    private var INSTANCE: RIOLocalDatabase? = null

    fun getDatabase(context: Context): RIOLocalDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
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
            .build().also { INSTANCE = it }
        }
    }
}
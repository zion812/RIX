package com.rio.rostry.core.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rio.rostry.core.database.entities.*
import com.rio.rostry.core.database.dao.*
import com.rio.rostry.core.database.converters.*

/**
 * Simplified Room database for Phase 2
 * Focus on core functionality with minimal complexity
 */
@Database(
    entities = [
        UserEntity::class,
        FowlEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    DateConverter::class
)
abstract class ROSTRYDatabase : RoomDatabase() {

    // Core DAOs
    abstract fun userDao(): UserDao
    abstract fun fowlDao(): FowlDao

    companion object {
        const val DATABASE_NAME = "rostry_database"
        
        /**
         * Migration from version 1 to 2 (when needed in future)
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add migration logic when needed
            }
        }
    }
}

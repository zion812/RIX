package com.rio.rostry.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migration from version 2 to 3
 * Adds the fowl_records table for timeline functionality
 */
object Migration2_3 : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create the fowl_records table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `fowl_records` (
                `id` TEXT NOT NULL,
                `fowl_id` TEXT NOT NULL,
                `record_type` TEXT NOT NULL,
                `record_date` INTEGER NOT NULL,
                `description` TEXT,
                `metrics` TEXT NOT NULL,
                `proof_urls` TEXT NOT NULL,
                `proof_count` INTEGER NOT NULL,
                `created_by` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                `version` INTEGER NOT NULL,
                `is_deleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`fowl_id`) REFERENCES `fowls`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())
        
        // Create indices for better query performance
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_fowl_records_fowl_id` ON `fowl_records` (`fowl_id`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_fowl_records_record_type` ON `fowl_records` (`record_type`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_fowl_records_record_date` ON `fowl_records` (`record_date`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_fowl_records_created_by` ON `fowl_records` (`created_by`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_fowl_records_is_deleted` ON `fowl_records` (`is_deleted`)")
    }
}
package com.rio.rostry.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from database version 1 to 2
 * Adds transfer_logs and outbox tables for verified transfer workflow and sync operations
 */
object Migration1_2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create transfer_logs table
        database.execSQL(
            """
            CREATE TABLE transfer_logs (
                id TEXT NOT NULL PRIMARY KEY,
                fowl_id TEXT NOT NULL,
                from_user_id TEXT NOT NULL,
                to_user_id TEXT NOT NULL,
                transfer_status TEXT NOT NULL,
                verification_required INTEGER NOT NULL DEFAULT 1,
                verification_status TEXT NOT NULL,
                verification_documents TEXT NOT NULL DEFAULT '[]',
                verification_notes TEXT,
                verified_by TEXT,
                verified_at INTEGER,
                expected_price REAL,
                expected_color TEXT,
                expected_age_weeks INTEGER,
                expected_weight_grams INTEGER,
                photo_reference TEXT,
                initiated_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                completed_at INTEGER,
                cancelled_at INTEGER,
                dispute_raised INTEGER NOT NULL DEFAULT 0,
                dispute_reason TEXT,
                dispute_status TEXT,
                dispute_resolution TEXT,
                dispute_resolved_at INTEGER,
                created_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (fowl_id) REFERENCES fowls(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // Create indices for transfer_logs
        database.execSQL("CREATE INDEX index_transfer_logs_fowl_id ON transfer_logs (fowl_id)")
        database.execSQL("CREATE INDEX index_transfer_logs_from_user_id ON transfer_logs (from_user_id)")
        database.execSQL("CREATE INDEX index_transfer_logs_to_user_id ON transfer_logs (to_user_id)")
        database.execSQL("CREATE INDEX index_transfer_logs_transfer_status ON transfer_logs (transfer_status)")
        database.execSQL("CREATE INDEX index_transfer_logs_verification_status ON transfer_logs (verification_status)")
        database.execSQL("CREATE INDEX index_transfer_logs_initiated_at ON transfer_logs (initiated_at)")
        database.execSQL("CREATE INDEX index_transfer_logs_verified_at ON transfer_logs (verified_at)")
        database.execSQL("CREATE INDEX index_transfer_logs_is_deleted ON transfer_logs (is_deleted)")
        
        // Create outbox table
        database.execSQL(
            """
            CREATE TABLE outbox (
                id TEXT NOT NULL PRIMARY KEY,
                entity_type TEXT NOT NULL,
                entity_id TEXT NOT NULL,
                operation_type TEXT NOT NULL,
                entity_data TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                sync_status TEXT NOT NULL DEFAULT 'PENDING',
                retry_count INTEGER NOT NULL DEFAULT 0,
                priority INTEGER NOT NULL DEFAULT 3,
                error_message TEXT,
                is_deleted INTEGER NOT NULL DEFAULT 0
                last_attempt_at INTEGER,
                synced_at INTEGER
            )
            """.trimIndent()
        )
        
        // Create indices for outbox
        database.execSQL("CREATE INDEX index_outbox_entity_type ON outbox (entity_type)")
        database.execSQL("CREATE INDEX index_outbox_operation_type ON outbox (operation_type)")
        database.execSQL("CREATE INDEX index_outbox_created_at ON outbox (created_at)")
        database.execSQL("CREATE INDEX index_outbox_sync_status ON outbox (sync_status)")
        database.execSQL("CREATE INDEX index_outbox_priority ON outbox (priority)")
    }
}
package com.rio.rostry.core.database.optimization

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database optimization for performance in rural network conditions
 * Includes proper indexing, query optimization, and storage management
 */

/**
 * Database migration with performance optimizations
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add performance indexes for critical queries
        
        // Fowl indexes for owner and regional queries
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_fowls_owner_region 
            ON fowls(owner_id, region, district, is_deleted)
        """)
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_fowls_breed_region 
            ON fowls(breed_primary, region, availability_status, is_deleted)
        """)
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_fowls_sync_priority 
            ON fowls(sync_status, sync_priority, retry_count)
        """)
        
        // Marketplace indexes for search and filtering
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_marketplace_region_status 
            ON marketplace_listings(region, district, listing_status, expires_at, is_deleted)
        """)
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_marketplace_breed_price 
            ON marketplace_listings(breed, base_price, listing_status, is_deleted)
        """)
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_marketplace_seller_status 
            ON marketplace_listings(seller_id, listing_status, created_at, is_deleted)
        """)
        
        // Message indexes for conversation queries
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_messages_conversation_time 
            ON messages(conversation_id, sent_at, is_deleted)
        """)
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_messages_delivery_status 
            ON messages(delivery_status, retry_count, sent_at)
        """)
        
        // Transfer indexes for critical operations
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_transfers_fowl_status 
            ON transfers(fowl_id, transfer_status, initiated_at)
        """)
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_transfers_user_status 
            ON transfers(from_user_id, to_user_id, transfer_status, verification_required)
        """)
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_transfers_verification 
            ON transfers(verification_status, verification_required, initiated_at)
        """)
        
        // User indexes for authentication and search
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_users_email_tier 
            ON users(email, user_tier, verification_status, is_deleted)
        """)
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_users_region_tier 
            ON users(region, district, user_tier, verification_status, is_deleted)
        """)
        
        // Sync queue indexes for processing
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_sync_queue_priority_status 
            ON sync_queue(sync_priority, sync_status, scheduled_at)
        """)
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_sync_queue_entity_type 
            ON sync_queue(entity_type, sync_status, retry_count)
        """)
        
        // Offline action indexes
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_offline_actions_priority_status 
            ON offline_actions(priority, status, queued_at)
        """)
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_offline_actions_dependencies 
            ON offline_actions(depends_on, status)
        """)
        
        // Media indexes for upload management
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_media_entity_upload 
            ON media(entity_type, entity_id, upload_status, created_at)
        """)
        
        // Notification indexes
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_notifications_user_read 
            ON notifications(user_id, is_read, created_at, is_deleted)
        """)
    }
}

/**
 * Database cleanup and maintenance operations
 */
class DatabaseMaintenanceManager {
    
    /**
     * Cleanup old synced data to free storage space
     */
    suspend fun performMaintenance(database: SupportSQLiteDatabase) {
        val olderThanDays = 30
        val olderThanMillis = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
        
        // Clean up old completed sync queue items
        database.execSQL("""
            DELETE FROM sync_queue 
            WHERE sync_status = 'COMPLETED' 
            AND last_attempt_at < $olderThanMillis
        """)
        
        // Clean up old completed offline actions
        database.execSQL("""
            DELETE FROM offline_actions 
            WHERE status = 'COMPLETED' 
            AND processed_at < $olderThanMillis
        """)
        
        // Clean up old notifications
        database.execSQL("""
            DELETE FROM notifications 
            WHERE (expires_at < ${System.currentTimeMillis()} OR created_at < $olderThanMillis)
            AND is_deleted = 0
        """)
        
        // Clean up old media files that failed to upload
        database.execSQL("""
            DELETE FROM media 
            WHERE upload_status = 'FAILED' 
            AND created_at < $olderThanMillis
        """)
        
        // Vacuum database to reclaim space
        database.execSQL("VACUUM")
        
        // Analyze tables for query optimization
        database.execSQL("ANALYZE")
    }
    
    /**
     * Get database storage statistics
     */
    suspend fun getStorageStatistics(database: SupportSQLiteDatabase): DatabaseStorageStats {
        val cursor = database.query("PRAGMA page_count")
        val pageCount = if (cursor.moveToFirst()) cursor.getLong(0) else 0L
        cursor.close()
        
        val pageSizeCursor = database.query("PRAGMA page_size")
        val pageSize = if (pageSizeCursor.moveToFirst()) pageSizeCursor.getLong(0) else 4096L
        pageSizeCursor.close()
        
        val totalSize = pageCount * pageSize
        
        // Get table sizes
        val tableSizes = mutableMapOf<String, Long>()
        val tablesCursor = database.query("""
            SELECT name FROM sqlite_master 
            WHERE type='table' AND name NOT LIKE 'sqlite_%'
        """)
        
        while (tablesCursor.moveToNext()) {
            val tableName = tablesCursor.getString(0)
            val sizeCursor = database.query("SELECT COUNT(*) FROM $tableName")
            val rowCount = if (sizeCursor.moveToFirst()) sizeCursor.getLong(0) else 0L
            sizeCursor.close()
            tableSizes[tableName] = rowCount
        }
        tablesCursor.close()
        
        return DatabaseStorageStats(
            totalSizeBytes = totalSize,
            pageCount = pageCount,
            pageSize = pageSize,
            tableSizes = tableSizes
        )
    }
}

/**
 * Query optimization utilities
 */
object QueryOptimizer {
    
    /**
     * Get optimized fowl query with proper joins
     */
    fun getOptimizedFowlQuery(
        region: String? = null,
        district: String? = null,
        ownerId: String? = null,
        breed: String? = null,
        limit: Int = 50
    ): String {
        val conditions = mutableListOf<String>()
        conditions.add("f.is_deleted = 0")
        
        region?.let { conditions.add("f.region = '$it'") }
        district?.let { conditions.add("f.district = '$it'") }
        ownerId?.let { conditions.add("f.owner_id = '$it'") }
        breed?.let { conditions.add("f.breed_primary = '$it'") }
        
        return """
            SELECT 
                f.id,
                f.owner_id,
                f.name,
                f.breed_primary,
                f.gender,
                f.age_category,
                f.weight,
                f.health_status,
                f.availability_status,
                f.primary_photo,
                f.region,
                f.district,
                u.display_name as owner_name,
                u.user_tier as owner_tier,
                u.rating as owner_rating
            FROM fowls f
            INNER JOIN users u ON f.owner_id = u.id
            WHERE ${conditions.joinToString(" AND ")}
            ORDER BY f.created_at DESC
            LIMIT $limit
        """.trimIndent()
    }
    
    /**
     * Get optimized marketplace query with seller info
     */
    fun getOptimizedMarketplaceQuery(
        region: String? = null,
        district: String? = null,
        breed: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        limit: Int = 50
    ): String {
        val conditions = mutableListOf<String>()
        conditions.add("m.is_deleted = 0")
        conditions.add("m.listing_status = 'ACTIVE'")
        conditions.add("(m.expires_at IS NULL OR m.expires_at > ${System.currentTimeMillis()})")
        
        region?.let { conditions.add("m.region = '$it'") }
        district?.let { conditions.add("m.district = '$it'") }
        breed?.let { conditions.add("m.breed = '$it'") }
        minPrice?.let { conditions.add("m.base_price >= $it") }
        maxPrice?.let { conditions.add("m.base_price <= $it") }
        
        return """
            SELECT 
                m.id,
                m.seller_id,
                m.fowl_id,
                m.title,
                m.description,
                m.listing_type,
                m.base_price,
                m.current_bid,
                m.breed,
                m.gender,
                m.age,
                m.weight,
                m.primary_photo_url,
                m.delivery_available,
                m.views,
                m.favorites,
                m.region,
                m.district,
                u.display_name as seller_name,
                u.user_tier as seller_tier,
                u.rating as seller_rating,
                f.health_status as fowl_health_status
            FROM marketplace_listings m
            INNER JOIN users u ON m.seller_id = u.id
            INNER JOIN fowls f ON m.fowl_id = f.id
            WHERE ${conditions.joinToString(" AND ")}
            ORDER BY m.created_at DESC
            LIMIT $limit
        """.trimIndent()
    }
    
    /**
     * Get optimized transfer query with user and fowl info
     */
    fun getOptimizedTransferQuery(
        userId: String? = null,
        status: String? = null,
        limit: Int = 50
    ): String {
        val conditions = mutableListOf<String>()
        conditions.add("t.is_deleted = 0")
        
        userId?.let { 
            conditions.add("(t.from_user_id = '$it' OR t.to_user_id = '$it')") 
        }
        status?.let { conditions.add("t.transfer_status = '$it'") }
        
        return """
            SELECT 
                t.id,
                t.fowl_id,
                t.from_user_id,
                t.to_user_id,
                t.transfer_type,
                t.transfer_status,
                t.amount,
                t.payment_status,
                t.verification_status,
                t.initiated_at,
                t.completed_at,
                from_user.display_name as from_user_name,
                to_user.display_name as to_user_name,
                f.name as fowl_name,
                f.breed_primary as fowl_breed,
                f.primary_photo as fowl_photo
            FROM transfers t
            INNER JOIN users from_user ON t.from_user_id = from_user.id
            INNER JOIN users to_user ON t.to_user_id = to_user.id
            INNER JOIN fowls f ON t.fowl_id = f.id
            WHERE ${conditions.joinToString(" AND ")}
            ORDER BY t.initiated_at DESC
            LIMIT $limit
        """.trimIndent()
    }
}

/**
 * Database storage statistics
 */
data class DatabaseStorageStats(
    val totalSizeBytes: Long,
    val pageCount: Long,
    val pageSize: Long,
    val tableSizes: Map<String, Long>
) {
    val totalSizeMB: Double
        get() = totalSizeBytes / (1024.0 * 1024.0)
    
    fun getLargestTables(count: Int = 5): List<Pair<String, Long>> {
        return tableSizes.toList()
            .sortedByDescending { it.second }
            .take(count)
    }
}

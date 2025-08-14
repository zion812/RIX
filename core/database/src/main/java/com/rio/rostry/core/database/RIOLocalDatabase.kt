package com.rio.rostry.core.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rio.rostry.core.database.entities.*
import com.rio.rostry.core.database.dao.*
import com.rio.rostry.core.database.converters.*
import java.util.*

/**
 * Local Room database for offline-first data synchronization
 * Comprehensive database supporting all RIO platform entities
 */
@Database(
    entities = [
        // Core entities
        UserEntity::class,
        FowlEntity::class,
        MarketplaceEntity::class,
        MessageEntity::class,
        ConversationEntity::class,
        TransferEntity::class,
        // BreedingRecordEntity::class, // Removed - not implemented

        // Coin payment system entities
        CoinTransactionEntity::class,
        CoinOrderEntity::class,
        RefundRequestEntity::class,
        DisputeEntity::class,
        FraudCheckEntity::class,
        SuspiciousActivityEntity::class,
        CoinPackageEntity::class,
        UserCoinBalanceEntity::class,

        // Sync and cache entities
        // SyncQueueEntity::class, // Removed - not implemented
        OfflineActionEntity::class,

        // Supporting entities
        // MediaEntity::class, // Removed - not implemented
        NotificationEntity::class,
        NotificationPreferenceEntity::class,
        TopicSubscriptionEntity::class,
        NotificationAnalyticsEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(
    DateConverter::class,
    SyncMetadataConverter::class,
    TransactionStatusConverter::class,
    TransactionTypeConverter::class,
    SyncStatusConverter::class,
    SyncPriorityConverter::class,
    StringListConverter::class
)
abstract class RIOLocalDatabase : RoomDatabase() {

    // Core DAOs
    abstract fun userDao(): UserDao
    abstract fun fowlDao(): FowlDao
    abstract fun marketplaceDao(): MarketplaceDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun transferDao(): TransferDao
    // abstract fun breedingRecordDao(): BreedingRecordDao // Removed - not implemented

    // Sync and cache DAOs
    // abstract fun syncQueueDao(): SyncQueueDao // Removed - not implemented
    abstract fun offlineActionDao(): OfflineActionDao

    // Coin payment system DAOs
    abstract fun coinTransactionDao(): CoinTransactionDao
    abstract fun coinOrderDao(): CoinOrderDao
    abstract fun refundRequestDao(): RefundRequestDao
    abstract fun disputeDao(): DisputeDao
    abstract fun userCoinBalanceDao(): UserCoinBalanceDao
    abstract fun coinPackageDao(): CoinPackageDao

    // Supporting DAOs
    // abstract fun mediaDao(): MediaDao // Removed - not implemented
    abstract fun notificationDao(): NotificationDao
    abstract fun notificationPreferenceDao(): NotificationPreferenceDao
    abstract fun topicSubscriptionDao(): TopicSubscriptionDao
    abstract fun notificationAnalyticsDao(): NotificationAnalyticsDao

    companion object {
        /**
         * Migration from version 1 to 2 - Adding coin payment system tables
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create coin_transactions table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS coin_transactions (
                        id TEXT PRIMARY KEY NOT NULL,
                        user_id TEXT NOT NULL,
                        transaction_type TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        purpose TEXT NOT NULL,
                        status TEXT NOT NULL,
                        order_id TEXT,
                        payment_id TEXT,
                        balance_before INTEGER NOT NULL DEFAULT 0,
                        balance_after INTEGER NOT NULL DEFAULT 0,
                        metadata TEXT,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        synced_at INTEGER,
                        is_synced INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                // Create indexes for coin_transactions
                database.execSQL("CREATE INDEX IF NOT EXISTS index_coin_transactions_user_id_created_at ON coin_transactions(user_id, created_at)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_coin_transactions_transaction_type_status ON coin_transactions(transaction_type, status)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_coin_transactions_purpose_created_at ON coin_transactions(purpose, created_at)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_coin_transactions_is_synced_created_at ON coin_transactions(is_synced, created_at)")

                // Create coin_orders table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS coin_orders (
                        id TEXT PRIMARY KEY NOT NULL,
                        order_id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        package_id TEXT NOT NULL,
                        coins INTEGER NOT NULL,
                        bonus_coins INTEGER NOT NULL,
                        total_coins INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        currency TEXT NOT NULL DEFAULT 'INR',
                        status TEXT NOT NULL,
                        payment_method TEXT NOT NULL,
                        payment_id TEXT,
                        user_tier TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        expires_at INTEGER,
                        completed_at INTEGER
                    )
                """.trimIndent())

                // Create indexes for coin_orders
                database.execSQL("CREATE INDEX IF NOT EXISTS index_coin_orders_user_id_status ON coin_orders(user_id, status)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_coin_orders_order_id ON coin_orders(order_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_coin_orders_created_at ON coin_orders(created_at)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_coin_orders_status_expires_at ON coin_orders(status, expires_at)")

                // Create user_coin_balances table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_coin_balances (
                        user_id TEXT PRIMARY KEY NOT NULL,
                        balance INTEGER NOT NULL,
                        total_earned INTEGER NOT NULL DEFAULT 0,
                        total_spent INTEGER NOT NULL DEFAULT 0,
                        total_purchased INTEGER NOT NULL DEFAULT 0,
                        last_transaction_id TEXT,
                        last_updated INTEGER NOT NULL,
                        synced_at INTEGER,
                        is_synced INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                // Create indexes for user_coin_balances
                database.execSQL("CREATE INDEX IF NOT EXISTS index_user_coin_balances_last_updated ON user_coin_balances(last_updated)")

                // Create coin_packages table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS coin_packages (
                        id TEXT PRIMARY KEY NOT NULL,
                        package_id TEXT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        coins INTEGER NOT NULL,
                        bonus_coins INTEGER NOT NULL,
                        total_coins INTEGER NOT NULL,
                        price REAL NOT NULL,
                        user_tier TEXT NOT NULL,
                        active INTEGER NOT NULL DEFAULT 1,
                        featured INTEGER NOT NULL DEFAULT 0,
                        discount_percentage INTEGER NOT NULL DEFAULT 0,
                        valid_from INTEGER,
                        valid_until INTEGER,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                """.trimIndent())

                // Create indexes for coin_packages
                database.execSQL("CREATE INDEX IF NOT EXISTS index_coin_packages_user_tier_active ON coin_packages(user_tier, active)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_coin_packages_package_id ON coin_packages(package_id)")

                // Create refund_requests table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS refund_requests (
                        id TEXT PRIMARY KEY NOT NULL,
                        order_id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        refund_type TEXT NOT NULL,
                        requested_amount REAL NOT NULL,
                        reason TEXT NOT NULL,
                        evidence TEXT,
                        status TEXT NOT NULL,
                        priority TEXT NOT NULL,
                        razorpay_refund_id TEXT,
                        processed_amount REAL,
                        failure_reason TEXT,
                        auto_process_eligible INTEGER NOT NULL DEFAULT 0,
                        created_at INTEGER NOT NULL,
                        processed_at INTEGER
                    )
                """.trimIndent())

                // Create indexes for refund_requests
                database.execSQL("CREATE INDEX IF NOT EXISTS index_refund_requests_user_id_status ON refund_requests(user_id, status)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_refund_requests_order_id ON refund_requests(order_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_refund_requests_created_at ON refund_requests(created_at)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_refund_requests_status_priority ON refund_requests(status, priority)")

                // Create disputes table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS disputes (
                        id TEXT PRIMARY KEY NOT NULL,
                        transaction_id TEXT NOT NULL,
                        disputant_id TEXT NOT NULL,
                        respondent_id TEXT NOT NULL,
                        dispute_type TEXT NOT NULL,
                        reason TEXT NOT NULL,
                        evidence TEXT,
                        requested_resolution TEXT NOT NULL,
                        status TEXT NOT NULL,
                        priority TEXT NOT NULL,
                        escalation_level INTEGER NOT NULL DEFAULT 0,
                        mediator_id TEXT,
                        resolution_deadline INTEGER NOT NULL,
                        escrow_amount INTEGER NOT NULL,
                        escrow_status TEXT NOT NULL,
                        resolution TEXT,
                        reasoning TEXT,
                        resolver_id TEXT,
                        compensation_amount REAL,
                        created_at INTEGER NOT NULL,
                        escalated_at INTEGER,
                        resolved_at INTEGER
                    )
                """.trimIndent())

                // Create indexes for disputes
                database.execSQL("CREATE INDEX IF NOT EXISTS index_disputes_transaction_id ON disputes(transaction_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_disputes_disputant_id_status ON disputes(disputant_id, status)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_disputes_respondent_id_status ON disputes(respondent_id, status)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_disputes_status_created_at ON disputes(status, created_at)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_disputes_mediator_id_status ON disputes(mediator_id, status)")
            }
        }

        /**
         * Migration from version 2 to 3 - Adding notification system tables
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create notifications table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS notifications (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        body TEXT NOT NULL,
                        imageUrl TEXT,
                        category TEXT NOT NULL,
                        priority TEXT NOT NULL,
                        deepLink TEXT,
                        data TEXT NOT NULL,
                        isRead INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        receivedAt INTEGER NOT NULL,
                        readAt INTEGER,
                        syncedAt INTEGER,
                        isSynced INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                // Create indexes for notifications
                database.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_category_created_at ON notifications(category, createdAt)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_isRead_created_at ON notifications(isRead, createdAt)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_priority_created_at ON notifications(priority, createdAt)")

                // Create notification_preferences table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS notification_preferences (
                        userId TEXT PRIMARY KEY NOT NULL,
                        marketplaceNotifications INTEGER NOT NULL DEFAULT 1,
                        transferNotifications INTEGER NOT NULL DEFAULT 1,
                        communicationNotifications INTEGER NOT NULL DEFAULT 1,
                        breedingNotifications INTEGER NOT NULL DEFAULT 1,
                        paymentNotifications INTEGER NOT NULL DEFAULT 1,
                        systemNotifications INTEGER NOT NULL DEFAULT 1,
                        quietHoursEnabled INTEGER NOT NULL DEFAULT 0,
                        quietHoursStart TEXT NOT NULL DEFAULT '22:00',
                        quietHoursEnd TEXT NOT NULL DEFAULT '08:00',
                        soundEnabled INTEGER NOT NULL DEFAULT 1,
                        vibrationEnabled INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                // Create topic_subscriptions table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS topic_subscriptions (
                        id TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL,
                        topicName TEXT NOT NULL,
                        isSubscribed INTEGER NOT NULL DEFAULT 1,
                        subscribedAt INTEGER NOT NULL,
                        unsubscribedAt INTEGER,
                        syncedAt INTEGER,
                        isSynced INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                // Create indexes for topic_subscriptions
                database.execSQL("CREATE INDEX IF NOT EXISTS index_topic_subscriptions_userId_topicName ON topic_subscriptions(userId, topicName)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_topic_subscriptions_isSubscribed ON topic_subscriptions(isSubscribed)")

                // Create notification_analytics table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS notification_analytics (
                        id TEXT PRIMARY KEY NOT NULL,
                        notificationId TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        eventType TEXT NOT NULL,
                        eventData TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        syncedAt INTEGER,
                        isSynced INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                // Create indexes for notification_analytics
                database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_analytics_notificationId ON notification_analytics(notificationId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_analytics_userId_eventType ON notification_analytics(userId, eventType)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_analytics_timestamp ON notification_analytics(timestamp)")
            }
        }
    }
}

/**
 * Generic cache entity for storing any type of data
 */
@Entity(tableName = "cache")
data class CacheEntity(
    @PrimaryKey val key: String,
    val data: String,
    val priority: String,
    val createdAt: Date,
    val expiresAt: Date,
    val compressed: Boolean = false,
    val size: Long = 0
)

/**
 * Offline action entity for queuing operations
 */
@Entity(tableName = "offline_actions")
data class OfflineActionEntity(
    @PrimaryKey val id: String,
    val action: String, // CREATE, UPDATE, DELETE
    val collection: String,
    val documentId: String,
    val data: String,
    val timestamp: Date,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val status: String = "pending", // pending, synced, failed
    val lastRetryAt: Date? = null
)

/**
 * User cache entity for offline user data
 */
@Entity(tableName = "user_cache")
data class UserCacheEntity(
    @PrimaryKey val userId: String,
    val userData: String,
    val lastUpdated: Date,
    val syncStatus: String = "synced"
)

/**
 * Fowl cache entity for offline fowl data
 */
@Entity(tableName = "fowl_cache")
data class FowlCacheEntity(
    @PrimaryKey val fowlId: String,
    val ownerId: String,
    val fowlData: String,
    val lastUpdated: Date,
    val syncStatus: String = "synced",
    val priority: String = "medium"
)

/**
 * Marketplace cache entity for offline marketplace data
 */
@Entity(tableName = "marketplace_cache")
data class MarketplaceCacheEntity(
    @PrimaryKey val listingId: String,
    val region: String,
    val district: String,
    val listingData: String,
    val lastUpdated: Date,
    val expiresAt: Date,
    val syncStatus: String = "synced"
)

/**
 * Message cache entity for offline messaging
 */
@Entity(tableName = "message_cache")
data class MessageCacheEntity(
    @PrimaryKey val messageId: String,
    val conversationId: String,
    val messageData: String,
    val lastUpdated: Date,
    val syncStatus: String = "synced",
    val deliveryStatus: String = "pending"
)

/**
 * Generic cache DAO
 */
@Dao
interface CacheDao {
    
    @Query("SELECT * FROM cache WHERE key = :key")
    suspend fun get(key: String): CacheEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cacheEntity: CacheEntity)
    
    @Query("DELETE FROM cache WHERE key = :key")
    suspend fun delete(key: String)
    
    @Query("DELETE FROM cache WHERE expiresAt < :currentTime")
    suspend fun deleteExpired(currentTime: Long = System.currentTimeMillis())
    
    @Query("SELECT SUM(size) FROM cache")
    suspend fun getTotalSize(): Long
    
    @Query("DELETE FROM cache WHERE priority = 'LOW' LIMIT :limit")
    suspend fun deleteLowPriorityItems(limit: Int)
    
    @Query("SELECT COUNT(*) FROM cache")
    suspend fun getCount(): Int
}

/**
 * Offline action DAO
 */
@Dao
interface OfflineActionDao {
    
    @Query("SELECT * FROM offline_actions WHERE status = 'pending' ORDER BY timestamp ASC")
    suspend fun getAllPending(): List<OfflineActionEntity>
    
    @Query("SELECT COUNT(*) FROM offline_actions WHERE status = 'pending'")
    suspend fun getPendingCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: OfflineActionEntity)
    
    @Update
    suspend fun update(action: OfflineActionEntity)
    
    @Query("UPDATE offline_actions SET status = 'synced' WHERE id = :id")
    suspend fun markAsSynced(id: String)
    
    @Query("UPDATE offline_actions SET status = 'failed' WHERE id = :id")
    suspend fun markAsFailed(id: String)
    
    @Query("DELETE FROM offline_actions WHERE status = 'synced' AND timestamp < :cutoffTime")
    suspend fun cleanupSynced(cutoffTime: Long)
}

/**
 * User cache DAO
 */
@Dao
interface UserCacheDao {
    
    @Query("SELECT * FROM user_cache WHERE userId = :userId")
    suspend fun getUser(userId: String): UserCacheEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserCacheEntity)
    
    @Query("DELETE FROM user_cache WHERE userId = :userId")
    suspend fun deleteUser(userId: String)
    
    @Query("SELECT * FROM user_cache WHERE syncStatus != 'synced'")
    suspend fun getUnsyncedUsers(): List<UserCacheEntity>
}

/**
 * Fowl cache DAO
 */
@Dao
interface FowlCacheDao {
    
    @Query("SELECT * FROM fowl_cache WHERE fowlId = :fowlId")
    suspend fun getFowl(fowlId: String): FowlCacheEntity?
    
    @Query("SELECT * FROM fowl_cache WHERE ownerId = :ownerId ORDER BY lastUpdated DESC")
    suspend fun getFowlsByOwner(ownerId: String): List<FowlCacheEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFowl(fowl: FowlCacheEntity)
    
    @Query("DELETE FROM fowl_cache WHERE fowlId = :fowlId")
    suspend fun deleteFowl(fowlId: String)
    
    @Query("SELECT * FROM fowl_cache WHERE syncStatus != 'synced'")
    suspend fun getUnsyncedFowls(): List<FowlCacheEntity>
    
    @Query("SELECT * FROM fowl_cache WHERE priority = 'high' ORDER BY lastUpdated DESC")
    suspend fun getHighPriorityFowls(): List<FowlCacheEntity>
}

/**
 * Marketplace cache DAO
 */
@Dao
interface MarketplaceCacheDao {
    
    @Query("SELECT * FROM marketplace_cache WHERE listingId = :listingId")
    suspend fun getListing(listingId: String): MarketplaceCacheEntity?
    
    @Query("SELECT * FROM marketplace_cache WHERE region = :region AND district = :district AND expiresAt > :currentTime ORDER BY lastUpdated DESC")
    suspend fun getRegionalListings(region: String, district: String, currentTime: Long = System.currentTimeMillis()): List<MarketplaceCacheEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: MarketplaceCacheEntity)
    
    @Query("DELETE FROM marketplace_cache WHERE listingId = :listingId")
    suspend fun deleteListing(listingId: String)
    
    @Query("DELETE FROM marketplace_cache WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredListings(currentTime: Long = System.currentTimeMillis())
    
    @Query("SELECT * FROM marketplace_cache WHERE syncStatus != 'synced'")
    suspend fun getUnsyncedListings(): List<MarketplaceCacheEntity>
}

/**
 * Message cache DAO
 */
@Dao
interface MessageCacheDao {
    
    @Query("SELECT * FROM message_cache WHERE messageId = :messageId")
    suspend fun getMessage(messageId: String): MessageCacheEntity?
    
    @Query("SELECT * FROM message_cache WHERE conversationId = :conversationId ORDER BY lastUpdated DESC LIMIT :limit")
    suspend fun getConversationMessages(conversationId: String, limit: Int = 50): List<MessageCacheEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageCacheEntity)
    
    @Query("DELETE FROM message_cache WHERE messageId = :messageId")
    suspend fun deleteMessage(messageId: String)
    
    @Query("SELECT * FROM message_cache WHERE syncStatus != 'synced' OR deliveryStatus = 'pending'")
    suspend fun getUnsyncedMessages(): List<MessageCacheEntity>
    
    @Query("UPDATE message_cache SET deliveryStatus = :status WHERE messageId = :messageId")
    suspend fun updateDeliveryStatus(messageId: String, status: String)
}

/**
 * Type converters for Room database
 */
class Converters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

/**
 * Database migration strategies
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new columns or tables for future versions
        database.execSQL("ALTER TABLE cache ADD COLUMN compressed INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * Database builder with optimizations for rural networks
 */
object DatabaseBuilder {
    
    fun build(context: android.content.Context): RIOLocalDatabase {
        return Room.databaseBuilder(
            context,
            RIOLocalDatabase::class.java,
            "rio_local_database"
        )
        .addMigrations(MIGRATION_1_2)
        .fallbackToDestructiveMigration() // For development only
        .enableMultiInstanceInvalidation()
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING) // Better performance
        .build()
    }
}

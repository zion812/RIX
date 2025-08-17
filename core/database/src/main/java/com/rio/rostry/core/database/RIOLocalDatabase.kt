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
        FowlRecordEntity::class, // Add our new FowlRecordEntity
        TransferLogEntity::class, // Add TransferLogEntity for verified transfer workflow

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
        OfflineActionEntity::class,

        // Supporting entities
        NotificationEntity::class,
        NotificationPreferenceEntity::class,
        TopicSubscriptionEntity::class,
        NotificationAnalyticsEntity::class,
        TimelineEntity::class
    ],
    version = 3, // Update version since we're adding a new entity
    exportSchema = true
)
@TypeConverters(Converters::class, FowlConverters::class) // Add our new converters
abstract class RIOLocalDatabase : RoomDatabase() {

    // Core DAOs
    abstract fun userDao(): UserDao
    abstract fun fowlDao(): FowlDaoV2
    abstract fun marketplaceDao(): com.rio.rostry.core.database.dao.MarketplaceDao
    abstract fun messageDao(): MessageDaoV2
    abstract fun conversationDao(): ConversationDaoV2
    abstract fun transferDao(): TransferDaoV2
    abstract fun transferLogDao(): TransferLogDao // Add TransferLogDao for verified transfer workflow
    abstract fun outboxDao(): OutboxDaoV2
    abstract fun fowlRecordDao(): FowlRecordDao // Add our new DAO

    // Sync and cache DAOs
    // abstract fun syncQueueDao(): SyncQueueDao // Removed - not implemented
    abstract fun cacheDao(): CacheDao

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
    abstract fun timelineDao(): TimelineDao // Add TimelineDao

    companion object {
        const val DATABASE_NAME = "rio_database"
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
    val size: Long = 0,
    @Embedded override val syncMetadata: com.rio.rostry.core.database.entities.SyncMetadata = com.rio.rostry.core.database.entities.SyncMetadata()
) : com.rio.rostry.core.database.entities.SyncableEntity {
    override val id: String
        get() = key
}

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
interface CacheDao : BaseSyncableDao<CacheEntity>

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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Migration logic from version 2 to 3 would go here
        // For example, creating a new table for TransferLogEntity
        database.execSQL("CREATE TABLE IF NOT EXISTS TransferLogEntity (id TEXT PRIMARY KEY, transferId TEXT, status TEXT, timestamp INTEGER)")
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
        .addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3 // Add our new migration
        )
        .fallbackToDestructiveMigration() // For development only
        .enableMultiInstanceInvalidation()
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING) // Better performance
        .build()
    }
}

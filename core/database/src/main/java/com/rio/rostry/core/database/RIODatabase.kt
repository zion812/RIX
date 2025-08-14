package com.rio.rostry.core.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rio.rostry.core.database.dao.*
import com.rio.rostry.core.database.entities.*
import com.rio.rostry.core.database.converters.*
import java.util.*

/**
 * Main Room database for RIO platform
 * Handles offline-first data storage with sync capabilities
 */
@Database(
    entities = [
        UserEntity::class,
        CoinTransactionEntity::class,
        UserCoinBalanceEntity::class,
        FowlEntity::class,
    MarketplaceEntity::class,
        TransferEntity::class,
        MessageEntity::class,
        NotificationEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    DateConverter::class,
    SyncMetadataConverter::class,
    TransactionStatusConverter::class,
    TransactionTypeConverter::class,
    StringListConverter::class
)
abstract class RIODatabase : RoomDatabase() {
    
    // DAOs
    abstract fun userDao(): UserDao
    abstract fun coinDao(): CoinDao
    abstract fun fowlDao(): FowlDao
    abstract fun marketplaceDao(): MarketplaceDao
    abstract fun transferDao(): TransferDao
    abstract fun messageDao(): MessageDao
    abstract fun notificationDao(): NotificationDao
    abstract fun syncDao(): SyncDao
    
    companion object {
        const val DATABASE_NAME = "rio_database"
        
        /**
         * Migration from version 1 to 2 (when needed)
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add migration logic when needed
            }
        }
    }
}

/**
 * Additional entities for complete functionality
 */

@Entity(tableName = "fowls")
data class FowlEntity(
    @PrimaryKey
    override val id: String,
    val name: String,
    val breed: String,
    val ownerId: String,
    val parentMaleId: String? = null,
    val parentFemaleId: String? = null,
    val birthDate: Date? = null,
    val gender: String,
    val color: String? = null,
    val weight: Double? = null,
    val height: Double? = null,
    val description: String? = null,
    val imageUrls: List<String> = emptyList(),
    val videoUrls: List<String> = emptyList(),
    val isForSale: Boolean = false,
    val priceInCoins: Int? = null,
    val location: String? = null,
    val healthStatus: String = "healthy",
    val vaccinationRecords: List<String> = emptyList(),
    val achievements: List<String> = emptyList(),
    val createdAt: Date,
    val updatedAt: Date = Date(),
    val isSynced: Boolean = false,
    @Embedded
    override val syncMetadata: SyncMetadata = SyncMetadata()
) : SyncableEntity

@Entity(tableName = "marketplace_listings")
data class MarketplaceListingEntity(
    @PrimaryKey
    override val id: String,
    val fowlId: String,
    val sellerId: String,
    val title: String,
    val description: String,
    val priceInCoins: Int,
    val priceInRupees: Int? = null,
    val category: String,
    val condition: String = "excellent",
    val location: String,
    val imageUrls: List<String> = emptyList(),
    val videoUrls: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val isActive: Boolean = true,
    val isFeatured: Boolean = false,
    val isPremium: Boolean = false,
    val viewCount: Int = 0,
    val favoriteCount: Int = 0,
    val inquiryCount: Int = 0,
    val expiresAt: Date? = null,
    val soldAt: Date? = null,
    val buyerId: String? = null,
    val createdAt: Date,
    val updatedAt: Date = Date(),
    val isSynced: Boolean = false,
    @Embedded
    override val syncMetadata: SyncMetadata = SyncMetadata()
) : SyncableEntity

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey
    override val id: String,
    val fowlId: String,
    val fromUserId: String,
    val toUserId: String,
    val transferType: String, // "sale", "gift", "breeding"
    val priceInCoins: Int? = null,
    val priceInRupees: Int? = null,
    val status: String = "pending", // "pending", "approved", "completed", "rejected"
    val reason: String? = null,
    val notes: String? = null,
    val proofDocuments: List<String> = emptyList(),
    val verificationRequired: Boolean = true,
    val verifiedBy: String? = null,
    val verifiedAt: Date? = null,
    val completedAt: Date? = null,
    val createdAt: Date,
    val updatedAt: Date = Date(),
    val isSynced: Boolean = false,
    @Embedded
    override val syncMetadata: SyncMetadata = SyncMetadata()
) : SyncableEntity

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    override val id: String,
    val conversationId: String,
    val senderId: String,
    val recipientId: String,
    val content: String,
    val messageType: String = "text", // "text", "image", "video", "audio", "document"
    val mediaUrl: String? = null,
    val mediaType: String? = null,
    val isRead: Boolean = false,
    val readAt: Date? = null,
    val isDelivered: Boolean = false,
    val deliveredAt: Date? = null,
    val replyToMessageId: String? = null,
    val isEdited: Boolean = false,
    val editedAt: Date? = null,
    val createdAt: Date,
    val updatedAt: Date = Date(),
    val isSynced: Boolean = false,
    @Embedded
    override val syncMetadata: SyncMetadata = SyncMetadata()
) : SyncableEntity

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    override val id: String,
    val userId: String,
    val type: String,
    val title: String,
    val message: String,
    val data: String? = null, // JSON data for action
    val imageUrl: String? = null,
    val actionUrl: String? = null,
    val isRead: Boolean = false,
    val readAt: Date? = null,
    val priority: String = "normal", // "low", "normal", "high", "urgent"
    val category: String = "general", // "general", "payment", "transfer", "marketplace", "system"
    val expiresAt: Date? = null,
    val createdAt: Date,
    val updatedAt: Date = Date(),
    val isSynced: Boolean = false,
    @Embedded
    override val syncMetadata: SyncMetadata = SyncMetadata()
) : SyncableEntity

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey
    override val id: String,
    val entityType: String,
    val entityId: String,
    val operation: String, // "CREATE", "UPDATE", "DELETE"
    val priority: SyncPriority,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val lastAttempt: Date? = null,
    val nextAttempt: Date? = null,
    val errorMessage: String? = null,
    val payload: String? = null, // JSON payload for the operation
    val createdAt: Date,
    val updatedAt: Date = Date(),
    @Embedded
    override val syncMetadata: SyncMetadata = SyncMetadata()
) : SyncableEntity
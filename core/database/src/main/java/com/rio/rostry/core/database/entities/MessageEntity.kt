package com.rio.rostry.core.database.entities

import androidx.room.*
import java.util.*

/**
 * Room entity for messages with offline-first capabilities
 * Mirrors the Firestore messages collection structure
 */
@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["conversation_id"]),
        Index(value = ["sender_id"]),
        Index(value = ["message_type"]),
        Index(value = ["sent_at"]),
        Index(value = ["sync_status"]),
        Index(value = ["delivery_status"]),
        Index(value = ["is_deleted"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["sender_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MessageEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    override val id: String,
    
    @ColumnInfo(name = "conversation_id")
    val conversationId: String,
    
    @ColumnInfo(name = "sender_id")
    val senderId: String,
    
    @ColumnInfo(name = "message_type")
    val messageType: String, // TEXT, IMAGE, VIDEO, AUDIO, FILE, LOCATION, FOWL_CARD, LISTING_CARD, CONTACT, SYSTEM
    
    // Message content based on type
    @ColumnInfo(name = "text_content")
    val textContent: String? = null,
    
    @ColumnInfo(name = "media_url")
    val mediaUrl: String? = null,
    
    @ColumnInfo(name = "media_caption")
    val mediaCaption: String? = null,
    
    @ColumnInfo(name = "media_thumbnail")
    val mediaThumbnail: String? = null,
    
    @ColumnInfo(name = "media_duration")
    val mediaDuration: Int? = null, // in seconds
    
    @ColumnInfo(name = "file_name")
    val fileName: String? = null,
    
    @ColumnInfo(name = "file_size")
    val fileSize: Long? = null,
    
    @ColumnInfo(name = "mime_type")
    val mimeType: String? = null,
    
    // Location content
    @ColumnInfo(name = "latitude")
    val latitude: Double? = null,
    
    @ColumnInfo(name = "longitude")
    val longitude: Double? = null,
    
    @ColumnInfo(name = "location_address")
    val locationAddress: String? = null,
    
    // Card content (fowl/listing)
    @ColumnInfo(name = "card_id")
    val cardId: String? = null,
    
    @ColumnInfo(name = "card_data")
    val cardData: String? = null, // JSON string
    
    // Contact content
    @ColumnInfo(name = "contact_name")
    val contactName: String? = null,
    
    @ColumnInfo(name = "contact_phone")
    val contactPhone: String? = null,
    
    @ColumnInfo(name = "contact_email")
    val contactEmail: String? = null,
    
    // System message data
    @ColumnInfo(name = "system_type")
    val systemType: String? = null,
    
    @ColumnInfo(name = "system_data")
    val systemData: String? = null, // JSON string
    
    // Message metadata
    @ColumnInfo(name = "reply_to_message_id")
    val replyToMessageId: String? = null,
    
    @ColumnInfo(name = "forwarded")
    val forwarded: Boolean = false,
    
    @ColumnInfo(name = "edited")
    val edited: Boolean = false,
    
    @ColumnInfo(name = "pinned")
    val pinned: Boolean = false,
    
    @ColumnInfo(name = "priority")
    val priority: String = "NORMAL", // LOW, NORMAL, HIGH, URGENT
    
    @ColumnInfo(name = "sent_at")
    val sentAt: Date,
    
    @ColumnInfo(name = "edited_at")
    val editedAt: Date? = null,
    
    @ColumnInfo(name = "expires_at")
    val expiresAt: Date? = null,
    
    // Delivery status
    @ColumnInfo(name = "delivery_status")
    val deliveryStatus: String = "PENDING", // PENDING, SENT, DELIVERED, READ, FAILED
    
    @ColumnInfo(name = "delivered_at")
    val deliveredAt: Date? = null,
    
    @ColumnInfo(name = "read_at")
    val readAt: Date? = null,
    
    @ColumnInfo(name = "read_by")
    val readBy: List<String> = emptyList(), // List of user IDs who read the message
    
    // Reactions and mentions
    @ColumnInfo(name = "reactions")
    val reactions: Map<String, List<String>> = emptyMap(), // emoji -> list of user IDs
    
    @ColumnInfo(name = "mentions")
    val mentions: List<String> = emptyList(), // List of mentioned user IDs
    
    // Thread information
    @ColumnInfo(name = "thread_id")
    val threadId: String? = null,
    
    @ColumnInfo(name = "root_message_id")
    val rootMessageId: String? = null,
    
    @ColumnInfo(name = "reply_count")
    val replyCount: Int = 0,
    
    // Encryption and security
    @ColumnInfo(name = "encrypted")
    val encrypted: Boolean = false,
    
    @ColumnInfo(name = "encryption_key_id")
    val encryptionKeyId: String? = null,
    
    // Moderation
    @ColumnInfo(name = "flagged")
    val flagged: Boolean = false,
    
    @ColumnInfo(name = "flagged_by")
    val flaggedBy: List<String> = emptyList(),
    
    @ColumnInfo(name = "flag_reason")
    val flagReason: String? = null,
    
    @ColumnInfo(name = "moderation_status")
    val moderationStatus: String = "APPROVED", // PENDING, APPROVED, REJECTED, FLAGGED
    
    // Edit history
    @ColumnInfo(name = "edit_history")
    val editHistory: List<String> = emptyList(), // JSON strings of previous versions
    
    @ColumnInfo(name = "forward_history")
    val forwardHistory: List<String> = emptyList(), // JSON strings of forward chain
    
    // Local message state
    @ColumnInfo(name = "local_message_id")
    val localMessageId: String? = null, // Temporary ID for offline messages
    
    @ColumnInfo(name = "upload_progress")
    val uploadProgress: Int = 100, // 0-100 for media uploads
    
    @ColumnInfo(name = "download_progress")
    val downloadProgress: Int = 100, // 0-100 for media downloads
    
    @ColumnInfo(name = "local_media_path")
    val localMediaPath: String? = null,
    
    // Sync metadata
    @Embedded
    val syncMetadata: SyncMetadata,
    
    // Conflict metadata
    @Embedded
    val conflictMetadata: ConflictMetadata = ConflictMetadata()
) : SyncableEntity {
    
    override val lastSyncTime: Date?
        get() = syncMetadata.lastSyncTime
    
    override val syncStatus: SyncStatus
        get() = syncMetadata.syncStatus
    
    override val conflictVersion: Long
        get() = syncMetadata.conflictVersion
    
    override val isDeleted: Boolean
        get() = syncMetadata.isDeleted
    
    override val createdAt: Date
        get() = syncMetadata.createdAt
    
    override val updatedAt: Date
        get() = syncMetadata.updatedAt
}

/**
 * Room entity for conversations
 */
@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["conversation_type"]),
        Index(value = ["last_activity_at"]),
        Index(value = ["sync_status"]),
        Index(value = ["is_deleted"])
    ]
)
data class ConversationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    override val id: String,
    
    @ColumnInfo(name = "conversation_type")
    val conversationType: String, // DIRECT, GROUP, MARKETPLACE, BREEDING, SUPPORT
    
    @ColumnInfo(name = "title")
    val title: String? = null,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "participants")
    val participants: List<String> = emptyList(), // List of user IDs
    
    @ColumnInfo(name = "admins")
    val admins: List<String> = emptyList(),
    
    @ColumnInfo(name = "owner_id")
    val ownerId: String? = null,
    
    @ColumnInfo(name = "last_message_id")
    val lastMessageId: String? = null,
    
    @ColumnInfo(name = "last_activity_at")
    val lastActivityAt: Date,
    
    @ColumnInfo(name = "message_count")
    val messageCount: Int = 0,
    
    @ColumnInfo(name = "unread_count")
    val unreadCount: Int = 0,
    
    // Conversation settings
    @ColumnInfo(name = "is_public")
    val isPublic: Boolean = false,
    
    @ColumnInfo(name = "allow_invites")
    val allowInvites: Boolean = true,
    
    @ColumnInfo(name = "require_approval")
    val requireApproval: Boolean = false,
    
    @ColumnInfo(name = "mute_notifications")
    val muteNotifications: Boolean = false,
    
    @ColumnInfo(name = "muted_until")
    val mutedUntil: Date? = null,
    
    // Related entities
    @ColumnInfo(name = "related_listing_id")
    val relatedListingId: String? = null,
    
    @ColumnInfo(name = "related_fowl_id")
    val relatedFowlId: String? = null,
    
    @ColumnInfo(name = "related_transfer_id")
    val relatedTransferId: String? = null,
    
    // Sync metadata
    @Embedded
    val syncMetadata: SyncMetadata,
    
    // Conflict metadata
    @Embedded
    val conflictMetadata: ConflictMetadata = ConflictMetadata()
) : SyncableEntity {
    
    override val lastSyncTime: Date?
        get() = syncMetadata.lastSyncTime
    
    override val syncStatus: SyncStatus
        get() = syncMetadata.syncStatus
    
    override val conflictVersion: Long
        get() = syncMetadata.conflictVersion
    
    override val isDeleted: Boolean
        get() = syncMetadata.isDeleted
    
    override val createdAt: Date
        get() = syncMetadata.createdAt
    
    override val updatedAt: Date
        get() = syncMetadata.updatedAt
}

/**
 * DAO for message entities
 */
@Dao
interface MessageDao : BaseSyncableDao<MessageEntity> {
    
    @Query("SELECT * FROM messages WHERE id = :id AND is_deleted = 0")
    override suspend fun getById(id: String): MessageEntity?
    
    @Query("SELECT * FROM messages WHERE sync_status = :status AND is_deleted = 0")
    override suspend fun getAllByStatus(status: SyncStatus): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE sync_priority = :priority AND is_deleted = 0")
    override suspend fun getAllByPriority(priority: SyncPriority): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE sync_status IN ('PENDING_UPLOAD', 'FAILED') AND is_deleted = 0 ORDER BY sync_priority ASC, sent_at ASC")
    override suspend fun getAllPendingSync(): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE has_conflict = 1 AND is_deleted = 0")
    override suspend fun getAllConflicted(): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId AND is_deleted = 0 ORDER BY sent_at DESC LIMIT :limit")
    suspend fun getMessagesByConversation(conversationId: String, limit: Int = 50): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId AND sent_at < :beforeTime AND is_deleted = 0 ORDER BY sent_at DESC LIMIT :limit")
    suspend fun getMessagesBefore(conversationId: String, beforeTime: Date, limit: Int = 50): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE delivery_status = 'PENDING' AND is_deleted = 0")
    suspend fun getPendingDeliveryMessages(): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE delivery_status = 'FAILED' AND retry_count < 3 AND is_deleted = 0")
    suspend fun getFailedMessages(): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE local_message_id = :localId AND is_deleted = 0")
    suspend fun getMessageByLocalId(localId: String): MessageEntity?
    
    @Query("SELECT * FROM messages WHERE (text_content LIKE '%' || :query || '%' OR media_caption LIKE '%' || :query || '%') AND conversation_id = :conversationId AND is_deleted = 0 ORDER BY sent_at DESC LIMIT :limit")
    suspend fun searchMessagesInConversation(query: String, conversationId: String, limit: Int = 50): List<MessageEntity>
    
    @Query("UPDATE messages SET delivery_status = :status, delivered_at = :deliveredAt WHERE id = :id")
    suspend fun updateDeliveryStatus(id: String, status: String, deliveredAt: Date = Date())
    
    @Query("UPDATE messages SET read_at = :readAt, delivery_status = 'READ' WHERE id = :id")
    suspend fun markAsRead(id: String, readAt: Date = Date())
    
    @Query("UPDATE messages SET reactions = :reactions WHERE id = :id")
    suspend fun updateReactions(id: String, reactions: Map<String, List<String>>)
    
    @Query("UPDATE messages SET upload_progress = :progress WHERE id = :id")
    suspend fun updateUploadProgress(id: String, progress: Int)
    
    @Query("UPDATE messages SET download_progress = :progress, local_media_path = :localPath WHERE id = :id")
    suspend fun updateDownloadProgress(id: String, progress: Int, localPath: String? = null)
    
    // Sync operations
    @Query("UPDATE messages SET sync_status = :status, last_sync_time = :lastSyncTime WHERE id = :id")
    override suspend fun updateSyncStatus(id: String, status: SyncStatus, lastSyncTime: Date)
    
    @Query("UPDATE messages SET conflict_version = :version WHERE id = :id")
    override suspend fun updateConflictVersion(id: String, version: Long)
    
    @Query("UPDATE messages SET is_deleted = 1, updated_at = :deletedAt WHERE id = :id")
    override suspend fun markAsDeleted(id: String)
    
    @Query("UPDATE messages SET retry_count = retry_count + 1 WHERE id = :id")
    override suspend fun incrementRetryCount(id: String)
    
    @Query("UPDATE messages SET retry_count = 0 WHERE id = :id")
    override suspend fun clearRetryCount(id: String)
    
    // Cleanup operations
    @Query("DELETE FROM messages WHERE sync_status = 'SYNCED' AND updated_at < :olderThan AND sync_priority = 'LOW'")
    override suspend fun deleteOldSyncedItems(olderThan: Date): Int
    
    @Query("DELETE FROM messages WHERE sync_priority = 'LOW' AND sync_status = 'SYNCED' ORDER BY last_sync_time ASC LIMIT :limit")
    override suspend fun deleteLowPriorityItems(limit: Int): Int
    
    @Query("SELECT SUM(data_size) FROM messages")
    override suspend fun getStorageSize(): Long
    
    // Base operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(entity: MessageEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertAll(entities: List<MessageEntity>): List<Long>
    
    @Update
    override suspend fun update(entity: MessageEntity): Int
    
    @Delete
    override suspend fun delete(entity: MessageEntity): Int
    
    @Query("DELETE FROM messages WHERE id = :id")
    override suspend fun deleteById(id: String): Int
    
    // Not applicable for messages
    override suspend fun getAllInRegion(region: String, district: String): List<MessageEntity> = emptyList()
}

/**
 * DAO for conversation entities
 */
@Dao
interface ConversationDao : BaseSyncableDao<ConversationEntity> {
    
    @Query("SELECT * FROM conversations WHERE id = :id AND is_deleted = 0")
    override suspend fun getById(id: String): ConversationEntity?
    
    @Query("SELECT * FROM conversations WHERE sync_status = :status AND is_deleted = 0")
    override suspend fun getAllByStatus(status: SyncStatus): List<ConversationEntity>
    
    @Query("SELECT * FROM conversations WHERE sync_priority = :priority AND is_deleted = 0")
    override suspend fun getAllByPriority(priority: SyncPriority): List<ConversationEntity>
    
    @Query("SELECT * FROM conversations WHERE sync_status IN ('PENDING_UPLOAD', 'FAILED') AND is_deleted = 0 ORDER BY sync_priority ASC, created_at ASC")
    override suspend fun getAllPendingSync(): List<ConversationEntity>
    
    @Query("SELECT * FROM conversations WHERE has_conflict = 1 AND is_deleted = 0")
    override suspend fun getAllConflicted(): List<ConversationEntity>
    
    @Query("SELECT * FROM conversations WHERE participants LIKE '%' || :userId || '%' AND is_deleted = 0 ORDER BY last_activity_at DESC")
    suspend fun getConversationsByUser(userId: String): List<ConversationEntity>
    
    @Query("SELECT * FROM conversations WHERE conversation_type = :type AND participants LIKE '%' || :userId || '%' AND is_deleted = 0 ORDER BY last_activity_at DESC")
    suspend fun getConversationsByTypeAndUser(type: String, userId: String): List<ConversationEntity>
    
    @Query("UPDATE conversations SET unread_count = :count WHERE id = :id")
    suspend fun updateUnreadCount(id: String, count: Int)
    
    @Query("UPDATE conversations SET last_message_id = :messageId, last_activity_at = :activityTime, message_count = message_count + 1 WHERE id = :id")
    suspend fun updateLastMessage(id: String, messageId: String, activityTime: Date = Date())
    
    // Sync operations
    @Query("UPDATE conversations SET sync_status = :status, last_sync_time = :lastSyncTime WHERE id = :id")
    override suspend fun updateSyncStatus(id: String, status: SyncStatus, lastSyncTime: Date)
    
    @Query("UPDATE conversations SET conflict_version = :version WHERE id = :id")
    override suspend fun updateConflictVersion(id: String, version: Long)
    
    @Query("UPDATE conversations SET is_deleted = 1, updated_at = :deletedAt WHERE id = :id")
    override suspend fun markAsDeleted(id: String)
    
    @Query("UPDATE conversations SET retry_count = retry_count + 1 WHERE id = :id")
    override suspend fun incrementRetryCount(id: String)
    
    @Query("UPDATE conversations SET retry_count = 0 WHERE id = :id")
    override suspend fun clearRetryCount(id: String)
    
    // Cleanup operations
    @Query("DELETE FROM conversations WHERE sync_status = 'SYNCED' AND updated_at < :olderThan AND sync_priority = 'LOW'")
    override suspend fun deleteOldSyncedItems(olderThan: Date): Int
    
    @Query("DELETE FROM conversations WHERE sync_priority = 'LOW' AND sync_status = 'SYNCED' ORDER BY last_sync_time ASC LIMIT :limit")
    override suspend fun deleteLowPriorityItems(limit: Int): Int
    
    @Query("SELECT SUM(data_size) FROM conversations")
    override suspend fun getStorageSize(): Long
    
    // Base operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(entity: ConversationEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertAll(entities: List<ConversationEntity>): List<Long>
    
    @Update
    override suspend fun update(entity: ConversationEntity): Int
    
    @Delete
    override suspend fun delete(entity: ConversationEntity): Int
    
    @Query("DELETE FROM conversations WHERE id = :id")
    override suspend fun deleteById(id: String): Int
    
    // Not applicable for conversations
    override suspend fun getAllInRegion(region: String, district: String): List<ConversationEntity> = emptyList()
}

package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.MessageEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Data Access Object for message-related operations
 */
@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun getMessagesByConversation(conversationId: String): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE (senderId = :userId OR recipientId = :userId) ORDER BY createdAt DESC")
    fun getMessagesByUser(userId: String): Flow<List<MessageEntity>>
    
    @Query("SELECT DISTINCT conversationId FROM messages WHERE senderId = :userId OR recipientId = :userId ORDER BY createdAt DESC")
    suspend fun getConversationsByUser(userId: String): List<String>
    
    @Query("SELECT * FROM messages WHERE recipientId = :userId AND isRead = 0 ORDER BY createdAt DESC")
    suspend fun getUnreadMessages(userId: String): List<MessageEntity>
    
    @Query("SELECT COUNT(*) FROM messages WHERE recipientId = :userId AND isRead = 0")
    suspend fun getUnreadMessageCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM messages WHERE recipientId = :userId AND isRead = 0")
    fun observeUnreadMessageCount(userId: String): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
    
    @Update
    suspend fun updateMessage(message: MessageEntity)
    
    @Query("UPDATE messages SET isRead = 1, readAt = :readAt, updatedAt = :updatedAt WHERE id = :messageId")
    suspend fun markAsRead(messageId: String, readAt: Date = Date(), updatedAt: Date = Date())
    
    @Query("UPDATE messages SET isRead = 1, readAt = :readAt, updatedAt = :updatedAt WHERE conversationId = :conversationId AND recipientId = :userId AND isRead = 0")
    suspend fun markConversationAsRead(conversationId: String, userId: String, readAt: Date = Date(), updatedAt: Date = Date())
    
    @Query("UPDATE messages SET isDelivered = 1, deliveredAt = :deliveredAt, updatedAt = :updatedAt WHERE id = :messageId")
    suspend fun markAsDelivered(messageId: String, deliveredAt: Date = Date(), updatedAt: Date = Date())
    
    @Query("UPDATE messages SET content = :newContent, isEdited = 1, editedAt = :editedAt, updatedAt = :updatedAt WHERE id = :messageId")
    suspend fun editMessage(messageId: String, newContent: String, editedAt: Date = Date(), updatedAt: Date = Date())
    
    @Delete
    suspend fun deleteMessage(message: MessageEntity)
    
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)
    
    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteConversation(conversationId: String)
    
    // Sync operations
    @Query("SELECT * FROM messages WHERE isSynced = 0")
    suspend fun getUnsyncedMessages(): List<MessageEntity>
    
    @Query("UPDATE messages SET isSynced = 1 WHERE id = :messageId")
    suspend fun markMessageAsSynced(messageId: String)
    
    // Search operations
    @Query("SELECT * FROM messages WHERE (senderId = :userId OR recipientId = :userId) AND content LIKE '%' || :query || '%' ORDER BY createdAt DESC LIMIT :limit")
    suspend fun searchMessages(userId: String, query: String, limit: Int = 50): List<MessageEntity>
    
    // Conversation management
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastMessageInConversation(conversationId: String): MessageEntity?
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getMessagesPaginated(conversationId: String, limit: Int = 20, offset: Int = 0): List<MessageEntity>
    
    // Media messages
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND messageType IN ('image', 'video', 'audio', 'document') ORDER BY createdAt DESC")
    suspend fun getMediaMessages(conversationId: String): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE messageType = :messageType ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getMessagesByType(messageType: String, limit: Int = 50): List<MessageEntity>
    
    // Analytics
    @Query("SELECT COUNT(*) FROM messages WHERE senderId = :userId")
    suspend fun getSentMessageCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM messages WHERE recipientId = :userId")
    suspend fun getReceivedMessageCount(userId: String): Int
    
    @Query("SELECT messageType, COUNT(*) as count FROM messages WHERE senderId = :userId OR recipientId = :userId GROUP BY messageType ORDER BY count DESC")
    suspend fun getMessageTypeDistribution(userId: String): List<MessageTypeCount>
    
    @Query("SELECT * FROM messages WHERE (senderId = :userId OR recipientId = :userId) AND createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC")
    suspend fun getMessagesByDateRange(userId: String, startDate: Date, endDate: Date): List<MessageEntity>
    
    // Cleanup operations
    @Query("DELETE FROM messages WHERE createdAt < :cutoffDate")
    suspend fun deleteOldMessages(cutoffDate: Date)
    
    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId")
    suspend fun getMessageCountInConversation(conversationId: String): Int
}

data class MessageTypeCount(
    val messageType: String,
    val count: Int
)
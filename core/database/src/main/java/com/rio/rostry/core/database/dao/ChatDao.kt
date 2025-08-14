package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.MessageEntity
import com.rio.rostry.core.database.entities.ConversationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for chat operations (alias for MessageDao for backward compatibility)
 */
@Dao
interface ChatDao {
    
    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY created_at ASC")
    suspend fun getMessagesByConversation(conversationId: String): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY created_at ASC")
    fun observeMessagesByConversation(conversationId: String): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM conversations WHERE participant1_id = :userId OR participant2_id = :userId ORDER BY last_message_at DESC")
    suspend fun getConversationsByUser(userId: String): List<ConversationEntity>
    
    @Query("SELECT * FROM conversations WHERE participant1_id = :userId OR participant2_id = :userId ORDER BY last_message_at DESC")
    fun observeConversationsByUser(userId: String): Flow<List<ConversationEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)
    
    @Query("UPDATE messages SET is_read = 1, read_at = :readAt WHERE id = :messageId")
    suspend fun markMessageAsRead(messageId: String, readAt: java.util.Date)
    
    @Query("SELECT * FROM messages WHERE is_synced = 0 ORDER BY created_at ASC")
    suspend fun getUnsyncedMessages(): List<MessageEntity>
    
    @Query("UPDATE messages SET is_synced = 1 WHERE id = :messageId")
    suspend fun markMessageAsSynced(messageId: String)
}

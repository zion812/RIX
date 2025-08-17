package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.ConversationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for conversation operations
 */
@Dao
interface ConversationDaoV2 {
    
    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversationById(conversationId: String): ConversationEntity?
    
    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    fun observeConversation(conversationId: String): Flow<ConversationEntity?>
    
    @Query("SELECT * FROM conversations WHERE participantIds LIKE '%' || :userId || '%' ORDER BY lastMessageTimestamp DESC")
    fun getConversationsForUser(userId: String): Flow<List<ConversationEntity>>
    
    @Query("SELECT * FROM conversations WHERE participantIds LIKE '%' || :userId || '%' ORDER BY lastMessageTimestamp DESC")
    suspend fun getConversationsForUserSync(userId: String): List<ConversationEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>): List<Long>
    
    @Update
    suspend fun updateConversation(conversation: ConversationEntity): Int
    
    @Query("UPDATE conversations SET lastMessage = :lastMessage, lastMessageTimestamp = :timestamp, unreadCount = :unreadCount WHERE id = :conversationId")
    suspend fun updateLastMessage(conversationId: String, lastMessage: String, timestamp: java.util.Date, unreadCount: Int): Int
    
    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity): Int
    
    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversationById(conversationId: String): Int
    
    // Sync operations
    @Query("SELECT * FROM conversations WHERE isSynced = 0")
    suspend fun getUnsyncedConversations(): List<ConversationEntity>
    
    @Query("UPDATE conversations SET isSynced = 1 WHERE id = :conversationId")
    suspend fun markConversationAsSynced(conversationId: String)
    
    @Query("SELECT COUNT(*) FROM conversations WHERE participantIds LIKE '%' || :userId || '%' AND unreadCount > 0")
    suspend fun getUnreadConversationsCount(userId: String): Int
}
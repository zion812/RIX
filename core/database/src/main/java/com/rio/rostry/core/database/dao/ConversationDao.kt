package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.ConversationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for conversation operations
 */
@Dao
interface ConversationDao {
    
    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getById(conversationId: String): ConversationEntity?
    
    @Query("SELECT * FROM conversations WHERE participant1_id = :userId OR participant2_id = :userId ORDER BY last_message_at DESC")
    suspend fun getConversationsByUser(userId: String): List<ConversationEntity>
    
    @Query("SELECT * FROM conversations WHERE participant1_id = :userId OR participant2_id = :userId ORDER BY last_message_at DESC")
    fun observeConversationsByUser(userId: String): Flow<List<ConversationEntity>>
    
    @Query("SELECT * FROM conversations WHERE (participant1_id = :user1 AND participant2_id = :user2) OR (participant1_id = :user2 AND participant2_id = :user1)")
    suspend fun getConversationBetweenUsers(user1: String, user2: String): ConversationEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: ConversationEntity)
    
    @Update
    suspend fun update(conversation: ConversationEntity)
    
    @Delete
    suspend fun delete(conversation: ConversationEntity)
    
    @Query("UPDATE conversations SET last_message_at = :timestamp, last_message_preview = :preview WHERE id = :conversationId")
    suspend fun updateLastMessage(conversationId: String, timestamp: Long, preview: String)
    
    @Query("UPDATE conversations SET unread_count = :count WHERE id = :conversationId")
    suspend fun updateUnreadCount(conversationId: String, count: Int)
    
    @Query("SELECT COUNT(*) FROM conversations WHERE (participant1_id = :userId OR participant2_id = :userId) AND unread_count > 0")
    suspend fun getTotalUnreadCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM conversations WHERE (participant1_id = :userId OR participant2_id = :userId) AND unread_count > 0")
    fun observeTotalUnreadCount(userId: String): Flow<Int>
}

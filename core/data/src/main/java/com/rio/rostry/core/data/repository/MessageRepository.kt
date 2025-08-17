package com.rio.rostry.core.data.repository

import com.rio.rostry.core.database.dao.MessageDao
import com.rio.rostry.core.database.entities.MessageEntity
import com.rio.rostry.core.data.model.Message
import com.rio.rostry.core.data.util.DataSyncManager
import com.rio.rostry.core.data.util.SyncOperation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository for managing messages with offline-first capabilities
 */
class MessageRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val syncManager: DataSyncManager
) {
    
    /**
     * Send a new message
     */
    suspend fun sendMessage(message: Message): Result<String> {
        return try {
            val messageEntity = message.toEntity()
            messageDao.insertMessage(messageEntity)
            
            // Queue for sync
            syncManager.queueSyncOperation(
                SyncOperation.Create(
                    collection = "messages",
                    documentId = messageEntity.id,
                    data = messageEntity.toMap()
                )
            )
            
            Result.success(messageEntity.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get messages between two users (in a thread)
     */
    fun getMessagesInThread(threadId: String): Flow<List<Message>> {
        return messageDao.getMessagesByThread(threadId)
            .map { entities ->
                entities.map { it.toModel() }
            }
    }
    
    /**
     * Get message threads for a user
     */
    fun getMessageThreads(userId: String): Flow<List<MessageThread>> {
        return messageDao.getMessageThreads(userId)
            .map { entities ->
                entities.groupBy { it.threadId }
                    .map { (threadId, messages) ->
                        val firstMessage = messages.firstOrNull()
                        MessageThread(
                            threadId = threadId,
                            participantIds = listOfNotNull(firstMessage?.senderId, firstMessage?.receiverId)
                                .distinct()
                                .filter { it != userId },
                            lastMessage = firstMessage?.toModel(),
                            unreadCount = messages.count { !it.isRead && it.receiverId == userId }
                        )
                    }
            }
    }
    
    /**
     * Mark messages as read
     */
    suspend fun markMessagesAsRead(threadId: String, userId: String): Result<Unit> {
        return try {
            messageDao.markMessagesAsRead(threadId, userId)
            
            // Queue for sync
            syncManager.queueSyncOperation(
                SyncOperation.Update(
                    collection = "messages",
                    documentId = "thread_$threadId", // Special identifier for batch updates
                    data = mapOf(
                        "thread_id" to threadId,
                        "user_id" to userId,
                        "mark_as_read" to true
                    )
                )
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Extension function to convert Message model to MessageEntity
     */
    private fun Message.toEntity(): MessageEntity {
        return MessageEntity(
            id = this.id,
            senderId = this.senderId,
            receiverId = this.receiverId,
            threadId = this.threadId,
            content = this.content,
            sentAt = this.sentAt,
            isRead = this.isRead,
            createdAt = java.util.Date(),
            syncStatus = "pending",
            syncPriority = 1,
            isDeleted = false
        )
    }
    
    /**
     * Extension function to convert MessageEntity to Message model
     */
    private fun MessageEntity.toModel(): Message {
        return Message(
            id = this.id,
            senderId = this.senderId,
            receiverId = this.receiverId,
            threadId = this.threadId,
            content = this.content,
            sentAt = this.sentAt,
            isRead = this.isRead
        )
    }
}

/**
 * Data model representing a message thread
 */
data class MessageThread(
    val threadId: String,
    val participantIds: List<String>,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0
)
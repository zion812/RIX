package com.rio.rostry.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.core.data.service.UserValidationService
import com.rio.rostry.core.database.dao.MessageDao
import com.rio.rostry.core.database.dao.ConversationDao
import com.rio.rostry.core.database.entities.MessageEntity
import com.rio.rostry.core.database.entities.ConversationEntity
import com.rio.rostry.core.network.NetworkStateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Chat repository implementation with offline-first capabilities
 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val userValidationService: UserValidationService,
    private val firestore: FirebaseFirestore,
    private val networkStateManager: NetworkStateManager
) {
    
    /**
     * Get conversations for a user
     */
    fun getConversations(userId: String): Flow<List<ConversationEntity>> {
        return conversationDao.observeConversationsByUser(userId)
    }
    
    /**
     * Get messages for a conversation
     */
    fun getMessages(conversationId: String): Flow<List<MessageEntity>> {
        return messageDao.observeMessagesByConversation(conversationId)
    }
    
    /**
     * Send a message
     */
    suspend fun sendMessage(
        senderId: String,
        recipientId: String,
        content: String,
        messageType: String = "text"
    ): Result<MessageEntity> {
        return try {
            // Validate sender
            val validation = userValidationService.validateUserExists(senderId)
            if (!validation.isValid) {
                return Result.failure(Exception("Invalid sender"))
            }
            
            // Get or create conversation
            val conversation = getOrCreateConversation(senderId, recipientId)
            
            // Create message
            val message = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = conversation.id,
                senderId = senderId,
                recipientId = recipientId,
                content = content,
                messageType = messageType,
                isRead = false,
                isDelivered = false,
                createdAt = Date(),
                updatedAt = Date(),
                isSynced = false
            )
            
            // Save locally first
            messageDao.insert(message)
            
            // Update conversation
            conversationDao.updateLastMessage(
                conversation.id,
                message.createdAt.time,
                content.take(100)
            )
            
            // Sync to server if online
            if (networkStateManager.isConnected()) {
                try {
                    syncMessageToServer(message)
                    messageDao.markAsSynced(message.id)
                } catch (e: Exception) {
                    // Message will be synced later
                }
            }
            
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mark message as read
     */
    suspend fun markMessageAsRead(messageId: String): Result<Unit> {
        return try {
            messageDao.markAsRead(messageId, Date())
            
            if (networkStateManager.isConnected()) {
                firestore.collection("messages")
                    .document(messageId)
                    .update(mapOf(
                        "isRead" to true,
                        "readAt" to Date()
                    ))
                    .await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get or create conversation between two users
     */
    private suspend fun getOrCreateConversation(user1Id: String, user2Id: String): ConversationEntity {
        // Check if conversation exists
        val existing = conversationDao.getConversationBetweenUsers(user1Id, user2Id)
        if (existing != null) {
            return existing
        }
        
        // Create new conversation
        val conversation = ConversationEntity(
            id = UUID.randomUUID().toString(),
            participant1Id = user1Id,
            participant2Id = user2Id,
            lastMessageAt = Date(),
            lastMessagePreview = "",
            unreadCount = 0,
            createdAt = Date(),
            updatedAt = Date()
        )
        
        conversationDao.insert(conversation)
        
        // Sync to server if online
        if (networkStateManager.isConnected()) {
            try {
                syncConversationToServer(conversation)
            } catch (e: Exception) {
                // Will be synced later
            }
        }
        
        return conversation
    }
    
    /**
     * Sync message to server
     */
    private suspend fun syncMessageToServer(message: MessageEntity) {
        val messageData = mapOf(
            "conversationId" to message.conversationId,
            "senderId" to message.senderId,
            "recipientId" to message.recipientId,
            "content" to message.content,
            "messageType" to message.messageType,
            "isRead" to message.isRead,
            "isDelivered" to message.isDelivered,
            "createdAt" to message.createdAt,
            "updatedAt" to message.updatedAt
        )
        
        firestore.collection("messages")
            .document(message.id)
            .set(messageData)
            .await()
    }
    
    /**
     * Sync conversation to server
     */
    private suspend fun syncConversationToServer(conversation: ConversationEntity) {
        val conversationData = mapOf(
            "participant1Id" to conversation.participant1Id,
            "participant2Id" to conversation.participant2Id,
            "lastMessageAt" to conversation.lastMessageAt,
            "lastMessagePreview" to conversation.lastMessagePreview,
            "unreadCount" to conversation.unreadCount,
            "createdAt" to conversation.createdAt,
            "updatedAt" to conversation.updatedAt
        )
        
        firestore.collection("conversations")
            .document(conversation.id)
            .set(conversationData)
            .await()
    }
    
    /**
     * Sync offline changes
     */
    suspend fun syncOfflineChanges(): Result<Unit> {
        return try {
            if (!networkStateManager.isConnected()) {
                return Result.failure(Exception("No network connection"))
            }
            
            // Sync unsynced messages
            val unsyncedMessages = messageDao.getUnsyncedMessages()
            for (message in unsyncedMessages) {
                try {
                    syncMessageToServer(message)
                    messageDao.markAsSynced(message.id)
                } catch (e: Exception) {
                    continue
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.rio.rostry.shared.domain.model

import java.util.*

/**
 * Domain model for Message
 */
data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val messageType: MessageType,
    val textContent: String?,
    val mediaUrl: String?,
    val mediaType: String?,
    val fowlId: String?,
    val listingId: String?,
    val isRead: Boolean,
    val isDelivered: Boolean,
    val readAt: Date?,
    val deliveredAt: Date?,
    val createdAt: Date,
    val updatedAt: Date
)

/**
 * Message type enumeration
 */
enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    FOWL_SHARE,
    LISTING_SHARE,
    LOCATION,
    SYSTEM
}

/**
 * Domain model for Conversation
 */
data class Conversation(
    val id: String,
    val conversationType: ConversationType,
    val title: String?,
    val participants: List<String>,
    val lastMessageId: String?,
    val lastMessageAt: Date?,
    val unreadCount: Int,
    val isArchived: Boolean,
    val isMuted: Boolean,
    val createdAt: Date,
    val updatedAt: Date
)

/**
 * Conversation type enumeration
 */
enum class ConversationType {
    DIRECT,
    GROUP,
    MARKETPLACE_INQUIRY,
    BREEDING_DISCUSSION,
    SUPPORT
}
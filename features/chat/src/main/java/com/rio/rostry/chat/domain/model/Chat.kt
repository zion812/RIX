package com.rio.rostry.chat.domain.model

import com.rio.rostry.core.common.model.*
import java.util.*

/**
 * Domain model representing a conversation
 */
data class Conversation(
    val id: String,
    val type: ConversationType,
    val participants: List<Participant>,
    val title: String? = null,
    val description: String? = null,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val settings: ConversationSettings,
    val metadata: ConversationMetadata,
    val createdAt: Date,
    val lastActivityAt: Date,
    val updatedAt: Date
)

/**
 * Conversation types
 */
enum class ConversationType {
    DIRECT,
    GROUP,
    MARKETPLACE,
    BREEDING,
    SUPPORT
}

/**
 * Conversation participant
 */
data class Participant(
    val userId: String,
    val displayName: String,
    val photoUrl: String? = null,
    val role: ParticipantRole,
    val status: ParticipantStatus,
    val joinedAt: Date,
    val lastReadAt: Date? = null,
    val permissions: ParticipantPermissions
)

/**
 * Participant roles
 */
enum class ParticipantRole {
    OWNER,
    ADMIN,
    MODERATOR,
    MEMBER,
    GUEST
}

/**
 * Participant status
 */
enum class ParticipantStatus {
    ACTIVE,
    LEFT,
    REMOVED,
    BANNED,
    MUTED
}

/**
 * Participant permissions
 */
data class ParticipantPermissions(
    val canSendMessages: Boolean = true,
    val canSendMedia: Boolean = true,
    val canAddParticipants: Boolean = false,
    val canRemoveParticipants: Boolean = false,
    val canChangeTitle: Boolean = false,
    val canChangePhoto: Boolean = false,
    val canPinMessages: Boolean = false,
    val canDeleteMessages: Boolean = false,
    val canMuteParticipants: Boolean = false
)

/**
 * Message model
 */
data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: MessageContent,
    val status: MessageStatus,
    val thread: MessageThread? = null,
    val reactions: Map<String, List<MessageReaction>> = emptyMap(),
    val mentions: List<MessageMention> = emptyList(),
    val replyTo: String? = null,
    val forwarded: Boolean = false,
    val edited: Boolean = false,
    val pinned: Boolean = false,
    val priority: Priority = Priority.NORMAL,
    val metadata: MessageMetadata,
    val sentAt: Date,
    val editedAt: Date? = null,
    val expiresAt: Date? = null
)

/**
 * Message content
 */
sealed class MessageContent {
    data class Text(val text: String) : MessageContent()
    data class Image(val imageUrl: String, val caption: String? = null, val thumbnail: String? = null) : MessageContent()
    data class Video(val videoUrl: String, val caption: String? = null, val thumbnail: String? = null, val duration: Int? = null) : MessageContent()
    data class Audio(val audioUrl: String, val duration: Int, val waveform: List<Float>? = null) : MessageContent()
    data class File(val fileUrl: String, val fileName: String, val fileSize: Long, val mimeType: String) : MessageContent()
    data class Location(val latitude: Double, val longitude: Double, val address: String? = null) : MessageContent()
    data class FowlCard(val fowlId: String, val fowlData: FowlCardData) : MessageContent()
    data class ListingCard(val listingId: String, val listingData: ListingCardData) : MessageContent()
    data class Contact(val name: String, val phoneNumber: String, val email: String? = null) : MessageContent()
    data class System(val systemType: SystemMessageType, val data: Map<String, Any> = emptyMap()) : MessageContent()
}

/**
 * Fowl card data for sharing fowl information
 */
data class FowlCardData(
    val name: String?,
    val breed: String,
    val gender: String,
    val age: String,
    val photoUrl: String?,
    val price: Double?,
    val ownerId: String
)

/**
 * Listing card data for sharing marketplace listings
 */
data class ListingCardData(
    val title: String,
    val price: Double,
    val currency: String,
    val photoUrl: String?,
    val location: String,
    val status: String
)

/**
 * System message types
 */
enum class SystemMessageType {
    USER_JOINED,
    USER_LEFT,
    USER_ADDED,
    USER_REMOVED,
    TITLE_CHANGED,
    PHOTO_CHANGED,
    CONVERSATION_CREATED,
    MESSAGE_DELETED
}

/**
 * Message status
 */
data class MessageStatus(
    val sent: Boolean = false,
    val delivered: Boolean = false,
    val read: Boolean = false,
    val failed: Boolean = false,
    val sentAt: Date? = null,
    val deliveredAt: Date? = null,
    val readAt: Date? = null,
    val readBy: List<MessageReadReceipt> = emptyList()
)

/**
 * Message read receipt
 */
data class MessageReadReceipt(
    val userId: String,
    val readAt: Date
)

/**
 * Message thread for replies
 */
data class MessageThread(
    val threadId: String,
    val rootMessageId: String,
    val replyCount: Int = 0,
    val lastReplyAt: Date? = null,
    val participants: List<String> = emptyList()
)

/**
 * Message reaction
 */
data class MessageReaction(
    val userId: String,
    val emoji: String,
    val reactedAt: Date
)

/**
 * Message mention
 */
data class MessageMention(
    val userId: String,
    val displayName: String,
    val startIndex: Int,
    val endIndex: Int
)

/**
 * Message metadata
 */
data class MessageMetadata(
    val source: String = "mobile",
    val deviceInfo: String? = null,
    val appVersion: String? = null,
    val messageSize: Long = 0,
    val editHistory: List<MessageEdit> = emptyList(),
    val forwardHistory: List<MessageForward> = emptyList(),
    val encryption: MessageEncryption? = null,
    val moderation: MessageModeration? = null
)

/**
 * Message edit history
 */
data class MessageEdit(
    val editedAt: Date,
    val previousContent: String,
    val reason: String? = null
)

/**
 * Message forward history
 */
data class MessageForward(
    val forwardedAt: Date,
    val forwardedBy: String,
    val originalConversationId: String,
    val originalMessageId: String
)

/**
 * Message encryption
 */
data class MessageEncryption(
    val encrypted: Boolean,
    val encryptionMethod: String? = null,
    val keyId: String? = null
)

/**
 * Message moderation
 */
data class MessageModeration(
    val flagged: Boolean = false,
    val flaggedBy: List<String> = emptyList(),
    val flaggedAt: Date? = null,
    val flagReason: String? = null,
    val moderationStatus: ModerationStatus = ModerationStatus.PENDING,
    val moderatedBy: String? = null,
    val moderatedAt: Date? = null,
    val contentAnalysis: ContentAnalysis? = null
)

/**
 * Content analysis for moderation
 */
data class ContentAnalysis(
    val spamScore: Double = 0.0,
    val toxicityScore: Double = 0.0,
    val languageDetected: String? = null,
    val sentimentScore: Double = 0.0,
    val containsPersonalInfo: Boolean = false,
    val containsProfanity: Boolean = false,
    val containsSpam: Boolean = false,
    val containsHateSpeech: Boolean = false
)

/**
 * Conversation settings
 */
data class ConversationSettings(
    val isPublic: Boolean = false,
    val allowInvites: Boolean = true,
    val requireApproval: Boolean = false,
    val muteNotifications: Boolean = false,
    val mutedUntil: Date? = null,
    val disappearingMessages: DisappearingMessagesSettings? = null,
    val backup: BackupSettings = BackupSettings(),
    val moderation: ModerationSettings = ModerationSettings()
)

/**
 * Disappearing messages settings
 */
data class DisappearingMessagesSettings(
    val enabled: Boolean,
    val duration: Long // in milliseconds
)

/**
 * Backup settings
 */
data class BackupSettings(
    val enabled: Boolean = true,
    val includeMedia: Boolean = true,
    val frequency: BackupFrequency = BackupFrequency.DAILY
)

/**
 * Backup frequency
 */
enum class BackupFrequency {
    NEVER,
    DAILY,
    WEEKLY,
    MONTHLY
}

/**
 * Moderation settings
 */
data class ModerationSettings(
    val autoModeration: Boolean = true,
    val spamFilter: Boolean = true,
    val profanityFilter: Boolean = true,
    val linkFilter: Boolean = false,
    val mediaFilter: Boolean = false
)

/**
 * Conversation metadata
 */
data class ConversationMetadata(
    val messageCount: Int = 0,
    val mediaCount: Int = 0,
    val participantCount: Int = 0,
    val isActive: Boolean = true,
    val tags: List<String> = emptyList(),
    val relatedToListing: String? = null,
    val relatedToFowl: String? = null,
    val relatedToTransfer: String? = null,
    val relatedToBreeding: String? = null,
    val analytics: ConversationAnalytics? = null
)

/**
 * Conversation analytics
 */
data class ConversationAnalytics(
    val totalMessages: Int = 0,
    val averageResponseTime: Double = 0.0, // in minutes
    val mostActiveParticipant: String? = null,
    val peakActivityHours: List<Int> = emptyList(),
    val engagementScore: Double = 0.0
)

/**
 * Chat search criteria
 */
data class ChatSearchCriteria(
    val query: String? = null,
    val conversationType: ConversationType? = null,
    val participantId: String? = null,
    val hasUnread: Boolean? = null,
    val dateRange: Pair<Date, Date>? = null,
    val messageType: String? = null,
    val sortBy: ChatSortBy = ChatSortBy.LAST_ACTIVITY,
    val sortOrder: SortOrder = SortOrder.DESC
)

/**
 * Chat sorting options
 */
enum class ChatSortBy {
    LAST_ACTIVITY,
    CREATED_AT,
    PARTICIPANT_COUNT,
    MESSAGE_COUNT,
    UNREAD_COUNT
}

/**
 * Message search criteria
 */
data class MessageSearchCriteria(
    val query: String? = null,
    val conversationId: String? = null,
    val senderId: String? = null,
    val messageType: String? = null,
    val hasMedia: Boolean? = null,
    val dateRange: Pair<Date, Date>? = null,
    val sortBy: MessageSortBy = MessageSortBy.SENT_AT,
    val sortOrder: SortOrder = SortOrder.DESC
)

/**
 * Message sorting options
 */
enum class MessageSortBy {
    SENT_AT,
    RELEVANCE,
    SENDER
}

/**
 * Typing indicator
 */
data class TypingIndicator(
    val conversationId: String,
    val userId: String,
    val displayName: String,
    val startedAt: Date,
    val lastUpdatedAt: Date
)

/**
 * Online status
 */
data class OnlineStatus(
    val userId: String,
    val status: UserOnlineStatus,
    val lastSeenAt: Date? = null,
    val customStatus: String? = null
)

/**
 * User online status
 */
enum class UserOnlineStatus {
    ONLINE,
    OFFLINE,
    AWAY,
    BUSY,
    INVISIBLE
}

/**
 * Message draft
 */
data class MessageDraft(
    val conversationId: String,
    val content: String,
    val mentions: List<MessageMention> = emptyList(),
    val replyToMessageId: String? = null,
    val attachments: List<DraftAttachment> = emptyList(),
    val lastUpdatedAt: Date
)

/**
 * Draft attachment
 */
data class DraftAttachment(
    val type: AttachmentType,
    val localPath: String,
    val fileName: String? = null,
    val mimeType: String? = null,
    val size: Long = 0
)

/**
 * Attachment types
 */
enum class AttachmentType {
    IMAGE,
    VIDEO,
    AUDIO,
    FILE,
    LOCATION,
    CONTACT
}

/**
 * Offline message queue item
 */
data class OfflineMessage(
    val id: String,
    val conversationId: String,
    val content: MessageContent,
    val tempId: String,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val createdAt: Date,
    val lastRetryAt: Date? = null
)

/**
 * Push notification data
 */
data class ChatNotification(
    val conversationId: String,
    val messageId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val type: NotificationType,
    val priority: Priority = Priority.NORMAL,
    val data: Map<String, String> = emptyMap()
)

/**
 * Notification types
 */
enum class NotificationType {
    NEW_MESSAGE,
    MENTION,
    REPLY,
    REACTION,
    GROUP_INVITE,
    CALL_MISSED
}

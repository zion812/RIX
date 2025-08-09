package com.rio.rostry.chat.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.common.base.BaseViewModel
import com.rio.rostry.core.common.model.*
import com.rio.rostry.chat.domain.model.*
import com.rio.rostry.chat.domain.usecases.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for chat functionality
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val createConversationUseCase: CreateConversationUseCase,
    private val updateConversationUseCase: UpdateConversationUseCase,
    private val deleteConversationUseCase: DeleteConversationUseCase,
    private val markMessageAsReadUseCase: MarkMessageAsReadUseCase,
    private val searchMessagesUseCase: SearchMessagesUseCase,
    private val uploadMediaUseCase: UploadMediaUseCase,
    private val getOnlineStatusUseCase: GetOnlineStatusUseCase,
    private val updateTypingStatusUseCase: UpdateTypingStatusUseCase,
    private val syncOfflineMessagesUseCase: SyncOfflineMessagesUseCase
) : BaseViewModel() {

    // Conversations list
    private val _conversations = MutableStateFlow<ListState<Conversation>>(ListState())
    val conversations: StateFlow<ListState<Conversation>> = _conversations.asStateFlow()

    // Current conversation
    private val _currentConversation = MutableStateFlow<Conversation?>(null)
    val currentConversation: StateFlow<Conversation?> = _currentConversation.asStateFlow()

    // Messages in current conversation
    private val _messages = MutableStateFlow<ListState<Message>>(ListState())
    val messages: StateFlow<ListState<Message>> = _messages.asStateFlow()

    // Message composition state
    private val _compositionState = MutableStateFlow(MessageCompositionState())
    val compositionState: StateFlow<MessageCompositionState> = _compositionState.asStateFlow()

    // Media upload state
    private val _mediaUploadState = MutableStateFlow(MediaUploadState())
    val mediaUploadState: StateFlow<MediaUploadState> = _mediaUploadState.asStateFlow()

    // Search state
    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    // Typing indicators
    private val _typingIndicators = MutableStateFlow<Map<String, List<TypingIndicator>>>(emptyMap())
    val typingIndicators: StateFlow<Map<String, List<TypingIndicator>>> = _typingIndicators.asStateFlow()

    // Online status
    private val _onlineStatus = MutableStateFlow<Map<String, OnlineStatus>>(emptyMap())
    val onlineStatus: StateFlow<Map<String, OnlineStatus>> = _onlineStatus.asStateFlow()

    // Offline messages queue
    private val _offlineMessages = MutableStateFlow<List<OfflineMessage>>(emptyList())
    val offlineMessages: StateFlow<List<OfflineMessage>> = _offlineMessages.asStateFlow()

    // Unread count
    private val _totalUnreadCount = MutableStateFlow(0)
    val totalUnreadCount: StateFlow<Int> = _totalUnreadCount.asStateFlow()

    init {
        loadConversations()
        observeOnlineStatus()
        observeTypingIndicators()
        syncOfflineMessages()
    }

    /**
     * Load user's conversations
     */
    fun loadConversations(refresh: Boolean = false) {
        val currentUserId = getCurrentUserId() ?: return

        if (refresh) {
            _conversations.value = _conversations.value.copy(isRefreshing = true)
        } else {
            _conversations.value = _conversations.value.copy(isLoading = true)
        }

        executeWithResult(
            showLoading = false,
            action = { getConversationsUseCase(currentUserId) },
            onSuccess = { conversationList ->
                _conversations.value = ListState(
                    items = conversationList,
                    isLoading = false,
                    isRefreshing = false
                )
                
                // Calculate total unread count
                val totalUnread = conversationList.sumOf { it.unreadCount }
                _totalUnreadCount.value = totalUnread
                
                logUserAction("conversations_loaded", mapOf("count" to conversationList.size))
            },
            onError = { exception ->
                _conversations.value = _conversations.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = exception.message
                )
            }
        )
    }

    /**
     * Open a conversation and load messages
     */
    fun openConversation(conversationId: String) {
        executeWithResult(
            action = { getConversationsUseCase.getConversationById(conversationId) },
            onSuccess = { conversation ->
                _currentConversation.value = conversation
                loadMessages(conversationId)
                markConversationAsRead(conversationId)
                
                logUserAction("conversation_opened", mapOf("conversation_id" to conversationId))
            }
        )
    }

    /**
     * Load messages for a conversation
     */
    fun loadMessages(conversationId: String, loadMore: Boolean = false) {
        if (!loadMore) {
            _messages.value = _messages.value.copy(isLoading = true)
        }

        val page = if (loadMore) _messages.value.items.size / DEFAULT_PAGE_SIZE else 0

        executeWithResult(
            showLoading = false,
            action = { getMessagesUseCase(conversationId, page, DEFAULT_PAGE_SIZE) },
            onSuccess = { messageList ->
                val currentMessages = if (loadMore) _messages.value.items else emptyList()
                val allMessages = (currentMessages + messageList).distinctBy { it.id }
                
                _messages.value = ListState(
                    items = allMessages,
                    isLoading = false,
                    hasMore = messageList.size >= DEFAULT_PAGE_SIZE
                )
            },
            onError = { exception ->
                _messages.value = _messages.value.copy(
                    isLoading = false,
                    error = exception.message
                )
            }
        )
    }

    /**
     * Send a text message
     */
    fun sendTextMessage(conversationId: String, text: String, replyToMessageId: String? = null) {
        if (text.isBlank()) return

        val tempMessage = createTempMessage(conversationId, MessageContent.Text(text), replyToMessageId)
        addTempMessageToList(tempMessage)

        executeWithResult(
            showLoading = false,
            action = { sendMessageUseCase(conversationId, MessageContent.Text(text), replyToMessageId) },
            onSuccess = { sentMessage ->
                replaceTempMessageWithSent(tempMessage.id, sentMessage)
                updateConversationLastMessage(conversationId, sentMessage)
                clearComposition()
                
                logUserAction("message_sent", mapOf(
                    "conversation_id" to conversationId,
                    "message_type" to "text"
                ))
            },
            onError = { exception ->
                markTempMessageAsFailed(tempMessage.id)
                if (!isOffline.value) {
                    showErrorMessage("Failed to send message: ${exception.message}")
                }
            }
        )
    }

    /**
     * Send media message
     */
    fun sendMediaMessage(
        conversationId: String,
        mediaData: ByteArray,
        mediaType: AttachmentType,
        caption: String? = null
    ) {
        _mediaUploadState.value = _mediaUploadState.value.copy(
            isUploading = true,
            progress = 0f,
            error = null
        )

        executeWithResult(
            showLoading = false,
            action = { uploadMediaUseCase(mediaData, mediaType) },
            onSuccess = { mediaUrl ->
                _mediaUploadState.value = _mediaUploadState.value.copy(
                    isUploading = false,
                    uploadedUrl = mediaUrl
                )

                val content = when (mediaType) {
                    AttachmentType.IMAGE -> MessageContent.Image(mediaUrl, caption)
                    AttachmentType.VIDEO -> MessageContent.Video(mediaUrl, caption)
                    AttachmentType.AUDIO -> MessageContent.Audio(mediaUrl, 0) // Duration would be calculated
                    AttachmentType.FILE -> MessageContent.File(mediaUrl, "file", mediaData.size.toLong(), "")
                    else -> MessageContent.Text(caption ?: "")
                }

                val tempMessage = createTempMessage(conversationId, content)
                addTempMessageToList(tempMessage)

                executeWithResult(
                    showLoading = false,
                    action = { sendMessageUseCase(conversationId, content) },
                    onSuccess = { sentMessage ->
                        replaceTempMessageWithSent(tempMessage.id, sentMessage)
                        updateConversationLastMessage(conversationId, sentMessage)
                        
                        logUserAction("media_message_sent", mapOf(
                            "conversation_id" to conversationId,
                            "media_type" to mediaType.name
                        ))
                    },
                    onError = { exception ->
                        markTempMessageAsFailed(tempMessage.id)
                    }
                )
            },
            onError = { exception ->
                _mediaUploadState.value = _mediaUploadState.value.copy(
                    isUploading = false,
                    error = exception.message ?: "Upload failed"
                )
            }
        )
    }

    /**
     * Send fowl card message
     */
    fun sendFowlCard(conversationId: String, fowlId: String, fowlData: FowlCardData) {
        val content = MessageContent.FowlCard(fowlId, fowlData)
        val tempMessage = createTempMessage(conversationId, content)
        addTempMessageToList(tempMessage)

        executeWithResult(
            showLoading = false,
            action = { sendMessageUseCase(conversationId, content) },
            onSuccess = { sentMessage ->
                replaceTempMessageWithSent(tempMessage.id, sentMessage)
                updateConversationLastMessage(conversationId, sentMessage)
                
                logUserAction("fowl_card_sent", mapOf(
                    "conversation_id" to conversationId,
                    "fowl_id" to fowlId
                ))
            },
            onError = { exception ->
                markTempMessageAsFailed(tempMessage.id)
            }
        )
    }

    /**
     * Send listing card message
     */
    fun sendListingCard(conversationId: String, listingId: String, listingData: ListingCardData) {
        val content = MessageContent.ListingCard(listingId, listingData)
        val tempMessage = createTempMessage(conversationId, content)
        addTempMessageToList(tempMessage)

        executeWithResult(
            showLoading = false,
            action = { sendMessageUseCase(conversationId, content) },
            onSuccess = { sentMessage ->
                replaceTempMessageWithSent(tempMessage.id, sentMessage)
                updateConversationLastMessage(conversationId, sentMessage)
                
                logUserAction("listing_card_sent", mapOf(
                    "conversation_id" to conversationId,
                    "listing_id" to listingId
                ))
            },
            onError = { exception ->
                markTempMessageAsFailed(tempMessage.id)
            }
        )
    }

    /**
     * Create a new conversation
     */
    fun createConversation(
        type: ConversationType,
        participantIds: List<String>,
        title: String? = null
    ) {
        executeWithResult(
            action = { createConversationUseCase(type, participantIds, title) },
            onSuccess = { conversation ->
                val currentConversations = _conversations.value.items.toMutableList()
                currentConversations.add(0, conversation)
                _conversations.value = _conversations.value.copy(items = currentConversations)
                
                // Open the new conversation
                openConversation(conversation.id)
                
                logUserAction("conversation_created", mapOf(
                    "conversation_id" to conversation.id,
                    "type" to type.name,
                    "participant_count" to participantIds.size
                ))
            }
        )
    }

    /**
     * Mark conversation as read
     */
    fun markConversationAsRead(conversationId: String) {
        val currentUserId = getCurrentUserId() ?: return

        executeWithResult(
            showLoading = false,
            action = { markMessageAsReadUseCase(conversationId, currentUserId) },
            onSuccess = {
                // Update conversation unread count
                val currentConversations = _conversations.value.items.toMutableList()
                val index = currentConversations.indexOfFirst { it.id == conversationId }
                if (index != -1) {
                    currentConversations[index] = currentConversations[index].copy(unreadCount = 0)
                    _conversations.value = _conversations.value.copy(items = currentConversations)
                    
                    // Update total unread count
                    val totalUnread = currentConversations.sumOf { it.unreadCount }
                    _totalUnreadCount.value = totalUnread
                }
            }
        )
    }

    /**
     * Search messages
     */
    fun searchMessages(query: String, conversationId: String? = null) {
        if (query.isBlank()) {
            _searchState.value = _searchState.value.copy(query = "", isSearching = false)
            return
        }

        _searchState.value = _searchState.value.copy(query = query, isSearching = true)

        val criteria = MessageSearchCriteria(
            query = query,
            conversationId = conversationId
        )

        executeWithResult(
            showLoading = false,
            action = { searchMessagesUseCase(criteria) },
            onSuccess = { searchResults ->
                _searchState.value = _searchState.value.copy(
                    isSearching = false,
                    suggestions = searchResults.map { it.content.toString() }
                )
                
                logUserAction("messages_searched", mapOf(
                    "query" to query,
                    "results" to searchResults.size
                ))
            },
            onError = { exception ->
                _searchState.value = _searchState.value.copy(
                    isSearching = false
                )
            }
        )
    }

    /**
     * Update typing status
     */
    fun updateTypingStatus(conversationId: String, isTyping: Boolean) {
        val currentUserId = getCurrentUserId() ?: return

        executeWithResult(
            showLoading = false,
            action = { updateTypingStatusUseCase(conversationId, currentUserId, isTyping) }
        )
    }

    /**
     * Update message composition state
     */
    fun updateComposition(
        text: String,
        mentions: List<MessageMention> = emptyList(),
        replyToMessageId: String? = null
    ) {
        _compositionState.value = _compositionState.value.copy(
            text = text,
            mentions = mentions,
            replyToMessageId = replyToMessageId
        )

        // Update typing status
        _currentConversation.value?.let { conversation ->
            updateTypingStatus(conversation.id, text.isNotEmpty())
        }
    }

    /**
     * Clear message composition
     */
    fun clearComposition() {
        _compositionState.value = MessageCompositionState()
        
        // Stop typing indicator
        _currentConversation.value?.let { conversation ->
            updateTypingStatus(conversation.id, false)
        }
    }

    /**
     * Add reaction to message
     */
    fun addReaction(messageId: String, emoji: String) {
        val currentUserId = getCurrentUserId() ?: return
        
        // Update message in list optimistically
        val currentMessages = _messages.value.items.toMutableList()
        val messageIndex = currentMessages.indexOfFirst { it.id == messageId }
        if (messageIndex != -1) {
            val message = currentMessages[messageIndex]
            val reactions = message.reactions.toMutableMap()
            val emojiReactions = reactions[emoji]?.toMutableList() ?: mutableListOf()
            
            // Check if user already reacted with this emoji
            if (emojiReactions.none { it.userId == currentUserId }) {
                emojiReactions.add(MessageReaction(currentUserId, emoji, Date()))
                reactions[emoji] = emojiReactions
                
                currentMessages[messageIndex] = message.copy(reactions = reactions)
                _messages.value = _messages.value.copy(items = currentMessages)
            }
        }

        logUserAction("reaction_added", mapOf(
            "message_id" to messageId,
            "emoji" to emoji
        ))
    }

    /**
     * Observe online status of participants
     */
    private fun observeOnlineStatus() {
        viewModelScope.launch {
            getOnlineStatusUseCase().collect { statusMap ->
                _onlineStatus.value = statusMap
            }
        }
    }

    /**
     * Observe typing indicators
     */
    private fun observeTypingIndicators() {
        // Implementation would listen to real-time typing indicators
        // This is a placeholder for the actual implementation
    }

    /**
     * Sync offline messages when coming back online
     */
    private fun syncOfflineMessages() {
        viewModelScope.launch {
            isOffline.collect { offline ->
                if (!offline && _offlineMessages.value.isNotEmpty()) {
                    executeWithResult(
                        showLoading = false,
                        action = { syncOfflineMessagesUseCase(_offlineMessages.value) },
                        onSuccess = {
                            _offlineMessages.value = emptyList()
                            loadConversations(refresh = true)
                        }
                    )
                }
            }
        }
    }

    /**
     * Create temporary message for optimistic UI updates
     */
    private fun createTempMessage(
        conversationId: String,
        content: MessageContent,
        replyToMessageId: String? = null
    ): Message {
        val currentUserId = getCurrentUserId() ?: ""
        return Message(
            id = "temp_${System.currentTimeMillis()}",
            conversationId = conversationId,
            senderId = currentUserId,
            content = content,
            status = MessageStatus(sent = false),
            replyTo = replyToMessageId,
            metadata = MessageMetadata(),
            sentAt = Date()
        )
    }

    /**
     * Add temporary message to list for optimistic UI
     */
    private fun addTempMessageToList(message: Message) {
        val currentMessages = _messages.value.items.toMutableList()
        currentMessages.add(0, message)
        _messages.value = _messages.value.copy(items = currentMessages)
    }

    /**
     * Replace temporary message with sent message
     */
    private fun replaceTempMessageWithSent(tempId: String, sentMessage: Message) {
        val currentMessages = _messages.value.items.toMutableList()
        val index = currentMessages.indexOfFirst { it.id == tempId }
        if (index != -1) {
            currentMessages[index] = sentMessage
            _messages.value = _messages.value.copy(items = currentMessages)
        }
    }

    /**
     * Mark temporary message as failed
     */
    private fun markTempMessageAsFailed(tempId: String) {
        val currentMessages = _messages.value.items.toMutableList()
        val index = currentMessages.indexOfFirst { it.id == tempId }
        if (index != -1) {
            val message = currentMessages[index]
            currentMessages[index] = message.copy(
                status = message.status.copy(failed = true)
            )
            _messages.value = _messages.value.copy(items = currentMessages)
            
            // Add to offline queue if offline
            if (isOffline.value) {
                val offlineMessage = OfflineMessage(
                    id = tempId,
                    conversationId = message.conversationId,
                    content = message.content,
                    tempId = tempId,
                    createdAt = Date()
                )
                _offlineMessages.value = _offlineMessages.value + offlineMessage
            }
        }
    }

    /**
     * Update conversation with last message
     */
    private fun updateConversationLastMessage(conversationId: String, message: Message) {
        val currentConversations = _conversations.value.items.toMutableList()
        val index = currentConversations.indexOfFirst { it.id == conversationId }
        if (index != -1) {
            currentConversations[index] = currentConversations[index].copy(
                lastMessage = message,
                lastActivityAt = message.sentAt
            )
            _conversations.value = _conversations.value.copy(items = currentConversations)
        }
    }

    /**
     * Clear current conversation
     */
    fun clearCurrentConversation() {
        _currentConversation.value = null
        _messages.value = ListState()
        clearComposition()
    }

    /**
     * Clear media upload state
     */
    fun clearMediaUploadState() {
        _mediaUploadState.value = MediaUploadState()
    }

    override fun refreshData() {
        loadConversations(refresh = true)
        _currentConversation.value?.let { conversation ->
            loadMessages(conversation.id)
        }
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 50
    }
}

/**
 * Message composition state
 */
data class MessageCompositionState(
    val text: String = "",
    val mentions: List<MessageMention> = emptyList(),
    val replyToMessageId: String? = null,
    val attachments: List<DraftAttachment> = emptyList()
)

/**
 * Media upload state
 */
data class MediaUploadState(
    val isUploading: Boolean = false,
    val progress: Float = 0f,
    val uploadedUrl: String? = null,
    val error: String? = null
)

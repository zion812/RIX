package com.rio.rostry.features.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.data.model.Message
import com.rio.rostry.core.data.repository.MessageRepository
import com.rio.rostry.core.data.repository.MessageThread
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<MessageUiState>(MessageUiState.Loading)
    val uiState = _uiState.asStateFlow()
    
    fun loadMessageThreads(userId: String) {
        viewModelScope.launch {
            _uiState.value = MessageUiState.Loading
            
            messageRepository.getMessageThreads(userId)
                .collect { threads ->
                    _uiState.value = MessageUiState.ThreadsLoaded(threads)
                }
        }
    }
    
    fun loadMessagesInThread(threadId: String) {
        viewModelScope.launch {
            _uiState.value = MessageUiState.Loading
            
            messageRepository.getMessagesInThread(threadId)
                .collect { messages ->
                    _uiState.value = MessageUiState.MessagesLoaded(messages)
                }
        }
    }
    
    fun sendMessage(senderId: String, receiverId: String, threadId: String, content: String) {
        viewModelScope.launch {
            val message = Message(
                senderId = senderId,
                receiverId = receiverId,
                threadId = threadId,
                content = content
            )
            
            messageRepository.sendMessage(message)
                .onSuccess {
                    // Message sent successfully, refresh the thread
                    loadMessagesInThread(threadId)
                }
                .onFailure { exception ->
                    _uiState.value = MessageUiState.Error(exception.message ?: "Failed to send message")
                }
        }
    }
    
    fun markMessagesAsRead(threadId: String, userId: String) {
        viewModelScope.launch {
            messageRepository.markMessagesAsRead(threadId, userId)
                .onSuccess {
                    // Refresh threads to update unread count
                    loadMessageThreads(userId)
                }
                .onFailure { exception ->
                    // Not critical, just log the error
                    println("Failed to mark messages as read: ${exception.message}")
                }
        }
    }
}

sealed class MessageUiState {
    object Loading : MessageUiState()
    data class ThreadsLoaded(val threads: List<MessageThread>) : MessageUiState()
    data class MessagesLoaded(val messages: List<Message>) : MessageUiState()
    data class Error(val message: String) : MessageUiState()
}
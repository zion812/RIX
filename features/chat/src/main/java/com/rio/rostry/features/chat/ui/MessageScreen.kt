package com.rio.rostry.features.chat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rio.rostry.core.data.model.Message
import com.rio.rostry.core.data.repository.MessageThread
import com.rio.rostry.features.chat.viewmodel.MessageUiState
import com.rio.rostry.features.chat.viewmodel.MessageViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageScreen(
    userId: String,
    threadId: String? = null,
    viewModel: MessageViewModel = hiltViewModel()
) {
    var messageState by remember { mutableStateOf<MessageUiState>(MessageUiState.Loading) }
    var newMessageText by remember { mutableStateOf("") }
    
    LaunchedEffect(userId, threadId) {
        if (threadId != null) {
            viewModel.loadMessagesInThread(threadId)
        } else {
            viewModel.loadMessageThreads(userId)
        }
    }
    
    LaunchedEffect(viewModel.uiState) {
        viewModel.uiState.collect { state ->
            messageState = state
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (threadId != null) {
            // Message thread view
            MessageThreadView(
                threadId = threadId,
                messageState = messageState,
                newMessageText = newMessageText,
                onMessageTextChange = { newMessageText = it },
                onSendMessage = { content ->
                    // In a real app, we would determine receiverId based on the thread
                    // For now, we'll use a placeholder
                    viewModel.sendMessage(
                        senderId = userId,
                        receiverId = "receiver_placeholder",
                        threadId = threadId,
                        content = content
                    )
                    newMessageText = ""
                }
            )
        } else {
            // Thread list view
            ThreadListView(
                messageState = messageState,
                onThreadSelected = { /* Navigate to thread */ }
            )
        }
    }
}

@Composable
fun ThreadListView(
    messageState: MessageUiState,
    onThreadSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Messages",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        when (messageState) {
            is MessageUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is MessageUiState.ThreadsLoaded -> {
                if (messageState.threads.isEmpty()) {
                    Text(
                        text = "No messages yet",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn {
                        items(messageState.threads) { thread ->
                            ThreadItem(
                                thread = thread,
                                onClick = { onThreadSelected(thread.threadId) }
                            )
                            Divider()
                        }
                    }
                }
            }
            is MessageUiState.Error -> {
                Text(
                    text = "Error: ${messageState.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {}
        }
    }
}

@Composable
fun ThreadItem(
    thread: MessageThread,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                // In a real app, we would display participant names
                Text(
                    text = "User ${thread.participantIds.firstOrNull() ?: "Unknown"}",
                    style = MaterialTheme.typography.titleMedium
                )
                
                thread.lastMessage?.let { message ->
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                thread.lastMessage?.let { message ->
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(message.sentAt),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                if (thread.unreadCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge {
                                Text(
                                    text = thread.unreadCount.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    ) {
                        Spacer(modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MessageThreadView(
    threadId: String,
    messageState: MessageUiState,
    newMessageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: (String) -> Unit
) {
    val listState = rememberLazyListState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Messages list
        when (messageState) {
            is MessageUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            }
            is MessageUiState.MessagesLoaded -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f)
                ) {
                    items(messageState.messages) { message ->
                        MessageItem(message = message)
                    }
                }
            }
            is MessageUiState.Error -> {
                Text(
                    text = "Error: ${messageState.message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            }
            else -> {}
        }
        
        // Message input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newMessageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = { Text("Type a message") },
                shape = RoundedCornerShape(24.dp)
            )
            
            IconButton(
                onClick = {
                    if (newMessageText.isNotBlank()) {
                        onSendMessage(newMessageText)
                    }
                },
                enabled = newMessageText.isNotBlank()
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Send,
                    contentDescription = "Send message"
                )
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                    .format(message.sentAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
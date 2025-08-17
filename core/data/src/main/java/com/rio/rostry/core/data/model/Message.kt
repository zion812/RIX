package com.rio.rostry.core.data.model

import java.util.Date

/**
 * Data model representing a message in the ROSTRY platform
 */
data class Message(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderId: String,
    val receiverId: String,
    val threadId: String, // Group messages by thread (typically based on listing or transfer)
    val content: String,
    val sentAt: Date = Date(),
    val isRead: Boolean = false
)
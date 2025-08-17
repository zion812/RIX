package com.rio.rostry.core.data.model

import java.util.Date
import java.util.UUID

/**
 * Data model representing a fowl transfer between users
 */
data class Transfer(
    val id: String = UUID.randomUUID().toString(),
    val fowlId: String,
    val giverId: String,
    val receiverId: String,
    val status: TransferStatus = TransferStatus.PENDING,
    val verificationDetails: Map<String, Any>? = null,
    val initiatedAt: Date = Date(),
    val verifiedAt: Date? = null,
    val rejectedAt: Date? = null
)

enum class TransferStatus {
    PENDING,
    VERIFIED,
    REJECTED
}
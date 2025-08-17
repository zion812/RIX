package com.rio.rostry.core.data.model

import java.util.Date

/**
 * Data model representing a coin transaction in the ROSTRY platform
 */
data class CoinTransaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String,
    val amount: Int, // Positive for credit, negative for debit
    val transactionType: CoinTransactionType,
    val relatedEntityId: String? = null, // ID of related entity (listing, transfer, etc.)
    val createdAt: Date = Date()
)

enum class CoinTransactionType {
    LISTING_FEE,
    TRANSFER_VERIFICATION,
    MAINTENANCE_FEE,
    ADMIN_CREDIT,
    ORDER_FEE
}
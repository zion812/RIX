package com.rio.rostry.core.common.model

/**
 * Status of coin transactions in the RIO platform
 */
enum class TransactionStatus {
    PENDING,        // Transaction initiated but not processed
    PROCESSING,     // Transaction being processed
    COMPLETED,      // Transaction completed successfully
    FAILED,         // Transaction failed
    CANCELLED,      // Transaction cancelled by user
    REFUNDED,       // Transaction refunded
    DISPUTED        // Transaction under dispute
}

/**
 * Types of coin transactions
 */
enum class TransactionType {
    COIN_PURCHASE,      // User purchased coins
    MARKETPLACE_LISTING, // User paid for marketplace listing
    PREMIUM_FEATURE,    // User paid for premium feature
    TRANSFER_FEE,       // User paid for fowl transfer
    REFUND,            // Refund transaction
    BONUS,             // Bonus coins awarded
    PENALTY            // Penalty deduction
}

/**
 * Payment methods supported
 */
enum class PaymentMethod {
    RAZORPAY,
    PAYU,
    UPI,
    NETBANKING,
    CARD,
    WALLET
}
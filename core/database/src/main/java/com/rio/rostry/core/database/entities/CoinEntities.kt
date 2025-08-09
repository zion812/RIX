package com.rio.rostry.core.database.entities

import androidx.room.*
import java.util.*

/**
 * Database entities for RIO coin-based payment system
 * Supports offline-first transactions with sync capabilities
 */

/**
 * Coin transaction entity for payment system
 */
@Entity(
    tableName = "coin_transactions",
    indices = [
        Index(value = ["user_id", "created_at"]),
        Index(value = ["transaction_type", "status"]),
        Index(value = ["purpose", "created_at"]),
        Index(value = ["is_synced", "created_at"])
    ]
)
data class CoinTransactionEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "transaction_type")
    val type: String, // CREDIT, SPEND, REFUND, EARN
    
    val amount: Int,
    val purpose: String, // COIN_PURCHASE, MARKETPLACE_LISTING, etc.
    val status: String, // PENDING, COMPLETED, FAILED
    
    @ColumnInfo(name = "order_id")
    val orderId: String? = null,
    
    @ColumnInfo(name = "payment_id")
    val paymentId: String? = null,
    
    @ColumnInfo(name = "balance_before")
    val balanceBefore: Int = 0,
    
    @ColumnInfo(name = "balance_after")
    val balanceAfter: Int = 0,
    
    val metadata: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
    
    @ColumnInfo(name = "synced_at")
    val syncedAt: Date? = null,
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false
)

/**
 * Coin order entity for purchase tracking
 */
@Entity(
    tableName = "coin_orders",
    indices = [
        Index(value = ["user_id", "status"]),
        Index(value = ["order_id"], unique = true),
        Index(value = ["created_at"]),
        Index(value = ["status", "expires_at"])
    ]
)
data class CoinOrderEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "order_id")
    val orderId: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "package_id")
    val packageId: String,
    
    val coins: Int,
    
    @ColumnInfo(name = "bonus_coins")
    val bonusCoins: Int,
    
    @ColumnInfo(name = "total_coins")
    val totalCoins: Int,
    
    val amount: Double, // Amount in INR
    val currency: String = "INR",
    val status: String, // CREATED, PENDING, COMPLETED, FAILED, CANCELLED
    
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String,
    
    @ColumnInfo(name = "payment_id")
    val paymentId: String? = null,
    
    @ColumnInfo(name = "user_tier")
    val userTier: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
    
    @ColumnInfo(name = "expires_at")
    val expiresAt: Date? = null,
    
    @ColumnInfo(name = "completed_at")
    val completedAt: Date? = null
)

/**
 * Refund request entity
 */
@Entity(
    tableName = "refund_requests",
    indices = [
        Index(value = ["user_id", "status"]),
        Index(value = ["order_id"]),
        Index(value = ["created_at"]),
        Index(value = ["status", "priority"])
    ]
)
data class RefundRequestEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "order_id")
    val orderId: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "refund_type")
    val refundType: String, // IMMEDIATE, STANDARD, DISPUTE
    
    @ColumnInfo(name = "requested_amount")
    val requestedAmount: Double,
    
    val reason: String,
    val evidence: String? = null,
    val status: String, // PENDING, APPROVED, REJECTED, PROCESSED
    val priority: String, // HIGH, MEDIUM, LOW
    
    @ColumnInfo(name = "razorpay_refund_id")
    val razorpayRefundId: String? = null,
    
    @ColumnInfo(name = "processed_amount")
    val processedAmount: Double? = null,
    
    @ColumnInfo(name = "failure_reason")
    val failureReason: String? = null,
    
    @ColumnInfo(name = "auto_process_eligible")
    val autoProcessEligible: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "processed_at")
    val processedAt: Date? = null
)

/**
 * Dispute entity for marketplace transactions
 */
@Entity(
    tableName = "disputes",
    indices = [
        Index(value = ["transaction_id"]),
        Index(value = ["disputant_id", "status"]),
        Index(value = ["respondent_id", "status"]),
        Index(value = ["status", "created_at"]),
        Index(value = ["mediator_id", "status"])
    ]
)
data class DisputeEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "transaction_id")
    val transactionId: String,
    
    @ColumnInfo(name = "disputant_id")
    val disputantId: String,
    
    @ColumnInfo(name = "respondent_id")
    val respondentId: String,
    
    @ColumnInfo(name = "dispute_type")
    val disputeType: String, // FRAUD, SERVICE_NOT_DELIVERED, QUALITY_ISSUE
    
    val reason: String,
    val evidence: String? = null,
    
    @ColumnInfo(name = "requested_resolution")
    val requestedResolution: String,
    
    val status: String, // OPEN, ESCALATED, RESOLVED, CLOSED
    val priority: String, // HIGH, MEDIUM, LOW
    
    @ColumnInfo(name = "escalation_level")
    val escalationLevel: Int = 0,
    
    @ColumnInfo(name = "mediator_id")
    val mediatorId: String? = null,
    
    @ColumnInfo(name = "resolution_deadline")
    val resolutionDeadline: Date,
    
    @ColumnInfo(name = "escrow_amount")
    val escrowAmount: Int,
    
    @ColumnInfo(name = "escrow_status")
    val escrowStatus: String, // HELD, RELEASED
    
    val resolution: String? = null,
    val reasoning: String? = null,
    
    @ColumnInfo(name = "resolver_id")
    val resolverId: String? = null,
    
    @ColumnInfo(name = "compensation_amount")
    val compensationAmount: Double? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "escalated_at")
    val escalatedAt: Date? = null,
    
    @ColumnInfo(name = "resolved_at")
    val resolvedAt: Date? = null
)

/**
 * Fraud check entity for security monitoring
 */
@Entity(
    tableName = "fraud_checks",
    indices = [
        Index(value = ["user_id", "created_at"]),
        Index(value = ["risk_score", "action"]),
        Index(value = ["order_id"])
    ]
)
data class FraudCheckEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "order_id")
    val orderId: String? = null,
    
    @ColumnInfo(name = "risk_score")
    val riskScore: Int,
    
    val action: String, // ALLOW, VERIFY, BLOCK, MONITOR
    val factors: String, // JSON array of risk factors
    
    @ColumnInfo(name = "primary_reason")
    val primaryReason: String? = null,
    
    @ColumnInfo(name = "device_info")
    val deviceInfo: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date
)

/**
 * Suspicious activity entity for monitoring
 */
@Entity(
    tableName = "suspicious_activities",
    indices = [
        Index(value = ["user_id", "type"]),
        Index(value = ["status", "created_at"]),
        Index(value = ["type", "created_at"])
    ]
)
data class SuspiciousActivityEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    val type: String, // VELOCITY_ATTACK, UNUSUAL_SPENDING, DEVICE_ANOMALY
    val metadata: String, // JSON metadata
    val status: String, // FLAGGED, REVIEWED, RESOLVED, FALSE_POSITIVE
    
    @ColumnInfo(name = "reviewed_by")
    val reviewedBy: String? = null,
    
    @ColumnInfo(name = "review_notes")
    val reviewNotes: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "reviewed_at")
    val reviewedAt: Date? = null
)

/**
 * Coin package entity for purchase options
 */
@Entity(
    tableName = "coin_packages",
    indices = [
        Index(value = ["user_tier", "active"]),
        Index(value = ["package_id"], unique = true)
    ]
)
data class CoinPackageEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "package_id")
    val packageId: String,
    
    val name: String,
    val description: String,
    val coins: Int,
    
    @ColumnInfo(name = "bonus_coins")
    val bonusCoins: Int,
    
    @ColumnInfo(name = "total_coins")
    val totalCoins: Int,
    
    val price: Double, // Price in INR
    
    @ColumnInfo(name = "user_tier")
    val userTier: String, // GENERAL, FARMER, ENTHUSIAST, ALL
    
    val active: Boolean = true,
    val featured: Boolean = false,
    
    @ColumnInfo(name = "discount_percentage")
    val discountPercentage: Int = 0,
    
    @ColumnInfo(name = "valid_from")
    val validFrom: Date? = null,
    
    @ColumnInfo(name = "valid_until")
    val validUntil: Date? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
)

/**
 * User coin balance entity for tracking
 */
@Entity(
    tableName = "user_coin_balances",
    indices = [
        Index(value = ["user_id"], unique = true),
        Index(value = ["last_updated"])
    ]
)
data class UserCoinBalanceEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    val balance: Int,
    
    @ColumnInfo(name = "total_earned")
    val totalEarned: Int = 0,
    
    @ColumnInfo(name = "total_spent")
    val totalSpent: Int = 0,
    
    @ColumnInfo(name = "total_purchased")
    val totalPurchased: Int = 0,
    
    @ColumnInfo(name = "last_transaction_id")
    val lastTransactionId: String? = null,
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Date,
    
    @ColumnInfo(name = "synced_at")
    val syncedAt: Date? = null,
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false
)

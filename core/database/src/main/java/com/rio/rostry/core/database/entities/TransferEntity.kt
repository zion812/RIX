package com.rio.rostry.core.database.entities

import androidx.room.*
import java.util.*

/**
 * Room entity for fowl ownership transfers with offline-first capabilities
 * Mirrors the Firestore transfers collection structure
 */
@Entity(
    tableName = "transfers",
    indices = [
        Index(value = ["fowl_id"]),
        Index(value = ["from_user_id"]),
        Index(value = ["to_user_id"]),
        Index(value = ["transfer_status"]),
        Index(value = ["transfer_type"]),
        Index(value = ["initiated_at"]),
        Index(value = ["sync_status"]),
        Index(value = ["verification_required"]),
        Index(value = ["is_deleted"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = FowlEntity::class,
            parentColumns = ["id"],
            childColumns = ["fowl_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["from_user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["to_user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TransferEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    override val id: String,
    
    @ColumnInfo(name = "fowl_id")
    val fowlId: String,
    
    @ColumnInfo(name = "from_user_id")
    val fromUserId: String,
    
    @ColumnInfo(name = "to_user_id")
    val toUserId: String,
    
    // Transfer details
    @ColumnInfo(name = "transfer_type")
    val transferType: String, // SALE, GIFT, INHERITANCE, BREEDING_LOAN, TRADE, RETURN
    
    @ColumnInfo(name = "transfer_status")
    val transferStatus: String, // INITIATED, PENDING_APPROVAL, APPROVED, IN_TRANSIT, COMPLETED, CANCELLED, REJECTED
    
    @ColumnInfo(name = "transfer_method")
    val transferMethod: String, // DIRECT, COURIER, TRANSPORT, PICKUP
    
    // Financial information
    @ColumnInfo(name = "amount")
    val amount: Double? = null,
    
    @ColumnInfo(name = "currency")
    val currency: String = "INR",
    
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String? = null, // CASH, BANK_TRANSFER, UPI, CHEQUE, BARTER
    
    @ColumnInfo(name = "payment_status")
    val paymentStatus: String = "PENDING", // PENDING, PAID, FAILED, REFUNDED
    
    @ColumnInfo(name = "payment_reference")
    val paymentReference: String? = null,
    
    @ColumnInfo(name = "platform_fee")
    val platformFee: Double = 0.0,
    
    @ColumnInfo(name = "payment_fee")
    val paymentFee: Double = 0.0,
    
    @ColumnInfo(name = "delivery_fee")
    val deliveryFee: Double = 0.0,
    
    @ColumnInfo(name = "total_amount")
    val totalAmount: Double? = null,
    
    // Transfer timeline
    @ColumnInfo(name = "initiated_at")
    val initiatedAt: Date,
    
    @ColumnInfo(name = "approved_at")
    val approvedAt: Date? = null,
    
    @ColumnInfo(name = "shipped_at")
    val shippedAt: Date? = null,
    
    @ColumnInfo(name = "delivered_at")
    val deliveredAt: Date? = null,
    
    @ColumnInfo(name = "completed_at")
    val completedAt: Date? = null,
    
    @ColumnInfo(name = "cancelled_at")
    val cancelledAt: Date? = null,
    
    @ColumnInfo(name = "expected_delivery_date")
    val expectedDeliveryDate: Date? = null,
    
    // Verification and documentation
    @ColumnInfo(name = "verification_required")
    val verificationRequired: Boolean = true,
    
    @ColumnInfo(name = "verification_status")
    val verificationStatus: String = "PENDING", // PENDING, VERIFIED, REJECTED
    
    @ColumnInfo(name = "verification_documents")
    val verificationDocuments: List<String> = emptyList(),
    
    @ColumnInfo(name = "verification_notes")
    val verificationNotes: String? = null,
    
    @ColumnInfo(name = "verified_by")
    val verifiedBy: String? = null,
    
    @ColumnInfo(name = "verified_at")
    val verifiedAt: Date? = null,
    
    @ColumnInfo(name = "transfer_certificate")
    val transferCertificate: String? = null,
    
    @ColumnInfo(name = "ownership_proof")
    val ownershipProof: String? = null,
    
    // Delivery information
    @ColumnInfo(name = "pickup_address")
    val pickupAddress: String? = null,
    
    @ColumnInfo(name = "delivery_address")
    val deliveryAddress: String,
    
    @ColumnInfo(name = "delivery_instructions")
    val deliveryInstructions: String? = null,
    
    @ColumnInfo(name = "tracking_number")
    val trackingNumber: String? = null,
    
    @ColumnInfo(name = "courier_service")
    val courierService: String? = null,
    
    @ColumnInfo(name = "delivery_contact_name")
    val deliveryContactName: String? = null,
    
    @ColumnInfo(name = "delivery_contact_phone")
    val deliveryContactPhone: String? = null,
    
    // Transfer conditions and terms
    @ColumnInfo(name = "conditions")
    val conditions: List<String> = emptyList(),
    
    @ColumnInfo(name = "warranty_period_days")
    val warrantyPeriodDays: Int? = null,
    
    @ColumnInfo(name = "return_policy")
    val returnPolicy: String? = null,
    
    @ColumnInfo(name = "health_guarantee")
    val healthGuarantee: Boolean = false,
    
    @ColumnInfo(name = "breeding_rights")
    val breedingRights: Boolean = true,
    
    @ColumnInfo(name = "show_rights")
    val showRights: Boolean = true,
    
    // Communication and notes
    @ColumnInfo(name = "transfer_notes")
    val transferNotes: String? = null,
    
    @ColumnInfo(name = "seller_notes")
    val sellerNotes: String? = null,
    
    @ColumnInfo(name = "buyer_notes")
    val buyerNotes: String? = null,
    
    @ColumnInfo(name = "admin_notes")
    val adminNotes: String? = null,
    
    @ColumnInfo(name = "cancellation_reason")
    val cancellationReason: String? = null,
    
    @ColumnInfo(name = "rejection_reason")
    val rejectionReason: String? = null,
    
    // Related entities
    @ColumnInfo(name = "related_listing_id")
    val relatedListingId: String? = null,
    
    @ColumnInfo(name = "related_conversation_id")
    val relatedConversationId: String? = null,
    
    @ColumnInfo(name = "parent_transfer_id")
    val parentTransferId: String? = null, // For return transfers
    
    // Quality and feedback
    @ColumnInfo(name = "seller_rating")
    val sellerRating: Int? = null, // 1-5 stars
    
    @ColumnInfo(name = "buyer_rating")
    val buyerRating: Int? = null, // 1-5 stars
    
    @ColumnInfo(name = "seller_feedback")
    val sellerFeedback: String? = null,
    
    @ColumnInfo(name = "buyer_feedback")
    val buyerFeedback: String? = null,
    
    @ColumnInfo(name = "platform_rating")
    val platformRating: Int? = null,
    
    @ColumnInfo(name = "platform_feedback")
    val platformFeedback: String? = null,
    
    // Dispute information
    @ColumnInfo(name = "dispute_raised")
    val disputeRaised: Boolean = false,
    
    @ColumnInfo(name = "dispute_reason")
    val disputeReason: String? = null,
    
    @ColumnInfo(name = "dispute_status")
    val disputeStatus: String? = null, // OPEN, INVESTIGATING, RESOLVED, CLOSED
    
    @ColumnInfo(name = "dispute_resolution")
    val disputeResolution: String? = null,
    
    @ColumnInfo(name = "dispute_resolved_at")
    val disputeResolvedAt: Date? = null,
    
    // Blockchain/immutable record (future feature)
    @ColumnInfo(name = "blockchain_hash")
    val blockchainHash: String? = null,
    
    @ColumnInfo(name = "blockchain_verified")
    val blockchainVerified: Boolean = false,
    
    // Regional information (flattened)
    val region: String = "",
    val district: String = "",
    val mandal: String? = null,
    val village: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,

    // Sync fields (flattened)
    @ColumnInfo(name = "last_sync_time")
    override val lastSyncTime: Date? = null,

    @ColumnInfo(name = "sync_status")
    val syncStatusString: String = "PENDING_UPLOAD",

    @ColumnInfo(name = "conflict_version")
    override val conflictVersion: Long = 1L,

    @ColumnInfo(name = "is_deleted")
    override val isDeleted: Boolean = false,

    @ColumnInfo(name = "created_at")
    override val createdAt: Date = Date(),

    @ColumnInfo(name = "updated_at")
    override val updatedAt: Date = Date(),

    // Additional sync fields
    @ColumnInfo(name = "sync_priority")
    val syncPriority: Int = 1,

    @ColumnInfo(name = "has_conflict")
    val hasConflict: Boolean = false,

    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,

    @ColumnInfo(name = "data_size")
    val dataSize: Long = 0L
) : SyncableEntity {

    // SyncableEntity implementation
    override val syncStatus: SyncStatus
        get() = SyncStatus.valueOf(syncStatusString)
}

/**
 * DAO for transfer entities with offline-optimized queries
 */
@Dao
interface TransferDao : BaseSyncableDao<TransferEntity> {
    
    @Query("SELECT * FROM transfers WHERE id = :id AND is_deleted = 0")
    override suspend fun getById(id: String): TransferEntity?
    
    @Query("SELECT * FROM transfers WHERE sync_status = :status AND is_deleted = 0")
    override suspend fun getAllByStatus(status: SyncStatus): List<TransferEntity>
    
    @Query("SELECT * FROM transfers WHERE sync_priority = :priority AND is_deleted = 0")
    override suspend fun getAllByPriority(priority: SyncPriority): List<TransferEntity>
    
    @Query("SELECT * FROM transfers WHERE sync_status IN ('PENDING_UPLOAD', 'FAILED') AND is_deleted = 0 ORDER BY sync_priority ASC, initiated_at ASC")
    override suspend fun getAllPendingSync(): List<TransferEntity>
    
    @Query("SELECT * FROM transfers WHERE has_conflict = 1 AND is_deleted = 0")
    override suspend fun getAllConflicted(): List<TransferEntity>
    
    @Query("SELECT * FROM transfers WHERE region = :region AND district = :district AND is_deleted = 0")
    override suspend fun getAllInRegion(region: String, district: String): List<TransferEntity>
    
    // Transfer-specific queries
    @Query("SELECT * FROM transfers WHERE fowl_id = :fowlId AND is_deleted = 0 ORDER BY initiated_at DESC")
    suspend fun getTransfersByFowl(fowlId: String): List<TransferEntity>
    
    @Query("SELECT * FROM transfers WHERE (from_user_id = :userId OR to_user_id = :userId) AND is_deleted = 0 ORDER BY initiated_at DESC")
    suspend fun getTransfersByUser(userId: String): List<TransferEntity>
    
    @Query("SELECT * FROM transfers WHERE from_user_id = :userId AND is_deleted = 0 ORDER BY initiated_at DESC")
    suspend fun getTransfersBySeller(userId: String): List<TransferEntity>
    
    @Query("SELECT * FROM transfers WHERE to_user_id = :userId AND is_deleted = 0 ORDER BY initiated_at DESC")
    suspend fun getTransfersByBuyer(userId: String): List<TransferEntity>
    
    @Query("SELECT * FROM transfers WHERE transfer_status = :status AND is_deleted = 0 ORDER BY initiated_at ASC")
    suspend fun getTransfersByStatus(status: String): List<TransferEntity>
    
    @Query("SELECT * FROM transfers WHERE verification_status = 'PENDING' AND verification_required = 1 AND is_deleted = 0 ORDER BY initiated_at ASC")
    suspend fun getPendingVerificationTransfers(): List<TransferEntity>
    
    @Query("SELECT * FROM transfers WHERE transfer_status IN ('INITIATED', 'PENDING_APPROVAL', 'APPROVED', 'IN_TRANSIT') AND is_deleted = 0")
    suspend fun getActiveTransfers(): List<TransferEntity>
    
    @Query("SELECT * FROM transfers WHERE transfer_status = 'COMPLETED' AND (seller_rating IS NULL OR buyer_rating IS NULL) AND completed_at > :recentDate AND is_deleted = 0")
    suspend fun getTransfersPendingRating(recentDate: Date): List<TransferEntity>
    
    @Query("SELECT * FROM transfers WHERE dispute_raised = 1 AND dispute_status IN ('OPEN', 'INVESTIGATING') AND is_deleted = 0")
    suspend fun getActiveDisputes(): List<TransferEntity>
    
    @Query("SELECT * FROM transfers WHERE expected_delivery_date < :currentDate AND transfer_status = 'IN_TRANSIT' AND is_deleted = 0")
    suspend fun getOverdueDeliveries(currentDate: Date = Date()): List<TransferEntity>
    
    @Query("SELECT COUNT(*) FROM transfers WHERE (from_user_id = :userId OR to_user_id = :userId) AND transfer_status = 'COMPLETED' AND is_deleted = 0")
    suspend fun getCompletedTransferCount(userId: String): Int
    
    @Query("SELECT SUM(total_amount) FROM transfers WHERE from_user_id = :userId AND transfer_status = 'COMPLETED' AND payment_status = 'PAID' AND is_deleted = 0")
    suspend fun getTotalSalesAmount(userId: String): Double?
    
    @Query("SELECT SUM(total_amount) FROM transfers WHERE to_user_id = :userId AND transfer_status = 'COMPLETED' AND payment_status = 'PAID' AND is_deleted = 0")
    suspend fun getTotalPurchaseAmount(userId: String): Double?
    
    @Query("SELECT AVG(CASE WHEN from_user_id = :userId THEN buyer_rating ELSE seller_rating END) FROM transfers WHERE (from_user_id = :userId OR to_user_id = :userId) AND transfer_status = 'COMPLETED' AND is_deleted = 0")
    suspend fun getAverageRating(userId: String): Double?
    
    // Status updates
    @Query("UPDATE transfers SET transfer_status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateTransferStatus(id: String, status: String, updatedAt: Date = Date())
    
    @Query("UPDATE transfers SET payment_status = :status, payment_reference = :reference WHERE id = :id")
    suspend fun updatePaymentStatus(id: String, status: String, reference: String? = null)
    
    @Query("UPDATE transfers SET verification_status = :status, verified_by = :verifiedBy, verified_at = :verifiedAt WHERE id = :id")
    suspend fun updateVerificationStatus(id: String, status: String, verifiedBy: String? = null, verifiedAt: Date = Date())
    
    @Query("UPDATE transfers SET tracking_number = :trackingNumber, courier_service = :courierService WHERE id = :id")
    suspend fun updateTrackingInfo(id: String, trackingNumber: String, courierService: String)
    
    @Query("UPDATE transfers SET seller_rating = :rating, seller_feedback = :feedback WHERE id = :id")
    suspend fun updateSellerRating(id: String, rating: Int, feedback: String? = null)
    
    @Query("UPDATE transfers SET buyer_rating = :rating, buyer_feedback = :feedback WHERE id = :id")
    suspend fun updateBuyerRating(id: String, rating: Int, feedback: String? = null)
    
    @Query("UPDATE transfers SET dispute_raised = 1, dispute_reason = :reason, dispute_status = 'OPEN' WHERE id = :id")
    suspend fun raiseDispute(id: String, reason: String)
    
    // Sync operations
    @Query("UPDATE transfers SET sync_status = :status, last_sync_time = :lastSyncTime WHERE id = :id")
    override suspend fun updateSyncStatus(id: String, status: SyncStatus, lastSyncTime: Date)
    
    @Query("UPDATE transfers SET conflict_version = :version WHERE id = :id")
    override suspend fun updateConflictVersion(id: String, version: Long)
    
    @Query("UPDATE transfers SET is_deleted = 1, updated_at = :deletedAt WHERE id = :id")
    override suspend fun markAsDeleted(id: String, deletedAt: Date)
    
    @Query("UPDATE transfers SET retry_count = retry_count + 1 WHERE id = :id")
    override suspend fun incrementRetryCount(id: String)
    
    @Query("UPDATE transfers SET retry_count = 0 WHERE id = :id")
    override suspend fun clearRetryCount(id: String)
    
    // Cleanup operations
    @Query("DELETE FROM transfers WHERE sync_status = 'SYNCED' AND updated_at < :olderThan AND sync_priority = 'LOW'")
    override suspend fun deleteOldSyncedItems(olderThan: Date): Int
    
    @Query("DELETE FROM transfers WHERE id IN (SELECT id FROM transfers WHERE sync_priority = 'LOW' AND sync_status = 'SYNCED' ORDER BY last_sync_time ASC LIMIT :limit)")
    override suspend fun deleteLowPriorityItems(limit: Int): Int
    
    @Query("SELECT SUM(data_size) FROM transfers")
    override suspend fun getStorageSize(): Long
    
    // Batch operations for sync
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(entity: TransferEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertAll(entities: List<TransferEntity>): List<Long>
    
    @Update
    override suspend fun update(entity: TransferEntity): Int
    
    @Delete
    override suspend fun delete(entity: TransferEntity): Int
    
    @Query("DELETE FROM transfers WHERE id = :id")
    override suspend fun deleteById(id: String): Int
    
    // Performance optimization queries
    @Query("SELECT id, fowl_id, from_user_id, to_user_id, transfer_status, amount, initiated_at FROM transfers WHERE (from_user_id = :userId OR to_user_id = :userId) AND is_deleted = 0 ORDER BY initiated_at DESC LIMIT :limit")
    suspend fun getTransferSummariesByUser(userId: String, limit: Int = 100): List<TransferSummary>
    
    @Query("SELECT COUNT(*) FROM transfers WHERE sync_status = 'PENDING_UPLOAD' AND is_deleted = 0")
    suspend fun getPendingSyncCount(): Int
    
    @Query("SELECT COUNT(*) FROM transfers WHERE has_conflict = 1 AND is_deleted = 0")
    suspend fun getConflictCount(): Int
}

/**
 * Lightweight transfer summary for list displays
 */
data class TransferSummary(
    val id: String,
    @ColumnInfo(name = "fowl_id") val fowlId: String,
    @ColumnInfo(name = "from_user_id") val fromUserId: String,
    @ColumnInfo(name = "to_user_id") val toUserId: String,
    @ColumnInfo(name = "transfer_status") val transferStatus: String,
    val amount: Double?,
    @ColumnInfo(name = "initiated_at") val initiatedAt: Date
)

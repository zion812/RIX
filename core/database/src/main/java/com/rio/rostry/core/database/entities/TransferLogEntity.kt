package com.rio.rostry.core.database.entities

import androidx.room.*
import java.util.*

/**
 * Room entity for immutable transfer logs with verified workflow
 * This entity tracks the complete lifecycle of a fowl transfer with emphasis on
 * receiver verification and audit trail.
 */
@Entity(
    tableName = "transfer_logs",
    indices = [
        Index(value = ["fowl_id"]),
        Index(value = ["from_user_id"]),
        Index(value = ["to_user_id"]),
        Index(value = ["transfer_status"]),
        Index(value = ["verification_status"]),
        Index(value = ["initiated_at"]),
        Index(value = ["verified_at"]),
        Index(value = ["is_deleted"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = FowlEntity::class,
            parentColumns = ["id"],
            childColumns = ["fowl_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TransferLogEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "fowl_id")
    val fowlId: String,
    
    @ColumnInfo(name = "from_user_id")
    val fromUserId: String,
    
    @ColumnInfo(name = "to_user_id")
    val toUserId: String,
    
    // Transfer status tracking
    @ColumnInfo(name = "transfer_status")
    val transferStatus: String, // PENDING, VERIFIED, REJECTED, COMPLETED, CANCELLED
    
    // Verification information
    @ColumnInfo(name = "verification_required")
    val verificationRequired: Boolean = true,
    
    @ColumnInfo(name = "verification_status")
    val verificationStatus: String, // PENDING, VERIFIED, REJECTED
    
    @ColumnInfo(name = "verification_documents")
    val verificationDocuments: List<String> = emptyList(),
    
    @ColumnInfo(name = "verification_notes")
    val verificationNotes: String? = null,
    
    @ColumnInfo(name = "verified_by")
    val verifiedBy: String? = null,
    
    @ColumnInfo(name = "verified_at")
    val verifiedAt: Date? = null,
    
    // Transfer details for verification
    @ColumnInfo(name = "expected_price")
    val expectedPrice: Double? = null,
    
    @ColumnInfo(name = "expected_color")
    val expectedColor: String? = null,
    
    @ColumnInfo(name = "expected_age_weeks")
    val expectedAgeWeeks: Int? = null,
    
    @ColumnInfo(name = "expected_weight_grams")
    val expectedWeightGrams: Int? = null,
    
    @ColumnInfo(name = "photo_reference")
    val photoReference: String? = null,
    
    // Timeline
    @ColumnInfo(name = "initiated_at")
    val initiatedAt: Date,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date,
    
    @ColumnInfo(name = "completed_at")
    val completedAt: Date? = null,
    
    @ColumnInfo(name = "cancelled_at")
    val cancelledAt: Date? = null,
    
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
    
    // Audit fields
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)

/**
 * DAO for transfer log operations with verified workflow support
 */
@Dao
interface TransferLogDao {
    
    @Query("SELECT * FROM transfer_logs WHERE id = :id AND is_deleted = 0")
    suspend fun getById(id: String): TransferLogEntity?
    
    @Query("SELECT * FROM transfer_logs WHERE fowl_id = :fowlId AND is_deleted = 0 ORDER BY initiated_at DESC")
    suspend fun getByFowlId(fowlId: String): List<TransferLogEntity>
    
    @Query("SELECT * FROM transfer_logs WHERE from_user_id = :userId AND is_deleted = 0 ORDER BY initiated_at DESC")
    suspend fun getTransfersFromUser(userId: String): List<TransferLogEntity>
    
    @Query("SELECT * FROM transfer_logs WHERE to_user_id = :userId AND is_deleted = 0 ORDER BY initiated_at DESC")
    suspend fun getTransfersToUser(userId: String): List<TransferLogEntity>
    
    @Query("SELECT * FROM transfer_logs WHERE transfer_status = 'PENDING' AND verification_required = 1 AND to_user_id = :userId AND is_deleted = 0 ORDER BY initiated_at DESC")
    suspend fun getPendingVerificationForUser(userId: String): List<TransferLogEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transferLog: TransferLogEntity): Long
    
    @Update
    suspend fun update(transferLog: TransferLogEntity): Int
    
    @Query("UPDATE transfer_logs SET is_deleted = 1 WHERE id = :id")
    suspend fun markAsDeleted(id: String): Int
    
    @Query("UPDATE transfer_logs SET transfer_status = :status, verification_status = :verificationStatus, verified_by = :verifiedBy, verified_at = :verifiedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateVerificationStatus(
        id: String,
        status: String,
        verificationStatus: String,
        verifiedBy: String?,
        verifiedAt: Date?,
        updatedAt: Date
    ): Int

}




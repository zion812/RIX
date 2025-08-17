package com.rio.rostry.core.data.repository

import com.rio.rostry.core.database.dao.TransferDao
import com.rio.rostry.core.database.entities.TransferEntity
import com.rio.rostry.core.data.model.Transfer
import com.rio.rostry.core.data.model.TransferStatus
import com.rio.rostry.core.data.util.DataSyncManager
import com.rio.rostry.core.data.util.SyncOperation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of TransferRepository that works with both local Room database and Firestore
 */
class TransferRepositoryImpl @Inject constructor(
    private val transferDao: TransferDao,
    private val remoteTransferRepository: TransferRepository,
    private val syncManager: DataSyncManager
) : TransferRepository {

    override suspend fun createTransfer(transfer: Transfer): Result<String> {
        return try {
            // Create local entity
            val transferEntity = transfer.toEntity()
            transferDao.insertTransfer(transferEntity)
            
            // Queue for sync
            syncManager.queueSyncOperation(
                SyncOperation.Create(
                    collection = "transfers",
                    documentId = transferEntity.id,
                    data = transferEntity.toMap()
                )
            )
            
            // Create remote transfer
            val remoteResult = remoteTransferRepository.createTransfer(transfer)
            if (remoteResult.isSuccess) {
                // Update local entity with remote ID if needed
                val remoteId = remoteResult.getOrNull()
                if (remoteId != null && remoteId != transferEntity.id) {
                    transferDao.updateTransfer(transferEntity.copy(id = remoteId))
                }
                Result.success(remoteId ?: transferEntity.id)
            } else {
                Result.success(transferEntity.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTransfer(transferId: String): Result<Transfer> {
        return try {
            // Try to get from local database first
            val localTransfer = transferDao.getTransferById(transferId)
            if (localTransfer != null) {
                Result.success(localTransfer.toModel())
            } else {
                // Fallback to remote repository
                remoteTransferRepository.getTransfer(transferId)
            }
        } catch (e: Exception) {
            // Fallback to remote repository
            remoteTransferRepository.getTransfer(transferId)
        }
    }

    override suspend fun updateTransfer(transfer: Transfer): Result<Unit> {
        return try {
            val transferEntity = transfer.toEntity()
            transferDao.updateTransfer(transferEntity)
            
            // Queue for sync
            syncManager.queueSyncOperation(
                SyncOperation.Update(
                    collection = "transfers",
                    documentId = transferEntity.id,
                    data = transferEntity.toMap()
                )
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTransfersForUser(userId: String): Flow<List<Transfer>> {
        return transferDao.getTransfersByUser(userId)
            .map { entities ->
                entities.map { it.toModel() }
            }
    }

    override suspend fun verifyTransfer(
        transferId: String,
        verificationDetails: Map<String, Any>
    ): Result<Transfer> {
        return try {
            // Get current transfer
            val transferResult = getTransfer(transferId)
            if (transferResult.isFailure) {
                return Result.failure(transferResult.exceptionOrNull() ?: Exception("Failed to get transfer"))
            }

            val transfer = transferResult.getOrNull()!!
            if (transfer.status != TransferStatus.PENDING) {
                return Result.failure(IllegalStateException("Transfer must be in PENDING state to verify"))
            }

            // Update transfer with verification details
            val verifiedTransfer = transfer.copy(
                status = TransferStatus.VERIFIED,
                verificationDetails = verificationDetails,
                verifiedAt = java.util.Date()
            )

            // Update locally
            val updateResult = updateTransfer(verifiedTransfer)
            if (updateResult.isFailure) {
                return Result.failure(updateResult.exceptionOrNull() ?: Exception("Failed to update transfer"))
            }

            // Also update remote
            remoteTransferRepository.verifyTransfer(transferId, verificationDetails)

            Result.success(verifiedTransfer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectTransfer(transferId: String): Result<Transfer> {
        return try {
            // Get current transfer
            val transferResult = getTransfer(transferId)
            if (transferResult.isFailure) {
                return Result.failure(transferResult.exceptionOrNull() ?: Exception("Failed to get transfer"))
            }

            val transfer = transferResult.getOrNull()!!
            if (transfer.status != TransferStatus.PENDING) {
                return Result.failure(IllegalStateException("Transfer must be in PENDING state to reject"))
            }

            // Update transfer status to rejected
            val rejectedTransfer = transfer.copy(
                status = TransferStatus.REJECTED,
                rejectedAt = java.util.Date()
            )

            // Update locally
            val updateResult = updateTransfer(rejectedTransfer)
            if (updateResult.isFailure) {
                return Result.failure(updateResult.exceptionOrNull() ?: Exception("Failed to update transfer"))
            }

            // Also update remote
            remoteTransferRepository.rejectTransfer(transferId)

            Result.success(rejectedTransfer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extension function to convert Transfer model to TransferEntity
     */
    private fun Transfer.toEntity(): TransferEntity {
        return TransferEntity(
            id = this.id,
            fowlId = this.fowlId,
            fromUserId = this.giverId,
            toUserId = this.receiverId,
            status = this.status.name.lowercase(),
            initiatedAt = this.initiatedAt,
            verifiedAt = this.verifiedAt,
            rejectedAt = this.rejectedAt,
            verificationDetails = this.verificationDetails,
            // Set default values for other required fields
            transferType = "direct",
            deliveryMethod = "pickup",
            deliveryFee = 0.0,
            totalAmount = 0.0,
            createdAt = this.initiatedAt,
            updatedAt = java.util.Date(),
            syncStatus = "pending",
            syncPriority = 1,
            isDeleted = false
        )
    }

    /**
     * Extension function to convert TransferEntity to Transfer model
     */
    private fun TransferEntity.toModel(): Transfer {
        return Transfer(
            id = this.id,
            fowlId = this.fowlId,
            giverId = this.fromUserId,
            receiverId = this.toUserId,
            status = when (this.status.uppercase()) {
                "PENDING" -> TransferStatus.PENDING
                "VERIFIED" -> TransferStatus.VERIFIED
                "REJECTED" -> TransferStatus.REJECTED
                else -> TransferStatus.PENDING
            },
            verificationDetails = this.verificationDetails,
            initiatedAt = this.initiatedAt,
            verifiedAt = this.verifiedAt,
            rejectedAt = this.rejectedAt
        )
    }

    /**
     * Extension function to convert TransferEntity to Map for syncing
     */
    private fun TransferEntity.toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "fowl_id" to fowlId,
            "from_user_id" to fromUserId,
            "to_user_id" to toUserId,
            "status" to status,
            "transfer_type" to transferType,
            "delivery_method" to deliveryMethod,
            "delivery_fee" to deliveryFee,
            "total_amount" to totalAmount,
            "initiated_at" to initiatedAt,
            "verified_at" to verifiedAt,
            "rejected_at" to rejectedAt,
            "verification_details" to verificationDetails,
            "created_at" to createdAt,
            "updated_at" to updatedAt,
            "sync_status" to syncStatus,
            "sync_priority" to syncPriority,
            "is_deleted" to isDeleted
        )
    }
}
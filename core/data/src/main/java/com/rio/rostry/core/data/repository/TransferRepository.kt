package com.rio.rostry.core.data.repository

import com.rio.rostry.core.database.entities.TransferLogDao
import com.rio.rostry.core.database.dao.OutboxDao
import com.rio.rostry.core.database.dao.FowlDaoV2
import com.rio.rostry.core.database.entities.TransferLogEntity
import com.rio.rostry.core.database.entities.OutboxEntity
import com.rio.rostry.core.common.model.Result
import com.rio.rostry.core.notifications.TransferNotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for transfer operations with verified workflow
 * This repository handles the complete lifecycle of fowl transfers with emphasis on
 * receiver verification and immutable audit logs.
 */
@Singleton
class TransferRepository @Inject constructor(
    private val transferLogDao: TransferLogDao,
    private val outboxDao: OutboxDao,
    private val fowlDao: FowlDaoV2,
    private val transferNotificationService: TransferNotificationService
) {
    /**
     * Initiate a new transfer with verification requirement
     */
    suspend fun initiateTransfer(
        fowlId: String,
        fromUserId: String,
        toUserId: String,
        expectedPrice: Double?,
        expectedColor: String?,
        expectedAgeWeeks: Int?,
        expectedWeightGrams: Int?,
        photoReference: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val transferId = UUID.randomUUID().toString()
            val now = Date()
            
            val transferLog = TransferLogEntity(
                id = transferId,
                fowlId = fowlId,
                fromUserId = fromUserId,
                toUserId = toUserId,
                transferStatus = "PENDING",
                verificationRequired = true,
                verificationStatus = "PENDING",
                expectedPrice = expectedPrice,
                expectedColor = expectedColor,
                expectedAgeWeeks = expectedAgeWeeks,
                expectedWeightGrams = expectedWeightGrams,
                photoReference = photoReference,
                initiatedAt = now,
                updatedAt = now,
                createdAt = now
            )
            
            transferLogDao.insert(transferLog)
            
            // Add to outbox for sync
            val outboxEntry = OutboxEntity(
                id = UUID.randomUUID().toString(),
                entityType = "TRANSFER_LOG",
                entityId = transferId,
                operationType = "CREATE",
                entityData = null, // Will be serialized by sync service
                createdAt = now,
                updatedAt = now,
                syncStatus = "PENDING",
                priority = 3 // Medium priority
            )
            
            outboxDao.insert(outboxEntry)
            
            // Send notification
            val fowl = fowlDao.getFowlById(fowlId)
            if (fowl != null) {
                transferNotificationService.sendTransferInitiatedNotification(transferLog, fowl)
            }

            Result.Success(transferId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Verify a transfer (to be called by receiver)
     */
    suspend fun verifyTransfer(
        transferId: String,
        verifiedBy: String,
        verificationDocuments: List<String> = emptyList(),
        verificationNotes: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            val transferLog = transferLogDao.getById(transferId)
            if (transferLog == null || transferLog.isDeleted) {
                return@withContext Result.Error(Exception("Transfer not found"))
            }
            
            if (transferLog.toUserId != verifiedBy) {
                return@withContext Result.Error(Exception("Only the receiver can verify the transfer"))
            }
            
            if (transferLog.verificationStatus != "PENDING") {
                return@withContext Result.Error(Exception("Transfer already verified or rejected"))
            }
            
            val now = Date()
            transferLogDao.updateVerificationStatus(
                id = transferId,
                status = "VERIFIED",
                verificationStatus = "VERIFIED",
                verifiedBy = verifiedBy,
                verifiedAt = now,
                updatedAt = now
            )
            
            // Add to outbox for sync
            val outboxEntry = OutboxEntity(
                id = UUID.randomUUID().toString(),
                entityType = "TRANSFER_LOG",
                entityId = transferId,
                operationType = "UPDATE",
                entityData = null, // Will be serialized by sync service
                createdAt = now,
                updatedAt = now,
                syncStatus = "PENDING",
                priority = 5 // High priority
            )
            
            outboxDao.insert(outboxEntry)
            
            // Send notification
            val fowl = fowlDao.getFowlById(transferLog.fowlId)
            if (fowl != null) {
                transferNotificationService.sendTransferVerifiedNotification(transferLog, fowl)
            }

            // Send notification
            val fowl = fowlDao.getFowlById(transferLog.fowlId)
            if (fowl != null) {
                transferNotificationService.sendTransferRejectedNotification(transferLog, fowl, rejectionReason ?: "")
            }

            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Reject a transfer (to be called by receiver)
     */
    suspend fun rejectTransfer(
        transferId: String,
        rejectedBy: String,
        rejectionReason: String?
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            val transferLog = transferLogDao.getById(transferId)
            if (transferLog == null || transferLog.isDeleted) {
                return@withContext Result.Error(Exception("Transfer not found"))
            }
            
            if (transferLog.toUserId != rejectedBy) {
                return@withContext Result.Error(Exception("Only the receiver can reject the transfer"))
            }
            
            if (transferLog.verificationStatus != "PENDING") {
                return@withContext Result.Error(Exception("Transfer already verified or rejected"))
            }
            
            val now = Date()
            transferLogDao.updateVerificationStatus(
                id = transferId,
                status = "REJECTED",
                verificationStatus = "REJECTED",
                verifiedBy = rejectedBy,
                verifiedAt = now,
                updatedAt = now
            )
            
            // Add to outbox for sync
            val outboxEntry = OutboxEntity(
                id = UUID.randomUUID().toString(),
                entityType = "TRANSFER_LOG",
                entityId = transferId,
                operationType = "UPDATE",
                entityData = null, // Will be serialized by sync service
                createdAt = now,
                updatedAt = now,
                syncStatus = "PENDING",
                priority = 4 // Medium-high priority
            )
            
            outboxDao.insert(outboxEntry)
            
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Get pending transfers for a user to verify
     */
    suspend fun getPendingTransfersForUser(userId: String): Result<List<TransferLogEntity>> = 
        withContext(Dispatchers.IO) {
            return@withContext try {
                val transfers = transferLogDao.getPendingVerificationForUser(userId)
                Result.Success(transfers)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    
    /**
     * Get transfer by ID
     */
    suspend fun getTransferById(transferId: String): Result<TransferLogEntity?> = 
        withContext(Dispatchers.IO) {
            return@withContext try {
                val transfer = transferLogDao.getById(transferId)
                Result.Success(transfer)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    
    /**
     * Get transfers for a user (both as giver or receiver)
     */
    fun getTransfersForUser(userId: String): Flow<List<TransferLogEntity>> = flow {
        try {
            // Combine transfers from user and transfers to user
            val fromTransfers = transferLogDao.getTransfersFromUser(userId)
            val toTransfers = transferLogDao.getTransfersToUser(userId)
            val allTransfers = (fromTransfers + toTransfers).distinctBy { it.id }
                .sortedByDescending { it.initiatedAt }
            emit(allTransfers)
        } catch (e: Exception) {
            // Handle error appropriately
        }
    }
}
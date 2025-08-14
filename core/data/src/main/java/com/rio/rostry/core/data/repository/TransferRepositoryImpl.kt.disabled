package com.rio.rostry.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.rio.rostry.core.data.service.UserValidationService
import com.rio.rostry.core.data.service.ValidationException
import com.rio.rostry.core.database.dao.TransferDao
import com.rio.rostry.core.database.dao.FowlDao
import com.rio.rostry.core.database.entities.TransferEntity
import com.rio.rostry.core.database.entities.FowlEntity
import com.rio.rostry.core.network.NetworkStateManager
import com.rio.rostry.core.common.model.TransferStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ Transfer repository with conflict resolution for offline-online sync
 * Handles simultaneous transfer attempts and ownership conflicts
 */
@Singleton
class TransferRepositoryImpl @Inject constructor(
    private val transferDao: TransferDao,
    private val fowlDao: FowlDao,
    private val userValidationService: UserValidationService,
    private val firestore: FirebaseFirestore,
    private val networkStateManager: NetworkStateManager
) : BaseOfflineRepository() {

    // ✅ Track active listeners for proper cleanup
    private val activeListeners = ConcurrentHashMap<String, ListenerRegistration>()
    
    // ✅ Track pending transfers to prevent conflicts
    private val pendingTransfers = ConcurrentHashMap<String, TransferEntity>()

    /**
     * ✅ Initiate transfer with conflict detection
     */
    suspend fun initiateTransfer(transfer: TransferEntity): Result<String> {
        return try {
            // Validate users can perform transfers
            val fromUserValidation = userValidationService.validateCanOwnFowls(transfer.fromUserId)
            if (!fromUserValidation.isValid) {
                return Result.failure(ValidationException(fromUserValidation))
            }
            
            val toUserValidation = userValidationService.validateUserExists(transfer.toUserId)
            if (!toUserValidation.isValid) {
                return Result.failure(ValidationException(toUserValidation))
            }
            
            // ✅ Check for existing pending transfers
            val existingTransfers = transferDao.getPendingTransfersForFowl(transfer.fowlId)
            if (existingTransfers.isNotEmpty()) {
                return Result.failure(TransferConflictException("Transfer already pending for this fowl"))
            }
            
            // ✅ Validate current ownership
            val fowl = fowlDao.getById(transfer.fowlId)
            if (fowl?.ownerId != transfer.fromUserId) {
                return Result.failure(TransferException("User is not the current owner"))
            }
            
            // ✅ Add optimistic lock version
            val transferWithVersion = transfer.copy(
                version = System.currentTimeMillis(),
                status = TransferStatus.PENDING_VALIDATION,
                createdAt = Date(),
                updatedAt = Date()
            )
            
            // Save locally first
            transferDao.insert(transferWithVersion)
            pendingTransfers[transfer.fowlId] = transferWithVersion
            
            if (networkStateManager.isConnected.value) {
                // ✅ Immediate server validation
                validateAndProcessTransfer(transferWithVersion)
            } else {
                // ✅ Queue for later processing
                queueTransferForSync(transferWithVersion)
            }
            
            Result.success(transferWithVersion.id)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * ✅ Server-side atomic validation and processing
     */
    private suspend fun validateAndProcessTransfer(transfer: TransferEntity) {
        try {
            // ✅ Server-side atomic validation using Firestore transaction
            val result = firestore.runTransaction { transaction ->
                val fowlRef = firestore.collection("fowls").document(transfer.fowlId)
                val fowlSnapshot = transaction.get(fowlRef)
                
                if (!fowlSnapshot.exists()) {
                    throw TransferException("Fowl not found")
                }
                
                val fowlData = fowlSnapshot.data!!
                val currentOwnerId = fowlData["ownerId"] as String
                
                // ✅ Validate current ownership
                if (currentOwnerId != transfer.fromUserId) {
                    throw TransferException("User is not the current owner")
                }
                
                // ✅ Check for conflicting transfers
                val transfersQuery = firestore.collection("transfers")
                    .whereEqualTo("fowlId", transfer.fowlId)
                    .whereIn("status", listOf("pending", "pending_validation"))
                
                val existingTransfers = transaction.get(transfersQuery)
                val conflictingTransfers = existingTransfers.documents.filter { doc ->
                    val docData = doc.data!!
                    val docVersion = docData["version"] as? Long ?: 0L
                    docVersion < transfer.version // Only consider earlier transfers as conflicts
                }
                
                if (conflictingTransfers.isNotEmpty()) {
                    throw TransferConflictException("Another transfer is already pending")
                }
                
                // ✅ Create transfer with server timestamp
                val serverTransfer = mapOf(
                    "id" to transfer.id,
                    "fowlId" to transfer.fowlId,
                    "fromUserId" to transfer.fromUserId,
                    "toUserId" to transfer.toUserId,
                    "version" to transfer.version,
                    "status" to "pending_approval",
                    "serverTimestamp" to FieldValue.serverTimestamp(),
                    "createdAt" to transfer.createdAt,
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "transferType" to transfer.transferType,
                    "notes" to (transfer.notes ?: ""),
                    "proofDocuments" to transfer.proofDocuments
                )
                
                val transferRef = firestore.collection("transfers").document(transfer.id)
                transaction.set(transferRef, serverTransfer)
                
                serverTransfer
            }.await()
            
            // Update local database with server result
            val updatedTransfer = transfer.copy(
                status = TransferStatus.PENDING_APPROVAL,
                updatedAt = Date()
            )
            transferDao.update(updatedTransfer)
            
            // Send notification to recipient
            sendTransferNotification(updatedTransfer)
            
        } catch (e: Exception) {
            // ✅ Handle conflicts gracefully
            when (e) {
                is TransferConflictException -> {
                    transferDao.updateStatus(transfer.id, TransferStatus.CONFLICT)
                    pendingTransfers.remove(transfer.fowlId)
                    notifyUserOfConflict(transfer)
                }
                is TransferException -> {
                    transferDao.updateStatus(transfer.id, TransferStatus.FAILED)
                    pendingTransfers.remove(transfer.fowlId)
                }
                else -> {
                    transferDao.updateStatus(transfer.id, TransferStatus.FAILED)
                    pendingTransfers.remove(transfer.fowlId)
                    throw e
                }
            }
        }
    }
    
    /**
     * ✅ Approve transfer with atomic ownership change
     */
    suspend fun approveTransfer(transferId: String): Result<Unit> {
        return try {
            val transfer = transferDao.getById(transferId)
                ?: return Result.failure(TransferException("Transfer not found"))
            
            if (transfer.status != TransferStatus.PENDING_APPROVAL) {
                return Result.failure(TransferException("Transfer is not in pending approval state"))
            }
            
            // ✅ Atomic ownership change
            if (networkStateManager.isConnected.value) {
                executeAtomicOwnershipChange(transfer)
            } else {
                // Mark for processing when online
                transferDao.updateStatus(transferId, TransferStatus.APPROVED_PENDING_SYNC)
            }
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * ✅ Execute atomic ownership change on server
     */
    private suspend fun executeAtomicOwnershipChange(transfer: TransferEntity) {
        firestore.runTransaction { transaction ->
            val fowlRef = firestore.collection("fowls").document(transfer.fowlId)
            val transferRef = firestore.collection("transfers").document(transfer.id)
            
            val fowlSnapshot = transaction.get(fowlRef)
            val transferSnapshot = transaction.get(transferRef)
            
            if (!fowlSnapshot.exists() || !transferSnapshot.exists()) {
                throw TransferException("Fowl or transfer not found")
            }
            
            val fowlData = fowlSnapshot.data!!
            val transferData = transferSnapshot.data!!
            
            // ✅ Final validation
            if (fowlData["ownerId"] != transfer.fromUserId) {
                throw TransferException("Ownership changed during transfer")
            }
            
            if (transferData["status"] != "pending_approval") {
                throw TransferException("Transfer status changed")
            }
            
            // ✅ Update fowl ownership
            transaction.update(fowlRef, mapOf(
                "ownerId" to transfer.toUserId,
                "updatedAt" to FieldValue.serverTimestamp(),
                "transferHistory" to FieldValue.arrayUnion(mapOf(
                    "transferId" to transfer.id,
                    "fromUserId" to transfer.fromUserId,
                    "toUserId" to transfer.toUserId,
                    "transferredAt" to FieldValue.serverTimestamp()
                ))
            ))
            
            // ✅ Update transfer status
            transaction.update(transferRef, mapOf(
                "status" to "completed",
                "completedAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ))
            
        }.await()
        
        // Update local database
        fowlDao.updateOwner(transfer.fowlId, transfer.toUserId)
        transferDao.updateStatus(transfer.id, TransferStatus.COMPLETED)
        pendingTransfers.remove(transfer.fowlId)
        
        // Send completion notifications
        sendTransferCompletionNotification(transfer)
    }
    
    /**
     * ✅ Sync pending transfers when connection restored
     */
    suspend fun syncPendingTransfers() {
        if (!networkStateManager.isConnected.value) return
        
        try {
            val unsyncedTransfers = transferDao.getUnsyncedTransfers()
            
            unsyncedTransfers.forEach { transfer ->
                try {
                    when (transfer.status) {
                        TransferStatus.PENDING_VALIDATION -> {
                            validateAndProcessTransfer(transfer)
                        }
                        TransferStatus.APPROVED_PENDING_SYNC -> {
                            executeAtomicOwnershipChange(transfer)
                        }
                        else -> {
                            // Mark as synced if no action needed
                            transferDao.markAsSynced(transfer.id)
                        }
                    }
                } catch (e: Exception) {
                    handleSyncError(transfer, e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TransferRepo", "Sync failed", e)
        }
    }
    
    /**
     * ✅ Handle sync errors with conflict resolution
     */
    private suspend fun handleSyncError(transfer: TransferEntity, error: Exception) {
        when (error) {
            is TransferConflictException -> {
                // ✅ Resolve conflict by checking server state
                resolveTransferConflict(transfer)
            }
            is NetworkException -> {
                // ✅ Retry later
                transferDao.markForRetry(transfer.id)
            }
            else -> {
                // ✅ Mark as failed and notify user
                transferDao.updateStatus(transfer.id, TransferStatus.FAILED)
                notifyUserOfFailure(transfer, error)
            }
        }
    }
    
    /**
     * ✅ Resolve transfer conflicts by checking server state
     */
    private suspend fun resolveTransferConflict(transfer: TransferEntity) {
        try {
            // Get current server state
            val serverFowl = firestore.collection("fowls")
                .document(transfer.fowlId)
                .get()
                .await()
            
            if (!serverFowl.exists()) {
                transferDao.updateStatus(transfer.id, TransferStatus.FAILED)
                return
            }
            
            val fowlData = serverFowl.data!!
            val currentOwnerId = fowlData["ownerId"] as String
            
            if (currentOwnerId == transfer.toUserId) {
                // ✅ Transfer already completed by another process
                transferDao.updateStatus(transfer.id, TransferStatus.COMPLETED)
                fowlDao.updateOwner(transfer.fowlId, transfer.toUserId)
            } else if (currentOwnerId != transfer.fromUserId) {
                // ✅ Ownership changed to someone else
                transferDao.updateStatus(transfer.id, TransferStatus.CONFLICT)
                notifyUserOfConflict(transfer)
            } else {
                // ✅ Original owner still owns, retry transfer
                validateAndProcessTransfer(transfer)
            }
            
        } catch (e: Exception) {
            transferDao.updateStatus(transfer.id, TransferStatus.FAILED)
        }
    }
    
    private suspend fun queueTransferForSync(transfer: TransferEntity) {
        // Implementation for offline queueing
    }
    
    private suspend fun sendTransferNotification(transfer: TransferEntity) {
        // Implementation for sending notifications
    }
    
    private suspend fun notifyUserOfConflict(transfer: TransferEntity) {
        // Implementation for conflict notifications
    }
    
    private suspend fun notifyUserOfFailure(transfer: TransferEntity, error: Exception) {
        // Implementation for failure notifications
    }
    
    private suspend fun sendTransferCompletionNotification(transfer: TransferEntity) {
        // Implementation for completion notifications
    }
    
    /**
     * ✅ Cleanup resources
     */
    fun cleanup() {
        activeListeners.values.forEach { it.remove() }
        activeListeners.clear()
        pendingTransfers.clear()
    }
}

/**
 * Transfer-specific exceptions
 */
class TransferException(message: String) : Exception(message)
class TransferConflictException(message: String) : Exception(message)
class NetworkException(message: String) : Exception(message)

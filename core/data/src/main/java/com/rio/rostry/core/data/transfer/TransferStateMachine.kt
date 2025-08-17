package com.rio.rostry.core.data.transfer

import com.rio.rostry.core.data.model.Transfer
import com.rio.rostry.core.data.model.TransferStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * State machine for managing the lifecycle of a fowl transfer
 */
class TransferStateMachine @Inject constructor() {
    
    private val _state = MutableStateFlow<TransferState>(TransferState.Idle)
    val state: StateFlow<TransferState> = _state.asStateFlow()
    
    /**
     * Initiate a transfer from giver to receiver
     */
    fun initiateTransfer(transfer: Transfer): Result<TransferState.Pending> {
        return try {
            val pendingTransfer = transfer.copy(status = TransferStatus.PENDING)
            _state.value = TransferState.Pending(pendingTransfer)
            Result.success(_state.value as TransferState.Pending)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verify a transfer by the receiver
     */
    fun verifyTransfer(
        transfer: Transfer,
        verificationDetails: Map<String, Any>
    ): Result<TransferState.Verified> {
        return try {
            if (transfer.status != TransferStatus.PENDING) {
                return Result.failure(IllegalStateException("Transfer must be in PENDING state to verify"))
            }
            
            val verifiedTransfer = transfer.copy(
                status = TransferStatus.VERIFIED,
                verificationDetails = verificationDetails
            )
            _state.value = TransferState.Verified(verifiedTransfer)
            Result.success(_state.value as TransferState.Verified)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Reject a transfer
     */
    fun rejectTransfer(transfer: Transfer): Result<TransferState.Rejected> {
        return try {
            if (transfer.status != TransferStatus.PENDING) {
                return Result.failure(IllegalStateException("Transfer must be in PENDING state to reject"))
            }
            
            val rejectedTransfer = transfer.copy(status = TransferStatus.REJECTED)
            _state.value = TransferState.Rejected(rejectedTransfer)
            Result.success(_state.value as TransferState.Rejected)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Reset the state machine
     */
    fun reset() {
        _state.value = TransferState.Idle
    }
}

sealed class TransferState {
    object Idle : TransferState()
    data class Pending(val transfer: Transfer) : TransferState()
    data class Verified(val transfer: Transfer) : TransferState()
    data class Rejected(val transfer: Transfer) : TransferState()
}
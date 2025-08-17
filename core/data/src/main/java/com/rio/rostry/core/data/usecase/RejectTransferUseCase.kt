package com.rio.rostry.core.data.usecase

import com.rio.rostry.core.data.repository.TransferRepository
import com.rio.rostry.core.common.model.Result
import javax.inject.Inject

/**
 * Use case for rejecting a transfer
 */
class RejectTransferUseCase @Inject constructor(
    private val transferRepository: TransferRepository
) {
    /**
     * Reject a transfer with the provided reason
     *
     * @param transferId The ID of the transfer to reject
     * @param rejectorId The ID of the user rejecting the transfer
     * @param reason The reason for rejecting the transfer
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(
        transferId: String,
        rejectorId: String,
        reason: String
    ): Result<Boolean> {
        return transferRepository.rejectTransfer(
            transferId = transferId,
            rejectedBy = rejectorId,
            rejectionReason = reason
        )
    }
}
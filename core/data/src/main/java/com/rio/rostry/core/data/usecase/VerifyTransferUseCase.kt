package com.rio.rostry.core.data.usecase

import com.rio.rostry.core.data.repository.TransferRepository
import com.rio.rostry.core.common.model.Result
import javax.inject.Inject

/**
 * Use case for verifying a transfer
 */
class VerifyTransferUseCase @Inject constructor(
    private val transferRepository: TransferRepository
) {
    /**
     * Verify a transfer with the provided verification data
     *
     * @param transferId The ID of the transfer to verify
     * @param verifierId The ID of the user verifying the transfer
     * @param verificationData The data to verify against
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(
        transferId: String,
        verifierId: String,
        verificationData: TransferVerificationData
    ): Result<Boolean> {
        return transferRepository.verifyTransfer(
            transferId = transferId,
            verifiedBy = verifierId,
            verificationDocuments = listOf(verificationData.photoReference),
            verificationNotes = "Verified by user"
        )
    }
}

/**
 * Data class representing the verification data for a transfer
 */
data class TransferVerificationData(
    val price: Int,
    val color: String,
    val ageInWeeks: Int,
    val weightInGrams: Int,
    val photoReference: String
)
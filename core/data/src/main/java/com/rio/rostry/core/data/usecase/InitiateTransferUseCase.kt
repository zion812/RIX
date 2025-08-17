package com.rio.rostry.core.data.usecase

import com.rio.rostry.core.data.repository.TransferRepository
import com.rio.rostry.core.common.model.Result
import javax.inject.Inject

/**
 * Use case for initiating a transfer
 */
class InitiateTransferUseCase @Inject constructor(
    private val transferRepository: TransferRepository
) {
    /**
     * Initiate a transfer with the provided data
     *
     * @param fowlId The ID of the fowl being transferred
     * @param fromUserId The ID of the user initiating the transfer
     * @param toUserId The ID of the user receiving the transfer
     * @param transferData The data for the transfer
     * @return Result containing the ID of the created transfer or an error
     */
    suspend operator fun invoke(
        fowlId: String,
        fromUserId: String,
        toUserId: String,
        transferData: TransferInitiationData
    ): Result<String> {
        return transferRepository.initiateTransfer(
            fowlId = fowlId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            expectedPrice = transferData.price.toDouble(),
            expectedColor = transferData.color,
            expectedAgeWeeks = transferData.ageInWeeks,
            expectedWeightGrams = transferData.weightInGrams,
            photoReference = transferData.photoReference
        )
    }
}

/**
 * Data class representing the initiation data for a transfer
 */
data class TransferInitiationData(
    val price: Int,
    val color: String,
    val ageInWeeks: Int,
    val weightInGrams: Int,
    val photoReference: String
)
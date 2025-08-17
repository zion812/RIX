package com.rio.rostry.core.data.usecase

import com.rio.rostry.core.data.repository.MarketplaceRepository
import com.rio.rostry.core.common.model.Result
import javax.inject.Inject

/**
 * Use case for getting available locations in the marketplace
 */
class GetAvailableLocationsUseCase @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository
) {
    /**
     * Get a list of available locations in the marketplace
     *
     * @return Result containing the list of locations or an error
     */
    suspend operator fun invoke(): Result<List<String>> {
        return marketplaceRepository.getAvailableLocations()
    }
}
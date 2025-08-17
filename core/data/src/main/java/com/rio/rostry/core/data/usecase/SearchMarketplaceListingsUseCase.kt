package com.rio.rostry.core.data.usecase

import com.rio.rostry.core.data.repository.MarketplaceRepository
import com.rio.rostry.core.data.model.MarketplaceFilter
import com.rio.rostry.core.database.entities.MarketplaceEntity
import com.rio.rostry.core.common.model.Result
import javax.inject.Inject

/**
 * Use case for searching marketplace listings with advanced filters
 */
class SearchMarketplaceListingsUseCase @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository
) {
    /**
     * Search marketplace listings with the provided filters
     *
     * @param filter The filters to apply to the search
     * @param limit The maximum number of listings to return
     * @return Result containing the matching listings or an error
     */
    suspend operator fun invoke(
        filter: MarketplaceFilter,
        limit: Int = 50
    ): Result<List<MarketplaceEntity>> {
        return marketplaceRepository.searchListings(filter, limit)
    }
}
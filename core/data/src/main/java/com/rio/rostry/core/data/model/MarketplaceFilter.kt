package com.rio.rostry.core.data.model

/**
 * Data model representing filters for marketplace listings
 */
data class MarketplaceFilter(
    val query: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val breed: String? = null,
    val gender: String? = null,
    val ageWeeksMin: Int? = null,
    val ageWeeksMax: Int? = null,
    val location: String? = null,
    val sortBy: SortBy = SortBy.NEWEST,
    val availability: AvailabilityFilter = AvailabilityFilter.AVAILABLE_ONLY
) {
    enum class SortBy {
        NEWEST,
        OLDEST,
        PRICE_LOW_TO_HIGH,
        PRICE_HIGH_TO_LOW,
        AGE_YOUNG_TO_OLD,
        AGE_OLD_TO_YOUNG
    }
    
    enum class AvailabilityFilter {
        AVAILABLE_ONLY,
        ALL
    }
}
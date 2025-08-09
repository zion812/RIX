package com.rio.rostry.marketplace.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.common.base.BaseViewModel
import com.rio.rostry.core.common.model.*
import com.rio.rostry.marketplace.domain.model.*
import com.rio.rostry.marketplace.domain.usecases.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for marketplace operations
 */
@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val getMarketplaceListingsUseCase: GetMarketplaceListingsUseCase,
    private val searchMarketplaceUseCase: SearchMarketplaceUseCase,
    private val createListingUseCase: CreateListingUseCase,
    private val updateListingUseCase: UpdateListingUseCase,
    private val deleteListingUseCase: DeleteListingUseCase,
    private val placeBidUseCase: PlaceBidUseCase,
    private val makeOfferUseCase: MakeOfferUseCase,
    private val addToWatchlistUseCase: AddToWatchlistUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase,
    private val getWatchlistUseCase: GetWatchlistUseCase
) : BaseViewModel() {

    // Marketplace listings
    private val _marketplaceListings = MutableStateFlow<ListState<MarketplaceListing>>(ListState())
    val marketplaceListings: StateFlow<ListState<MarketplaceListing>> = _marketplaceListings.asStateFlow()

    // User's listings
    private val _userListings = MutableStateFlow<ListState<MarketplaceListing>>(ListState())
    val userListings: StateFlow<ListState<MarketplaceListing>> = _userListings.asStateFlow()

    // Selected listing for details
    private val _selectedListing = MutableStateFlow<MarketplaceListing?>(null)
    val selectedListing: StateFlow<MarketplaceListing?> = _selectedListing.asStateFlow()

    // Search state
    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    // Filter state
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    // Listing creation state
    private val _listingCreationState = MutableStateFlow(ListingCreationState())
    val listingCreationState: StateFlow<ListingCreationState> = _listingCreationState.asStateFlow()

    // Bidding state
    private val _biddingState = MutableStateFlow(BiddingState())
    val biddingState: StateFlow<BiddingState> = _biddingState.asStateFlow()

    // Watchlist state
    private val _watchlistState = MutableStateFlow<ListState<WatchlistItem>>(ListState())
    val watchlistState: StateFlow<ListState<WatchlistItem>> = _watchlistState.asStateFlow()

    // Pagination state
    private val _paginationState = MutableStateFlow(PaginationState())
    val paginationState: StateFlow<PaginationState> = _paginationState.asStateFlow()

    // Search facets
    private val _searchFacets = MutableStateFlow<SearchFacets?>(null)
    val searchFacets: StateFlow<SearchFacets?> = _searchFacets.asStateFlow()

    init {
        loadMarketplaceListings()
        loadUserListings()
        loadWatchlist()
    }

    /**
     * Load marketplace listings
     */
    fun loadMarketplaceListings(refresh: Boolean = false) {
        if (refresh) {
            _marketplaceListings.value = _marketplaceListings.value.copy(isRefreshing = true)
        } else {
            _marketplaceListings.value = _marketplaceListings.value.copy(isLoading = true)
        }

        executeWithResult(
            showLoading = false,
            action = { getMarketplaceListingsUseCase(0, DEFAULT_PAGE_SIZE) },
            onSuccess = { result ->
                _marketplaceListings.value = ListState(
                    items = result.listings,
                    isLoading = false,
                    isRefreshing = false,
                    hasMore = result.hasMore
                )
                _searchFacets.value = result.facets
                logUserAction("marketplace_loaded", mapOf("count" to result.listings.size))
            },
            onError = { exception ->
                _marketplaceListings.value = _marketplaceListings.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = exception.message
                )
            }
        )
    }

    /**
     * Search marketplace with criteria
     */
    fun searchMarketplace(criteria: MarketplaceSearchCriteria) {
        _searchState.value = _searchState.value.copy(
            isSearching = true,
            query = criteria.query ?: ""
        )

        executeWithResult(
            showLoading = false,
            action = { searchMarketplaceUseCase(criteria, 0, DEFAULT_PAGE_SIZE) },
            onSuccess = { result ->
                _marketplaceListings.value = ListState(
                    items = result.listings,
                    isLoading = false,
                    hasMore = result.hasMore
                )
                _searchState.value = _searchState.value.copy(isSearching = false)
                _searchFacets.value = result.facets
                
                logUserAction("marketplace_searched", mapOf(
                    "query" to (criteria.query ?: ""),
                    "breed" to (criteria.breed ?: ""),
                    "region" to (criteria.region ?: ""),
                    "results" to result.listings.size
                ))
            },
            onError = { exception ->
                _searchState.value = _searchState.value.copy(isSearching = false)
                _marketplaceListings.value = _marketplaceListings.value.copy(
                    error = exception.message
                )
            }
        )
    }

    /**
     * Load user's listings
     */
    fun loadUserListings() {
        executeWithTierCheck(UserTier.FARMER) {
            val currentUserId = getCurrentUserId() ?: return@executeWithTierCheck
            
            _userListings.value = _userListings.value.copy(isLoading = true)

            executeWithResult(
                showLoading = false,
                action = { getMarketplaceListingsUseCase(0, DEFAULT_PAGE_SIZE, currentUserId) },
                onSuccess = { result ->
                    _userListings.value = ListState(
                        items = result.listings,
                        isLoading = false,
                        hasMore = result.hasMore
                    )
                },
                onError = { exception ->
                    _userListings.value = _userListings.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    /**
     * Create a new listing
     */
    fun createListing(request: ListingCreateRequest) {
        executeWithTierCheck(UserTier.FARMER) {
            if (!validateListingRequest(request)) return@executeWithTierCheck

            _listingCreationState.value = _listingCreationState.value.copy(
                isLoading = true,
                error = null
            )

            executeWithResult(
                showLoading = false,
                action = { createListingUseCase(request) },
                onSuccess = { listing ->
                    _listingCreationState.value = _listingCreationState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    
                    // Add to user listings
                    val currentUserListings = _userListings.value.items.toMutableList()
                    currentUserListings.add(0, listing)
                    _userListings.value = _userListings.value.copy(items = currentUserListings)
                    
                    // Add to marketplace if published
                    if (request.autoPublish) {
                        val currentMarketplace = _marketplaceListings.value.items.toMutableList()
                        currentMarketplace.add(0, listing)
                        _marketplaceListings.value = _marketplaceListings.value.copy(items = currentMarketplace)
                    }
                    
                    logUserAction("listing_created", mapOf(
                        "listing_id" to listing.id,
                        "listing_type" to listing.listingType.name,
                        "price" to listing.pricing.basePrice
                    ))
                },
                onError = { exception ->
                    _listingCreationState.value = _listingCreationState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to create listing"
                    )
                }
            )
        }
    }

    /**
     * Update a listing
     */
    fun updateListing(listingId: String, request: ListingUpdateRequest) {
        executeWithTierCheck(UserTier.FARMER) {
            executeWithResult(
                action = { updateListingUseCase(listingId, request) },
                onSuccess = { updatedListing ->
                    // Update in user listings
                    updateListingInList(_userListings, listingId, updatedListing)
                    
                    // Update in marketplace listings
                    updateListingInList(_marketplaceListings, listingId, updatedListing)
                    
                    // Update selected listing if it's the same
                    if (_selectedListing.value?.id == listingId) {
                        _selectedListing.value = updatedListing
                    }
                    
                    logUserAction("listing_updated", mapOf("listing_id" to listingId))
                }
            )
        }
    }

    /**
     * Delete a listing
     */
    fun deleteListing(listingId: String) {
        executeWithTierCheck(UserTier.FARMER) {
            executeWithResult(
                action = { deleteListingUseCase(listingId) },
                onSuccess = {
                    // Remove from user listings
                    removeListingFromList(_userListings, listingId)
                    
                    // Remove from marketplace listings
                    removeListingFromList(_marketplaceListings, listingId)
                    
                    // Clear selected listing if it's the same
                    if (_selectedListing.value?.id == listingId) {
                        _selectedListing.value = null
                    }
                    
                    logUserAction("listing_deleted", mapOf("listing_id" to listingId))
                }
            )
        }
    }

    /**
     * Place a bid on an auction listing
     */
    fun placeBid(request: BidPlacementRequest) {
        if (!hasRequiredTier(UserTier.FARMER)) {
            _biddingState.value = _biddingState.value.copy(
                error = "You need to be a verified farmer to place bids"
            )
            return
        }

        _biddingState.value = _biddingState.value.copy(
            isPlacingBid = true,
            error = null
        )

        executeWithResult(
            showLoading = false,
            action = { placeBidUseCase(request) },
            onSuccess = { bidInfo ->
                _biddingState.value = _biddingState.value.copy(
                    isPlacingBid = false,
                    lastBid = bidInfo
                )
                
                // Update listing with new bid info
                updateListingBidInfo(request.listingId, bidInfo)
                
                logUserAction("bid_placed", mapOf(
                    "listing_id" to request.listingId,
                    "amount" to request.amount
                ))
            },
            onError = { exception ->
                _biddingState.value = _biddingState.value.copy(
                    isPlacingBid = false,
                    error = exception.message ?: "Failed to place bid"
                )
            }
        )
    }

    /**
     * Make an offer on a listing
     */
    fun makeOffer(request: OfferRequest) {
        executeWithTierCheck(UserTier.FARMER) {
            executeWithResult(
                action = { makeOfferUseCase(request) },
                onSuccess = {
                    logUserAction("offer_made", mapOf(
                        "listing_id" to request.listingId,
                        "amount" to request.amount
                    ))
                }
            )
        }
    }

    /**
     * Add listing to watchlist
     */
    fun addToWatchlist(listingId: String, priceAlert: PriceAlert? = null) {
        val currentUserId = getCurrentUserId() ?: return

        executeWithResult(
            action = { addToWatchlistUseCase(currentUserId, listingId, priceAlert) },
            onSuccess = { watchlistItem ->
                val currentWatchlist = _watchlistState.value.items.toMutableList()
                currentWatchlist.add(0, watchlistItem)
                _watchlistState.value = _watchlistState.value.copy(items = currentWatchlist)
                
                logUserAction("added_to_watchlist", mapOf("listing_id" to listingId))
            }
        )
    }

    /**
     * Remove listing from watchlist
     */
    fun removeFromWatchlist(listingId: String) {
        val currentUserId = getCurrentUserId() ?: return

        executeWithResult(
            action = { removeFromWatchlistUseCase(currentUserId, listingId) },
            onSuccess = {
                val currentWatchlist = _watchlistState.value.items.toMutableList()
                currentWatchlist.removeAll { it.listingId == listingId }
                _watchlistState.value = _watchlistState.value.copy(items = currentWatchlist)
                
                logUserAction("removed_from_watchlist", mapOf("listing_id" to listingId))
            }
        )
    }

    /**
     * Load user's watchlist
     */
    fun loadWatchlist() {
        val currentUserId = getCurrentUserId() ?: return

        executeWithResult(
            showLoading = false,
            action = { getWatchlistUseCase(currentUserId) },
            onSuccess = { watchlistItems ->
                _watchlistState.value = ListState(
                    items = watchlistItems,
                    isLoading = false
                )
            },
            onError = { exception ->
                _watchlistState.value = _watchlistState.value.copy(
                    isLoading = false,
                    error = exception.message
                )
            }
        )
    }

    /**
     * Select a listing for detailed view
     */
    fun selectListing(listing: MarketplaceListing) {
        _selectedListing.value = listing
        logUserAction("listing_viewed", mapOf(
            "listing_id" to listing.id,
            "listing_type" to listing.listingType.name
        ))
    }

    /**
     * Clear selected listing
     */
    fun clearSelectedListing() {
        _selectedListing.value = null
    }

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchState.value = _searchState.value.copy(query = query)
    }

    /**
     * Update filter state
     */
    fun updateFilters(filters: FilterState) {
        _filterState.value = filters
    }

    /**
     * Clear filters
     */
    fun clearFilters() {
        _filterState.value = FilterState()
    }

    /**
     * Load more listings for pagination
     */
    fun loadMoreListings() {
        if (_paginationState.value.isLoadingMore || !_paginationState.value.hasMore) return

        _paginationState.value = _paginationState.value.copy(isLoadingMore = true)

        val nextPage = _paginationState.value.currentPage + 1

        executeWithResult(
            showLoading = false,
            action = { getMarketplaceListingsUseCase(nextPage, DEFAULT_PAGE_SIZE) },
            onSuccess = { result ->
                val currentList = _marketplaceListings.value.items.toMutableList()
                currentList.addAll(result.listings)
                
                _marketplaceListings.value = _marketplaceListings.value.copy(items = currentList)
                _paginationState.value = _paginationState.value.copy(
                    currentPage = nextPage,
                    isLoadingMore = false,
                    hasMore = result.hasMore
                )
            },
            onError = {
                _paginationState.value = _paginationState.value.copy(isLoadingMore = false)
            }
        )
    }

    /**
     * Validate listing creation request
     */
    private fun validateListingRequest(request: ListingCreateRequest): Boolean {
        val errors = mutableListOf<String>()

        if (request.details.title.isBlank()) {
            errors.add("Title is required")
        }

        if (request.details.description.isBlank()) {
            errors.add("Description is required")
        }

        if (request.pricing.basePrice <= 0) {
            errors.add("Price must be greater than 0")
        }

        if (request.listingType == ListingType.AUCTION) {
            if (request.auction == null) {
                errors.add("Auction details are required for auction listings")
            } else {
                if (request.auction.endTime.before(Date())) {
                    errors.add("Auction end time must be in the future")
                }
            }
        }

        if (errors.isNotEmpty()) {
            _listingCreationState.value = _listingCreationState.value.copy(
                error = errors.joinToString(", ")
            )
            return false
        }

        return true
    }

    /**
     * Update listing in a list
     */
    private fun updateListingInList(
        listStateFlow: MutableStateFlow<ListState<MarketplaceListing>>,
        listingId: String,
        updatedListing: MarketplaceListing
    ) {
        val currentList = listStateFlow.value.items.toMutableList()
        val index = currentList.indexOfFirst { it.id == listingId }
        if (index != -1) {
            currentList[index] = updatedListing
            listStateFlow.value = listStateFlow.value.copy(items = currentList)
        }
    }

    /**
     * Remove listing from a list
     */
    private fun removeListingFromList(
        listStateFlow: MutableStateFlow<ListState<MarketplaceListing>>,
        listingId: String
    ) {
        val currentList = listStateFlow.value.items.toMutableList()
        currentList.removeAll { it.id == listingId }
        listStateFlow.value = listStateFlow.value.copy(items = currentList)
    }

    /**
     * Update listing with new bid information
     */
    private fun updateListingBidInfo(listingId: String, bidInfo: BidInfo) {
        // Update in marketplace listings
        updateListingInList(_marketplaceListings, listingId) { listing ->
            listing.copy(
                auction = listing.auction?.copy(
                    currentHighestBid = bidInfo,
                    totalBids = listing.auction.totalBids + 1
                ),
                pricing = listing.pricing.copy(currentBid = bidInfo.amount)
            )
        }

        // Update selected listing if it's the same
        _selectedListing.value?.let { selectedListing ->
            if (selectedListing.id == listingId) {
                _selectedListing.value = selectedListing.copy(
                    auction = selectedListing.auction?.copy(
                        currentHighestBid = bidInfo,
                        totalBids = selectedListing.auction.totalBids + 1
                    ),
                    pricing = selectedListing.pricing.copy(currentBid = bidInfo.amount)
                )
            }
        }
    }

    /**
     * Update listing in list with transform function
     */
    private fun updateListingInList(
        listStateFlow: MutableStateFlow<ListState<MarketplaceListing>>,
        listingId: String,
        transform: (MarketplaceListing) -> MarketplaceListing
    ) {
        val currentList = listStateFlow.value.items.toMutableList()
        val index = currentList.indexOfFirst { it.id == listingId }
        if (index != -1) {
            currentList[index] = transform(currentList[index])
            listStateFlow.value = listStateFlow.value.copy(items = currentList)
        }
    }

    /**
     * Clear listing creation state
     */
    fun clearListingCreationState() {
        _listingCreationState.value = ListingCreationState()
    }

    /**
     * Clear bidding state
     */
    fun clearBiddingState() {
        _biddingState.value = BiddingState()
    }

    override fun refreshData() {
        loadMarketplaceListings(refresh = true)
        loadUserListings()
        loadWatchlist()
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
    }
}

/**
 * Listing creation state
 */
data class ListingCreationState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

/**
 * Bidding state
 */
data class BiddingState(
    val isPlacingBid: Boolean = false,
    val lastBid: BidInfo? = null,
    val error: String? = null
)

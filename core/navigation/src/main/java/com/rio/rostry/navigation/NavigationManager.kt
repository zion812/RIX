package com.rio.rostry.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavOptions
import com.rio.rostry.core.common.model.UserTier
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central navigation manager for handling navigation across feature modules
 */
@Singleton
class NavigationManager @Inject constructor() {

    private var navController: NavController? = null

    /**
     * Set the main navigation controller
     */
    fun setNavController(navController: NavController) {
        this.navController = navController
    }

    /**
     * Navigate to user profile
     */
    fun navigateToUserProfile(userId: String) {
        val deepLink = createDeepLink("rio://user/profile/$userId")
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to fowl details
     */
    fun navigateToFowlDetails(fowlId: String) {
        val deepLink = createDeepLink("rio://fowl/details/$fowlId")
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to fowl registration
     */
    fun navigateToFowlRegistration() {
        val deepLink = createDeepLink("rio://fowl/register")
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to marketplace listing details
     */
    fun navigateToListingDetails(listingId: String) {
        val deepLink = createDeepLink("rio://marketplace/listing/$listingId")
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to marketplace search with filters
     */
    fun navigateToMarketplaceSearch(
        breed: String? = null,
        region: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null
    ) {
        val uri = Uri.Builder()
            .scheme("rio")
            .authority("marketplace")
            .path("/search")
            .apply {
                breed?.let { appendQueryParameter("breed", it) }
                region?.let { appendQueryParameter("region", it) }
                minPrice?.let { appendQueryParameter("minPrice", it.toString()) }
                maxPrice?.let { appendQueryParameter("maxPrice", it.toString()) }
            }
            .build()
        
        navigateWithDeepLink(NavDeepLinkRequest.Builder.fromUri(uri).build())
    }

    /**
     * Navigate to create marketplace listing
     */
    fun navigateToCreateListing(fowlId: String? = null) {
        val uri = if (fowlId != null) {
            "rio://marketplace/create?fowlId=$fowlId"
        } else {
            "rio://marketplace/create"
        }
        val deepLink = createDeepLink(uri)
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to chat conversation
     */
    fun navigateToChatConversation(conversationId: String) {
        val deepLink = createDeepLink("rio://chat/conversation/$conversationId")
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to create new conversation
     */
    fun navigateToCreateConversation(
        participantId: String? = null,
        listingId: String? = null,
        fowlId: String? = null
    ) {
        val uri = Uri.Builder()
            .scheme("rio")
            .authority("chat")
            .path("/create")
            .apply {
                participantId?.let { appendQueryParameter("participantId", it) }
                listingId?.let { appendQueryParameter("listingId", it) }
                fowlId?.let { appendQueryParameter("fowlId", it) }
            }
            .build()
        
        navigateWithDeepLink(NavDeepLinkRequest.Builder.fromUri(uri).build())
    }

    /**
     * Navigate to tier verification
     */
    fun navigateToTierVerification(targetTier: UserTier) {
        val deepLink = createDeepLink("rio://user/verification?tier=${targetTier.name}")
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to breeding records
     */
    fun navigateToBreedingRecords(fowlId: String? = null) {
        val uri = if (fowlId != null) {
            "rio://fowl/breeding?fowlId=$fowlId"
        } else {
            "rio://fowl/breeding"
        }
        val deepLink = createDeepLink(uri)
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to lineage tree
     */
    fun navigateToLineageTree(fowlId: String) {
        val deepLink = createDeepLink("rio://fowl/lineage/$fowlId")
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to health records
     */
    fun navigateToHealthRecords(fowlId: String) {
        val deepLink = createDeepLink("rio://fowl/health/$fowlId")
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to user settings
     */
    fun navigateToSettings() {
        val deepLink = createDeepLink("rio://user/settings")
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to notifications
     */
    fun navigateToNotifications() {
        val deepLink = createDeepLink("rio://user/notifications")
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to watchlist
     */
    fun navigateToWatchlist() {
        val deepLink = createDeepLink("rio://marketplace/watchlist")
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to user's listings
     */
    fun navigateToMyListings() {
        val deepLink = createDeepLink("rio://marketplace/my-listings")
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to user discovery
     */
    fun navigateToUserDiscovery(
        tier: UserTier? = null,
        region: String? = null,
        specialization: String? = null
    ) {
        val uri = Uri.Builder()
            .scheme("rio")
            .authority("user")
            .path("/discovery")
            .apply {
                tier?.let { appendQueryParameter("tier", it.name) }
                region?.let { appendQueryParameter("region", it) }
                specialization?.let { appendQueryParameter("specialization", it) }
            }
            .build()
        
        navigateWithDeepLink(NavDeepLinkRequest.Builder.fromUri(uri).build())
    }

    /**
     * Navigate to QR scanner
     */
    fun navigateToQRScanner() {
        val deepLink = createDeepLink("rio://fowl/qr-scanner")
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to photo gallery
     */
    fun navigateToPhotoGallery(fowlId: String) {
        val deepLink = createDeepLink("rio://fowl/gallery/$fowlId")
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to transfer history
     */
    fun navigateToTransferHistory(fowlId: String? = null) {
        val uri = if (fowlId != null) {
            "rio://fowl/transfers?fowlId=$fowlId"
        } else {
            "rio://fowl/transfers"
        }
        val deepLink = createDeepLink(uri)
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to analytics dashboard (enthusiast only)
     */
    fun navigateToAnalytics() {
        val deepLink = createDeepLink("rio://analytics/dashboard")
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate to help and support
     */
    fun navigateToSupport() {
        val deepLink = createDeepLink("rio://support/help")
        navigateWithDeepLink(deepLink)
    }

    /**
     * Navigate back
     */
    fun navigateBack(): Boolean {
        return navController?.popBackStack() ?: false
    }

    /**
     * Navigate to main screen based on user tier
     */
    fun navigateToMainScreen(userTier: UserTier) {
        val destination = when (userTier) {
            UserTier.GENERAL -> "rio://main/general"
            UserTier.FARMER -> "rio://main/farmer"
            UserTier.ENTHUSIAST -> "rio://main/enthusiast"
        }
        val deepLink = createDeepLink(destination)
        navigateWithDeepLink(deepLink, clearBackStack = true)
    }

    /**
     * Navigate to login screen
     */
    fun navigateToLogin() {
        val deepLink = createDeepLink("rio://auth/login")
        navigateWithDeepLink(deepLink, clearBackStack = true)
    }

    /**
     * Handle external deep link
     */
    fun handleExternalDeepLink(uri: Uri): Boolean {
        return try {
            val deepLinkRequest = NavDeepLinkRequest.Builder.fromUri(uri).build()
            navController?.navigate(deepLinkRequest) != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Create deep link request from URI string
     */
    private fun createDeepLink(uri: String): NavDeepLinkRequest {
        return NavDeepLinkRequest.Builder.fromUri(Uri.parse(uri)).build()
    }

    /**
     * Navigate with deep link request
     */
    private fun navigateWithDeepLink(
        deepLinkRequest: NavDeepLinkRequest,
        clearBackStack: Boolean = false
    ) {
        navController?.let { controller ->
            val navOptions = if (clearBackStack) {
                NavOptions.Builder()
                    .setPopUpTo(controller.graph.startDestinationId, true)
                    .build()
            } else {
                null
            }
            
            try {
                controller.navigate(deepLinkRequest, navOptions)
            } catch (e: Exception) {
                // Handle navigation error
                // Could log error or show fallback navigation
            }
        }
    }

    /**
     * Get current destination
     */
    fun getCurrentDestination(): String? {
        return navController?.currentDestination?.route
    }

    /**
     * Check if can navigate back
     */
    fun canNavigateBack(): Boolean {
        return navController?.previousBackStackEntry != null
    }
}

/**
 * Navigation destinations for type-safe navigation
 */
object NavigationDestinations {
    
    // Authentication
    const val LOGIN = "rio://auth/login"
    const val REGISTER = "rio://auth/register"
    const val FORGOT_PASSWORD = "rio://auth/forgot-password"
    
    // Main screens
    const val MAIN_GENERAL = "rio://main/general"
    const val MAIN_FARMER = "rio://main/farmer"
    const val MAIN_ENTHUSIAST = "rio://main/enthusiast"
    
    // User module
    const val USER_PROFILE = "rio://user/profile/{userId}"
    const val USER_SETTINGS = "rio://user/settings"
    const val USER_NOTIFICATIONS = "rio://user/notifications"
    const val USER_VERIFICATION = "rio://user/verification"
    const val USER_DISCOVERY = "rio://user/discovery"
    
    // Fowl module
    const val FOWL_DETAILS = "rio://fowl/details/{fowlId}"
    const val FOWL_REGISTER = "rio://fowl/register"
    const val FOWL_LINEAGE = "rio://fowl/lineage/{fowlId}"
    const val FOWL_HEALTH = "rio://fowl/health/{fowlId}"
    const val FOWL_GALLERY = "rio://fowl/gallery/{fowlId}"
    const val FOWL_BREEDING = "rio://fowl/breeding"
    const val FOWL_TRANSFERS = "rio://fowl/transfers"
    const val FOWL_QR_SCANNER = "rio://fowl/qr-scanner"
    
    // Marketplace module
    const val MARKETPLACE_HOME = "rio://marketplace/home"
    const val MARKETPLACE_SEARCH = "rio://marketplace/search"
    const val MARKETPLACE_LISTING = "rio://marketplace/listing/{listingId}"
    const val MARKETPLACE_CREATE = "rio://marketplace/create"
    const val MARKETPLACE_MY_LISTINGS = "rio://marketplace/my-listings"
    const val MARKETPLACE_WATCHLIST = "rio://marketplace/watchlist"
    
    // Chat module
    const val CHAT_HOME = "rio://chat/home"
    const val CHAT_CONVERSATION = "rio://chat/conversation/{conversationId}"
    const val CHAT_CREATE = "rio://chat/create"
    
    // Analytics (enthusiast only)
    const val ANALYTICS_DASHBOARD = "rio://analytics/dashboard"
    
    // Support
    const val SUPPORT_HELP = "rio://support/help"
}

/**
 * Navigation arguments for passing data between destinations
 */
object NavigationArgs {
    const val USER_ID = "userId"
    const val FOWL_ID = "fowlId"
    const val LISTING_ID = "listingId"
    const val CONVERSATION_ID = "conversationId"
    const val BREED = "breed"
    const val REGION = "region"
    const val MIN_PRICE = "minPrice"
    const val MAX_PRICE = "maxPrice"
    const val TIER = "tier"
    const val PARTICIPANT_ID = "participantId"
    const val SPECIALIZATION = "specialization"
}

/**
 * Deep link patterns for external navigation
 */
object DeepLinkPatterns {
    const val FOWL_DETAILS = "https://rio.app/fowl/{fowlId}"
    const val LISTING_DETAILS = "https://rio.app/listing/{listingId}"
    const val USER_PROFILE = "https://rio.app/user/{userId}"
    const val MARKETPLACE_SEARCH = "https://rio.app/search"
    const val QR_CODE = "https://rio.app/qr/{fowlId}"
}

/**
 * Navigation extensions for easier usage
 */
fun NavController.navigateToFowlDetails(fowlId: String) {
    val uri = NavigationDestinations.FOWL_DETAILS.replace("{fowlId}", fowlId)
    navigate(Uri.parse(uri))
}

fun NavController.navigateToListingDetails(listingId: String) {
    val uri = NavigationDestinations.MARKETPLACE_LISTING.replace("{listingId}", listingId)
    navigate(Uri.parse(uri))
}

fun NavController.navigateToUserProfile(userId: String) {
    val uri = NavigationDestinations.USER_PROFILE.replace("{userId}", userId)
    navigate(Uri.parse(uri))
}

fun NavController.navigateToChatConversation(conversationId: String) {
    val uri = NavigationDestinations.CHAT_CONVERSATION.replace("{conversationId}", conversationId)
    navigate(Uri.parse(uri))
}

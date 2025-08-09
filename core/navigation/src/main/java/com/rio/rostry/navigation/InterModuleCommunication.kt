package com.rio.rostry.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Event bus for inter-module communication
 */
@Singleton
class EventBus @Inject constructor() {
    
    private val _events = MutableSharedFlow<AppEvent>()
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()
    
    suspend fun emit(event: AppEvent) {
        _events.emit(event)
    }
}

/**
 * Application-wide events for cross-module communication
 */
sealed class AppEvent {
    
    // User events
    data class UserLoggedIn(val userId: String, val userTier: String) : AppEvent()
    data class UserLoggedOut(val userId: String) : AppEvent()
    data class UserTierUpgraded(val userId: String, val newTier: String) : AppEvent()
    data class UserProfileUpdated(val userId: String) : AppEvent()
    
    // Fowl events
    data class FowlRegistered(val fowlId: String, val ownerId: String) : AppEvent()
    data class FowlUpdated(val fowlId: String) : AppEvent()
    data class FowlDeleted(val fowlId: String) : AppEvent()
    data class FowlTransferred(val fowlId: String, val fromUserId: String, val toUserId: String) : AppEvent()
    data class FowlHealthRecordAdded(val fowlId: String, val recordId: String) : AppEvent()
    data class FowlPhotoUploaded(val fowlId: String, val photoUrl: String) : AppEvent()
    
    // Marketplace events
    data class ListingCreated(val listingId: String, val sellerId: String) : AppEvent()
    data class ListingUpdated(val listingId: String) : AppEvent()
    data class ListingDeleted(val listingId: String) : AppEvent()
    data class ListingSold(val listingId: String, val buyerId: String) : AppEvent()
    data class BidPlaced(val listingId: String, val bidderId: String, val amount: Double) : AppEvent()
    data class OfferMade(val listingId: String, val buyerId: String, val amount: Double) : AppEvent()
    data class WatchlistUpdated(val userId: String, val listingId: String, val added: Boolean) : AppEvent()
    
    // Chat events
    data class ConversationCreated(val conversationId: String, val participants: List<String>) : AppEvent()
    data class MessageSent(val conversationId: String, val messageId: String, val senderId: String) : AppEvent()
    data class MessageReceived(val conversationId: String, val messageId: String, val senderId: String) : AppEvent()
    data class ConversationRead(val conversationId: String, val userId: String) : AppEvent()
    data class TypingStarted(val conversationId: String, val userId: String) : AppEvent()
    data class TypingStopped(val conversationId: String, val userId: String) : AppEvent()
    
    // Network events
    object NetworkConnected : AppEvent()
    object NetworkDisconnected : AppEvent()
    data class NetworkTypeChanged(val networkType: String) : AppEvent()
    
    // Sync events
    data class DataSyncStarted(val module: String) : AppEvent()
    data class DataSyncCompleted(val module: String) : AppEvent()
    data class DataSyncFailed(val module: String, val error: String) : AppEvent()
    
    // Notification events
    data class NotificationReceived(val notificationId: String, val type: String) : AppEvent()
    data class NotificationClicked(val notificationId: String) : AppEvent()
    
    // Analytics events
    data class UserActionTracked(val action: String, val parameters: Map<String, Any>) : AppEvent()
    data class ScreenViewed(val screenName: String, val userId: String) : AppEvent()
    
    // Error events
    data class ErrorOccurred(val module: String, val error: Throwable) : AppEvent()
    data class CriticalErrorOccurred(val error: Throwable) : AppEvent()
}

/**
 * Shared data manager for cross-module data sharing
 */
@Singleton
class SharedDataManager @Inject constructor() {
    
    private val _sharedData = mutableMapOf<String, Any>()
    
    /**
     * Store shared data
     */
    fun <T> putData(key: String, data: T) {
        _sharedData[key] = data as Any
    }
    
    /**
     * Retrieve shared data
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getData(key: String): T? {
        return _sharedData[key] as? T
    }
    
    /**
     * Remove shared data
     */
    fun removeData(key: String) {
        _sharedData.remove(key)
    }
    
    /**
     * Clear all shared data
     */
    fun clearAll() {
        _sharedData.clear()
    }
    
    /**
     * Check if data exists
     */
    fun hasData(key: String): Boolean {
        return _sharedData.containsKey(key)
    }
}

/**
 * Shared data keys for consistent access
 */
object SharedDataKeys {
    const val SELECTED_FOWL = "selected_fowl"
    const val SELECTED_LISTING = "selected_listing"
    const val SELECTED_USER = "selected_user"
    const val CURRENT_CONVERSATION = "current_conversation"
    const val SEARCH_FILTERS = "search_filters"
    const val CAMERA_RESULT = "camera_result"
    const val LOCATION_RESULT = "location_result"
    const val QR_SCAN_RESULT = "qr_scan_result"
    const val BREEDING_PAIR = "breeding_pair"
    const val TRANSFER_DATA = "transfer_data"
    const val VERIFICATION_DOCUMENTS = "verification_documents"
}

/**
 * Module coordinator for managing inter-module dependencies
 */
@Singleton
class ModuleCoordinator @Inject constructor(
    private val eventBus: EventBus,
    private val sharedDataManager: SharedDataManager,
    private val navigationManager: NavigationManager
) {
    
    /**
     * Handle fowl selection across modules
     */
    suspend fun selectFowl(fowlId: String, fowlData: Any) {
        sharedDataManager.putData(SharedDataKeys.SELECTED_FOWL, fowlData)
        eventBus.emit(AppEvent.FowlUpdated(fowlId))
    }
    
    /**
     * Handle listing selection across modules
     */
    suspend fun selectListing(listingId: String, listingData: Any) {
        sharedDataManager.putData(SharedDataKeys.SELECTED_LISTING, listingData)
        eventBus.emit(AppEvent.ListingUpdated(listingId))
    }
    
    /**
     * Handle user selection across modules
     */
    suspend fun selectUser(userId: String, userData: Any) {
        sharedDataManager.putData(SharedDataKeys.SELECTED_USER, userData)
        eventBus.emit(AppEvent.UserProfileUpdated(userId))
    }
    
    /**
     * Handle conversation creation from marketplace
     */
    suspend fun createConversationFromListing(listingId: String, sellerId: String, buyerId: String) {
        // Store context data
        sharedDataManager.putData("conversation_context", mapOf(
            "type" to "marketplace",
            "listingId" to listingId,
            "sellerId" to sellerId,
            "buyerId" to buyerId
        ))
        
        // Navigate to create conversation
        navigationManager.navigateToCreateConversation(
            participantId = sellerId,
            listingId = listingId
        )
    }
    
    /**
     * Handle fowl sharing to chat
     */
    suspend fun shareFowlToChat(fowlId: String, fowlData: Any, conversationId: String? = null) {
        sharedDataManager.putData("share_fowl_data", mapOf(
            "fowlId" to fowlId,
            "fowlData" to fowlData
        ))
        
        if (conversationId != null) {
            navigationManager.navigateToChatConversation(conversationId)
        } else {
            navigationManager.navigateToCreateConversation(fowlId = fowlId)
        }
    }
    
    /**
     * Handle listing sharing to chat
     */
    suspend fun shareListingToChat(listingId: String, listingData: Any, conversationId: String? = null) {
        sharedDataManager.putData("share_listing_data", mapOf(
            "listingId" to listingId,
            "listingData" to listingData
        ))
        
        if (conversationId != null) {
            navigationManager.navigateToChatConversation(conversationId)
        } else {
            navigationManager.navigateToCreateConversation(listingId = listingId)
        }
    }
    
    /**
     * Handle QR code scan result
     */
    suspend fun handleQRScanResult(qrData: String) {
        sharedDataManager.putData(SharedDataKeys.QR_SCAN_RESULT, qrData)
        
        // Parse QR data and navigate accordingly
        when {
            qrData.startsWith("fowl:") -> {
                val fowlId = qrData.removePrefix("fowl:")
                navigationManager.navigateToFowlDetails(fowlId)
            }
            qrData.startsWith("listing:") -> {
                val listingId = qrData.removePrefix("listing:")
                navigationManager.navigateToListingDetails(listingId)
            }
            qrData.startsWith("user:") -> {
                val userId = qrData.removePrefix("user:")
                navigationManager.navigateToUserProfile(userId)
            }
            else -> {
                // Handle unknown QR format
                eventBus.emit(AppEvent.ErrorOccurred("qr_scanner", Exception("Unknown QR format: $qrData")))
            }
        }
    }
    
    /**
     * Handle photo capture result
     */
    suspend fun handlePhotoCaptureResult(photoPath: String, context: String) {
        sharedDataManager.putData(SharedDataKeys.CAMERA_RESULT, mapOf(
            "photoPath" to photoPath,
            "context" to context
        ))
        
        when (context) {
            "fowl_registration" -> {
                // Continue with fowl registration flow
                eventBus.emit(AppEvent.UserActionTracked("photo_captured_fowl_registration", emptyMap()))
            }
            "listing_creation" -> {
                // Continue with listing creation flow
                eventBus.emit(AppEvent.UserActionTracked("photo_captured_listing_creation", emptyMap()))
            }
            "profile_update" -> {
                // Continue with profile update flow
                eventBus.emit(AppEvent.UserActionTracked("photo_captured_profile_update", emptyMap()))
            }
        }
    }
    
    /**
     * Handle location selection result
     */
    suspend fun handleLocationResult(latitude: Double, longitude: Double, address: String?) {
        sharedDataManager.putData(SharedDataKeys.LOCATION_RESULT, mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "address" to address
        ))
        
        eventBus.emit(AppEvent.UserActionTracked("location_selected", mapOf(
            "latitude" to latitude,
            "longitude" to longitude
        )))
    }
    
    /**
     * Handle breeding pair selection
     */
    suspend fun handleBreedingPairSelection(sireId: String, damId: String) {
        sharedDataManager.putData(SharedDataKeys.BREEDING_PAIR, mapOf(
            "sireId" to sireId,
            "damId" to damId
        ))
        
        eventBus.emit(AppEvent.UserActionTracked("breeding_pair_selected", mapOf(
            "sireId" to sireId,
            "damId" to damId
        )))
    }
    
    /**
     * Clear module context data
     */
    fun clearModuleContext() {
        sharedDataManager.clearAll()
    }
    
    /**
     * Handle deep link navigation with context
     */
    suspend fun handleDeepLinkWithContext(uri: android.net.Uri) {
        val handled = navigationManager.handleExternalDeepLink(uri)
        
        if (handled) {
            eventBus.emit(AppEvent.UserActionTracked("deep_link_handled", mapOf(
                "uri" to uri.toString()
            )))
        } else {
            eventBus.emit(AppEvent.ErrorOccurred("navigation", Exception("Failed to handle deep link: $uri")))
        }
    }
}

package com.rio.rostry.core.sync

import com.rio.rostry.core.database.RIOLocalDatabase
import com.rio.rostry.core.database.entities.*
import com.rio.rostry.core.network.NetworkStateManager
import com.rio.rostry.core.common.utils.DataValidator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline action queue for managing user actions during disconnection
 * Ensures data integrity and proper sequencing of operations
 */
@Singleton
class OfflineActionQueue @Inject constructor(
    private val database: RIOLocalDatabase,
    private val networkStateManager: NetworkStateManager,
    private val dataValidator: DataValidator
) {
    
    private val _queueSize = MutableStateFlow(0)
    val queueSize: StateFlow<Int> = _queueSize.asStateFlow()
    
    private val _processingStatus = MutableStateFlow<ProcessingStatus>(ProcessingStatus.IDLE)
    val processingStatus: StateFlow<ProcessingStatus> = _processingStatus.asStateFlow()
    
    init {
        updateQueueSize()
        observeNetworkChanges()
    }
    
    /**
     * Queue an action for offline execution
     */
    suspend fun queueAction(action: OfflineAction): Result<String> {
        return try {
            // Validate action data
            if (!dataValidator.validateOfflineAction(action)) {
                return Result.failure(IllegalArgumentException("Invalid action data"))
            }
            
            // Create queue entity
            val queueEntity = OfflineActionEntity(
                id = action.id,
                entityType = action.entityType,
                entityId = action.entityId,
                actionType = action.actionType.name,
                actionData = action.actionData,
                priority = action.priority.value,
                dependsOn = action.dependsOn,
                validationRules = action.validationRules,
                retryCount = 0,
                maxRetries = action.maxRetries,
                status = OfflineActionStatus.QUEUED.name,
                queuedAt = Date(),
                syncMetadata = SyncMetadata(
                    syncPriority = action.priority,
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
            
            // Save to database
            database.offlineActionDao().insert(queueEntity)
            updateQueueSize()
            
            // Try immediate processing if online
            if (networkStateManager.isConnected.value) {
                processQueue()
            }
            
            Result.success(action.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Process all queued actions
     */
    suspend fun processQueue(): ProcessingResult = supervisorScope {
        if (_processingStatus.value == ProcessingStatus.PROCESSING) {
            return@supervisorScope ProcessingResult.alreadyProcessing()
        }
        
        _processingStatus.value = ProcessingStatus.PROCESSING
        
        try {
            val queuedActions = database.offlineActionDao().getAllQueued()
            val sortedActions = sortActionsByPriorityAndDependencies(queuedActions)
            
            var processedCount = 0
            var failedCount = 0
            val errors = mutableListOf<ActionError>()
            
            for (actionEntity in sortedActions) {
                try {
                    val action = actionEntity.toOfflineAction()
                    val result = processAction(action)
                    
                    if (result.isSuccess) {
                        // Mark as completed
                        database.offlineActionDao().updateStatus(
                            actionEntity.id,
                            OfflineActionStatus.COMPLETED.name,
                            Date()
                        )
                        processedCount++
                    } else {
                        // Handle failure
                        handleActionFailure(actionEntity, result.error)
                        failedCount++
                        errors.add(ActionError(
                            actionId = actionEntity.id,
                            entityType = actionEntity.entityType,
                            error = result.error ?: "Unknown error"
                        ))
                    }
                } catch (e: Exception) {
                    handleActionFailure(actionEntity, e.message)
                    failedCount++
                    errors.add(ActionError(
                        actionId = actionEntity.id,
                        entityType = actionEntity.entityType,
                        error = e.message ?: "Processing failed"
                    ))
                }
            }
            
            updateQueueSize()
            _processingStatus.value = ProcessingStatus.IDLE
            
            ProcessingResult(
                totalActions = sortedActions.size,
                processedCount = processedCount,
                failedCount = failedCount,
                errors = errors
            )
        } catch (e: Exception) {
            _processingStatus.value = ProcessingStatus.FAILED
            ProcessingResult.failure(e.message ?: "Queue processing failed")
        }
    }
    
    /**
     * Process a single action
     */
    private suspend fun processAction(action: OfflineAction): ActionResult {
        return try {
            when (action.actionType) {
                OfflineActionType.CREATE -> processCreateAction(action)
                OfflineActionType.UPDATE -> processUpdateAction(action)
                OfflineActionType.DELETE -> processDeleteAction(action)
                OfflineActionType.TRANSFER -> processTransferAction(action)
                OfflineActionType.PAYMENT -> processPaymentAction(action)
            }
        } catch (e: Exception) {
            ActionResult.failure(e.message ?: "Action processing failed")
        }
    }
    
    /**
     * Process create action
     */
    private suspend fun processCreateAction(action: OfflineAction): ActionResult {
        // Validate dependencies
        if (!validateDependencies(action)) {
            return ActionResult.failure("Dependencies not met")
        }
        
        // Execute create operation based on entity type
        return when (action.entityType) {
            "fowl" -> createFowlEntity(action)
            "marketplace_listing" -> createMarketplaceListing(action)
            "message" -> createMessage(action)
            "transfer" -> createTransfer(action)
            else -> ActionResult.failure("Unknown entity type: ${action.entityType}")
        }
    }
    
    /**
     * Process update action
     */
    private suspend fun processUpdateAction(action: OfflineAction): ActionResult {
        // Validate entity exists
        if (!entityExists(action.entityType, action.entityId)) {
            return ActionResult.failure("Entity not found: ${action.entityId}")
        }
        
        // Execute update operation
        return when (action.entityType) {
            "fowl" -> updateFowlEntity(action)
            "marketplace_listing" -> updateMarketplaceListing(action)
            "user" -> updateUserEntity(action)
            else -> ActionResult.failure("Unknown entity type: ${action.entityType}")
        }
    }
    
    /**
     * Process delete action
     */
    private suspend fun processDeleteAction(action: OfflineAction): ActionResult {
        // Check if entity can be deleted
        if (!canDeleteEntity(action.entityType, action.entityId)) {
            return ActionResult.failure("Entity cannot be deleted")
        }
        
        // Execute delete operation
        return when (action.entityType) {
            "fowl" -> deleteFowlEntity(action)
            "marketplace_listing" -> deleteMarketplaceListing(action)
            else -> ActionResult.failure("Unknown entity type: ${action.entityType}")
        }
    }
    
    /**
     * Process transfer action (critical)
     */
    private suspend fun processTransferAction(action: OfflineAction): ActionResult {
        // Transfers require special validation
        if (!validateTransferAction(action)) {
            return ActionResult.failure("Transfer validation failed")
        }
        
        return createTransfer(action)
    }
    
    /**
     * Process payment action (critical)
     */
    private suspend fun processPaymentAction(action: OfflineAction): ActionResult {
        // Payments require special validation
        if (!validatePaymentAction(action)) {
            return ActionResult.failure("Payment validation failed")
        }
        
        return processPayment(action)
    }
    
    /**
     * Sort actions by priority and dependencies
     */
    private fun sortActionsByPriorityAndDependencies(actions: List<OfflineActionEntity>): List<OfflineActionEntity> {
        val actionMap = actions.associateBy { it.id }
        val sorted = mutableListOf<OfflineActionEntity>()
        val processed = mutableSetOf<String>()
        
        fun addAction(action: OfflineActionEntity) {
            if (action.id in processed) return
            
            // Add dependencies first
            action.dependsOn.forEach { dependencyId ->
                actionMap[dependencyId]?.let { dependency ->
                    addAction(dependency)
                }
            }
            
            sorted.add(action)
            processed.add(action.id)
        }
        
        // Sort by priority first, then add with dependencies
        actions.sortedBy { it.priority }.forEach { action ->
            addAction(action)
        }
        
        return sorted
    }
    
    /**
     * Handle action failure
     */
    private suspend fun handleActionFailure(actionEntity: OfflineActionEntity, error: String?) {
        val newRetryCount = actionEntity.retryCount + 1
        
        if (newRetryCount >= actionEntity.maxRetries) {
            // Max retries reached - mark as failed
            database.offlineActionDao().updateStatus(
                actionEntity.id,
                OfflineActionStatus.FAILED.name,
                Date(),
                error
            )
        } else {
            // Increment retry count and schedule retry
            database.offlineActionDao().updateRetryCount(
                actionEntity.id,
                newRetryCount,
                calculateRetryDelay(newRetryCount)
            )
        }
    }
    
    /**
     * Calculate exponential backoff retry delay
     */
    private fun calculateRetryDelay(retryCount: Int): Date {
        val baseDelay = 1000L // 1 second
        val exponentialDelay = baseDelay * Math.pow(2.0, retryCount.toDouble()).toLong()
        val maxDelay = 300000L // 5 minutes
        val delay = minOf(exponentialDelay, maxDelay)
        
        return Date(System.currentTimeMillis() + delay)
    }
    
    /**
     * Observe network changes and process queue when connected
     */
    private fun observeNetworkChanges() {
        networkStateManager.isConnected
            .distinctUntilChanged()
            .onEach { isConnected ->
                if (isConnected && _queueSize.value > 0) {
                    processQueue()
                }
            }
    }
    
    /**
     * Update queue size
     */
    private suspend fun updateQueueSize() {
        val size = database.offlineActionDao().getQueuedCount()
        _queueSize.value = size
    }
    
    /**
     * Get queue statistics
     */
    suspend fun getQueueStatistics(): QueueStatistics {
        val dao = database.offlineActionDao()
        return QueueStatistics(
            totalQueued = dao.getQueuedCount(),
            totalProcessing = dao.getProcessingCount(),
            totalFailed = dao.getFailedCount(),
            totalCompleted = dao.getCompletedCount(),
            oldestAction = dao.getOldestQueuedAction()?.queuedAt,
            averageProcessingTime = dao.getAverageProcessingTime()
        )
    }
    
    /**
     * Clear completed actions older than specified date
     */
    suspend fun cleanupCompletedActions(olderThan: Date): Int {
        return database.offlineActionDao().deleteCompletedOlderThan(olderThan)
    }
    
    /**
     * Retry failed actions
     */
    suspend fun retryFailedActions(): ProcessingResult {
        // Reset failed actions to queued status
        database.offlineActionDao().resetFailedToQueued()
        updateQueueSize()
        
        // Process queue
        return processQueue()
    }
    
    // Entity operation implementations
    private suspend fun createFowlEntity(action: OfflineAction): ActionResult {
        return try {
            val fowlData = parseActionData<FowlActionData>(action.actionData)

            // Validate fowl data
            if (!dataValidator.validateFowlData(fowlData)) {
                return ActionResult.failure("Invalid fowl data")
            }

            // Create fowl entity
            val fowlEntity = fowlData.toEntity()
            database.fowlDao().insert(fowlEntity)

            ActionResult.success()
        } catch (e: Exception) {
            ActionResult.failure("Failed to create fowl: ${e.message}")
        }
    }

    private suspend fun createMarketplaceListing(action: OfflineAction): ActionResult {
        return try {
            val listingData = parseActionData<MarketplaceActionData>(action.actionData)

            // Validate listing data
            if (!dataValidator.validateMarketplaceData(listingData)) {
                return ActionResult.failure("Invalid listing data")
            }

            // Verify fowl ownership
            val fowl = database.fowlDao().getById(listingData.fowlId)
            if (fowl?.ownerId != listingData.sellerId) {
                return ActionResult.failure("User does not own the fowl")
            }

            // Create listing entity
            val listingEntity = listingData.toEntity()
            database.marketplaceDao().insert(listingEntity)

            ActionResult.success()
        } catch (e: Exception) {
            ActionResult.failure("Failed to create listing: ${e.message}")
        }
    }

    private suspend fun createMessage(action: OfflineAction): ActionResult {
        return try {
            val messageData = parseActionData<MessageActionData>(action.actionData)

            // Validate message data
            if (!dataValidator.validateMessageData(messageData)) {
                return ActionResult.failure("Invalid message data")
            }

            // Verify conversation exists
            val conversation = database.conversationDao().getById(messageData.conversationId)
            if (conversation == null) {
                return ActionResult.failure("Conversation not found")
            }

            // Create message entity
            val messageEntity = messageData.toEntity()
            database.messageDao().insert(messageEntity)

            // Update conversation
            database.conversationDao().updateLastMessage(
                messageData.conversationId,
                messageData.id,
                messageData.sentAt
            )

            ActionResult.success()
        } catch (e: Exception) {
            ActionResult.failure("Failed to create message: ${e.message}")
        }
    }

    private suspend fun createTransfer(action: OfflineAction): ActionResult {
        return try {
            val transferData = parseActionData<TransferActionData>(action.actionData)

            // Validate transfer data
            if (!validateTransferData(transferData)) {
                return ActionResult.failure("Invalid transfer data")
            }

            // Verify fowl ownership
            val fowl = database.fowlDao().getById(transferData.fowlId)
            if (fowl?.ownerId != transferData.fromUserId) {
                return ActionResult.failure("User does not own the fowl")
            }

            // Create transfer entity
            val transferEntity = transferData.toEntity()
            database.transferDao().insert(transferEntity)

            ActionResult.success()
        } catch (e: Exception) {
            ActionResult.failure("Failed to create transfer: ${e.message}")
        }
    }

    private suspend fun updateFowlEntity(action: OfflineAction): ActionResult {
        return try {
            val fowlData = parseActionData<FowlActionData>(action.actionData)

            // Verify fowl exists and ownership
            val existingFowl = database.fowlDao().getById(action.entityId)
            if (existingFowl == null) {
                return ActionResult.failure("Fowl not found")
            }

            // Update fowl entity
            val updatedEntity = fowlData.toEntity().copy(
                id = action.entityId,
                syncMetadata = existingFowl.syncMetadata.copy(
                    updatedAt = Date(),
                    conflictVersion = existingFowl.conflictVersion + 1
                )
            )
            database.fowlDao().update(updatedEntity)

            ActionResult.success()
        } catch (e: Exception) {
            ActionResult.failure("Failed to update fowl: ${e.message}")
        }
    }

    private suspend fun updateMarketplaceListing(action: OfflineAction): ActionResult {
        return try {
            val listingData = parseActionData<MarketplaceActionData>(action.actionData)

            // Verify listing exists and ownership
            val existingListing = database.marketplaceDao().getById(action.entityId)
            if (existingListing == null) {
                return ActionResult.failure("Listing not found")
            }

            if (existingListing.sellerId != listingData.sellerId) {
                return ActionResult.failure("User does not own the listing")
            }

            // Update listing entity
            val updatedEntity = listingData.toEntity().copy(
                id = action.entityId,
                syncMetadata = existingListing.syncMetadata.copy(
                    updatedAt = Date(),
                    conflictVersion = existingListing.conflictVersion + 1
                )
            )
            database.marketplaceDao().update(updatedEntity)

            ActionResult.success()
        } catch (e: Exception) {
            ActionResult.failure("Failed to update listing: ${e.message}")
        }
    }

    private suspend fun updateUserEntity(action: OfflineAction): ActionResult {
        return try {
            val userData = parseActionData<UserActionData>(action.actionData)

            // Verify user exists
            val existingUser = database.userDao().getById(action.entityId)
            if (existingUser == null) {
                return ActionResult.failure("User not found")
            }

            // Update user entity
            val updatedEntity = userData.toEntity().copy(
                id = action.entityId,
                syncMetadata = existingUser.syncMetadata.copy(
                    updatedAt = Date(),
                    conflictVersion = existingUser.conflictVersion + 1
                )
            )
            database.userDao().update(updatedEntity)

            ActionResult.success()
        } catch (e: Exception) {
            ActionResult.failure("Failed to update user: ${e.message}")
        }
    }

    private suspend fun deleteFowlEntity(action: OfflineAction): ActionResult {
        return try {
            // Verify fowl exists
            val fowl = database.fowlDao().getById(action.entityId)
            if (fowl == null) {
                return ActionResult.failure("Fowl not found")
            }

            // Check if fowl can be deleted (no active transfers, etc.)
            val activeTransfers = database.transferDao().getTransfersByFowl(action.entityId)
                .filter { it.transferStatus in listOf("INITIATED", "PENDING_APPROVAL", "APPROVED", "IN_TRANSIT") }

            if (activeTransfers.isNotEmpty()) {
                return ActionResult.failure("Cannot delete fowl with active transfers")
            }

            // Mark as deleted
            database.fowlDao().markAsDeleted(action.entityId)

            ActionResult.success()
        } catch (e: Exception) {
            ActionResult.failure("Failed to delete fowl: ${e.message}")
        }
    }

    private suspend fun deleteMarketplaceListing(action: OfflineAction): ActionResult {
        return try {
            // Verify listing exists
            val listing = database.marketplaceDao().getById(action.entityId)
            if (listing == null) {
                return ActionResult.failure("Listing not found")
            }

            // Mark as deleted
            database.marketplaceDao().markAsDeleted(action.entityId)

            ActionResult.success()
        } catch (e: Exception) {
            ActionResult.failure("Failed to delete listing: ${e.message}")
        }
    }

    private suspend fun processPayment(action: OfflineAction): ActionResult {
        return try {
            val paymentData = parseActionData<PaymentActionData>(action.actionData)

            // Validate payment data
            if (!validatePaymentData(paymentData)) {
                return ActionResult.failure("Invalid payment data")
            }

            // Update transfer with payment information
            val transfer = database.transferDao().getById(paymentData.transferId)
            if (transfer == null) {
                return ActionResult.failure("Transfer not found")
            }

            database.transferDao().updatePaymentStatus(
                paymentData.transferId,
                paymentData.paymentStatus,
                paymentData.paymentReference
            )

            ActionResult.success()
        } catch (e: Exception) {
            ActionResult.failure("Failed to process payment: ${e.message}")
        }
    }

    // Validation methods
    private suspend fun validateDependencies(action: OfflineAction): Boolean {
        return try {
            action.dependsOn.all { dependencyId ->
                val dependency = database.offlineActionDao().getById(dependencyId)
                dependency?.status == "COMPLETED"
            }
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun entityExists(entityType: String, entityId: String): Boolean {
        return try {
            when (entityType) {
                "fowl" -> database.fowlDao().getById(entityId) != null
                "user" -> database.userDao().getById(entityId) != null
                "marketplace_listing" -> database.marketplaceDao().getById(entityId) != null
                "message" -> database.messageDao().getById(entityId) != null
                "transfer" -> database.transferDao().getById(entityId) != null
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun canDeleteEntity(entityType: String, entityId: String): Boolean {
        return try {
            when (entityType) {
                "fowl" -> {
                    val activeTransfers = database.transferDao().getTransfersByFowl(entityId)
                        .filter { it.transferStatus in listOf("INITIATED", "PENDING_APPROVAL", "APPROVED", "IN_TRANSIT") }
                    activeTransfers.isEmpty()
                }
                "marketplace_listing" -> {
                    val listing = database.marketplaceDao().getById(entityId)
                    listing?.listingStatus != "SOLD"
                }
                else -> true
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun validateTransferData(transferData: TransferActionData): Boolean {
        return transferData.fowlId.isNotEmpty() &&
                transferData.fromUserId.isNotEmpty() &&
                transferData.toUserId.isNotEmpty() &&
                transferData.fromUserId != transferData.toUserId &&
                transferData.deliveryAddress.isNotEmpty()
    }

    private fun validatePaymentData(paymentData: PaymentActionData): Boolean {
        return paymentData.transferId.isNotEmpty() &&
                paymentData.paymentStatus.isNotEmpty() &&
                (paymentData.amount == null || paymentData.amount > 0)
    }

    // Helper method to parse action data
    private inline fun <reified T> parseActionData(actionData: String): T {
        return try {
            com.google.gson.Gson().fromJson(actionData, T::class.java)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse action data: ${e.message}")
        }
    }
}

/**
 * Processing status
 */
enum class ProcessingStatus {
    IDLE,
    PROCESSING,
    FAILED
}

/**
 * Offline action types
 */
enum class OfflineActionType {
    CREATE,
    UPDATE,
    DELETE,
    TRANSFER,
    PAYMENT
}

/**
 * Offline action status
 */
enum class OfflineActionStatus {
    QUEUED,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Offline action data class
 */
data class OfflineAction(
    val id: String = UUID.randomUUID().toString(),
    val entityType: String,
    val entityId: String,
    val actionType: OfflineActionType,
    val actionData: String, // JSON data
    val priority: SyncPriority,
    val dependsOn: List<String> = emptyList(),
    val validationRules: List<String> = emptyList(),
    val maxRetries: Int = 3,
    val queuedAt: Date = Date()
)

/**
 * Action processing result
 */
data class ActionResult(
    val isSuccess: Boolean,
    val error: String? = null
) {
    companion object {
        fun success() = ActionResult(true)
        fun failure(error: String) = ActionResult(false, error)
    }
}

/**
 * Queue processing result
 */
data class ProcessingResult(
    val totalActions: Int,
    val processedCount: Int,
    val failedCount: Int,
    val errors: List<ActionError>
) {
    companion object {
        fun alreadyProcessing() = ProcessingResult(0, 0, 0, emptyList())
        fun failure(error: String) = ProcessingResult(0, 0, 1, listOf(
            ActionError("", "", error)
        ))
    }
}

/**
 * Action error
 */
data class ActionError(
    val actionId: String,
    val entityType: String,
    val error: String
)

/**
 * Queue statistics
 */
data class QueueStatistics(
    val totalQueued: Int,
    val totalProcessing: Int,
    val totalFailed: Int,
    val totalCompleted: Int,
    val oldestAction: Date?,
    val averageProcessingTime: Long? // milliseconds
)

/**
 * Action data classes for different entity types
 */
data class FowlActionData(
    val id: String,
    val ownerId: String,
    val name: String? = null,
    val breedPrimary: String,
    val breedSecondary: String? = null,
    val gender: String,
    val ageCategory: String,
    val color: String,
    val weight: Double,
    val height: Double,
    val healthStatus: String,
    val availabilityStatus: String,
    val notes: String? = null,
    val tags: List<String> = emptyList(),
    val region: String,
    val district: String
) {
    fun toEntity(): FowlEntity {
        return FowlEntity(
            id = id,
            ownerId = ownerId,
            name = name,
            breedPrimary = breedPrimary,
            breedSecondary = breedSecondary,
            gender = gender,
            ageCategory = ageCategory,
            color = color,
            weight = weight,
            height = height,
            combType = "SINGLE",
            legColor = "YELLOW",
            eyeColor = "RED",
            healthStatus = healthStatus,
            availabilityStatus = availabilityStatus,
            notes = notes,
            tags = tags,
            regionalMetadata = RegionalMetadata(region = region, district = district),
            syncMetadata = SyncMetadata(
                syncStatus = SyncStatus.PENDING_UPLOAD,
                createdAt = Date(),
                updatedAt = Date()
            )
        )
    }
}

data class MarketplaceActionData(
    val id: String,
    val sellerId: String,
    val fowlId: String,
    val title: String,
    val description: String,
    val listingType: String,
    val basePrice: Double,
    val breed: String,
    val gender: String,
    val age: String,
    val weight: Double,
    val healthStatus: String,
    val deliveryAvailable: Boolean,
    val region: String,
    val district: String
) {
    fun toEntity(): MarketplaceEntity {
        return MarketplaceEntity(
            id = id,
            sellerId = sellerId,
            fowlId = fowlId,
            title = title,
            description = description,
            listingType = listingType,
            basePrice = basePrice,
            breed = breed,
            gender = gender,
            age = age,
            weight = weight,
            color = "UNKNOWN",
            healthStatus = healthStatus,
            deliveryAvailable = deliveryAvailable,
            listingStatus = "DRAFT",
            category = "POULTRY",
            regionalMetadata = RegionalMetadata(region = region, district = district),
            syncMetadata = SyncMetadata(
                syncStatus = SyncStatus.PENDING_UPLOAD,
                createdAt = Date(),
                updatedAt = Date()
            )
        )
    }
}

data class MessageActionData(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val messageType: String,
    val textContent: String? = null,
    val mediaUrl: String? = null,
    val sentAt: Date = Date()
) {
    fun toEntity(): MessageEntity {
        return MessageEntity(
            id = id,
            conversationId = conversationId,
            senderId = senderId,
            messageType = messageType,
            textContent = textContent,
            mediaUrl = mediaUrl,
            sentAt = sentAt,
            deliveryStatus = "PENDING",
            syncMetadata = SyncMetadata(
                syncStatus = SyncStatus.PENDING_UPLOAD,
                createdAt = Date(),
                updatedAt = Date()
            )
        )
    }
}

data class TransferActionData(
    val id: String,
    val fowlId: String,
    val fromUserId: String,
    val toUserId: String,
    val transferType: String,
    val amount: Double? = null,
    val deliveryAddress: String,
    val transferNotes: String? = null,
    val region: String,
    val district: String
) {
    fun toEntity(): TransferEntity {
        return TransferEntity(
            id = id,
            fowlId = fowlId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            transferType = transferType,
            transferStatus = "INITIATED",
            amount = amount,
            paymentStatus = "PENDING",
            verificationRequired = true,
            verificationStatus = "PENDING",
            deliveryAddress = deliveryAddress,
            transferNotes = transferNotes,
            initiatedAt = Date(),
            regionalMetadata = RegionalMetadata(region = region, district = district),
            syncMetadata = SyncMetadata(
                syncStatus = SyncStatus.PENDING_UPLOAD,
                syncPriority = SyncPriority.CRITICAL,
                createdAt = Date(),
                updatedAt = Date()
            )
        )
    }
}

data class UserActionData(
    val id: String,
    val displayName: String,
    val bio: String? = null,
    val farmName: String? = null,
    val specializations: List<String> = emptyList(),
    val language: String = "en",
    val region: String,
    val district: String
) {
    fun toEntity(): UserEntity {
        return UserEntity(
            id = id,
            email = "", // Will be filled from existing entity
            displayName = displayName,
            userTier = "GENERAL", // Will be filled from existing entity
            verificationStatus = "PENDING", // Will be filled from existing entity
            bio = bio,
            farmName = farmName,
            specializations = specializations,
            language = language,
            regionalMetadata = RegionalMetadata(region = region, district = district),
            syncMetadata = SyncMetadata(
                syncStatus = SyncStatus.PENDING_UPLOAD,
                createdAt = Date(),
                updatedAt = Date()
            )
        )
    }
}

data class PaymentActionData(
    val transferId: String,
    val paymentStatus: String,
    val paymentReference: String? = null,
    val amount: Double? = null
)

/**
 * Extension function for OfflineActionEntity
 */
fun OfflineActionEntity.toOfflineAction(): OfflineAction {
    return OfflineAction(
        id = id,
        entityType = entityType,
        entityId = entityId,
        actionType = OfflineActionType.valueOf(actionType),
        actionData = actionData,
        priority = SyncPriority.values().find { it.value == priority } ?: SyncPriority.MEDIUM,
        dependsOn = dependsOn,
        validationRules = validationRules,
        maxRetries = maxRetries,
        queuedAt = queuedAt
    )
}

package com.rio.rostry.core.common.ui

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ Centralized loading state management for consistent UX
 * Provides standardized loading states across all features
 */
@Singleton
class LoadingStateManager @Inject constructor() {
    
    private val _globalLoadingState = MutableStateFlow<GlobalLoadingState>(GlobalLoadingState.Idle)
    val globalLoadingState: StateFlow<GlobalLoadingState> = _globalLoadingState.asStateFlow()
    
    private val activeOperations = mutableMapOf<String, LoadingOperation>()
    
    /**
     * ✅ Start a loading operation with progress tracking
     */
    fun startOperation(
        operationId: String,
        title: String,
        description: String = "",
        showProgress: Boolean = true,
        estimatedDuration: Long? = null
    ) {
        val operation = LoadingOperation(
            id = operationId,
            title = title,
            description = description,
            showProgress = showProgress,
            estimatedDuration = estimatedDuration,
            startTime = System.currentTimeMillis()
        )
        
        activeOperations[operationId] = operation
        updateGlobalState()
    }
    
    /**
     * ✅ Update operation progress
     */
    fun updateProgress(
        operationId: String,
        progress: Float,
        message: String? = null
    ) {
        activeOperations[operationId]?.let { operation ->
            activeOperations[operationId] = operation.copy(
                progress = progress.coerceIn(0f, 1f),
                currentMessage = message ?: operation.currentMessage
            )
            updateGlobalState()
        }
    }
    
    /**
     * ✅ Complete an operation
     */
    fun completeOperation(operationId: String, successMessage: String? = null) {
        activeOperations.remove(operationId)
        updateGlobalState()
        
        if (successMessage != null) {
            showSuccessMessage(successMessage)
        }
    }
    
    /**
     * ✅ Fail an operation
     */
    fun failOperation(operationId: String, errorMessage: String) {
        activeOperations.remove(operationId)
        updateGlobalState()
        showErrorMessage(errorMessage)
    }
    
    /**
     * ✅ Update global loading state based on active operations
     */
    private fun updateGlobalState() {
        val state = when {
            activeOperations.isEmpty() -> GlobalLoadingState.Idle
            activeOperations.size == 1 -> {
                val operation = activeOperations.values.first()
                GlobalLoadingState.Loading(
                    title = operation.title,
                    description = operation.currentMessage,
                    progress = if (operation.showProgress) operation.progress else null,
                    estimatedTimeRemaining = calculateEstimatedTime(operation)
                )
            }
            else -> {
                val totalProgress = activeOperations.values
                    .filter { it.showProgress }
                    .map { it.progress }
                    .average()
                    .toFloat()
                
                GlobalLoadingState.Loading(
                    title = "Processing ${activeOperations.size} operations...",
                    description = activeOperations.values.joinToString(", ") { it.title },
                    progress = if (totalProgress > 0) totalProgress else null,
                    estimatedTimeRemaining = null
                )
            }
        }
        
        _globalLoadingState.value = state
    }
    
    /**
     * ✅ Calculate estimated time remaining
     */
    private fun calculateEstimatedTime(operation: LoadingOperation): Long? {
        if (operation.estimatedDuration == null || operation.progress <= 0f) {
            return null
        }
        
        val elapsed = System.currentTimeMillis() - operation.startTime
        val totalEstimated = elapsed / operation.progress
        return (totalEstimated - elapsed).toLong().coerceAtLeast(0)
    }
    
    private fun showSuccessMessage(message: String) {
        // Implementation for success toast/snackbar
    }
    
    private fun showErrorMessage(message: String) {
        // Implementation for error toast/snackbar
    }
}

/**
 * ✅ Loading operation data class
 */
data class LoadingOperation(
    val id: String,
    val title: String,
    val description: String,
    val showProgress: Boolean,
    val estimatedDuration: Long?,
    val startTime: Long,
    val progress: Float = 0f,
    val currentMessage: String = description
)

/**
 * ✅ Global loading states
 */
sealed class GlobalLoadingState {
    object Idle : GlobalLoadingState()
    
    data class Loading(
        val title: String,
        val description: String,
        val progress: Float? = null,
        val estimatedTimeRemaining: Long? = null
    ) : GlobalLoadingState()
    
    data class Success(val message: String) : GlobalLoadingState()
    data class Error(val message: String) : GlobalLoadingState()
}

/**
 * ✅ UI State wrapper for consistent loading handling
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: Throwable) : UiState<Nothing>()
    data class LoadingWithProgress(
        val progress: Float, 
        val message: String = "",
        val estimatedTimeRemaining: Long? = null
    ) : UiState<Nothing>()
}

/**
 * ✅ Extension functions for easy state management
 */
fun <T> Flow<T>.asUiState(): Flow<UiState<T>> = flow {
    emit(UiState.Loading)
    try {
        collect { data ->
            emit(UiState.Success(data))
        }
    } catch (e: Exception) {
        emit(UiState.Error(e))
    }
}

fun <T> Flow<T>.asUiStateWithProgress(
    progressFlow: Flow<Float>,
    messageFlow: Flow<String> = flowOf("")
): Flow<UiState<T>> = flow {
    var latestData: T? = null
    var latestProgress = 0f
    var latestMessage = ""
    
    // Combine all flows
    combine(this@asUiStateWithProgress, progressFlow, messageFlow) { data, progress, message ->
        latestData = data
        latestProgress = progress
        latestMessage = message
        Triple(data, progress, message)
    }.collect { (data, progress, message) ->
        if (progress >= 1f) {
            emit(UiState.Success(data))
        } else {
            emit(UiState.LoadingWithProgress(progress, message))
        }
    }
}

/**
 * ✅ Specific loading states for different operations
 */
object LoadingStates {
    
    // Family Tree Loading
    object FamilyTree {
        const val LOADING_ROOT = "Loading root fowl..."
        const val LOADING_DESCENDANTS = "Loading descendants..."
        const val LOADING_ANCESTORS = "Loading ancestors..."
        const val BUILDING_RELATIONSHIPS = "Building family tree..."
        const val CALCULATING_LAYOUT = "Calculating layout..."
        const val RENDERING_TREE = "Rendering family tree..."
    }
    
    // Marketplace Loading
    object Marketplace {
        const val LOADING_LISTINGS = "Loading marketplace listings..."
        const val SEARCHING = "Searching marketplace..."
        const val CREATING_LISTING = "Creating your listing..."
        const val UPLOADING_IMAGES = "Uploading images..."
        const val PROCESSING_PAYMENT = "Processing payment..."
    }
    
    // Fowl Management Loading
    object FowlManagement {
        const val LOADING_FOWLS = "Loading your fowls..."
        const val CREATING_FOWL = "Registering new fowl..."
        const val UPLOADING_PHOTOS = "Uploading fowl photos..."
        const val GENERATING_QR = "Generating QR code..."
        const val ANALYZING_BREED = "Analyzing breed characteristics..."
    }
    
    // Transfer Loading
    object Transfer {
        const val INITIATING = "Initiating transfer..."
        const val VALIDATING_OWNERSHIP = "Validating ownership..."
        const val PROCESSING_TRANSFER = "Processing transfer..."
        const val UPDATING_RECORDS = "Updating ownership records..."
        const val SENDING_NOTIFICATIONS = "Sending notifications..."
    }
    
    // Sync Loading
    object Sync {
        const val SYNCING_FOWLS = "Syncing fowl data..."
        const val SYNCING_TRANSFERS = "Syncing transfers..."
        const val SYNCING_MARKETPLACE = "Syncing marketplace..."
        const val SYNCING_MESSAGES = "Syncing messages..."
        const val RESOLVING_CONFLICTS = "Resolving data conflicts..."
    }
}

/**
 * ✅ Progress calculation helpers
 */
object ProgressCalculator {
    
    /**
     * Calculate progress for multi-step operations
     */
    fun calculateStepProgress(
        currentStep: Int,
        totalSteps: Int,
        stepProgress: Float = 1f
    ): Float {
        val baseProgress = (currentStep - 1).toFloat() / totalSteps
        val stepContribution = stepProgress / totalSteps
        return (baseProgress + stepContribution).coerceIn(0f, 1f)
    }
    
    /**
     * Calculate progress for file operations
     */
    fun calculateFileProgress(
        bytesTransferred: Long,
        totalBytes: Long
    ): Float {
        return if (totalBytes > 0) {
            (bytesTransferred.toFloat() / totalBytes).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
    
    /**
     * Calculate weighted progress for multiple operations
     */
    fun calculateWeightedProgress(
        operations: List<Pair<Float, Float>> // (progress, weight)
    ): Float {
        val totalWeight = operations.sumOf { it.second.toDouble() }.toFloat()
        if (totalWeight <= 0f) return 0f
        
        val weightedSum = operations.sumOf { (progress, weight) ->
            (progress * weight).toDouble()
        }.toFloat()
        
        return (weightedSum / totalWeight).coerceIn(0f, 1f)
    }
}

/**
 * ✅ Loading operation builder for complex operations
 */
class LoadingOperationBuilder(private val loadingStateManager: LoadingStateManager) {
    
    fun familyTreeLoading(fowlId: String) = MultiStepOperation(
        operationId = "family_tree_$fowlId",
        loadingStateManager = loadingStateManager,
        steps = listOf(
            LoadingStates.FamilyTree.LOADING_ROOT,
            LoadingStates.FamilyTree.LOADING_DESCENDANTS,
            LoadingStates.FamilyTree.LOADING_ANCESTORS,
            LoadingStates.FamilyTree.BUILDING_RELATIONSHIPS,
            LoadingStates.FamilyTree.CALCULATING_LAYOUT,
            LoadingStates.FamilyTree.RENDERING_TREE
        )
    )
    
    fun fowlRegistration(fowlName: String) = MultiStepOperation(
        operationId = "fowl_registration_${System.currentTimeMillis()}",
        loadingStateManager = loadingStateManager,
        steps = listOf(
            LoadingStates.FowlManagement.CREATING_FOWL,
            LoadingStates.FowlManagement.UPLOADING_PHOTOS,
            LoadingStates.FowlManagement.GENERATING_QR,
            LoadingStates.FowlManagement.ANALYZING_BREED
        )
    )
}

/**
 * ✅ Multi-step operation helper
 */
class MultiStepOperation(
    private val operationId: String,
    private val loadingStateManager: LoadingStateManager,
    private val steps: List<String>
) {
    private var currentStep = 0
    
    fun start(title: String) {
        loadingStateManager.startOperation(
            operationId = operationId,
            title = title,
            description = steps.firstOrNull() ?: "",
            showProgress = true,
            estimatedDuration = steps.size * 2000L // 2 seconds per step estimate
        )
    }
    
    fun nextStep(stepProgress: Float = 1f) {
        if (currentStep < steps.size) {
            val overallProgress = ProgressCalculator.calculateStepProgress(
                currentStep + 1,
                steps.size,
                stepProgress
            )
            
            loadingStateManager.updateProgress(
                operationId = operationId,
                progress = overallProgress,
                message = steps.getOrNull(currentStep) ?: ""
            )
            
            if (stepProgress >= 1f) {
                currentStep++
            }
        }
    }
    
    fun complete(successMessage: String? = null) {
        loadingStateManager.completeOperation(operationId, successMessage)
    }
    
    fun fail(errorMessage: String) {
        loadingStateManager.failOperation(operationId, errorMessage)
    }
}

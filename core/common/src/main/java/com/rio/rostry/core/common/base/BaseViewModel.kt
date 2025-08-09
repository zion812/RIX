package com.rio.rostry.core.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.common.model.UiState
import com.rio.rostry.core.common.model.UserTier
import com.rio.rostry.core.common.model.Result
import com.rio.rostry.core.common.utils.ErrorHandler
import com.rio.rostry.shared.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Base ViewModel class providing common functionality for all ViewModels in the RIO app.
 * Includes error handling, loading states, user tier management, and coroutine utilities.
 */
abstract class BaseViewModel : ViewModel() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var errorHandler: ErrorHandler

    // UI State management
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // User tier for permission checking
    val userTier: StateFlow<UserTier?> = userRepository.getCurrentUserTier()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Network connectivity state
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    // Global error handler for coroutines
    protected val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        handleError(exception)
    }

    /**
     * Execute a suspend function with proper error handling and loading states
     */
    protected fun executeWithLoading(
        showLoading: Boolean = true,
        action: suspend () -> Unit
    ) {
        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                if (showLoading) {
                    _uiState.value = UiState.Loading
                }
                action()
                if (showLoading) {
                    _uiState.value = UiState.Success
                }
            } catch (exception: Exception) {
                handleError(exception)
            }
        }
    }

    /**
     * Execute a suspend function that returns a Result and handle it appropriately
     */
    protected fun <T> executeWithResult(
        showLoading: Boolean = true,
        action: suspend () -> Result<T>,
        onSuccess: (T) -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                if (showLoading) {
                    _uiState.value = UiState.Loading
                }
                
                when (val result = action()) {
                    is Result.Success -> {
                        onSuccess(result.data)
                        if (showLoading) {
                            _uiState.value = UiState.Success
                        }
                    }
                    is Result.Error -> {
                        onError(result.exception)
                        handleError(result.exception)
                    }
                    is Result.Loading -> {
                        if (showLoading) {
                            _uiState.value = UiState.Loading
                        }
                    }
                }
            } catch (exception: Exception) {
                onError(exception)
                handleError(exception)
            }
        }
    }

    /**
     * Handle errors with proper categorization and user-friendly messages
     */
    protected open fun handleError(exception: Throwable) {
        val errorMessage = errorHandler.getErrorMessage(exception)
        _uiState.value = UiState.Error(exception, errorMessage)
    }

    /**
     * Set UI state to success
     */
    protected fun setSuccessState() {
        _uiState.value = UiState.Success
    }

    /**
     * Set UI state to empty
     */
    protected fun setEmptyState() {
        _uiState.value = UiState.Empty
    }

    /**
     * Set UI state to loading
     */
    protected fun setLoadingState() {
        _uiState.value = UiState.Loading
    }

    /**
     * Check if user has required tier permissions
     */
    protected fun hasRequiredTier(requiredTier: UserTier): Boolean {
        val currentTier = userTier.value ?: return false
        return when (requiredTier) {
            UserTier.GENERAL -> true
            UserTier.FARMER -> currentTier in listOf(UserTier.FARMER, UserTier.ENTHUSIAST)
            UserTier.ENTHUSIAST -> currentTier == UserTier.ENTHUSIAST
        }
    }

    /**
     * Execute action only if user has required tier
     */
    protected fun executeWithTierCheck(
        requiredTier: UserTier,
        action: suspend () -> Unit,
        onInsufficientTier: () -> Unit = {}
    ) {
        if (hasRequiredTier(requiredTier)) {
            executeWithLoading { action() }
        } else {
            onInsufficientTier()
        }
    }

    /**
     * Refresh data - override in child ViewModels
     */
    open fun refreshData() {
        // Default implementation - override in child ViewModels
    }

    /**
     * Handle offline state changes
     */
    fun setOfflineState(isOffline: Boolean) {
        _isOffline.value = isOffline
        if (!isOffline) {
            // Trigger data refresh when coming back online
            refreshData()
        }
    }

    /**
     * Utility function to safely collect Flow with error handling
     */
    protected fun <T> Flow<T>.collectSafely(
        onEach: (T) -> Unit,
        onError: (Throwable) -> Unit = { handleError(it) }
    ) {
        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                collect(onEach)
            } catch (exception: Exception) {
                onError(exception)
            }
        }
    }

    /**
     * Utility function to combine multiple StateFlows
     */
    protected fun <T1, T2, R> combineStates(
        flow1: StateFlow<T1>,
        flow2: StateFlow<T2>,
        transform: (T1, T2) -> R
    ): StateFlow<R> {
        return combine(flow1, flow2, transform)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = transform(flow1.value, flow2.value)
            )
    }

    /**
     * Utility function to map StateFlow with error handling
     */
    protected fun <T, R> StateFlow<T>.mapState(
        transform: (T) -> R
    ): StateFlow<R> {
        return map(transform)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = transform(value)
            )
    }

    /**
     * Debounce user input for search functionality
     */
    protected fun <T> Flow<T>.debounceSearch(timeoutMillis: Long = 300): Flow<T> {
        return debounce(timeoutMillis)
    }

    /**
     * Execute action with retry logic
     */
    protected fun executeWithRetry(
        maxRetries: Int = 3,
        delayMillis: Long = 1000,
        action: suspend () -> Unit
    ) {
        viewModelScope.launch(coroutineExceptionHandler) {
            var retryCount = 0
            var lastException: Exception? = null

            while (retryCount <= maxRetries) {
                try {
                    action()
                    return@launch
                } catch (exception: Exception) {
                    lastException = exception
                    retryCount++
                    
                    if (retryCount <= maxRetries) {
                        kotlinx.coroutines.delay(delayMillis * retryCount)
                    }
                }
            }
            
            // If all retries failed, handle the last exception
            lastException?.let { handleError(it) }
        }
    }

    /**
     * Log user action for analytics
     */
    protected fun logUserAction(action: String, parameters: Map<String, Any> = emptyMap()) {
        // Implementation for analytics logging
        // This would integrate with Firebase Analytics or other analytics services
    }

    /**
     * Check if user is authenticated
     */
    protected fun isUserAuthenticated(): Boolean {
        return userRepository.isUserAuthenticated()
    }

    /**
     * Get current user ID
     */
    protected fun getCurrentUserId(): String? {
        return userRepository.getCurrentUserId()
    }
}

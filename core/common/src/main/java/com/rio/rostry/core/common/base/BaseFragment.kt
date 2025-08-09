package com.rio.rostry.core.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.rio.rostry.core.common.utils.NetworkMonitor
import com.rio.rostry.core.common.utils.PermissionManager
import com.rio.rostry.core.common.utils.LoadingStateManager
import com.rio.rostry.core.common.model.UiState
import com.rio.rostry.core.common.model.UserTier
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Base Fragment class providing common functionality for all fragments in the RIO app.
 * Includes network monitoring, permission handling, loading states, and tier-based access control.
 */
abstract class BaseFragment<VB : ViewBinding, VM : BaseViewModel> : Fragment() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var loadingStateManager: LoadingStateManager

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected abstract val viewModel: VM
    protected abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    protected abstract fun setupUI()
    protected abstract fun observeViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = getViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        observeNetworkState()
        observeLoadingState()
        
        // Check tier-based access permissions
        checkTierPermissions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Observe network connectivity changes and adapt UI accordingly
     */
    private fun observeNetworkState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkMonitor.isConnected.collect { isConnected ->
                    onNetworkStateChanged(isConnected)
                }
            }
        }
    }

    /**
     * Observe loading states and show/hide loading indicators
     */
    private fun observeLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> showLoading()
                        is UiState.Success -> hideLoading()
                        is UiState.Error -> {
                            hideLoading()
                            handleError(state.exception)
                        }
                        is UiState.Empty -> {
                            hideLoading()
                            showEmptyState()
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if user has required tier permissions for this fragment
     */
    private fun checkTierPermissions() {
        val requiredTier = getRequiredTier()
        if (requiredTier != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.userTier.collect { currentTier ->
                    if (!hasRequiredTierAccess(currentTier, requiredTier)) {
                        handleInsufficientPermissions(requiredTier)
                    }
                }
            }
        }
    }

    /**
     * Override this method to specify the minimum tier required to access this fragment
     */
    protected open fun getRequiredTier(): UserTier? = null

    /**
     * Check if current user tier has access to required tier functionality
     */
    private fun hasRequiredTierAccess(currentTier: UserTier?, requiredTier: UserTier): Boolean {
        return when (requiredTier) {
            UserTier.GENERAL -> true
            UserTier.FARMER -> currentTier in listOf(UserTier.FARMER, UserTier.ENTHUSIAST)
            UserTier.ENTHUSIAST -> currentTier == UserTier.ENTHUSIAST
        }
    }

    /**
     * Handle network state changes - override in child fragments for custom behavior
     */
    protected open fun onNetworkStateChanged(isConnected: Boolean) {
        if (!isConnected) {
            showOfflineMessage()
        } else {
            hideOfflineMessage()
            // Trigger data refresh when coming back online
            viewModel.refreshData()
        }
    }

    /**
     * Show loading indicator - override for custom loading UI
     */
    protected open fun showLoading() {
        loadingStateManager.showLoading(requireContext())
    }

    /**
     * Hide loading indicator
     */
    protected open fun hideLoading() {
        loadingStateManager.hideLoading()
    }

    /**
     * Handle errors - override for custom error handling
     */
    protected open fun handleError(exception: Throwable) {
        loadingStateManager.showError(requireContext(), exception.message ?: "Unknown error occurred")
    }

    /**
     * Show empty state - override for custom empty state UI
     */
    protected open fun showEmptyState() {
        // Default implementation - override in child fragments
    }

    /**
     * Show offline message
     */
    protected open fun showOfflineMessage() {
        loadingStateManager.showOfflineMessage(requireContext())
    }

    /**
     * Hide offline message
     */
    protected open fun hideOfflineMessage() {
        loadingStateManager.hideOfflineMessage()
    }

    /**
     * Handle insufficient permissions for tier-based access
     */
    protected open fun handleInsufficientPermissions(requiredTier: UserTier) {
        loadingStateManager.showTierUpgradePrompt(requireContext(), requiredTier)
    }

    /**
     * Utility method to safely collect StateFlow in fragments
     */
    protected fun <T> StateFlow<T>.collectSafely(action: suspend (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collect(action)
            }
        }
    }

    /**
     * Request permissions with proper handling
     */
    protected fun requestPermissions(
        permissions: Array<String>,
        onGranted: () -> Unit,
        onDenied: () -> Unit = {}
    ) {
        permissionManager.requestPermissions(
            fragment = this,
            permissions = permissions,
            onGranted = onGranted,
            onDenied = onDenied
        )
    }

    /**
     * Check if all permissions are granted
     */
    protected fun hasPermissions(permissions: Array<String>): Boolean {
        return permissionManager.hasPermissions(requireContext(), permissions)
    }

    /**
     * Navigate back with proper handling
     */
    protected fun navigateBack() {
        if (!findNavController().popBackStack()) {
            requireActivity().finish()
        }
    }

    /**
     * Show snackbar message
     */
    protected fun showMessage(message: String) {
        loadingStateManager.showMessage(binding.root, message)
    }

    /**
     * Show success message
     */
    protected fun showSuccessMessage(message: String) {
        loadingStateManager.showSuccessMessage(binding.root, message)
    }

    /**
     * Show error message
     */
    protected fun showErrorMessage(message: String) {
        loadingStateManager.showErrorMessage(binding.root, message)
    }
}

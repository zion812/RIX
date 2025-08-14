package com.rio.rostry.core.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Base fragment class with common functionality
 */
abstract class BaseFragment<VB : ViewBinding, VM : ViewModel> : Fragment() {
    
    private var _binding: VB? = null
    protected val binding get() = _binding!!
    
    protected abstract val viewModel: VM
    
    abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    abstract fun setupUI()
    abstract fun observeViewModel()
    
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
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    protected fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    
    protected fun showSuccessMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    
    protected open fun showLoading() {
        // Override in subclasses to show loading UI
    }
    
    protected open fun hideLoading() {
        // Override in subclasses to hide loading UI
    }
    
    protected open fun handleError(exception: Throwable) {
        showErrorMessage(exception.message ?: "An error occurred")
    }
    
    /**
     * Helper function to collect flows safely
     */
    protected fun <T> Flow<T>.collectInLifecycle(action: suspend (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            this@collectInLifecycle.collectLatest(action)
        }
    }
    
    /**
     * Navigate safely with error handling
     */
    protected fun navigateSafely(action: () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            // Handle navigation errors gracefully
            handleError(e)
        }
    }
}
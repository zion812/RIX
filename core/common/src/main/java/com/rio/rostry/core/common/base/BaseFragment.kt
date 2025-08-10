package com.rio.rostry.core.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
// ViewBinding is part of Android Gradle Plugin, not a separate dependency
// We'll use a generic View approach instead
import kotlinx.coroutines.launch

/**
 * ✅ Simplified base fragment with common functionality
 */
abstract class BaseFragment : Fragment() {

    // ✅ Abstract methods
    protected abstract fun getLayoutId(): Int
    protected abstract fun setupViews()
    protected abstract fun observeData()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeData()
    }

    /**
     * ✅ Safe fragment transaction execution
     */
    protected fun executeWhenResumed(action: () -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                action()
            }
        }
    }
}

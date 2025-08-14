package com.rio.rostry.user.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.rio.rostry.core.common.base.BaseFragment
import com.rio.rostry.core.common.model.UserTier
import com.rio.rostry.core.common.model.AppError
import com.rio.rostry.user.databinding.FragmentLoginBinding
import com.rio.rostry.user.domain.model.AuthState
import com.rio.rostry.user.ui.viewmodels.AuthViewModel
import com.rio.rostry.user.ui.viewmodels.LoginFormState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment for user login functionality
 */
@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding, AuthViewModel>() {

    override val viewModel: AuthViewModel by viewModels()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLoginBinding {
        return FragmentLoginBinding.inflate(inflater, container, false)
    }

    override fun setupUI() {
        setupClickListeners()
        setupTextWatchers()
    }

    override fun observeViewModel() {
        // Observe authentication state
        viewModel.authState.collectInLifecycle { authState ->
            when (authState) {
                is AuthState.Loading -> showLoading()
                is AuthState.Authenticated -> {
                    hideLoading()
                    showSuccessMessage("Welcome, ${authState.user.displayName}!")
                    // Navigate to main screen based on user tier
                    navigateToMainScreen(authState.user.tier)
                }
                is AuthState.Unauthenticated -> hideLoading()
                is AuthState.Error -> {
                    hideLoading()
                    handleError(authState.exception)
                }
            }
        }

        // Observe login form state
        viewModel.loginState.collectInLifecycle { loginState ->
            updateUIFromLoginState(loginState)
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            viewModel.signInWithEmail(email, password)
        }

        binding.btnPhoneLogin.setOnClickListener {
            // Navigate to phone login screen
            navigateSafely {
                // TODO: Implement phone login navigation
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            navigateSafely {
                // TODO: Implement forgot password navigation
            }
        }

        binding.tvSignUp.setOnClickListener {
            navigateSafely {
                // TODO: Implement sign up navigation
            }
        }
    }

    private fun setupTextWatchers() {
        binding.etEmail.setOnFocusChangeListener { _, _ ->
            updateFormValidation()
        }

        binding.etPassword.setOnFocusChangeListener { _, _ ->
            updateFormValidation()
        }
    }

    private fun updateFormValidation() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        viewModel.updateLoginForm(email, password)
    }

    private fun updateUIFromLoginState(loginState: LoginFormState) {
        binding.btnLogin.isEnabled = loginState.isFormValid && !loginState.isLoading

        // Show email error
        if (!loginState.isEmailValid && loginState.email.isNotEmpty()) {
            binding.tilEmail.error = "Please enter a valid email address"
        } else {
            binding.tilEmail.error = null
        }

        // Show password error
        if (!loginState.isPasswordValid && loginState.password.isNotEmpty()) {
            binding.tilPassword.error = "Password must be at least 8 characters"
        } else {
            binding.tilPassword.error = null
        }

        // Show general error
        loginState.error?.let { error ->
            showErrorMessage(error)
            viewModel.clearError()
        }
    }

    private fun navigateToMainScreen(userTier: UserTier) {
        navigateSafely {
            when (userTier) {
                UserTier.FARMER -> {
                    // Navigate to farmer dashboard
                    // TODO: Implement farmer navigation
                }
                UserTier.ENTHUSIAST -> {
                    // Navigate to enthusiast dashboard
                    // TODO: Implement enthusiast navigation
                }
                UserTier.GENERAL -> {
                    // Navigate to general user dashboard
                    // TODO: Implement general user navigation
                }
            }
        }
    }

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnLogin.isEnabled = true
    }

    override fun handleError(exception: Throwable) {
        super.handleError(exception)
        
        // Handle specific authentication errors
        when (exception) {
            is AppError.AuthenticationError -> {
                binding.tilPassword.error = "Invalid email or password"
            }
            is AppError.NetworkError -> {
                showErrorMessage("Network error. Please check your connection and try again.")
            }
            else -> {
                showErrorMessage("Sign in failed. Please try again.")
            }
        }
    }
}
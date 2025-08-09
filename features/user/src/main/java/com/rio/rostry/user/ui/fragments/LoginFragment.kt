package com.rio.rostry.user.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rio.rostry.core.common.base.BaseFragment
import com.rio.rostry.core.common.model.UserTier
import com.rio.rostry.user.databinding.FragmentLoginBinding
import com.rio.rostry.user.domain.model.AuthState
import com.rio.rostry.user.ui.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

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
        setupKeyboardHandling()
    }

    override fun observeViewModel() {
        // Observe authentication state
        viewModel.authState.collectSafely { authState ->
            when (authState) {
                is AuthState.Authenticated -> {
                    hideLoading()
                    navigateToMainScreen(authState.user.tier)
                }
                is AuthState.Unauthenticated -> {
                    hideLoading()
                    // Stay on login screen
                }
                is AuthState.Loading -> {
                    showLoading()
                }
                is AuthState.Error -> {
                    hideLoading()
                    showErrorMessage(authState.exception.message ?: "Authentication failed")
                }
            }
        }

        // Observe login form state
        viewModel.loginState.collectSafely { loginState ->
            updateLoginForm(loginState)
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            // Login button
            btnLogin.setOnClickListener {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString()
                viewModel.signInWithEmail(email, password)
            }

            // Register button
            btnRegister.setOnClickListener {
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginToRegister()
                )
            }

            // Forgot password
            tvForgotPassword.setOnClickListener {
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginToForgotPassword()
                )
            }

            // Phone login
            btnPhoneLogin.setOnClickListener {
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginToPhoneLogin()
                )
            }

            // Guest access (for general users)
            btnGuestAccess.setOnClickListener {
                navigateToMainScreen(UserTier.GENERAL)
            }

            // Language selector
            btnLanguage.setOnClickListener {
                showLanguageSelector()
            }
        }
    }

    private fun setupTextWatchers() {
        binding.apply {
            // Email text watcher
            etEmail.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    updateFormState()
                }
            })

            // Password text watcher
            etPassword.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    updateFormState()
                }
            })
        }
    }

    private fun setupKeyboardHandling() {
        binding.apply {
            // Handle enter key on password field
            etPassword.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    if (btnLogin.isEnabled) {
                        btnLogin.performClick()
                    }
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun updateFormState() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        viewModel.updateLoginForm(email, password)
    }

    private fun updateLoginForm(loginState: com.rio.rostry.user.ui.viewmodels.LoginFormState) {
        binding.apply {
            // Update button state
            btnLogin.isEnabled = loginState.isFormValid

            // Show/hide loading
            if (loginState.isLoading) {
                btnLogin.text = "Signing in..."
                progressBar.visibility = View.VISIBLE
            } else {
                btnLogin.text = "Sign In"
                progressBar.visibility = View.GONE
            }

            // Show email validation
            if (loginState.email.isNotEmpty() && !loginState.isEmailValid) {
                tilEmail.error = "Please enter a valid email address"
            } else {
                tilEmail.error = null
            }

            // Show password validation
            if (loginState.password.isNotEmpty() && !loginState.isPasswordValid) {
                tilPassword.error = "Password must be at least 8 characters"
            } else {
                tilPassword.error = null
            }

            // Show general error
            if (loginState.error != null) {
                showErrorMessage(loginState.error)
            }
        }
    }

    private fun navigateToMainScreen(userTier: UserTier) {
        val action = when (userTier) {
            UserTier.GENERAL -> LoginFragmentDirections.actionLoginToMainGeneral()
            UserTier.FARMER -> LoginFragmentDirections.actionLoginToMainFarmer()
            UserTier.ENTHUSIAST -> LoginFragmentDirections.actionLoginToMainEnthusiast()
        }
        findNavController().navigate(action)
    }

    private fun showLanguageSelector() {
        val languages = arrayOf("English", "తెలుగు", "हिन्दी")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Select Language")
        builder.setItems(languages) { _, which ->
            val selectedLanguage = when (which) {
                0 -> com.rio.rostry.core.common.model.Language.ENGLISH
                1 -> com.rio.rostry.core.common.model.Language.TELUGU
                2 -> com.rio.rostry.core.common.model.Language.HINDI
                else -> com.rio.rostry.core.common.model.Language.ENGLISH
            }
            // Update language preference
            updateLanguage(selectedLanguage)
        }
        builder.show()
    }

    private fun updateLanguage(language: com.rio.rostry.core.common.model.Language) {
        // Update language in preferences and restart activity
        // This would be handled by LocalizationManager
    }

    override fun onNetworkStateChanged(isConnected: Boolean) {
        super.onNetworkStateChanged(isConnected)
        
        binding.apply {
            if (!isConnected) {
                // Disable online-only features
                btnLogin.isEnabled = false
                btnRegister.isEnabled = false
                btnPhoneLogin.isEnabled = false
                
                // Show offline message
                tvOfflineMessage.visibility = View.VISIBLE
                tvOfflineMessage.text = "You're offline. Please connect to the internet to sign in."
            } else {
                // Re-enable features
                updateFormState()
                btnRegister.isEnabled = true
                btnPhoneLogin.isEnabled = true
                
                // Hide offline message
                tvOfflineMessage.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Clear any previous errors when returning to the screen
        binding.apply {
            tilEmail.error = null
            tilPassword.error = null
        }
    }

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        updateFormState() // Re-evaluate button state
    }

    override fun handleError(exception: Throwable) {
        super.handleError(exception)
        
        // Handle specific authentication errors
        when (exception) {
            is com.rio.rostry.core.common.model.AppError.AuthenticationError -> {
                binding.tilPassword.error = "Invalid email or password"
            }
            is com.rio.rostry.core.common.model.AppError.NetworkError -> {
                showErrorMessage("Network error. Please check your connection and try again.")
            }
            else -> {
                showErrorMessage("Sign in failed. Please try again.")
            }
        }
    }
}

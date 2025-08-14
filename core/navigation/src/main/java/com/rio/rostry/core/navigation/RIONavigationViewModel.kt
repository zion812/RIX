package com.rio.rostry.core.navigation

import androidx.lifecycle.ViewModel
import com.rio.rostry.auth.FirebaseAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RIONavigationViewModel @Inject constructor(
    private val authManager: FirebaseAuthManager
) : ViewModel() {
    val userClaims = authManager.userClaims
    val isLoading = authManager.isLoading

    suspend fun checkAccess(requiredRole: String): Boolean {
        val claims = authManager.userClaims.value
        return when (claims?.tier) {
            "enthusiast" -> true // Enthusiasts have access to all features
            requiredRole -> true // User has exactly the required role
            else -> false
        }
    }
}

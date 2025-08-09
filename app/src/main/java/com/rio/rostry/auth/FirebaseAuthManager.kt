package com.rio.rostry.auth

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manages Firebase Authentication and user claims for the RIO platform
 */
class FirebaseAuthManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: FirebaseAuthManager? = null

        fun getInstance(): FirebaseAuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseAuthManager().also { INSTANCE = it }
            }
        }

        private const val TAG = "FirebaseAuthManager"
    }

    private val auth = FirebaseAuth.getInstance()
    private val functions = FirebaseFunctions.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userClaims = MutableStateFlow<UserClaims?>(null)
    val userClaims: StateFlow<UserClaims?> = _userClaims.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Listen for authentication state changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user

            if (user != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    refreshUserClaims()
                }
            } else {
                _userClaims.value = null
            }
        }
    }

    /**
     * Sign in with email and password
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            _isLoading.value = true
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                refreshUserClaims()
                Result.success(user)
            } else {
                Result.failure(Exception("Sign in failed: User is null"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Create account with email and password
     */
    suspend fun createUserWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            _isLoading.value = true
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Send email verification
                user.sendEmailVerification().await()
                refreshUserClaims()
                Result.success(user)
            } else {
                Result.failure(Exception("Account creation failed: User is null"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Account creation failed", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        auth.signOut()
        _userClaims.value = null
    }

    /**
     * Send email verification to current user
     */
    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val user = auth.currentUser
            if (user != null) {
                user.sendEmailVerification().await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No user signed in"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email verification", e)
            Result.failure(e)
        }
    }

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send password reset email", e)
            Result.failure(e)
        }
    }

    /**
     * Refresh user claims from Firebase
     */
    suspend fun refreshUserClaims() {
        try {
            val user = auth.currentUser
            if (user != null) {
                // Force refresh the ID token to get latest claims
                val tokenResult = user.getIdToken(true).await()
                val claims = tokenResult.claims
                _userClaims.value = UserClaims.fromMap(claims)
                Log.d(TAG, "User claims refreshed: ${_userClaims.value}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh user claims", e)
        }
    }

    /**
     * Request tier upgrade through Cloud Function
     */
    suspend fun requestTierUpgrade(requestedTier: UserTier, documents: List<String> = emptyList()): Result<String> {
        return try {
            _isLoading.value = true

            val data = hashMapOf(
                "requestedTier" to requestedTier.name.lowercase(),
                "documents" to documents
            )

            val result = functions
                .getHttpsCallable("requestTierUpgrade")
                .call(data)
                .await()

            // Access the result data using getData() method
            val response = result.getData() as? Map<String, Any>
            val message = response?.get("message") as? String ?: "Tier upgrade request submitted"

            Result.success(message)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request tier upgrade", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Check if user has specific permission
     */
    fun hasPermission(permission: String): Boolean {
        val claims = _userClaims.value ?: return false
        return when (permission) {
            "canCreateListings" -> claims.permissions.canCreateListings
            "canEditListings" -> claims.permissions.canEditListings
            "canDeleteListings" -> claims.permissions.canDeleteListings
            "canAccessMarketplace" -> claims.permissions.canAccessMarketplace
            "canManageBreedingRecords" -> claims.permissions.canManageBreedingRecords
            "canAccessAnalytics" -> claims.permissions.canAccessAnalytics
            "canAccessPremiumFeatures" -> claims.permissions.canAccessPremiumFeatures
            "canVerifyTransfers" -> claims.permissions.canVerifyTransfers
            "canAccessPrioritySupport" -> claims.permissions.canAccessPrioritySupport
            "canModerateContent" -> claims.permissions.canModerateContent
            else -> false
        }
    }

    /**
     * Get current user tier
     */
    fun getCurrentTier(): UserTier {
        return _userClaims.value?.tier ?: UserTier.GENERAL
    }

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Check if user's email is verified
     */
    fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified == true
    }
}
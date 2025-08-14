package com.rio.rostry.core.data.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to provide current user information to repositories
 * Bridges the gap between FirebaseAuthManager and data layer
 */
@Singleton
class AuthenticationService @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()
    
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()
    
    init {
        // Initialize with current user
        _currentUser.value = firebaseAuth.currentUser
        _currentUserId.value = firebaseAuth.currentUser?.uid
        
        // Listen for auth state changes
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
            _currentUserId.value = auth.currentUser?.uid
        }
    }
    
    /**
     * Get current user ID synchronously
     * Returns null if no user is authenticated
     */
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
    
    /**
     * Get current user synchronously
     * Returns null if no user is authenticated
     */
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }
    
    /**
     * Get current user email
     */
    fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }
    
    /**
     * Check if current user's email is verified
     */
    fun isEmailVerified(): Boolean {
        return firebaseAuth.currentUser?.isEmailVerified == true
    }
}
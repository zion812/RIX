package com.rio.rostry.ui

import androidx.lifecycle.ViewModel
import com.rio.rostry.auth.FirebaseAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for MainActivity to provide Hilt-injected dependencies
 */
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    val authManager: FirebaseAuthManager
) : ViewModel()

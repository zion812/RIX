package com.rio.rostry.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rio.rostry.ui.auth.AuthenticationScreen
import com.rio.rostry.ui.home.HomeScreen
import com.rio.rostry.user.domain.model.AuthState
import com.rio.rostry.user.ui.viewmodels.AuthViewModel

/**
 * Main navigation component for RIO app
 */
@Composable
fun RIONavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    
    NavHost(
        navController = navController,
        startDestination = when (authState) {
            is AuthState.Authenticated -> "home"
            else -> "auth"
        }
    ) {
        composable("auth") {
            AuthenticationScreen(
                onBack = { /* Handle back navigation */ },
                onAuthSuccess = {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        
        composable("home") {
            HomeScreen()
        }
    }
}
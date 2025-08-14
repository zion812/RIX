package com.rio.rostry.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rio.rostry.auth.FirebaseAuthManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Navigation manager for RIO platform with role-based access control
 */
@Singleton
class RIONavigation @Inject constructor(
    private val authManager: FirebaseAuthManager
) {
    sealed class Screen(val route: String) {
        object Login : Screen("login")
        object Home : Screen("home")
        object MarketplaceBrowse : Screen("marketplace/browse")
        object MarketplaceCreate : Screen("marketplace/create")
        object FowlList : Screen("fowl/list")
        object FowlCreate : Screen("fowl/create")
        object FamilyTree : Screen("family_tree")
        object Analytics : Screen("analytics")
        object Profile : Screen("profile")
    }

    companion object {
        // Role-based start destinations
        private val generalUserStartDestination = Screen.MarketplaceBrowse.route
        private val farmerStartDestination = Screen.FowlList.route
        private val enthusiastStartDestination = Screen.Analytics.route
    }

    @Composable
    fun RIOAppNavigation(
        navController: NavHostController,
        viewModel: RIONavigationViewModel = hiltViewModel()
    ) {
        val userClaims by authManager.userClaims.collectAsState()
        val startDestination = when (userClaims?.tier) {
            "farmer" -> farmerStartDestination
            "enthusiast" -> enthusiastStartDestination
            else -> generalUserStartDestination
        }

        LaunchedEffect(userClaims) {
            // Handle auth state changes
            if (userClaims == null) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        NavHost(navController = navController, startDestination = startDestination) {
            // Public routes
            composable(Screen.Login.route) {
                // LoginScreen()
            }
            composable(Screen.MarketplaceBrowse.route) {
                // MarketplaceBrowseScreen()
            }

            // Farmer routes
            composable(Screen.FowlList.route) {
                CheckUserRole(requiredRole = "farmer") {
                    // FowlListScreen()
                }
            }
            composable(Screen.FowlCreate.route) {
                CheckUserRole(requiredRole = "farmer") {
                    // FowlCreateScreen()
                }
            }
            composable(Screen.MarketplaceCreate.route) {
                CheckUserRole(requiredRole = "farmer") {
                    // MarketplaceCreateScreen()
                }
            }

            // Enthusiast routes
            composable(Screen.FamilyTree.route) {
                CheckUserRole(requiredRole = "enthusiast") {
                    // FamilyTreeScreen()
                }
            }
            composable(Screen.Analytics.route) {
                CheckUserRole(requiredRole = "enthusiast") {
                    // AnalyticsScreen()
                }
            }
        }
    }

    @Composable
    private fun CheckUserRole(
        requiredRole: String,
        content: @Composable () -> Unit
    ) {
        val userClaims by authManager.userClaims.collectAsState()
        
        if (userClaims?.tier == requiredRole || userClaims?.tier == "enthusiast") {
            content()
        } else {
            // Show unauthorized screen or navigate back
            // UnauthorizedScreen()
        }
    }
}

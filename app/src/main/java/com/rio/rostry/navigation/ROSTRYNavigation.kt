package com.rio.rostry.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.rio.rostry.auth.FirebaseAuthManager
import com.rio.rostry.auth.UserTier
import com.rio.rostry.ui.auth.AuthScreen
import com.rio.rostry.ui.dashboard.GeneralUserDashboard
import com.rio.rostry.ui.dashboard.FarmerDashboard
import com.rio.rostry.ui.dashboard.EnthusiastDashboard
import com.rio.rostry.ui.fowl.FowlManagementScreen
import com.rio.rostry.ui.marketplace.MarketplaceScreen
import com.rio.rostry.ui.familytree.FamilyTreeScreen
import com.rio.rostry.ui.chat.ChatScreen
import com.rio.rostry.ui.profile.ProfileScreen
import com.rio.rostry.ui.fowl.FowlManagementScreen
import com.rio.rostry.ui.payment.PaymentScreen
import com.rio.rostry.ui.sync.SyncSettingsScreen
import com.rio.rostry.ui.notifications.NotificationsScreen
import com.rio.rostry.ui.screens.*
import com.rio.rostry.ui.listing.ListingCreationWizard

/**
 * Main navigation component for ROSTRY platform
 * Handles tier-based routing and authentication flow
 */
@Composable
fun ROSTRYNavigation(
    authManager: FirebaseAuthManager,
    navController: NavHostController = rememberNavController()
) {
    val currentUser by authManager.currentUser.collectAsState()
    val userClaims by authManager.userClaims.collectAsState()

    // Handle navigation when authentication state changes
    LaunchedEffect(currentUser, userClaims) {
        when {
            currentUser == null -> {
                navController.navigate(ROSTRYDestinations.AUTH) {
                    popUpTo(0) { inclusive = true }
                }
            }
            currentUser != null && userClaims != null -> {
                val claims = userClaims!! // Non-null assertion since we checked above
                val destination = when (claims.tier) {
                    UserTier.GENERAL -> ROSTRYDestinations.GENERAL_DASHBOARD
                    UserTier.FARMER -> ROSTRYDestinations.FARMER_DASHBOARD
                    UserTier.ENTHUSIAST -> ROSTRYDestinations.ENTHUSIAST_DASHBOARD
                }
                navController.navigate(destination) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = ROSTRYDestinations.AUTH
    ) {
        // Authentication Screen
        composable(ROSTRYDestinations.AUTH) {
            AuthScreen(
                onAuthSuccess = {
                    // Navigation will be handled automatically by the NavHost
                    // when currentUser and userClaims change
                }
            )
        }

        // Loading Screen
        composable(ROSTRYDestinations.LOADING) {
            LoadingScreen()
        }

        // General User Dashboard
        composable(ROSTRYDestinations.GENERAL_DASHBOARD) {
            userClaims?.let { claims ->
                GeneralUserDashboard(
                    claims = claims,
                    navController = navController,
                    onSignOut = {
                        authManager.signOut()
                        navController.navigate(ROSTRYDestinations.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Farmer Dashboard
        composable(ROSTRYDestinations.FARMER_DASHBOARD) {
            userClaims?.let { claims ->
                FarmerDashboard(
                    claims = claims,
                    navController = navController,
                    onSignOut = {
                        authManager.signOut()
                        navController.navigate(ROSTRYDestinations.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Enthusiast Dashboard
        composable(ROSTRYDestinations.ENTHUSIAST_DASHBOARD) {
            userClaims?.let { claims ->
                EnthusiastDashboard(
                    claims = claims,
                    navController = navController,
                    onSignOut = {
                        authManager.signOut()
                        navController.navigate(ROSTRYDestinations.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Fowl Management - guard for Farmer or Enthusiast tiers
        composable(ROSTRYDestinations.FOWL_MANAGEMENT) {
            val claims = userClaims
            if (claims != null && claims.tier != UserTier.GENERAL) {
                FowlManagementScreen(navController = navController)
            } else {
                NotAuthorizedScreen()
            }
        }

        // Marketplace - require access permission
        composable(ROSTRYDestinations.MARKETPLACE) {
            if (authManager.hasPermission("canAccessMarketplace")) {
                MarketplaceScreen(navController = navController)
            } else {
                NotAuthorizedScreen()
            }
        }

        // Family Tree
        composable(ROSTRYDestinations.FAMILY_TREE) {
            FamilyTreeScreen(navController = navController)
        }

        // Chat
        composable(ROSTRYDestinations.CHAT) {
            ChatScreen(navController = navController)
        }

        // Profile
        composable(ROSTRYDestinations.PROFILE) {
            ProfileScreen(
                navController = navController,
                userClaims = userClaims,
                onSignOut = {
                    authManager.signOut()
                    navController.navigate(ROSTRYDestinations.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Payment
        composable(ROSTRYDestinations.PAYMENT) {
            PaymentScreen(navController = navController)
        }

        // Sync Settings - Phase 3.1
        composable(ROSTRYDestinations.SYNC_SETTINGS) {
            SyncSettingsScreen(navController = navController)
        }

        // Notifications - Phase 3.3
        composable(ROSTRYDestinations.NOTIFICATIONS) {
            NotificationsScreen(navController = navController)
        }

        // New destinations for bottom navigation
        composable(ROSTRYDestinations.EXPLORE) {
            ExploreScreen(navController = navController)
        }

        composable(ROSTRYDestinations.CREATE) {
            CreateScreen(navController = navController)
        }

        composable(ROSTRYDestinations.CART) {
            CartScreen(navController = navController)
        }

        composable(ROSTRYDestinations.COMMUNITY) {
            CommunityScreen(navController = navController)
        }

        composable(ROSTRYDestinations.TRANSFERS) {
            TransfersScreen(navController = navController)
        }

        composable(ROSTRYDestinations.ANALYTICS_DASHBOARD) {
            if (authManager.hasPermission("canAccessAnalytics")) {
                AnalyticsDashboardScreen(navController = navController)
            } else {
                NotAuthorizedScreen()
            }
        }

        composable(ROSTRYDestinations.LISTING_CREATION_WIZARD) {
            ListingCreationWizard(
                navController = navController,
                onListingCreated = { listingData ->
                    // TODO: Handle listing creation
                    navController.navigateUp()
                }
            )
        }
    }
}

/**
 * Navigation destinations
 */
object ROSTRYDestinations {
    const val AUTH = "auth"
    const val LOADING = "loading"
    const val GENERAL_DASHBOARD = "general_dashboard"
    const val FARMER_DASHBOARD = "farmer_dashboard"
    const val ENTHUSIAST_DASHBOARD = "enthusiast_dashboard"
    const val FOWL_MANAGEMENT = "fowl_management"
    const val MARKETPLACE = "marketplace"
    const val FAMILY_TREE = "family_tree"
    const val CHAT = "chat"
    const val PROFILE = "profile"
    const val PAYMENT = "payment"
    const val SYNC_SETTINGS = "sync_settings"
    const val NOTIFICATIONS = "notifications"

    // New destinations for bottom navigation
    const val EXPLORE = "explore"
    const val CREATE = "create"
    const val CART = "cart"
    const val COMMUNITY = "community"
    const val TRANSFERS = "transfers"
    const val ANALYTICS_DASHBOARD = "analytics_dashboard"
    const val LISTING_CREATION_WIZARD = "listing_creation_wizard"
}

@Composable
private fun LoadingScreen() {
    // Simple loading screen implementation
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}

@Composable
private fun NotAuthorizedScreen() {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text("అనుమతి లేదు")
    }
}

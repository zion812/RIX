package com.rio.rostry.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rio.rostry.auth.FirebaseAuthManager
import com.rio.rostry.chat.ui.ChatScreen
import com.rio.rostry.chat.ui.ConversationListScreen
import com.rio.rostry.core.payment.ui.CoinPurchaseScreen
import com.rio.rostry.fowl.ui.AddFowlRecordScreen
import com.rio.rostry.fowl.ui.FowlDetailScreen
import com.rio.rostry.fowl.ui.FowlEditScreen
import com.rio.rostry.fowl.ui.SimpleFowlManagementScreen
import com.rio.rostry.marketplace.ui.CreateListingScreen
import com.rio.rostry.marketplace.ui.MarketplaceScreen
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
        object FowlDetail : Screen("fowl_detail/{fowlId}") {
            fun createRoute(fowlId: String) = "fowl_detail/$fowlId"
        }
        object FowlEdit : Screen("fowl_edit/{fowlId}") {
            fun createRoute(fowlId: String) = "fowl_edit/$fowlId"
        }
        object FowlAddRecord : Screen("fowl_add_record/{fowlId}") {
            fun createRoute(fowlId: String) = "fowl_add_record/$fowlId"
        }
        object ChatConversationList : Screen("chat")
        object ChatConversation : Screen("chat/{conversationId}") {
            fun createRoute(conversationId: String) = "chat/$conversationId"
        }
        object CoinPurchase : Screen("coins/purchase")
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
                MarketplaceScreen(navController = navController)
            }
            composable(Screen.ChatConversationList.route) {
                ConversationListScreen(navController = navController)
            }
            composable(
                route = Screen.ChatConversation.route,
                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
                ChatScreen(conversationId = conversationId, navController = navController)
            }
            composable(Screen.CoinPurchase.route) {
                // Accessible to all authenticated users
                CoinPurchaseScreen(navController = navController)
            }


            // Farmer routes
            composable(Screen.FowlList.route) {
                CheckUserRole(requiredRole = "farmer") {
                    SimpleFowlManagementScreen(navController = navController)
                }
            }
            composable(Screen.FowlCreate.route) {
                CheckUserRole(requiredRole = "farmer") {
                    // FowlCreateScreen()
                }
            }
            composable(
                route = Screen.FowlDetail.route,
                arguments = listOf(navArgument("fowlId") { type = NavType.StringType })
            ) { backStackEntry ->
                CheckUserRole(requiredRole = "farmer") {
                    val fowlId = backStackEntry.arguments?.getString("fowlId") ?: return@CheckUserRole
                    FowlDetailScreen(fowlId = fowlId, navController = navController)
                }
            }
            composable(
                route = Screen.FowlEdit.route,
                arguments = listOf(navArgument("fowlId") { type = NavType.StringType })
            ) { backStackEntry ->
                CheckUserRole(requiredRole = "farmer") {
                    val fowlId = backStackEntry.arguments?.getString("fowlId") ?: return@CheckUserRole
                    FowlEditScreen(fowlId = fowlId, navController = navController)
                }
            }
            composable(
                route = Screen.FowlAddRecord.route,
                arguments = listOf(navArgument("fowlId") { type = NavType.StringType })
            ) { backStackEntry ->
                CheckUserRole(requiredRole = "farmer") {
                    val fowlId = backStackEntry.arguments?.getString("fowlId") ?: return@CheckUserRole
                    AddFowlRecordScreen(fowlId = fowlId, navController = navController)
                }
            }
            composable(Screen.MarketplaceCreate.route) {
                CheckUserRole(requiredRole = "farmer") {
                    CreateListingScreen(navController = navController)
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
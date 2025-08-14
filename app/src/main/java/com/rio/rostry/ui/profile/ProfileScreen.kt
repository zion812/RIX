package com.rio.rostry.ui.profile

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.rio.rostry.auth.UserClaims

/**
 * Profile screen - delegates to existing UserProfileScreen
 */
@Composable
fun ProfileScreen(
    navController: NavController,
    userClaims: UserClaims?,
    onSignOut: () -> Unit
) {
    UserProfileScreen(
        onBack = { navController.navigateUp() }
    )
}

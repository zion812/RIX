package com.rio.rostry.ui.fowl

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

/**
 * Fowl Management screen - delegates to existing SimpleFowlManagementScreen
 * TODO: Enable features:fowl module for full implementation
 */
@Composable
fun FowlManagementScreen(
    navController: NavController
) {
    SimpleFowlManagementScreen(
        onBack = { navController.navigateUp() }
    )
}

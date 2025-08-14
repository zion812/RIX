package com.rio.rostry.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rio.rostry.auth.UserClaims
import com.rio.rostry.navigation.ROSTRYDestinations
import com.rio.rostry.navigation.GeneralUserNavigation
import com.rio.rostry.ui.components.ROSTRYBottomNavigation
import com.rio.rostry.ui.marketplace.MarketplaceScreen
import com.rio.rostry.ui.profile.ProfileScreen

/**
 * Dashboard for General tier users with bottom navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralUserDashboard(
    claims: UserClaims,
    navController: NavController,
    onSignOut: () -> Unit
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ROSTRY - General User") },
                actions = {
                    // Field Testing Dashboard Access (only during field testing)
                    IconButton(onClick = {
                        navController.navigate("field_testing_dashboard")
                    }) {
                        Icon(Icons.Default.Science, contentDescription = "Field Testing")
                    }

                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
                    }
                }
            )
        },
        bottomBar = {
            ROSTRYBottomNavigation(
                navController = bottomNavController,
                items = GeneralUserNavigation.items
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = ROSTRYDestinations.MARKETPLACE,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(ROSTRYDestinations.MARKETPLACE) {
                MarketplaceScreen(navController = navController)
            }

            composable(ROSTRYDestinations.EXPLORE) {
                ExploreScreen(claims = claims)
            }

            composable(ROSTRYDestinations.CREATE) {
                CreateScreen(claims = claims, navController = navController)
            }

            composable(ROSTRYDestinations.CART) {
                CartScreen(claims = claims)
            }

            composable(ROSTRYDestinations.PROFILE) {
                ProfileScreen(
                    navController = navController,
                    userClaims = claims,
                    onSignOut = onSignOut
                )
            }
        }
    }
}

/**
 * Explore screen for General users
 */
@Composable
private fun ExploreScreen(claims: UserClaims) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Explore",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Discover Roosters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Browse verified listings, learn about breeds, and connect with farmers in your area.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Educational Content",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Learn about rooster care, breeding basics, and farming best practices.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Create screen for General users (limited functionality)
 */
@Composable
private fun CreateScreen(claims: UserClaims, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked Feature",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Create Listings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Upgrade to Farmer tier to create and manage your own rooster listings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate(ROSTRYDestinations.PROFILE) }
        ) {
            Text("Upgrade Account")
        }
    }
}

/**
 * Cart screen for General users
 */
@Composable
private fun CartScreen(claims: UserClaims) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = "Empty Cart",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your Cart",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Items you're interested in will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WelcomeCard(claims: UserClaims) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Welcome to RIO!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You're currently a General user. Explore the fowl community and consider upgrading for more features.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun UpgradePromptCard(onUpgrade: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Upgrade to Farmer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Get full access to fowl management, marketplace, and more for ₹500/year",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onUpgrade,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Upgrade Now")
            }
        }
    }
}

@Composable
private fun QuickActionsCard(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigate(ROSTRYDestinations.FOWL_MANAGEMENT) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Pets, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Fowls")
                }
                
                OutlinedButton(
                    onClick = { navController.navigate(ROSTRYDestinations.MARKETPLACE) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Marketplace")
                }
            }
        }
    }
}

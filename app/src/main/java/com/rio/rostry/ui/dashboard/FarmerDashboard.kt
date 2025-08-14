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
import com.rio.rostry.navigation.FarmerNavigation
import com.rio.rostry.ui.components.ROSTRYBottomNavigation
import com.rio.rostry.ui.marketplace.MarketplaceScreen
import com.rio.rostry.ui.profile.ProfileScreen

/**
 * Dashboard for Farmer tier users with bottom navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerDashboard(
    claims: UserClaims,
    navController: NavController,
    onSignOut: () -> Unit
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ROSTRY - Farmer") },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
                    }
                }
            )
        },
        bottomBar = {
            ROSTRYBottomNavigation(
                navController = bottomNavController,
                items = FarmerNavigation.items
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = ROSTRYDestinations.FARMER_DASHBOARD,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(ROSTRYDestinations.FARMER_DASHBOARD) {
                FarmerHomeContent(claims = claims, navController = navController, onSignOut = onSignOut)
            }

            composable(ROSTRYDestinations.MARKETPLACE) {
                MarketplaceScreen(navController = navController)
            }

            composable(ROSTRYDestinations.CREATE) {
                FarmerCreateScreen(claims = claims, navController = navController)
            }

            composable(ROSTRYDestinations.COMMUNITY) {
                FarmerCommunityScreen(claims = claims)
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
 * Farmer home content (original dashboard content)
 */
@Composable
private fun FarmerHomeContent(
    claims: UserClaims,
    navController: NavController,
    onSignOut: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            FarmerWelcomeCard(claims)
        }

        item {
            FarmerStatsCard()
        }

        item {
            FarmerActionsCard(navController)
        }

        item {
            UpgradeToEnthusiastCard(
                onUpgrade = { /* TODO: Implement upgrade flow */ }
            )
        }
    }
}

/**
 * Farmer create/listing screen
 */
@Composable
private fun FarmerCreateScreen(claims: UserClaims, navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Create Listing",
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
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { navController.navigate(ROSTRYDestinations.LISTING_CREATION_WIZARD) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create New Listing")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { /* TODO: Implement batch listing */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Batch Listing")
                    }
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
                        text = "Listing Guidelines",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Add clear photos from multiple angles\n• Include accurate breed information\n• Provide health and vaccination records\n• Set competitive pricing",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Farmer community screen
 */
@Composable
private fun FarmerCommunityScreen(claims: UserClaims) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Community",
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
                        text = "Local Farmers Network",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Connect with farmers in your region, share experiences, and learn from each other.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { /* TODO: Implement */ },
                            label = { Text("Join Groups") }
                        )
                        AssistChip(
                            onClick = { /* TODO: Implement */ },
                            label = { Text("Ask Questions") }
                        )
                    }
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
                        text = "Knowledge Sharing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Share your farming tips, breeding insights, and success stories with the community.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun FarmerWelcomeCard(claims: UserClaims) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Welcome, Farmer!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Manage your fowls, create marketplace listings, and track your breeding success.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun FarmerStatsCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Your Farm Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Fowls", "0")
                StatItem("Listings", "0")
                StatItem("Transfers", "0")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun FarmerActionsCard(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Farmer Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = { navController.navigate(ROSTRYDestinations.FOWL_MANAGEMENT) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Pets, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage Fowls")
                    }
                    
                    FilledTonalButton(
                        onClick = { navController.navigate(ROSTRYDestinations.MARKETPLACE) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Store, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Marketplace")
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate(ROSTRYDestinations.FAMILY_TREE) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AccountTree, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Family Tree")
                    }
                    
                    OutlinedButton(
                        onClick = { navController.navigate(ROSTRYDestinations.CHAT) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Community")
                    }
                }

                // Payment row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate(ROSTRYDestinations.PAYMENT) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buy Coins")
                    }
                }
            }
        }
    }
}

@Composable
private fun UpgradeToEnthusiastCard(onUpgrade: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Upgrade to Enthusiast",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Get advanced breeding analytics, premium marketplace features, and priority support for ₹2000/year",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onUpgrade,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Upgrade to Enthusiast")
            }
        }
    }
}

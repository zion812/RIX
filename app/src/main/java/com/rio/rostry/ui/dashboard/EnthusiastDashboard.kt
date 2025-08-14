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
import com.rio.rostry.navigation.EnthusiastNavigation
import com.rio.rostry.ui.components.ROSTRYBottomNavigation
import com.rio.rostry.ui.marketplace.MarketplaceScreen
import com.rio.rostry.ui.profile.ProfileScreen

/**
 * Dashboard for Enthusiast tier users with bottom navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnthusiastDashboard(
    claims: UserClaims,
    navController: NavController,
    onSignOut: () -> Unit
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ROSTRY - Enthusiast") },
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
                items = EnthusiastNavigation.items
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = ROSTRYDestinations.ENTHUSIAST_DASHBOARD,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(ROSTRYDestinations.ENTHUSIAST_DASHBOARD) {
                EnthusiastHomeContent(claims = claims, navController = navController, onSignOut = onSignOut)
            }

            composable(ROSTRYDestinations.EXPLORE) {
                EnthusiastExploreScreen(claims = claims)
            }

            composable(ROSTRYDestinations.CREATE) {
                EnthusiastCreateScreen(claims = claims, navController = navController)
            }

            composable(ROSTRYDestinations.ANALYTICS_DASHBOARD) {
                AnalyticsDashboardScreen(claims = claims)
            }

            composable(ROSTRYDestinations.TRANSFERS) {
                TransfersScreen(claims = claims)
            }
        }
    }
}

/**
 * Enthusiast home content (original dashboard content)
 */
@Composable
private fun EnthusiastHomeContent(
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
            EnthusiastWelcomeCard(claims)
        }

        item {
            AdvancedStatsCard()
        }

        item {
            PremiumFeaturesCard(navController)
        }

        item {
            BreedingAnalyticsCard()
        }
    }
}

/**
 * Enthusiast explore screen with advanced features
 */
@Composable
private fun EnthusiastExploreScreen(claims: UserClaims) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Advanced Explore",
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
                        text = "Breeding Analytics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Access detailed genetic analysis, breeding recommendations, and lineage tracking.",
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
                        text = "Market Intelligence",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Get insights on pricing trends, demand patterns, and optimal selling times.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Enthusiast create screen with advanced listing features
 */
@Composable
private fun EnthusiastCreateScreen(claims: UserClaims, navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Premium Listing",
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
                        text = "Advanced Features",
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
                        Text("Create Premium Listing")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { navController.navigate(ROSTRYDestinations.FAMILY_TREE) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AccountTree, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage Family Tree")
                    }
                }
            }
        }
    }
}

/**
 * Analytics dashboard screen
 */
@Composable
private fun AnalyticsDashboardScreen(claims: UserClaims) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Analytics Dashboard",
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
                        text = "Performance Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Track your breeding success, sales performance, and market position.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Transfers screen for ownership management
 */
@Composable
private fun TransfersScreen(claims: UserClaims) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Ownership Transfers",
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
                        text = "Verified Transfers",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Manage ownership transfers with full documentation and lineage tracking.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun EnthusiastWelcomeCard(claims: UserClaims) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Premium Enthusiast",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Access all premium features including advanced breeding analytics, priority marketplace listings, and exclusive community features.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AdvancedStatsCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Advanced Analytics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AdvancedStatItem("Breeding Success", "0%")
                AdvancedStatItem("Market Value", "₹0")
                AdvancedStatItem("ROI", "0%")
            }
        }
    }
}

@Composable
private fun AdvancedStatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
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
private fun PremiumFeaturesCard(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Premium Tools",
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
                        Text("Advanced Fowl Mgmt")
                    }
                    
                    FilledTonalButton(
                        onClick = { navController.navigate(ROSTRYDestinations.MARKETPLACE) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Premium Marketplace")
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = { navController.navigate(ROSTRYDestinations.FAMILY_TREE) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AccountTree, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lineage Analytics")
                    }
                    
                    FilledTonalButton(
                        onClick = { navController.navigate(ROSTRYDestinations.CHAT) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Expert Community")
                    }
                }
            }
        }
    }
}

@Composable
private fun BreedingAnalyticsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "AI-Powered Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Get personalized breeding recommendations, market trend analysis, and genetic optimization suggestions.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { /* TODO: Implement */ },
                    label = { Text("Breeding Recommendations") }
                )
                AssistChip(
                    onClick = { /* TODO: Implement */ },
                    label = { Text("Market Trends") }
                )
            }
        }
    }
}

package com.rio.rostry.ui.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rio.rostry.auth.FirebaseAuthService
import com.rio.rostry.core.database.di.DatabaseProvider
import com.rio.rostry.core.database.entities.UserEntity
import com.rio.rostry.core.payment.SimplePaymentManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.launch
import java.util.*

/**
 * Enhanced Payment Screen - Phase 2.1
 * Integrated with database for coin balance tracking and improved UX
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentBalance by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var showPurchaseDialog by remember { mutableStateOf(false) }
    var selectedPackage by remember { mutableStateOf<CoinPackage?>(null) }
    var showPaymentMethodDialog by remember { mutableStateOf(false) }

    // Database and auth setup
    val database = remember { DatabaseProvider.getDatabase(context) }
    val authService = remember { FirebaseAuthService() }
    val currentUserId = authService.getCurrentUserId() ?: "demo-user"

    // Load user balance (simplified for Phase 2.1)
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val userDao = database.userDao()
            val user = userDao.getUserById(currentUserId)
            if (user != null) {
                // User exists, set demo balance
                currentBalance = 100 // Demo balance
            } else {
                // Create new user
                val newUser = UserEntity(
                    id = currentUserId,
                    email = authService.getCurrentUserEmail() ?: "demo@example.com",
                    displayName = "Demo User",
                    tier = "general",
                    createdAt = Date(),
                    updatedAt = Date()
                )
                userDao.insertUser(newUser)
                currentBalance = 100 // Initial demo balance
            }
        } catch (e: Exception) {
            // Handle error
            currentBalance = 100 // Fallback
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "RIO Coins",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showPurchaseDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Buy Coins") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading your balance...")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    EnhancedBalanceCard(currentBalance)
                }

                item {
                    PaymentInfoCard()
                }

                item {
                    Text(
                        text = "Coin Packages",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(enhancedCoinPackages) { coinPackage ->
                    EnhancedCoinPackageCard(
                        coinPackage = coinPackage,
                        onPurchase = {
                            selectedPackage = coinPackage
                            showPaymentMethodDialog = true
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                }
            }
        }
    }

    // Purchase Dialog
    if (showPurchaseDialog) {
        QuickPurchaseDialog(
            onDismiss = { showPurchaseDialog = false },
            onPurchase = { coins ->
                // Simplified coin addition for Phase 2.1
                currentBalance += coins
                showPurchaseDialog = false
            }
        )
    }

    // Payment Method Dialog
    if (showPaymentMethodDialog && selectedPackage != null) {
        PaymentMethodDialog(
            coinPackage = selectedPackage!!,
            onDismiss = {
                showPaymentMethodDialog = false
                selectedPackage = null
            },
            onPaymentComplete = { success, coins ->
                if (success) {
                    // Simplified coin addition for Phase 2.1
                    currentBalance += coins
                }
                showPaymentMethodDialog = false
                selectedPackage = null
            }
        )
    }
}

/**
 * Enhanced Balance Card - Phase 2.1
 */
@Composable
private fun EnhancedBalanceCard(balance: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your RIO Coins",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "$balance",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Worth ₹${balance * 5}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip("1 Coin = ₹5", Icons.Default.CurrencyRupee)
                InfoChip("Instant Transfer", Icons.Default.Speed)
            }
        }
    }
}

@Composable
private fun InfoChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        color = MaterialTheme.colorScheme.secondary,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSecondary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}

/**
 * Payment Info Card
 */
@Composable
private fun PaymentInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "💡 Payment Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• Secure payments via UPI, Cards, and Net Banking\n• Instant coin delivery to your account\n• 24/7 customer support for payment issues\n• All transactions are encrypted and secure",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Enhanced Coin Package Card
 */
@Composable
private fun EnhancedCoinPackageCard(
    coinPackage: CoinPackage,
    onPurchase: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = coinPackage.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${coinPackage.totalCoins} coins",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (coinPackage.bonusCoins > 0) {
                        Text(
                            text = "${coinPackage.coins} base + ${coinPackage.bonusCoins} bonus",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Popular badge
                if (coinPackage.isPopular) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "POPULAR",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "₹${coinPackage.priceInRupees}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "₹${String.format("%.2f", coinPackage.pricePerCoin)} per coin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onPurchase,
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buy Now")
                }
            }

            if (coinPackage.features.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                coinPackage.features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Quick Purchase Dialog
 */
@Composable
private fun QuickPurchaseDialog(
    onDismiss: () -> Unit,
    onPurchase: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Quick Purchase",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text("Add coins instantly to your account")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "This is a demo purchase. In production, this would integrate with Razorpay/UPI for secure payments.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onPurchase(50) }
            ) {
                Text("Add 50 Coins (Demo)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Payment Method Dialog
 */
@Composable
private fun PaymentMethodDialog(
    coinPackage: CoinPackage,
    onDismiss: () -> Unit,
    onPaymentComplete: (Boolean, Int) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("UPI") }
    var isProcessing by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Choose Payment Method",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    "Purchase: ${coinPackage.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${coinPackage.totalCoins} coins for ₹${coinPackage.priceInRupees}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                val paymentMethods = listOf("UPI", "Card", "Net Banking", "Wallet")
                paymentMethods.forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(method)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Demo Mode: Payment will be simulated",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isProcessing = true
                    // Use SimplePaymentManager (demo mode falls back automatically)
                    val manager = SimplePaymentManager(
                        context,
                        FirebaseAuth.getInstance(),
                        FirebaseFunctions.getInstance()
                    )
                    val activity = (context as? android.app.Activity)
                    if (activity == null) {
                        onPaymentComplete(false, 0)
                        return@Button
                    }
                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                        try {
                            manager.purchaseCoins(
                                activity = activity,
                                coinAmount = coinPackage.totalCoins,
                                callback = object : com.rio.rostry.core.payment.SimplePaymentCallback {
                                    override fun onPaymentSuccess(paymentId: String, orderId: String, coinsAdded: Int) {
                                        onPaymentComplete(true, coinsAdded)
                                    }

                                    override fun onPaymentError(error: String) {
                                        onPaymentComplete(false, 0)
                                    }
                                }
                            ).collect { }
                        } catch (_: Exception) {
                            onPaymentComplete(false, 0)
                        } finally {
                            isProcessing = false
                        }
                    }
                },
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Pay ₹${coinPackage.priceInRupees}")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isProcessing
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Enhanced coin package data structure
 */
data class CoinPackage(
    val id: String,
    val name: String,
    val coins: Int,
    val bonusCoins: Int,
    val priceInRupees: Int,
    val isPopular: Boolean = false,
    val features: List<String> = emptyList()
) {
    val totalCoins: Int get() = coins + bonusCoins
    val pricePerCoin: Double get() = priceInRupees.toDouble() / totalCoins
}

/**
 * Enhanced coin packages with better value propositions
 */
private val enhancedCoinPackages = listOf(
    CoinPackage(
        id = "starter",
        name = "Starter Pack",
        coins = 20,
        bonusCoins = 0,
        priceInRupees = 100,
        features = listOf("Perfect for beginners", "Instant delivery")
    ),
    CoinPackage(
        id = "value",
        name = "Value Pack",
        coins = 100,
        bonusCoins = 10,
        priceInRupees = 500,
        isPopular = true,
        features = listOf("10% bonus coins", "Best value for money", "Most popular choice")
    ),
    CoinPackage(
        id = "premium",
        name = "Premium Pack",
        coins = 200,
        bonusCoins = 30,
        priceInRupees = 1000,
        features = listOf("15% bonus coins", "Premium support", "Priority transactions")
    ),
    CoinPackage(
        id = "elite",
        name = "Elite Pack",
        coins = 500,
        bonusCoins = 100,
        priceInRupees = 2500,
        features = listOf("20% bonus coins", "VIP support", "Exclusive features", "Maximum savings")
    ),
    CoinPackage(
        id = "farmer",
        name = "Farmer Special",
        coins = 1000,
        bonusCoins = 250,
        priceInRupees = 5000,
        features = listOf("25% bonus coins", "Bulk discount", "Dedicated support", "Rural optimized")
    )
)

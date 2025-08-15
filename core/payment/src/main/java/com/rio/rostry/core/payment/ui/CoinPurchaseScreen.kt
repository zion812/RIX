package com.rio.rostry.core.payment.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rio.rostry.core.data.repository.CoinPackage
import com.rio.rostry.core.payment.CoinPackages
import com.rio.rostry.core.payment.ui.viewmodels.PaymentViewModel
import com.rio.rostry.core.payment.ui.viewmodels.PurchaseState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinPurchaseScreen(
    navController: NavController,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coinPackages = CoinPackages.ALL

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Purchase Coins") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                "Your Balance: ${uiState.balance} Coins",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(coinPackages) { coinPackage ->
                    CoinPackageItem(
                        coinPackage = coinPackage,
                        onPurchaseClick = { viewModel.purchaseCoins(coinPackage) },
                        isLoading = uiState.purchaseState is PurchaseState.Loading
                    )
                }
            }

            if (uiState.purchaseState is PurchaseState.Error) {
                Text(
                    text = (uiState.purchaseState as PurchaseState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            if (uiState.purchaseState is PurchaseState.Success) {
                Text(
                    text = "Purchase successful! Transaction ID: ${(uiState.purchaseState as PurchaseState.Success).transactionId}",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun CoinPackageItem(
    coinPackage: CoinPackage,
    onPurchaseClick: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${coinPackage.totalCoins} Coins",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "for ₹${coinPackage.priceInRupees}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(onClick = onPurchaseClick, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Purchase")
                }
            }
        }
    }
}

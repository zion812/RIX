package com.rio.rostry.features.user.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rio.rostry.features.user.viewmodel.CoinUiState
import com.rio.rostry.features.user.viewmodel.CoinViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CoinWalletScreen(
    userId: String,
    viewModel: CoinViewModel = hiltViewModel()
) {
    var coinState by remember { mutableStateOf<CoinUiState>(CoinUiState.Loading) }
    
    LaunchedEffect(userId) {
        viewModel.loadCoinBalance(userId)
        viewModel.loadTransactionHistory(userId)
    }
    
    LaunchedEffect(viewModel.uiState) {
        viewModel.uiState.collect { state ->
            coinState = state
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Wallet header with balance
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Coin Balance",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                when (coinState) {
                    is CoinUiState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is CoinUiState.Success -> {
                        Text(
                            text = "${coinState.balance} coins",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    is CoinUiState.Error -> {
                        Text(
                            text = "Error: ${coinState.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        
        // Transaction history
        Text(
            text = "Transaction History",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        when (coinState) {
            is CoinUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is CoinUiState.Success -> {
                LazyColumn {
                    items(coinState.transactions) { transaction ->
                        TransactionItem(transaction = transaction)
                        Divider()
                    }
                }
            }
            is CoinUiState.Error -> {
                Text(
                    text = "Error loading transactions: ${coinState.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: com.rio.rostry.core.data.model.CoinTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = transaction.transactionType.name.replace("_", " ").lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            )
            
            Text(
                text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(transaction.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            transaction.relatedEntityId?.let { entityId ->
                Text(
                    text = "ID: $entityId",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Text(
            text = if (transaction.amount > 0) "+${transaction.amount}" else transaction.amount.toString(),
            color = if (transaction.amount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}
package com.rio.rostry.ui.payment

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rio.rostry.core.payment.*
import com.rio.rostry.ui.theme.RIOTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Demo Payment Activity - Fully Functional Payment UI
 * Provides realistic payment interface for testing and demonstration
 */
@AndroidEntryPoint
class DemoPaymentActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val amount = intent.getDoubleExtra("amount", 0.0)
        val packageId = intent.getStringExtra("packageId") ?: ""
        
        setContent {
            RIOTheme {
                DemoPaymentScreen(
                    amount = amount,
                    packageId = packageId,
                    onPaymentComplete = { success, message ->
                        if (success) {
                            Toast.makeText(this, "Payment Successful: $message", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Payment Failed: $message", Toast.LENGTH_LONG).show()
                        }
                    },
                    onCancel = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoPaymentScreen(
    amount: Double,
    packageId: String,
    onPaymentComplete: (Boolean, String) -> Unit,
    onCancel: () -> Unit,
    viewModel: DemoPaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var selectedPaymentMethod by remember { mutableStateOf(DemoPaymentMethod.UPI) }
    var showPaymentForm by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.initializePayment(amount, packageId)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Demo Payment Gateway",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Amount: ₹${String.format("%.2f", amount)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Package: $packageId",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (val state = uiState) {
            is DemoPaymentUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = state.message)
                    }
                }
            }
            
            is DemoPaymentUiState.PaymentMethodSelection -> {
                PaymentMethodSelectionContent(
                    selectedMethod = selectedPaymentMethod,
                    onMethodSelected = { selectedPaymentMethod = it },
                    onProceed = { showPaymentForm = true }
                )
            }
            
            is DemoPaymentUiState.Processing -> {
                PaymentProcessingContent(
                    message = state.message,
                    progress = state.progress
                )
            }
            
            is DemoPaymentUiState.Success -> {
                LaunchedEffect(state) {
                    onPaymentComplete(true, state.message)
                }
            }
            
            is DemoPaymentUiState.Error -> {
                PaymentErrorContent(
                    error = state.error,
                    onRetry = { viewModel.retryPayment() },
                    onCancel = onCancel
                )
            }
        }
        
        if (showPaymentForm) {
            PaymentFormDialog(
                paymentMethod = selectedPaymentMethod,
                amount = amount,
                onPaymentSubmit = { paymentDetails ->
                    viewModel.processPayment(selectedPaymentMethod, paymentDetails)
                    showPaymentForm = false
                },
                onDismiss = { showPaymentForm = false }
            )
        }
    }
}

@Composable
fun PaymentMethodSelectionContent(
    selectedMethod: DemoPaymentMethod,
    onMethodSelected: (DemoPaymentMethod) -> Unit,
    onProceed: () -> Unit
) {
    Column {
        Text(
            text = "Select Payment Method",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val paymentMethods = listOf(
            DemoPaymentMethod.UPI to "UPI (Recommended)",
            DemoPaymentMethod.GOOGLE_PAY to "Google Pay",
            DemoPaymentMethod.CARD to "Debit/Credit Card",
            DemoPaymentMethod.NET_BANKING to "Net Banking",
            DemoPaymentMethod.WALLET to "Digital Wallet"
        )
        
        LazyColumn {
            items(paymentMethods) { (method, displayName) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .selectable(
                            selected = selectedMethod == method,
                            onClick = { onMethodSelected(method) }
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedMethod == method) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMethod == method,
                            onClick = { onMethodSelected(method) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = getPaymentMethodDescription(method),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { /* Cancel */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = onProceed,
                modifier = Modifier.weight(1f)
            ) {
                Text("Proceed")
            }
        }
    }
}

@Composable
fun PaymentProcessingContent(
    message: String,
    progress: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Please do not close this screen",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PaymentErrorContent(
    error: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.Error,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Payment Failed",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = onRetry,
                modifier = Modifier.weight(1f)
            ) {
                Text("Retry")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentFormDialog(
    paymentMethod: DemoPaymentMethod,
    amount: Double,
    onPaymentSubmit: (DemoPaymentDetails) -> Unit,
    onDismiss: () -> Unit
) {
    var upiId by remember { mutableStateOf("") }
    var upiPin by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryMonth by remember { mutableStateOf("") }
    var expiryYear by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardHolderName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Enter ${getPaymentMethodDisplayName(paymentMethod)} Details")
        },
        text = {
            Column {
                when (paymentMethod) {
                    DemoPaymentMethod.UPI -> {
                        OutlinedTextField(
                            value = upiId,
                            onValueChange = { upiId = it },
                            label = { Text("UPI ID") },
                            placeholder = { Text("example@upi") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = upiPin,
                            onValueChange = { upiPin = it },
                            label = { Text("UPI PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    DemoPaymentMethod.CARD -> {
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { cardNumber = it },
                            label = { Text("Card Number") },
                            placeholder = { Text("1234 5678 9012 3456") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = expiryMonth,
                                onValueChange = { expiryMonth = it },
                                label = { Text("MM") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            
                            OutlinedTextField(
                                value = expiryYear,
                                onValueChange = { expiryYear = it },
                                label = { Text("YY") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            
                            OutlinedTextField(
                                value = cvv,
                                onValueChange = { cvv = it },
                                label = { Text("CVV") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = cardHolderName,
                            onValueChange = { cardHolderName = it },
                            label = { Text("Cardholder Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    else -> {
                        Text("Click Pay to proceed with ${getPaymentMethodDisplayName(paymentMethod)}")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val paymentDetails = DemoPaymentDetails(
                        amount = amount,
                        upiId = upiId.takeIf { it.isNotEmpty() },
                        upiPin = upiPin.takeIf { it.isNotEmpty() },
                        cardNumber = cardNumber.takeIf { it.isNotEmpty() },
                        expiryMonth = expiryMonth.toIntOrNull(),
                        expiryYear = expiryYear.toIntOrNull(),
                        cvv = cvv.takeIf { it.isNotEmpty() },
                        cardHolderName = cardHolderName.takeIf { it.isNotEmpty() }
                    )
                    onPaymentSubmit(paymentDetails)
                }
            ) {
                Text("Pay ₹${String.format("%.2f", amount)}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getPaymentMethodDescription(method: DemoPaymentMethod): String {
    return when (method) {
        DemoPaymentMethod.UPI -> "Fast and secure UPI payments"
        DemoPaymentMethod.GOOGLE_PAY -> "Quick payments with Google Pay"
        DemoPaymentMethod.CARD -> "Debit/Credit card payments"
        DemoPaymentMethod.NET_BANKING -> "Direct bank account transfer"
        DemoPaymentMethod.WALLET -> "Digital wallet payments"
    }
}

private fun getPaymentMethodDisplayName(method: DemoPaymentMethod): String {
    return when (method) {
        DemoPaymentMethod.UPI -> "UPI"
        DemoPaymentMethod.GOOGLE_PAY -> "Google Pay"
        DemoPaymentMethod.CARD -> "Card"
        DemoPaymentMethod.NET_BANKING -> "Net Banking"
        DemoPaymentMethod.WALLET -> "Wallet"
    }
}
